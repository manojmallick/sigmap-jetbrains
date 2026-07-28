package com.sigmap.plugin

import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import java.io.File

class OpenContextFileAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val projectPath = project.basePath ?: return

        val contextFile = File(projectPath, ".github/copilot-instructions.md")

        if (!contextFile.exists()) {
            // Try alternative paths
            val alternativePaths = listOf(
                "CLAUDE.md",
                ".cursorrules",
                ".windsurfrules"
            )

            for (altPath in alternativePaths) {
                val altFile = File(projectPath, altPath)
                if (altFile.exists()) {
                    openFile(project, altFile)
                    return
                }
            }

            notifyMissing(project)
            return
        }

        openFile(project, contextFile)
    }

    /** No context file anywhere — say so instead of silently doing nothing. */
    private fun notifyMissing(project: Project) {
        val n = Notification(
            "SigMap",
            "SigMap: no context file found",
            "No .github/copilot-instructions.md (or CLAUDE.md / .cursorrules / .windsurfrules) exists yet.",
            NotificationType.INFORMATION,
        )
        n.addAction(NotificationAction.createSimple("Generate now") {
            ActionManager.getInstance().getAction("SigMap.RegenerateContext")?.let { action ->
                ActionManager.getInstance().tryToExecute(action, null, null, "SigMapOpenContextFile", true)
            }
            n.expire()
        })
        Notifications.Bus.notify(n, project)
    }

    private fun openFile(project: com.intellij.openapi.project.Project, file: File) {
        val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file) ?: return
        FileEditorManager.getInstance(project).openFile(virtualFile, true)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }
}
