package org.succlz123.naix.plugin

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector

class LocalMessageCollector(private val messageCollector: MessageCollector) {
    var enable = false

    fun logAlways(msg: String) {
        messageCollector.report(CompilerMessageSeverity.WARNING, msg)
    }

    fun log(msg: String) {
        if (enable) {
            messageCollector.report(CompilerMessageSeverity.WARNING, msg)
        }
    }
}