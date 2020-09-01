package org.succlz123.naix.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class NaixGradlePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.extensions.create("naix", NaixExtension::class.java, project)
    }
}
