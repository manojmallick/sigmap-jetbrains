package com.sigmap.plugin

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SigMapQueryTest {

    @Test
    fun testBuildArgsForBinaryCommand() {
        val cmd = GenContextLocator.Command("/usr/local/bin/sigmap", emptyList())
        assertEquals(
            listOf("/usr/local/bin/sigmap", "--query", "auth flow", "--json", "--top", "10"),
            SigMapQuery.buildArgs(cmd, "auth flow", 10),
        )
    }

    @Test
    fun testBuildArgsForScriptCommand() {
        val cmd = GenContextLocator.Command("/usr/bin/node", listOf("/ws/gen-context.js"))
        assertEquals(
            listOf("/usr/bin/node", "/ws/gen-context.js", "--query", "status bar", "--json", "--top", "5"),
            SigMapQuery.buildArgs(cmd, "status bar", 5),
        )
    }

    @Test
    fun testParseResultsValidJson() {
        val json = """
            {"query":"auth","results":[
              {"rank":1,"file":"src/auth.js","score":3.5,"sigs":["function login()","function logout()"],"tokens":42},
              {"rank":2,"file":"src/token.js","score":1,"sigs":[],"tokens":10}
            ]}
        """.trimIndent()
        val results = SigMapQuery.parseResults(json)
        assertEquals(2, results.size)
        assertEquals(QueryResult(1, "src/auth.js", 3.5, listOf("function login()", "function logout()"), 42), results[0])
        assertEquals(QueryResult(2, "src/token.js", 1.0, emptyList(), 10), results[1])
    }

    @Test
    fun testParseResultsToleratesMissingFields() {
        val results = SigMapQuery.parseResults("""{"results":[{"file":"a.js"},{"rank":2}]}""")
        assertEquals(1, results.size) // the entry without "file" is dropped
        assertEquals(QueryResult(0, "a.js", 0.0, emptyList(), 0), results[0])
    }

    @Test
    fun testParseResultsMalformedInputReturnsEmpty() {
        assertTrue(SigMapQuery.parseResults("").isEmpty())
        assertTrue(SigMapQuery.parseResults("not json").isEmpty())
        assertTrue(SigMapQuery.parseResults("{}").isEmpty())
        assertTrue(SigMapQuery.parseResults("""{"results":"nope"}""").isEmpty())
    }
}
