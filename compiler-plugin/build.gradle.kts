import com.vanniktech.maven.publish.KotlinJvm

plugins {
    id("kompile-time-names-convention")
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.buildConfig)
    alias(libs.plugins.mavenPublish)
    `java-test-fixtures`
    idea
}

sourceSets {
    testFixtures {
        java.setSrcDirs(listOf("src/test-fixtures"))
    }
    test {
        java.setSrcDirs(listOf("src/test", "src/test-gen"))
        resources.setSrcDirs(listOf("src/testData"))
    }
}

idea {
    module.generatedSourceDirs.add(projectDir.resolve("src/test-gen"))
}

val apiRuntimeClasspath: Configuration by configurations.creating { isTransitive = false }

dependencies {
    compileOnly(libs.kotlin.compiler)

    testFixturesApi(libs.kotlin.testJunit5)
    testFixturesApi(libs.kotlin.compilerInternalTestFramework)
    testFixturesApi(libs.kotlin.compiler)

    apiRuntimeClasspath(projects.api)

    // Dependencies required to run the internal test framework.
    testRuntimeOnly(libs.junit)
    testRuntimeOnly(libs.kotlin.reflect)
    testRuntimeOnly(libs.kotlin.test)
    testRuntimeOnly(libs.kotlin.scriptRuntime)
    testRuntimeOnly(libs.kotlin.annotationsJvm)
}

buildConfig {
    packageName("${BuildConfig.PACKAGE_NAME}.compiler")

    buildConfigField("DISPLAY_NAME", BuildConfig.DISPLAY_NAME)
    buildConfigField("COMPILER_PLUGIN_ID", BuildConfig.COMPILER_PLUGIN_ID)
}

mavenPublishing {
    coordinates(BuildConfig.GROUP, BuildConfig.COMPILER_PLUGIN_ARTIFACT, BuildConfig.VERSION)

    pom { ktn(BuildConfig.COMPILER_PLUGIN_ARTIFACT) }

    configure(KotlinJvm())

    publishToMavenCentral(automaticRelease = true)

    signAllPublications()
}

tasks.test {
    dependsOn(apiRuntimeClasspath)

    useJUnitPlatform()
    workingDir = rootDir

    systemProperty("apiRuntime.classpath", apiRuntimeClasspath.asPath)

    // Properties required to run the internal test framework.
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-stdlib", "kotlin-stdlib")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-stdlib-jdk8", "kotlin-stdlib-jdk8")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-reflect", "kotlin-reflect")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-test", "kotlin-test")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-script-runtime", "kotlin-script-runtime")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-annotations-jvm", "kotlin-annotations-jvm")

    systemProperty("idea.ignore.disabled.plugins", "true")
    systemProperty("idea.home.path", rootDir)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
        optIn.add("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
        optIn.add("org.jetbrains.kotlin.fir.extensions.ExperimentalTopLevelDeclarationsGenerationApi")
    }
}

val generateTests by tasks.registering(JavaExec::class) {
    inputs.dir(layout.projectDirectory.dir("src/testData"))
        .withPropertyName("testData")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    outputs.dir(layout.projectDirectory.dir("src/test-gen"))
        .withPropertyName("generatedTests")

    classpath = sourceSets.testFixtures.get().runtimeClasspath
    mainClass.set("com.tebi.ktn.testing.GenerateTestsKt")
    workingDir = rootDir
}

tasks.compileTestKotlin {
    dependsOn(generateTests)
}

fun Test.setLibraryProperty(propName: String, jarName: String) {
    val path = project.configurations
        .testRuntimeClasspath.get()
        .files
        .find { """$jarName-\d.*jar""".toRegex().matches(it.name) }
        ?.absolutePath
        ?: return
    systemProperty(propName, path)
}
