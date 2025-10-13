import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("kompile-time-names-convention")
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.buildConfig)
    `maven-publish`
}

kotlin {
    explicitApi()

    androidLibrary {
        namespace = BuildConfig.PACKAGE_NAME
        compileSdk = 36
        minSdk = 23

        withJava()

        compilations.configureEach {
            compilerOptions.configure {
                jvmTarget.set(
                    JvmTarget.JVM_11
                )
            }
        }
    }

    iosArm64()
    iosSimulatorArm64()
    iosX64()

    js {
        browser()
        nodejs()
    }

    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_1_8
        }
    }

    applyDefaultHierarchyTemplate()
}

publishing {
    publications {
        all {
            (this as? MavenPublication)?.run {
                if (artifactId == "api" || artifactId.startsWith("api-")) {
                    artifactId = BuildConfig.API_ARTIFACT + artifactId.substring(3)
                }
            }
        }
    }

    repositories {
        maven {
            name = "localPluginRepository"
            url = uri("/Users/Desmond/local-plugin-repository")
        }
    }
}
