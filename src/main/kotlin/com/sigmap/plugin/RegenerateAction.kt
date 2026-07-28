package com.sigmap.plugin

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project

class RegenerateAction : AnAction() {

    companion object {
        private const val TIMEOUT_MS = 5 * 60 * 1000L
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Regenerating SigMap Context", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = "Running gen-context..."

                try {
                    val projectPath = project.basePath ?: return

                    val command = GenContextLocator.fromOverride(SigMapSettings.getInstance(project).cliPath)
                        ?: GenContextLocator.resolve(projectPath)
                        ?: run {
                            showNotification(
                                project,
                                "SigMap: command not found",
                                installHelpMessage(),
                                NotificationType.WARNING
                            )
                            return
                        }

                    val commandLine = GeneralCommandLine()
                        .withWorkDirectory(projectPath)
                        .withExePath(command.exe)

                    command.params.forEach { param ->
                        commandLine.addParameter(param)
                    }

                    val processHandler: ProcessHandler = ProcessHandlerFactory.getInstance()
                        .createColoredProcessHandler(commandLine)

                    ProcessTerminatedListener.attach(processHandler)
                    processHandler.startNotify()

                    val deadline = System.currentTimeMillis() + TIMEOUT_MS
                    while (!processHandler.waitFor(500)) {
                        if (indicator.isCanceled) {
                            processHandler.destroyProcess()
                            return
                        }
                        if (System.currentTimeMillis() > deadline) {
                            processHandler.destroyProcess()
                            showNotification(
                                project,
                                "SigMap: Generation Timed Out",
                                "gen-context did not finish within ${TIMEOUT_MS / 60_000} minutes",
                                NotificationType.ERROR
                            )
                            return
                        }
                    }

                    val exitCode = processHandler.exitCode
                    if (exitCode == 0) {
                        project.messageBus.syncPublisher(SigMapContextListener.TOPIC).contextRegenerated()
                        showNotification(
                            project,
                            "SigMap: Context Regenerated",
                            "Successfully updated context file (.github/copilot-instructions.md or CLAUDE.md)",
                            NotificationType.INFORMATION
                        )
                    } else {
                        showNotification(
                            project,
                            "SigMap: Generation Failed",
                            "gen-context exited with code $exitCode",
                            NotificationType.ERROR
                        )
                    }

                } catch (ex: Exception) {
                    showNotification(
                        project,
                        "SigMap: Error",
                        "Failed to run gen-context: ${ex.message}",
                        NotificationType.ERROR
                    )
                }
            }
        })
    }

    private fun installHelpMessage(): String {
        return if (GenContextLocator.isWindows()) {
            "Try one of:\n" +
            "1) npm global: npm install -g sigmap\n" +
            "2) npm local: npm install sigmap\n" +
            "3) standalone binary: place sigmap.exe in %USERPROFILE%\\bin and add it to PATH\n" +
            "4) put gen-context.js in project root"
        } else {
            "Try one of:\n" +
            "1) npm global: npm install -g sigmap\n" +
            "2) npm local: npm install sigmap\n" +
            "3) standalone binary: place sigmap in ~/.local/bin and add it to PATH\n" +
            "4) put gen-context.js in project root"
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }

    private fun showNotification(project: Project, title: String, content: String, type: NotificationType) {
        Notifications.Bus.notify(
            Notification("SigMap", title, content, type),
            project
        )
    }
}
