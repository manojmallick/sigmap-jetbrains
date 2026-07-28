package com.sigmap.plugin

import org.junit.Test
import kotlin.test.assertEquals

class SigMapSettingsTest {

    @Test
    fun testDefaults() {
        val settings = SigMapSettings()
        assertEquals("", settings.cliPath)
        assertEquals(10, settings.probeIntervalMinutes)
    }

    @Test
    fun testLoadStateRoundTrip() {
        val settings = SigMapSettings()
        val state = SigMapSettings.State().apply {
            cliPath = "/opt/bin/sigmap"
            probeIntervalMinutes = 30
        }
        settings.loadState(state)
        assertEquals("/opt/bin/sigmap", settings.cliPath)
        assertEquals(30, settings.probeIntervalMinutes)

        settings.cliPath = "/other/path"
        settings.probeIntervalMinutes = 5
        assertEquals("/other/path", settings.state.cliPath)
        assertEquals(5, settings.state.probeIntervalMinutes)
    }
}
