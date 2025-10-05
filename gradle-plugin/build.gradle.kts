plugins {
    id("kompile-time-names-convention")
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.gradlePublish)
    alias(libs.plugins.buildConfig)
}

dependencies {
    compileOnly(libs.kotlin.gradlePlugin)
}

buildConfig {
    packageName("${BuildConfig.PACKAGE_NAME}.gradle")

    buildConfigField("KOTLIN_VERSION", BuildConfig.KOTLIN_VERSION)
    buildConfigField("PLUGIN_VERSION", BuildConfig.PLUGIN_VERSION)
    buildConfigField("VERSION", BuildConfig.VERSION)
    buildConfigField("GROUP", BuildConfig.GROUP)
    buildConfigField("COMPILER_PLUGIN_ID", BuildConfig.COMPILER_PLUGIN_ID)
    buildConfigField("COMPILER_PLUGIN_ARTIFACT", BuildConfig.COMPILER_PLUGIN_ARTIFACT)
    buildConfigField("GRADLE_PLUGIN_ID", BuildConfig.GRADLE_PLUGIN_ID)
}

gradlePlugin {
    website = BuildConfig.WEBSITE
    vcsUrl = BuildConfig.VCS_URL
    plugins {
        create(BuildConfig.GRADLE_PLUGIN_ID).apply {
            id = BuildConfig.GRADLE_PLUGIN_ID
            displayName = BuildConfig.DISPLAY_NAME
            description = BuildConfig.DESCRIPTION
            tags = BuildConfig.TAGS
            implementationClass = "${BuildConfig.PACKAGE_NAME}.gradle.KTNGradlePlugin"
        }
    }
}
