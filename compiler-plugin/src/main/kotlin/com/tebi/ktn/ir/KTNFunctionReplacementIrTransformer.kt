package com.tebi.ktn.ir

import com.tebi.ktn.KTNIDs
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.util.callableId
import org.jetbrains.kotlin.ir.util.toIrConst
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid


@OptIn(UnsafeDuringIrConstructionAPI::class)
class KTNFunctionReplacementIrTransformer(
    context: IrPluginContext,
) : IrElementTransformerVoid() {

    private val irBuiltIns = context.irBuiltIns

    override fun visitCall(expression: IrCall): IrExpression {
        val owner = expression.symbol.owner
        val origin = owner.origin
        if (
            origin !is IrDeclarationOrigin.GeneratedByPlugin ||
            origin.pluginKey != KTNIDs.GeneratorKey
        ) {
            return super.visitCall(expression)
        }

        return when(owner.callableId) {
            KTNIDs.CallableIDs.KompileTimeQualifiedName -> {
                expression.typeArguments[0]?.classFqName?.asString().toIrConst(expression)
            }
            KTNIDs.CallableIDs.KompileTimeSimpleName -> {
                expression.typeArguments[0]?.classFqName?.shortName()?.asString().toIrConst(expression)
            }
            else -> super.visitCall(expression)
        }
    }

    private fun String?.toIrConst(expression: IrCall) = toIrConst(
        irType = if (this != null) irBuiltIns.stringType else irBuiltIns.nothingNType,
        startOffset = expression.startOffset,
        endOffset = expression.endOffset,
    )

}
