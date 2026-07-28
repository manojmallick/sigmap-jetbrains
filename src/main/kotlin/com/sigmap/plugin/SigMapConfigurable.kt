package com.sigmap.plugin

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

/** Tools → SigMap settings page. */
class SigMapConfigurable(private val project: Project) : Configurable {

    private val cliPathField = JBTextField()
    private val intervalSpinner = JSpinner(SpinnerNumberModel(10, 1, 120, 1))
    private var panel: JPanel? = null

    override fun getDisplayName(): String = "SigMap"

    override fun createComponent(): JComponent {
        cliPathField.emptyText.text = "Auto-detect (gen-context.js, node_modules/.bin, global installs)"
        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent("CLI path override:", cliPathField, true)
            .addLabeledComponent("Health probe interval (minutes):", intervalSpinner)
            .addComponentFillVertically(JPanel(), 0)
            .panel
        reset()
        return panel!!
    }

    override fun isModified(): Boolean {
        val s = SigMapSettings.getInstance(project)
        return cliPathField.text.trim() != s.cliPath ||
            (intervalSpinner.value as Int) != s.probeIntervalMinutes
    }

    override fun apply() {
        val s = SigMapSettings.getInstance(project)
        s.cliPath = cliPathField.text.trim()
        s.probeIntervalMinutes = intervalSpinner.value as Int
    }

    override fun reset() {
        val s = SigMapSettings.getInstance(project)
        cliPathField.text = s.cliPath
        intervalSpinner.value = s.probeIntervalMinutes
    }
}
