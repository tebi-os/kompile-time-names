package com.tebi.ktn.ir

import com.tebi.ktn.KTNIDs
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.lower.irThrow
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCallConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.callableId
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.toIrConst
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid


@OptIn(UnsafeDuringIrConstructionAPI::class)
class KTNFunctionIrGenerator(
    private val context: IrPluginContext,
) : IrVisitorVoid() {

    private val notImplementedErrorConstructorSymbol: IrConstructorSymbol? by lazy {
        val errorSymbol = context.referenceClass(KTNIDs.ClassIDs.NotImplementedError)
        if (errorSymbol == null) {
            context.messageCollector.report(
                severity = CompilerMessageSeverity.ERROR,
                message = "Could not resolve symbol for ${KTNIDs.ClassIDs.NotImplementedError.asFqNameString()}",
            )
            return@lazy null
        }
        val constructorSymbol = errorSymbol.constructors.firstOrNull { constructorSymbol ->
            val parameter = constructorSymbol.owner.parameters.singleOrNull() ?: return@firstOrNull false
            parameter.kind == IrParameterKind.Regular && parameter.type == context.irBuiltIns.stringType
        }
        if (constructorSymbol == null) {
            context.messageCollector.report(
                severity = CompilerMessageSeverity.ERROR,
                message = "Could not resolve constructor symbol for ${KTNIDs.ClassIDs.NotImplementedError.asFqNameString()}(String)",
            )
            return@lazy null
        }
        constructorSymbol
    }

    override fun visitElement(element: IrElement) {
        when (element) {
            is IrFile, is IrModuleFragment -> element.acceptChildrenVoid(this)
            else -> {}
        }
    }

    override fun visitSimpleFunction(declaration: IrSimpleFunction) {
        val origin = declaration.origin
        if (
            origin !is IrDeclarationOrigin.GeneratedByPlugin ||
            origin.pluginKey != KTNIDs.GeneratorKey ||
            declaration.body != null
        ) return

        when (declaration.callableId) {
            KTNIDs.CallableIDs.KompileTimeQualifiedName, KTNIDs.CallableIDs.KompileTimeSimpleName -> {
                declaration.body = createThrowNotImplementedErrorBody(declaration.symbol)
            }
        }
    }

    private fun createThrowNotImplementedErrorBody(
        symbol: IrSymbol,
    ): IrBlockBody? {
        val constructorSymbol = notImplementedErrorConstructorSymbol ?: return null
        return DeclarationIrBuilder(context, symbol).irBlockBody {
            +irThrow(
                irCallConstructor(constructorSymbol, emptyList()).apply {
                    arguments[0] = "This call should have been replaced by a String constant"
                        .toIrConst(context.irBuiltIns.stringType)
                }
            )
        }
    }

}
