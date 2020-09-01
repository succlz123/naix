package org.succlz123.naix.plugin

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.config.kotlinSourceRoots
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.codegen.extensions.ClassBuilderInterceptorExtension
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.succlz123.naix.plugin.NaixClassBuilder.Companion.TAG

@AutoService(ComponentRegistrar::class)
class NaixComponentRegistrar : ComponentRegistrar {

    override fun registerProjectComponents(
        project: MockProject,
        configuration: CompilerConfiguration
    ) {
        val messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
        val localMessageCollector = LocalMessageCollector(messageCollector)
        if (configuration[KEY_ENABLED] == false) {
            localMessageCollector.logAlways("$TAG -> isEnable: false")
            return
        }
        configuration.kotlinSourceRoots.forEach {
            localMessageCollector.log("$TAG Find Kt File -> " + it.path)
        }
        ClassBuilderInterceptorExtension.registerExtension(
            project,
            NaixClassBuilderInterceptorExtension(localMessageCollector)
        )
    }
}
