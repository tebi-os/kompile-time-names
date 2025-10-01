package com.tebi.ktn.gradle

import org.gradle.api.Project
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

        plugins.withId("org.jetbrains.kotlin.android") {
            dependencies.add("implementation", dependency)
            dependencies.add("testImplementation", dependency)

            plugins.withId("java-test-fixtures") {
                dependencies.add("testFixturesImplementation", dependency)
            }

            extensions.getByType(KotlinAndroidProjectExtension::class.java).apply {
                compilerOptions {
                    optIn.add(kompileTimeNamesAnnotation)
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

}
