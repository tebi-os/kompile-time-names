package com.tebi.ktn.compiler.ir

import com.tebi.ktn.compiler.KTNIDs
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.visitors.IrTransformer
import org.jetbrains.kotlin.name.Name


class WithKompileTimeNamesFunctionTransformer(
    context: IrPluginContext,
) : IrTransformer<MutableMap<IrSimpleFunctionSymbol, List<GeneratedParameters>>>() {

    /* Properties */

    private val nullableStringType = context.irBuiltIns.stringType.makeNullable()

    private val skippedExternalDeclarationSymbols = mutableSetOf<IrSimpleFunctionSymbol>()


    /* Public functions */

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    override fun visitSimpleFunction(
        declaration: IrSimpleFunction,
        data: MutableMap<IrSimpleFunctionSymbol, List<GeneratedParameters>>,
    ): IrStatement {
        val declarationSymbol = declaration.symbol

        if (
            !declaration.hasKompileTimeNamesAnnotation() ||
            declaration.typeParameters.none { it.hasWithKompileTimeNameAnnotation() } ||
            declarationSymbol in data
        ) {
            return super.visitSimpleFunction(declaration, data)
        }

        data[declarationSymbol] = declaration.addKompileTimeNameParameters()

        return super.visitSimpleFunction(declaration, data)
    }

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    override fun visitCall(
        expression: IrCall,
        data: MutableMap<IrSimpleFunctionSymbol, List<GeneratedParameters>>,
    ): IrElement {
        val declarationSymbol = expression.symbol
        val declaration = declarationSymbol.owner

        if (
            !declaration.isExternalDeclarationStub() ||
            declarationSymbol in skippedExternalDeclarationSymbols ||
            declarationSymbol in data
        ) {
            return super.visitCall(expression, data)
        }

        if (
            !declaration.hasKompileTimeNamesAnnotation() ||
            declaration.typeParameters.none { it.hasWithKompileTimeNameAnnotation() }
        ) {
            skippedExternalDeclarationSymbols.add(declarationSymbol)
            return super.visitCall(expression, data)
        }

        data[declarationSymbol] = declaration.addKompileTimeNameParameters()

        return super.visitCall(expression, data)
    }


    /* Private functions */

    private fun IrSimpleFunction.addKompileTimeNameParameters() = typeParameters.mapNotNull { typeParameter ->
        val qualifiedNameParameter = if (typeParameter.hasAnnotation(KTNIDs.ClassIDs.WithQualifiedName)) {
            addKompileTimeNameParameter(typeParameter.name.makeQualifiedNameParameterName())
        } else {
            null
        }

        val simpleNameParameter = if (typeParameter.hasAnnotation(KTNIDs.ClassIDs.WithSimpleName)) {
            addKompileTimeNameParameter(typeParameter.name.makeSimpleNameParameterName())
        } else {
            null
        }

        if (qualifiedNameParameter != null || simpleNameParameter != null) {
            GeneratedParameters(typeParameter, qualifiedNameParameter, simpleNameParameter)
        } else {
            null
        }
    }

    private fun IrSimpleFunction.addKompileTimeNameParameter(name: Name) = addValueParameter {
        startOffset = SYNTHETIC_OFFSET
        endOffset = SYNTHETIC_OFFSET
        origin = KTNIDs.PluginOrigin
        this.name = name
        type = nullableStringType
    }

}
