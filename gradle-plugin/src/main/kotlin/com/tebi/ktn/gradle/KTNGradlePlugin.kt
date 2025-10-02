package com.tebi.ktn.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion


@Suppress("unused")
class KTNGradlePlugin : KotlinCompilerPluginSupportPlugin {

    override fun apply(target: Project) {
        target.checkCompatibility()
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>) = true

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>
    ): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        return project.provider {
            emptyList()
        }
    }

    override fun getCompilerPluginId() = BuildConfig.COMPILER_PLUGIN_ID

    override fun getPluginArtifact() = SubpluginArtifact(
        groupId = BuildConfig.GROUP,
        artifactId = BuildConfig.PLUGIN_ARTIFACT,
        version = BuildConfig.VERSION,
    )

    private fun Project.checkCompatibility() {
        val kotlinVersion = getKotlinPluginVersion()
        if (kotlinVersion != BuildConfig.KOTLIN_VERSION) {
            logger.warn(
                "Project is using Kotlin version $kotlinVersion, but ${BuildConfig.PLUGIN_ID} was built using " +
                        "version ${BuildConfig.KOTLIN_VERSION}. This is highly likely to lead to compilation failure."
            )
        }
    }

}
