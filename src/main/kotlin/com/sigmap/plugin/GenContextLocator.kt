package com.sigmap.plugin

import java.io.File
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * Resolves the sigmap/gen-context command for a project: local gen-context.js,
 * project node_modules/.bin, known global install paths, PATH, then a login-shell
 * lookup. Successful resolutions are cached per project so callers polling on a
 * timer don't re-probe the filesystem every tick; a cached entry is dropped when
 * its files disappear (e.g. node_modules removed), and failures are never cached
 * so installing sigmap takes effect on the next call.
 */
object GenContextLocator {

    data class Command(val exe: String, val params: List<String>)

    private val cache = ConcurrentHashMap<String, Command>()

    fun resolve(projectPath: String): Command? {
        cache[projectPath]?.let { cached ->
            if (stillValid(cached)) return cached
            cache.remove(projectPath)
        }
        return doResolve(projectPath)?.also { cache[projectPath] = it }
    }

    /**
     * Build a Command from an explicit user-configured path (settings override).
     * Returns null for blank or nonexistent paths so callers can fall back to
     * auto-resolution. A `.js` path runs through node; anything else runs directly.
     */
    fun fromOverride(overridePath: String?): Command? {
        val p = overridePath?.trim().orEmpty()
        if (p.isEmpty()) return null
        val f = File(p)
        if (!f.exists() || !f.isFile) return null
        return if (p.endsWith(".js")) Command(findNodeExecutable(), listOf(f.absolutePath))
        else Command(f.absolutePath, emptyList())
    }

    // The exe may be a bare command like "node" (resolved by the OS); only
    // absolute paths can be existence-checked. Params are always file paths.
    private fun stillValid(c: Command): Boolean =
        (!File(c.exe).isAbsolute || File(c.exe).exists()) && c.params.all { File(it).exists() }

    private fun doResolve(projectPath: String): Command? {
        // 1. Local gen-context.js in the project root
        val localGenContext = File(projectPath, "gen-context.js")
        if (localGenContext.exists()) {
            return Command(findNodeExecutable(), listOf(localGenContext.absolutePath))
        }

        // 2. Project-local node_modules/.bin (prefer sigmap, then gen-context)
        val localBinDir = File(projectPath, "node_modules/.bin").absolutePath
        findFirstExisting(
            commandCandidates(localBinDir, "sigmap") + commandCandidates(localBinDir, "gen-context")
        )?.let { return Command(it, emptyList()) }

        // 3. Known global install paths
        findFirstExisting(globalCommandCandidates())?.let { return Command(it, emptyList()) }

        // 4. Current PATH (prefer sigmap)
        (findCommandInPath("sigmap") ?: findCommandInPath("gen-context"))
            ?.let { return Command(it, emptyList()) }

        // 5. Last resort: shell lookup (login shell / where)
        (resolveViaShell("sigmap") ?: resolveViaShell("gen-context"))
            ?.let { return Command(it, emptyList()) }

        return null
    }

    private fun findNodeExecutable(): String {
        return findCommandInPath("node") ?: "node"
    }

    private fun commandCandidates(baseDir: String, command: String): List<String> {
        val base = File(baseDir)
        if (!base.exists()) return emptyList()
        return if (isWindows()) {
            listOf(
                File(base, "$command.cmd").absolutePath,
                File(base, "$command.exe").absolutePath,
                File(base, "$command.bat").absolutePath,
                File(base, command).absolutePath
            )
        } else {
            listOf(File(base, command).absolutePath)
        }
    }

    private fun findFirstExisting(paths: List<String>): String? {
        for (p in paths) {
            val f = File(p)
            if (f.exists() && f.isFile) return f.absolutePath
        }
        return null
    }

    private fun globalCommandCandidates(): List<String> {
        val home = System.getProperty("user.home") ?: ""
        val paths = mutableListOf<String>()

        // Volta
        paths += commandCandidates(File(home, ".volta/bin").absolutePath, "sigmap")
        paths += commandCandidates(File(home, ".volta/bin").absolutePath, "gen-context")

        // nvm (Unix)
        val nvmRoot = File(home, ".nvm/versions/node")
        if (nvmRoot.exists() && nvmRoot.isDirectory) {
            val versions = nvmRoot.listFiles()?.filter { it.isDirectory }?.sortedByDescending { it.name } ?: emptyList()
            versions.forEach { versionDir ->
                val binDir = File(versionDir, "bin").absolutePath
                paths += commandCandidates(binDir, "sigmap")
                paths += commandCandidates(binDir, "gen-context")
            }
        }

        // Common Unix locations
        paths += commandCandidates("/usr/local/bin", "sigmap")
        paths += commandCandidates("/usr/local/bin", "gen-context")
        paths += commandCandidates("/opt/homebrew/bin", "sigmap")
        paths += commandCandidates("/opt/homebrew/bin", "gen-context")
        paths += commandCandidates(File(home, ".npm-global/bin").absolutePath, "sigmap")
        paths += commandCandidates(File(home, ".npm-global/bin").absolutePath, "gen-context")
        paths += commandCandidates(File(home, "npm/bin").absolutePath, "sigmap")
        paths += commandCandidates(File(home, "npm/bin").absolutePath, "gen-context")

        // Windows global npm + user bins
        val appData = System.getenv("APPDATA") ?: File(home, "AppData/Roaming").absolutePath
        paths += commandCandidates(File(appData, "npm").absolutePath, "sigmap")
        paths += commandCandidates(File(appData, "npm").absolutePath, "gen-context")
        paths += commandCandidates(File(home, "bin").absolutePath, "sigmap")
        paths += commandCandidates(File(home, "bin").absolutePath, "gen-context")
        paths += commandCandidates(File(home, ".local/bin").absolutePath, "sigmap")
        paths += commandCandidates(File(home, ".local/bin").absolutePath, "gen-context")

        return paths
    }

    /**
     * Find an executable command in the system PATH.
     * Returns the full path to the command if found, null otherwise.
     */
    private fun findCommandInPath(command: String): String? {
        val pathEnv = System.getenv("PATH") ?: return null
        val pathDirs = pathEnv.split(File.pathSeparator)
        val candidates = if (isWindows()) {
            listOf("$command.cmd", "$command.exe", "$command.bat", command)
        } else {
            listOf(command)
        }

        for (dir in pathDirs) {
            for (candidate in candidates) {
                val executable = File(dir, candidate)
                if (!executable.exists() || !executable.isFile) continue
                if (!isWindows() && !executable.canExecute()) continue
                return executable.absolutePath
            }
        }

        return null
    }

    private fun resolveViaShell(command: String): String? {
        return try {
            val output = if (isWindows()) {
                ProcessBuilder("where", command).start().inputStream.bufferedReader().readText()
            } else {
                val shell = if (File("/bin/zsh").exists()) "/bin/zsh" else "/bin/bash"
                ProcessBuilder(shell, "-lc", "command -v $command || which $command")
                    .start().inputStream.bufferedReader().readText()
            }
            output.lineSequence().map { it.trim() }.firstOrNull { it.isNotEmpty() && File(it).exists() }
        } catch (_: Exception) {
            null
        }
    }

    fun isWindows(): Boolean {
        return System.getProperty("os.name")?.lowercase(Locale.ROOT)?.contains("win") == true
    }
}
