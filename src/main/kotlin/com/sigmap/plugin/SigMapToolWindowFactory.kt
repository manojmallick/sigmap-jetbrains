package com.sigmap.plugin

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SearchTextField
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import java.awt.BorderLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.DefaultListModel
import javax.swing.JList
import javax.swing.JPanel

class SigMapToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = SigMapToolWindowPanel(project)
        val content = ContentFactory.getInstance().createContent(panel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}

/** "Ask SigMap" panel: query field → ranked files → double-click to open. */
class SigMapToolWindowPanel(private val project: Project) : JPanel(BorderLayout()) {

    private val queryField = SearchTextField(true)
    private val listModel = DefaultListModel<QueryResult>()
    private val resultList = JBList(listModel)
    private val statusLabel = JBLabel("Ask about your codebase, e.g. \"where is authentication handled\"")

    init {
        add(queryField, BorderLayout.NORTH)

        resultList.cellRenderer = object : ColoredListCellRenderer<QueryResult>() {
            override fun customizeCellRenderer(
                list: JList<out QueryResult>, value: QueryResult,
                index: Int, selected: Boolean, hasFocus: Boolean,
            ) {
                append("${value.rank}. ${value.file}")
                append("  score ${value.score}", SimpleTextAttributes.GRAYED_ATTRIBUTES)
                value.sigs.firstOrNull()?.let {
                    append("  ·  ${it.trim()}", SimpleTextAttributes.GRAYED_ITALIC_ATTRIBUTES)
                }
            }
        }
        add(JBScrollPane(resultList), BorderLayout.CENTER)
        add(statusLabel, BorderLayout.SOUTH)

        queryField.textEditor.addActionListener { runQuery() }
        resultList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) openSelected()
            }
        })
    }

    private fun runQuery() {
        val text = queryField.text.trim()
        if (text.isEmpty()) return
        val projectPath = project.basePath ?: return
        statusLabel.text = "Searching…"

        ApplicationManager.getApplication().executeOnPooledThread {
            val command = GenContextLocator.fromOverride(SigMapSettings.getInstance(project).cliPath)
                ?: GenContextLocator.resolve(projectPath)
            val results = command?.let { SigMapQuery.run(projectPath, it, text) }

            ApplicationManager.getApplication().invokeLater {
                listModel.clear()
                when {
                    command == null ->
                        statusLabel.text = "SigMap CLI not found — npm install -g sigmap, or set the path in Tools → SigMap"
                    results.isNullOrEmpty() ->
                        statusLabel.text = "No results for \"$text\" — try regenerating the context"
                    else -> {
                        results.forEach(listModel::addElement)
                        statusLabel.text = "${results.size} files ranked — double-click to open"
                    }
                }
            }
        }
    }

    private fun openSelected() {
        val selected = resultList.selectedValue ?: return
        val projectPath = project.basePath ?: return
        val virtualFile = LocalFileSystem.getInstance()
            .findFileByIoFile(File(projectPath, selected.file)) ?: return
        FileEditorManager.getInstance(project).openFile(virtualFile, true)
    }
}
