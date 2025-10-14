package com.tebi.ktn.compiler.ir

import com.tebi.ktn.compiler.KTNIDs
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrSymbolOwner
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.classifierOrNull
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.toIrConst
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid


class WithKompileTimeNamesCallTransformer(
    private val context: IrPluginContext,
    private val transformedFunctions: MutableMap<IrSimpleFunctionSymbol, List<GeneratedQualifiedNameParameter>>,
) : IrElementTransformerVoid() {

    private val kompileTimeQualifiedNameSymbol = context
        .referenceFunctions(KTNIDs.CallableIDs.KompileTimeQualifiedName)
        .singleOrNull()

    override fun visitCall(expression: IrCall): IrExpression {
        if (expression.symbol == kompileTimeQualifiedNameSymbol) {
            return substituteKompileTimeQualifiedName(expression)
        }

        transformedFunctions[expression.symbol]?.let { cachedAdditionalParameters ->
            addAdditionalArguments(expression, cachedAdditionalParameters)
            return super.visitCall(expression)
        }

        checkFunction(expression)?.let { additionalParameters ->
            addAdditionalArguments(expression, additionalParameters)
            return super.visitCall(expression)
        }

        return super.visitCall(expression)
    }

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    private fun checkFunction(expression: IrCall): List<GeneratedQualifiedNameParameter>? {
        val declaration = expression.symbol.owner

        if (declaration.parameters.size <= expression.arguments.size) {
            return null
        }

        val annotatedTypeParameters = declaration.typeParameters
            .filter { it.hasAnnotation(KTNIDs.ClassIDs.WithKompileTimeNames) }

        if (declaration.parameters.size - expression.arguments.size != annotatedTypeParameters.size) {
            // TODO: Diagnostics error
            return null
        }

        val result = annotatedTypeParameters.mapIndexed { index, typeParameter ->
            GeneratedQualifiedNameParameter(
                typeParameter = typeParameter,
                qualifiedNameParameter = declaration.parameters[declaration.parameters.size - expression.arguments.size + index],
            )
        }

        transformedFunctions[expression.symbol] = result

        return result
    }

    private fun substituteKompileTimeQualifiedName(expression: IrCall): IrExpression {
        return expression.typeArguments[0].resolveQualifiedName(
            startOffset = expression.startOffset,
            endOffset = expression.endOffset,
        )
    }

    private fun addAdditionalArguments(
        expression: IrCall,
        additionalParameters: List<GeneratedQualifiedNameParameter>,
    ) {
        for (additionalParameter in additionalParameters) {
            val typeArgument = expression.typeArguments[additionalParameter.typeParameter.index]
            expression.arguments.add(typeArgument.resolveQualifiedName())
        }
    }

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    private fun IrType?.resolveQualifiedName(
        startOffset: Int = SYNTHETIC_OFFSET,
        endOffset: Int = SYNTHETIC_OFFSET,
    ): IrExpression {
        if (this == null) {
            // TODO: Diagnostics error
            return null.toIrConst(startOffset, endOffset)
        }

        val classSymbol = classOrNull
        if (classSymbol != null) {
            return classSymbol.owner.fqNameWhenAvailable?.asString().toIrConst(startOffset, endOffset)
        }

        val typeParameterSymbol = classifierOrNull as? IrTypeParameterSymbol
        if (typeParameterSymbol == null) {
            // TODO: Diagnostics error
            return null.toIrConst(startOffset, endOffset)
        }

        val functionSymbol = (typeParameterSymbol.owner.parent as? IrSymbolOwner)?.symbol
        if (functionSymbol == null) {
            // TODO: Diagnostics error
            return null.toIrConst(startOffset, endOffset)
        }

        val functionAdditionalParameters = transformedFunctions[functionSymbol]
        if (functionAdditionalParameters == null) {
            // TODO: Diagnostics error
            return null.toIrConst(startOffset, endOffset)
        }

        val param = functionAdditionalParameters.firstOrNull { it.typeParameter.symbol == typeParameterSymbol }
        if (param == null) {
            // TODO: Diagnostics error
            return null.toIrConst(startOffset, endOffset)
        }

        return IrGetValueImpl(
            startOffset = startOffset,
            endOffset = endOffset,
            symbol = param.qualifiedNameParameter.symbol,
        )
    }

    private fun String?.toIrConst(startOffset: Int, endOffset: Int) = toIrConst(
        irType = if (this != null) context.irBuiltIns.stringType else context.irBuiltIns.nothingNType,
        startOffset = startOffset,
        endOffset = endOffset,
    )

}
