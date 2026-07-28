package com.sigmap.plugin

import com.intellij.execution.configurations.GeneralCommandLine

/**
 * Spawns SigMap CLI processes through [GeneralCommandLine] with the login-shell
 * environment ([GeneralCommandLine.ParentEnvironmentType.CONSOLE]). A bare
 * ProcessBuilder inherits the GUI-app environment, where `node` is usually not
 * on PATH on macOS — the node_modules/.bin shim then fails with
 * "env: node: No such file or directory".
 */
object SigMapProcess {

    fun start(command: GenContextLocator.Command, args: List<String>, workDir: String): Process =
        GeneralCommandLine(listOf(command.exe) + command.params + args)
            .withWorkDirectory(workDir)
            .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
            .createProcess()
}
