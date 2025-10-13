package com.tebi.ktn.compiler.ir

import com.tebi.ktn.compiler.KTNIDs
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrPackageFragment
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.IrClassifierSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.util.classId
import org.jetbrains.kotlin.ir.util.toIrConst
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.CallableId


@OptIn(UnsafeDuringIrConstructionAPI::class)
class KTNFunctionReplacementIrTransformer(
    context: IrPluginContext,
) : IrElementTransformerVoid() {

    private val irBuiltIns = context.irBuiltIns

    override fun visitCall(expression: IrCall): IrExpression {
        return when(expression.symbol.owner.callableIdOrNull) {
            KTNIDs.CallableIDs.KompileTimeQualifiedName -> {
                expression.typeArguments.firstOrNull()?.classFqName?.asString().toIrConst(expression)
            }
            KTNIDs.CallableIDs.KompileTimeSimpleName -> {
                expression.typeArguments.firstOrNull()?.classFqName?.shortName()?.asString().toIrConst(expression)
            }
            else -> super.visitCall(expression)
        }
    }

    private val IrFunction.callableIdOrNull: CallableId?
        get() {
            if (this.symbol is IrClassifierSymbol) return null
            return when (val parent = this.parent) {
                is IrClass -> parent.classId?.let { CallableId(it, name) }
                is IrPackageFragment -> CallableId(parent.packageFqName, name)
                else -> null
            }
        }

    private fun String?.toIrConst(expression: IrCall) = toIrConst(
        irType = if (this != null) irBuiltIns.stringType else irBuiltIns.nothingNType,
        startOffset = expression.startOffset,
        endOffset = expression.endOffset,
    )

}
