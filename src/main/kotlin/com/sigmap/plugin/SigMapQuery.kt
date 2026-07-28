package com.sigmap.plugin

import com.google.gson.JsonParser
import java.io.File
import java.util.concurrent.TimeUnit

/** One ranked file from `--query <text> --json`. */
data class QueryResult(
    val rank: Int,
    val file: String,
    val score: Double,
    val sigs: List<String>,
    val tokens: Int,
)

/**
 * Runs SigMap's ranked retrieval (`--query <text> --json --top N`) through a
 * resolved command. Pure argument building and JSON parsing are separated out
 * for unit testing; [run] is blocking and must be called from a pooled thread.
 */
object SigMapQuery {

    const val DEFAULT_TOP = 10
    private const val TIMEOUT_SECONDS = 15L

    fun buildArgs(command: GenContextLocator.Command, text: String, top: Int): List<String> =
        listOf(command.exe) + command.params + listOf("--query", text, "--json", "--top", top.toString())

    fun parseResults(json: String): List<QueryResult> = try {
        val root = JsonParser.parseString(json).asJsonObject
        root.getAsJsonArray("results")?.mapNotNull { el ->
            val o = el.asJsonObject
            val file = o.get("file")?.asString ?: return@mapNotNull null
            QueryResult(
                rank = o.get("rank")?.asInt ?: 0,
                file = file,
                score = o.get("score")?.asDouble ?: 0.0,
                sigs = o.getAsJsonArray("sigs")?.mapNotNull { runCatching { it.asString }.getOrNull() } ?: emptyList(),
                tokens = o.get("tokens")?.asInt ?: 0,
            )
        } ?: emptyList()
    } catch (_: Exception) {
        emptyList()
    }

    /** Blocking, bounded query. Returns [] on any failure. */
    fun run(projectPath: String, command: GenContextLocator.Command, text: String, top: Int = DEFAULT_TOP): List<QueryResult> {
        return try {
            val proc = ProcessBuilder(buildArgs(command, text, top))
                .directory(File(projectPath))
                .start()
            if (!proc.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                proc.destroyForcibly()
                return emptyList()
            }
            parseResults(proc.inputStream.bufferedReader().readText())
        } catch (_: Exception) {
            emptyList()
        }
    }
}
