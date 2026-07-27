package com.sigmap.plugin

import com.intellij.util.messages.Topic

/** Project-level SigMap events, published on the project message bus. */
interface SigMapContextListener {
    /** Fired after the context file has been regenerated successfully. */
    fun contextRegenerated()

    companion object {
        val TOPIC: Topic<SigMapContextListener> =
            Topic.create("SigMap context events", SigMapContextListener::class.java)
    }
}
