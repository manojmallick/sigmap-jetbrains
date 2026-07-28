package com.sigmap.plugin

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project

/** Project-level SigMap settings, persisted to .idea/sigmap.xml. */
@Service(Service.Level.PROJECT)
@State(name = "SigMapSettings", storages = [Storage("sigmap.xml")])
class SigMapSettings : PersistentStateComponent<SigMapSettings.State> {

    class State {
        /** Explicit path to the sigmap/gen-context executable or a gen-context.js script. Empty = auto-detect. */
        var cliPath: String = ""
        /** How often the CLI health probe may run, in minutes. */
        var probeIntervalMinutes: Int = 10
    }

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    var cliPath: String
        get() = myState.cliPath
        set(value) { myState.cliPath = value }

    var probeIntervalMinutes: Int
        get() = myState.probeIntervalMinutes
        set(value) { myState.probeIntervalMinutes = value }

    companion object {
        fun getInstance(project: Project): SigMapSettings =
            project.getService(SigMapSettings::class.java)
    }
}
