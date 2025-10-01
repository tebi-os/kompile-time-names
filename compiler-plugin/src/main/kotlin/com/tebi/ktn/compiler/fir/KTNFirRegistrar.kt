package com.tebi.ktn.compiler.fir

import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar


class KTNFirRegistrar : FirExtensionRegistrar() {

    override fun ExtensionRegistrarContext.configurePlugin() {
        +::KTNAdditionalCheckersExtension

        registerDiagnosticContainers(KTNFirErrors)
    }

}
