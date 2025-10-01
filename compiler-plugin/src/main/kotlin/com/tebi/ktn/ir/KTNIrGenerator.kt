package com.tebi.ktn.ir

import com.tebi.ktn.KTNIDs
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.createBlockBody
import org.jetbrains.kotlin.ir.expressions.IrConstKind
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrReturnImpl
import org.jetbrains.kotlin.ir.util.callableId
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid


class KTNIrGenerator(
    context: IrPluginContext,
) : IrVisitorVoid() {

    private val irBuiltIns = context.irBuiltIns
    private val irFactory = context.irFactory

    override fun visitElement(element: IrElement) {
        element.acceptChildrenVoid(this)
    }

    override fun visitSimpleFunction(declaration: IrSimpleFunction) {
        val origin = declaration.origin
        if (
            origin !is IrDeclarationOrigin.GeneratedByPlugin ||
            origin.pluginKey != KTNIDs.GeneratorKey ||
            declaration.body != null
        ) return

        when {
            declaration.callableId == KTNIDs.CallableIDs.KompileTimeQualifiedName -> {
                val const = IrConstImpl(
                    startOffset = -1,
                    endOffset = -1,
                    type = irBuiltIns.stringType,
                    kind = IrConstKind.String,
                    value = "Hey there!",
                )
                val returnStatement = IrReturnImpl(
                    startOffset = -1,
                    endOffset = -1,
                    type = irBuiltIns.nothingType,
                    returnTargetSymbol = declaration.symbol,
                    value = const,
                )
                val body = irFactory.createBlockBody(
                    startOffset = -1,
                    endOffset = -1,
                    statements = listOf(returnStatement),
                )
                declaration.body = body
            }
        }
    }

}
