import com.vanniktech.maven.publish.KotlinMultiplatform
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("kompile-time-names-convention")
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.buildConfig)
    alias(libs.plugins.mavenPublish)
}

kotlin {
    explicitApi()

    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_1_8
        }
    }

    androidNativeArm32()
    androidNativeArm64()
    androidNativeX64()
    androidNativeX86()

    iosArm64()
    iosSimulatorArm64()
    iosX64()

    watchosArm32()
    watchosArm64()
    watchosDeviceArm64()
    watchosSimulatorArm64()
    watchosX64()

    tvosArm64()
    tvosSimulatorArm64()
    tvosX64()

    macosArm64()
    macosX64()

    js {
        browser()
        nodejs()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmWasi {
        nodejs()
    }

    linuxArm64()
    linuxX64()

    mingwX64()

    applyDefaultHierarchyTemplate()
}

mavenPublishing {
    coordinates(BuildConfig.GROUP, BuildConfig.API_ARTIFACT, BuildConfig.VERSION)

    pom { ktn(BuildConfig.API_ARTIFACT) }

    configure(KotlinMultiplatform())

    publishToMavenCentral(automaticRelease = true)

    signAllPublications()
}
