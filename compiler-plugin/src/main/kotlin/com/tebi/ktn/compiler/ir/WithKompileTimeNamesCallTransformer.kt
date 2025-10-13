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
import org.jetbrains.kotlin.ir.util.toIrConst
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid


class WithKompileTimeNamesCallTransformer(
    private val context: IrPluginContext,
    private val transformedFunctions: Map<IrSimpleFunctionSymbol, List<GeneratedQualifiedNameParameter>>,
) : IrElementTransformerVoid() {

    private val kompileTimeQualifiedNameSymbol = context
        .referenceFunctions(KTNIDs.CallableIDs.KompileTimeQualifiedName)
        .singleOrNull()
        ?: error("Unable to resolve symbol for ${KTNIDs.CallableIDs.KompileTimeQualifiedName}")

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    override fun visitCall(expression: IrCall): IrExpression {
        if (expression.symbol == kompileTimeQualifiedNameSymbol) {
            return expression.typeArguments[0].resolveQualifiedName()
        }

        val additionalParameters = transformedFunctions[expression.symbol]
            ?: return super.visitCall(expression)

        for (additionalParameter in additionalParameters) {
            val typeArgument = expression.typeArguments[additionalParameter.typeParameter.index]
            expression.arguments.add(typeArgument.resolveQualifiedName())
        }

        return super.visitCall(expression)
    }

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    private fun IrType?.resolveQualifiedName(): IrExpression {
        if (this == null) {
            // TODO: Diagnostics error
            return null.toIrConst()
        }

        val classSymbol = classOrNull
        if (classSymbol != null) {
            return classSymbol.owner.fqNameWhenAvailable?.asString().toIrConst()
        }

        val typeParameterSymbol = classifierOrNull as? IrTypeParameterSymbol
        if (typeParameterSymbol == null) {
            // TODO: Diagnostics error
            return null.toIrConst()
        }

        val functionSymbol = (typeParameterSymbol.owner.parent as? IrSymbolOwner)?.symbol
        if (functionSymbol == null) {
            // TODO: Diagnostics error
            return null.toIrConst()
        }

        val functionAdditionalParameters = transformedFunctions[functionSymbol]
        if (functionAdditionalParameters == null) {
            // TODO: Diagnostics error
            return null.toIrConst()
        }

        val param = functionAdditionalParameters.firstOrNull { it.typeParameter.symbol == typeParameterSymbol }
        if (param == null) {
            // TODO: Diagnostics error
            return null.toIrConst()
        }

        return IrGetValueImpl(
            startOffset = SYNTHETIC_OFFSET,
            endOffset = SYNTHETIC_OFFSET,
            symbol = param.qualifiedNameParameter.symbol,
        )
    }

    private fun String?.toIrConst() = toIrConst(
        irType = if (this != null) context.irBuiltIns.stringType else context.irBuiltIns.nothingNType,
    )

}
