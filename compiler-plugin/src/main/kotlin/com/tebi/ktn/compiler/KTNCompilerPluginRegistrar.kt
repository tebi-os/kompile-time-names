package com.tebi.ktn.compiler

import com.tebi.ktn.compiler.fir.KTNFirRegistrar
import com.tebi.ktn.compiler.ir.KTNIrRegistrar
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter


class KTNCompilerPluginRegistrar : CompilerPluginRegistrar() {

    override val supportsK2 = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        FirExtensionRegistrarAdapter.registerExtension(KTNFirRegistrar())
        IrGenerationExtension.registerExtension(KTNIrRegistrar())
    }

}