package com.tebi.ktn.compiler.ir

import com.tebi.ktn.compiler.KTNIDs
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
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
import org.jetbrains.kotlin.ir.util.callableId
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.toIrConst
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid


class WithKompileTimeNamesCallTransformer(
    private val context: IrPluginContext,
    private val transformations: WithKompileTimeNamesTransformations,
) : IrElementTransformerVoid() {

    private val kompileTimeQualifiedNameSymbol = context
        .referenceFunctions(KTNIDs.CallableIDs.KompileTimeQualifiedName)
        .singleOrNull()

    override fun visitCall(expression: IrCall): IrExpression {
        if (expression.symbol == kompileTimeQualifiedNameSymbol) {
            return substituteKompileTimeQualifiedName(expression)
        }

        transformations.getShadowedFunction(expression.symbol)?.let { originalFunctionSymbol ->
            expression.symbol = originalFunctionSymbol
        }

        transformations.getGeneratedQualifiedNameParameters(expression.symbol)?.let { additionalParameters ->
            addAdditionalArguments(expression, additionalParameters)
            return super.visitCall(expression)
        }

        checkExternalFunction(expression)?.let { (originalFunctionSymbol, additionalParameters) ->
            addAdditionalArguments(expression, additionalParameters)
            expression.symbol = originalFunctionSymbol
            return super.visitCall(expression)
        }

        return super.visitCall(expression)
    }

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    private fun checkExternalFunction(expression: IrCall): Pair<IrSimpleFunctionSymbol, List<GeneratedQualifiedNameParameter>>? {
        val declaration = expression.symbol.owner

        if (
            declaration.origin != IrDeclarationOrigin.IR_EXTERNAL_DECLARATION_STUB ||
            declaration.typeParameters.none { it.hasAnnotation(KTNIDs.ClassIDs.WithKompileTimeNames) }
        ) return null

        val transformedCallableId = declaration.callableId.copy(
            callableName = KTNIDs.transformWithKompileTimeNamesFunctionName(declaration.callableId.callableName),
        )
        val transformedDeclarationSymbols = context.referenceFunctions(transformedCallableId)
        val transformedDeclaration = transformedDeclarationSymbols.singleOrNull()?.owner
            ?: error("Could not resolve transformed function of ${declaration.fqNameWhenAvailable}")

        val annotatedTypeParameters = declaration.typeParameters
            .filter { it.hasAnnotation(KTNIDs.ClassIDs.WithKompileTimeNames) }

        if (transformedDeclaration.parameters.size - declaration.parameters.size != annotatedTypeParameters.size) {
            // TODO: Diagnostics error
            return null
        }

        val additionalParameters = annotatedTypeParameters.mapIndexed { index, typeParameter ->
            GeneratedQualifiedNameParameter(
                typeParameter = typeParameter,
                qualifiedNameParameter = transformedDeclaration.parameters[
                    transformedDeclaration.parameters.size - declaration.parameters.size + index
                ],
            )
        }

        transformations.register(transformedDeclaration, declaration, additionalParameters)

        return Pair(transformedDeclaration.symbol, additionalParameters)
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

        val functionSymbol = (typeParameterSymbol.owner.parent as? IrSymbolOwner)?.symbol as? IrSimpleFunctionSymbol
        if (functionSymbol == null) {
            // TODO: Diagnostics error
            return null.toIrConst(startOffset, endOffset)
        }

        val additionalParameters = transformations.getGeneratedQualifiedNameParameters(functionSymbol)
        if (additionalParameters == null) {
            // TODO: Diagnostics error
            return null.toIrConst(startOffset, endOffset)
        }

        val param = additionalParameters.firstOrNull { it.typeParameter.symbol == typeParameterSymbol }
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
