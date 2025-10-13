plugins {
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}
