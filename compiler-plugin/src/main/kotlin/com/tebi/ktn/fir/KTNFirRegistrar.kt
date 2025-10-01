package com.tebi.ktn.fir

import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar


class KTNFirRegistrar : FirExtensionRegistrar() {

    override fun ExtensionRegistrarContext.configurePlugin() {
        +::KTNFunctionFirGenerator
    }

}