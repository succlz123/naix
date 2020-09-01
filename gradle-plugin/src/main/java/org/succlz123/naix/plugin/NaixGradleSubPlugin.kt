package org.succlz123.naix.plugin

import com.google.auto.service.AutoService
import org.gradle.api.Project
import org.gradle.api.tasks.compile.AbstractCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.plugin.*

const val VERSION = "0.0.9"

@AutoService(KotlinGradleSubplugin::class)
class NaixGradleSubPlugin : KotlinGradleSubplugin<AbstractCompile> {

    override fun apply(
        project: Project,
        kotlinCompile: AbstractCompile,
        javaCompile: AbstractCompile?,
        variantData: Any?,
        androidProjectHandler: Any?,
        kotlinCompilation: KotlinCompilation<KotlinCommonOptions>?
    ): List<SubpluginOption> {
        val extension = project.extensions.findByType(NaixExtension::class.java) ?: NaixExtension(project)
        val enabledOption = SubpluginOption(key = "enabled", value = extension.enable.toString())
        return listOf(enabledOption)
    }

    override fun getCompilerPluginId(): String {
        return "naixPlugin"
    }

    override fun getPluginArtifact(): SubpluginArtifact {
        return SubpluginArtifact(
            groupId = "com.github.succlz123.naix",
            artifactId = "compiler-plugin",
            version = VERSION
        )
    }

    override fun isApplicable(project: Project, task: AbstractCompile): Boolean {
        return project.plugins.hasPlugin(NaixGradlePlugin::class.java)
    }
}
