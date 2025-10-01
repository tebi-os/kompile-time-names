package com.tebi.ktn.compiler.ir

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid


class KTNIrRegistrar : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val transformedFunctions = mutableMapOf<IrSimpleFunctionSymbol, List<GeneratedParameters>>()
        moduleFragment.transformChildren(WithKompileTimeNamesFunctionTransformer(pluginContext), transformedFunctions)
        moduleFragment.transformChildrenVoid(WithKompileTimeNamesCallTransformer(pluginContext, transformedFunctions))
    }

}
