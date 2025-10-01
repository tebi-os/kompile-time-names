plugins {
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.buildConfig) apply false
    alias(libs.plugins.mavenPublish) apply false
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}

val publishKtn by tasks.registering {
    dependsOn(":api:publishToMavenCentral")
    dependsOn(":compiler-plugin:publishToMavenCentral")
    dependsOn(":gradle-plugin:publishPlugins")
}
