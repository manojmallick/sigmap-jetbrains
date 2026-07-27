package com.sigmap.plugin

import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GenContextLocatorTest {
    private lateinit var testProjectDir: File

    @Before
    fun setUp() {
        // Create a temporary test project directory
        testProjectDir = File.createTempFile("sigmap-test-", "-project").also { it.delete() }
        testProjectDir.mkdirs()
    }

    @After
    fun tearDown() {
        // Clean up test directory
        testProjectDir.deleteRecursively()
    }

    @Test
    fun testResolveWithLocalFile() {
        // Create a local gen-context.js file
        val genContextFile = File(testProjectDir, "gen-context.js")
        genContextFile.writeText("console.log('test')")

        val result = GenContextLocator.resolve(testProjectDir.absolutePath)

        assertNotNull(result, "Should find local gen-context.js")
        assertTrue(result.exe.endsWith("node") || result.exe.endsWith("node.exe"),
            "Should use node executor for local gen-context.js")
        assertEquals(genContextFile.absolutePath, result.params.firstOrNull(), "Should pass absolute path to gen-context.js")
    }

    @Test
    fun testResolveWithNodeModules() {
        // Create node_modules/.bin/gen-context
        val nodeModulesDir = File(testProjectDir, "node_modules/.bin")
        nodeModulesDir.mkdirs()
        val genContextBin = File(nodeModulesDir, "gen-context")
        genContextBin.writeText("#!/bin/bash\necho 'gen-context'")
        genContextBin.setExecutable(true)

        val result = GenContextLocator.resolve(testProjectDir.absolutePath)

        // When gen-context.js doesn't exist locally, the code should find gen-context
        // either in node_modules/.bin or globally (if installed)
        assertNotNull(result, "Should find gen-context somewhere")
        assertTrue(
            result.exe.contains("gen-context") ||
            result.exe.contains("sigmap") ||
            result.exe == "node",
            "Executor should contain gen-context/sigmap or be 'node'"
        )
    }

    @Test
    fun testResolveNotFound() {
        // Empty project directory without any gen-context
        val result = GenContextLocator.resolve(testProjectDir.absolutePath)

        // When no gen-context.js and no node_modules, result is null unless
        // gen-context/sigmap is installed globally on this machine — both are valid.
        if (result != null) {
            assertTrue(
                result.exe.contains("gen-context") || result.exe.contains("sigmap") || result.exe.contains("node"),
                "A global fallback resolution must still be a sigmap/gen-context command"
            )
        }
    }

    @Test
    fun testResolveLocalPrecedence() {
        // Create both local file and node_modules binary
        val genContextFile = File(testProjectDir, "gen-context.js")
        genContextFile.writeText("console.log('local')")

        val nodeModulesDir = File(testProjectDir, "node_modules/.bin")
        nodeModulesDir.mkdirs()
        val genContextBin = File(nodeModulesDir, "gen-context")
        genContextBin.writeText("#!/bin/bash\necho 'nodemodules'")
        genContextBin.setExecutable(true)

        val result = GenContextLocator.resolve(testProjectDir.absolutePath)

        assertNotNull(result, "Should find gen-context")
        assertTrue(result.exe.endsWith("node") || result.exe.endsWith("node.exe"),
            "Should prefer local gen-context.js (use node executor)")
        assertTrue(result.params.isNotEmpty(), "Should have parameters for node execution")
    }

    @Test
    fun testCachedResolutionDroppedWhenFileDisappears() {
        val genContextFile = File(testProjectDir, "gen-context.js")
        genContextFile.writeText("console.log('test')")

        val first = GenContextLocator.resolve(testProjectDir.absolutePath)
        assertNotNull(first, "Should resolve while gen-context.js exists")
        assertEquals(genContextFile.absolutePath, first.params.firstOrNull())

        // Second call is served from cache
        val cached = GenContextLocator.resolve(testProjectDir.absolutePath)
        assertEquals(first, cached, "Repeat resolution should return the cached command")

        // After the file disappears, the stale entry must not be returned
        genContextFile.delete()
        val after = GenContextLocator.resolve(testProjectDir.absolutePath)
        if (after != null) {
            assertTrue(after.params.none { it == genContextFile.absolutePath },
                "Stale cached command pointing at a deleted file must not be reused")
        }
    }

    @Test
    fun testFindCommandInPathValidCommand() {
        // Try to find a common system command that should exist on Unix-like systems
        val result = findCommandInPathReflection("ls")

        assertNotNull(result, "Should find 'ls' command in PATH")
        assertTrue(File(result).exists(), "Found command should exist")
        assertTrue(File(result).isFile, "Found command should be a file")
    }

    @Test
    fun testFindCommandInPathInvalidCommand() {
        val result = findCommandInPathReflection("nonexistent-command-xyz-12345")

        assertNull(result, "Should not find non-existent command")
    }

    /**
     * Reflection helper to call the private findCommandInPath on the locator object.
     */
    private fun findCommandInPathReflection(command: String): String? {
        val method = GenContextLocator::class.java.getDeclaredMethod(
            "findCommandInPath",
            String::class.java
        )
        method.isAccessible = true
        return method.invoke(GenContextLocator, command) as? String
    }
}
