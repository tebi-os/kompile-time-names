package com.tebi.ktn.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion


@Suppress("unused")
class KTNGradlePlugin : KotlinCompilerPluginSupportPlugin {

    private var androidConfigured = false

    override fun apply(target: Project) {
        target.checkCompatibility()
        target.addApiDependency()
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
        artifactId = BuildConfig.COMPILER_PLUGIN_ARTIFACT,
        version = BuildConfig.VERSION,
    )

    private fun Project.checkCompatibility() {
        val kotlinVersion = getKotlinPluginVersion()
        if (kotlinVersion != BuildConfig.KOTLIN_VERSION) {
            logger.warn(
                "Project is using Kotlin version $kotlinVersion, but ${BuildConfig.GRADLE_PLUGIN_ID} was built using " +
                        "version ${BuildConfig.KOTLIN_VERSION}. This is highly likely to lead to compilation failure."
            )
        }
    }

    private fun Project.addApiDependency() {
        val dependency = dependencies.create("${BuildConfig.GROUP}:${BuildConfig.API_ARTIFACT}:${BuildConfig.VERSION}")
        val kompileTimeNamesAnnotation = "${BuildConfig.PACKAGE_NAME}.KompileTimeNames"

        plugins.withId("org.jetbrains.kotlin.jvm") {
            dependencies.add("implementation", dependency)
            dependencies.add("testImplementation", dependency)

            plugins.withId("java-test-fixtures") {
                dependencies.add("testFixturesImplementation", dependency)
            }

            extensions.getByType(KotlinJvmProjectExtension::class.java).apply {
                compilerOptions {
                    optIn.add(kompileTimeNamesAnnotation)
                }
            }
        }

        // AGP <= 8.x: kotlin.android plugin applied explicitly by the user
        plugins.withId("org.jetbrains.kotlin.android") {
            configureAndroid(dependency, kompileTimeNamesAnnotation)
        }

        // AGP 9+: built-in Kotlin support means kotlin.android is no longer applied.
        // Detect Android projects via AGP plugin IDs. Use afterEvaluate to ensure all
        // plugins have been applied before checking, avoiding false positives with AGP 8.x
        // where kotlin.android may be applied after the AGP plugin.
        AGP_PLUGIN_IDS.forEach { pluginId ->
            plugins.withId(pluginId) {
                afterEvaluate {
                    if (!plugins.hasPlugin("org.jetbrains.kotlin.android") &&
                        !plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")
                    ) {
                        configureAndroid(dependency, kompileTimeNamesAnnotation)
                    }
                }
            }
        }

        plugins.withId("org.jetbrains.kotlin.multiplatform") {
            kotlinExtension.sourceSets.named("commonMain").configure {
                it.dependencies {
                    implementation(dependency)
                }
            }

            extensions.getByType(KotlinMultiplatformExtension::class.java).apply {
                compilerOptions {
                    optIn.add(kompileTimeNamesAnnotation)
                }
            }
        }
    }

    private fun Project.configureAndroid(dependency: Dependency, kompileTimeNamesAnnotation: String) {
        if (androidConfigured) return
        androidConfigured = true

        dependencies.add("implementation", dependency)
        dependencies.add("testImplementation", dependency)

        plugins.withId("java-test-fixtures") {
            dependencies.add("testFixturesImplementation", dependency)
        }

        extensions.findByType(KotlinAndroidProjectExtension::class.java)?.apply {
            compilerOptions {
                optIn.add(kompileTimeNamesAnnotation)
            }
        }
    }

    companion object {
        private val AGP_PLUGIN_IDS = listOf(
            "com.android.application",
            "com.android.library",
            "com.android.dynamic-feature",
            "com.android.test",
        )
    }

}
