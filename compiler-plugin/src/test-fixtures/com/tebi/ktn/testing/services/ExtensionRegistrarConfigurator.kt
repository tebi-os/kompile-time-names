package com.tebi.ktn.testing.services

import com.tebi.ktn.compiler.fir.KTNFirRegistrar
import com.tebi.ktn.compiler.ir.KTNIrRegistrar
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoot
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.EnvironmentConfigurator
import org.jetbrains.kotlin.test.services.TestServices
import java.io.File


class ExtensionRegistrarConfigurator(testServices: TestServices) : EnvironmentConfigurator(testServices) {

    companion object {

        private val apiRuntimeClasspath =
            System.getProperty("apiRuntime.classpath")?.split(File.pathSeparator)?.map(::File)
                ?: error("Unable to get a valid classpath from 'apiRuntime.classpath' property")

    }

    override fun configureCompilerConfiguration(configuration: CompilerConfiguration, module: TestModule) {
        for (file in apiRuntimeClasspath) {
            configuration.addJvmClasspathRoot(file)
        }
    }

    override fun CompilerPluginRegistrar.ExtensionStorage.registerCompilerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration
    ) {
        FirExtensionRegistrarAdapter.registerExtension(KTNFirRegistrar())
        IrGenerationExtension.registerExtension(KTNIrRegistrar())
    }

}
