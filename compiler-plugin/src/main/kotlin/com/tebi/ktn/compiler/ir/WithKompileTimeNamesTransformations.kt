package com.tebi.ktn.compiler.ir

import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import kotlin.collections.set


class WithKompileTimeNamesTransformations {

    /* Private functions */

    private val functionShadows = mutableMapOf<IrSimpleFunctionSymbol, IrSimpleFunctionSymbol>()

    private val transformedFunctions = mutableMapOf<IrSimpleFunctionSymbol, List<GeneratedQualifiedNameParameter>>()


    /* Public functions */

    fun register(
        declaration: IrSimpleFunction,
        original: IrSimpleFunction,
        additionalParameters: List<GeneratedQualifiedNameParameter>,
    ) {
        functionShadows[declaration.symbol] = original.symbol
        transformedFunctions[original.symbol] = additionalParameters
    }

    fun getShadowedFunction(symbol: IrSimpleFunctionSymbol): IrSimpleFunctionSymbol? {
        return functionShadows[symbol]
    }

    fun getGeneratedQualifiedNameParameters(symbol: IrSimpleFunctionSymbol): List<GeneratedQualifiedNameParameter>? {
        return transformedFunctions[symbol]
    }

}