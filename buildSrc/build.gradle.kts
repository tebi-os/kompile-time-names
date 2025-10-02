plugins {
    `kotlin-dsl`
    alias(libs.plugins.buildConfig)
}

buildConfig {
    useKotlinOutput { internalVisibility = false }
    withoutPackage()

    fun property(key: String) = properties[key] as? String
        ?: error("Could not get property $key")

    buildConfigField("KOTLIN_VERSION", libs.versions.kotlin.get())
    buildConfigField("PLUGIN_VERSION", property("ktn.version.plugin"))
    buildConfigField("String", "VERSION", $$""""$KOTLIN_VERSION-$PLUGIN_VERSION"""")
    buildConfigField("GROUP", property("ktn.group"))
    buildConfigField("DISPLAY_NAME", property("ktn.displayName"))
    buildConfigField("DESCRIPTION", property("ktn.description"))
    buildConfigField("TAGS", property("ktn.tags").splitToSequence(',').map { it.trim() }.toList())
    buildConfigField("PACKAGE_NAME", property("ktn.packageName"))
    buildConfigField("COMPILER_PLUGIN_ID", property("ktn.compilerPlugin.id"))
    buildConfigField("GRADLE_PLUGIN_ID", property("ktn.gradlePlugin.id"))
    buildConfigField("GRADLE_PLUGIN_ARTIFACT", property("ktn.gradlePlugin.artifact"))
    buildConfigField("WEBSITE", property("ktn.website"))
    buildConfigField("VCS_URL", property("ktn.vcsUrl"))
}
