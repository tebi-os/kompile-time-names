package com.tebi.ktn.compiler.ir

import com.tebi.ktn.compiler.KTNIDs
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.classifierOrNull
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.parents
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull


class WithKompileTimeNamesCallTransformer(
    context: IrPluginContext,
    private val transformedFunctions: MutableMap<IrSimpleFunctionSymbol, List<GeneratedParameters>>,
) : IrElementTransformerVoidWithContext() {

    /* Properties */

    private val stringType = context.irBuiltIns.stringType
    private val nullableNothingType = context.irBuiltIns.nothingNType

    private val diagnosticReporter = context.diagnosticReporter

    private val kompileTimeQualifiedNameSymbol = context
        .referenceFunctions(KTNIDs.CallableIDs.KompileTimeQualifiedName)
        .singleOrNull()
        ?: error("Unable to resolve symbol for ${KTNIDs.CallableIDs.KompileTimeQualifiedName}")

    private val kompileTimeSimpleNameSymbol = context
        .referenceFunctions(KTNIDs.CallableIDs.KompileTimeSimpleName)
        .singleOrNull()
        ?: error("Unable to resolve symbol for ${KTNIDs.CallableIDs.KompileTimeSimpleName}")


    /* Public functions */

    override fun visitCall(expression: IrCall): IrExpression {
        val expressionSymbol = expression.symbol

        if (expressionSymbol == kompileTimeQualifiedNameSymbol) {
            return expression.substituteKompileTimeQualifiedName()
        }
        if (expressionSymbol == kompileTimeSimpleNameSymbol) {
            return expression.substituteKompileTimeSimpleName()
        }

        transformedFunctions[expressionSymbol]?.let { generatedParameters ->
            expression.addAdditionalArguments(generatedParameters)
            return super.visitCall(expression)
        }

        return super.visitCall(expression)
    }


    /* Private functions */

    private fun IrCall.substituteKompileTimeQualifiedName() =
        typeArguments[0].resolveQualifiedName(this, startOffset, endOffset)

    private fun IrCall.substituteKompileTimeSimpleName() =
        typeArguments[0].resolveSimpleName(this, startOffset, endOffset)

    private fun IrCall.addAdditionalArguments(
        additionalParameters: List<GeneratedParameters>,
    ) {
        for (additionalParameter in additionalParameters) {
            val typeArgument = typeArguments[additionalParameter.typeParameter.index]
            if (additionalParameter.qualifiedNameParameter != null) {
                arguments.add(typeArgument.resolveQualifiedName(this))
            }
            if (additionalParameter.simpleNameParameter != null) {
                arguments.add(typeArgument.resolveSimpleName(this))
            }
        }
    }

    private fun IrType?.resolveQualifiedName(
        expression: IrCall,
        startOffset: Int = SYNTHETIC_OFFSET,
        endOffset: Int = SYNTHETIC_OFFSET,
    ) = resolveKompileTimeName(
        expression = expression,
        startOffset = startOffset,
        endOffset = endOffset,
        classToName = { it.fqNameWhenAvailable?.asString() },
        parameterSelector = { it.qualifiedNameParameter },
        expectedAnnotation = KTNIDs.ClassIDs.WithQualifiedName,
    )

    private fun IrType?.resolveSimpleName(
        expression: IrCall,
        startOffset: Int = SYNTHETIC_OFFSET,
        endOffset: Int = SYNTHETIC_OFFSET,
    ) = resolveKompileTimeName(
        expression = expression,
        startOffset = startOffset,
        endOffset = endOffset,
        classToName = { it.name.asString() },
        parameterSelector = { it.simpleNameParameter },
        expectedAnnotation = KTNIDs.ClassIDs.WithSimpleName,
    )

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    private fun IrType?.resolveKompileTimeName(
        expression: IrCall,
        startOffset: Int,
        endOffset: Int,
        classToName: (IrClass) -> String?,
        parameterSelector: (GeneratedParameters) -> IrValueParameter?,
        expectedAnnotation: ClassId,
    ): IrExpression {
        if (this == null) {
            diagnosticReporter.at(expression, currentFile)
                .report(KTNIrErrors.KTN_TYPE_ARG_NULL)
            return null.toIrConst(startOffset, endOffset)
        }

        classOrNull?.owner?.let { irClass ->
            return classToName(irClass).toIrConst(startOffset, endOffset)
        }

        val typeParameterSymbol = classifierOrNull as? IrTypeParameterSymbol
        if (typeParameterSymbol == null) {
            diagnosticReporter.at(expression, currentFile)
                .report(KTNIrErrors.KTN_TYPE_PARAM_SYMBOL_NULL)
            return null.toIrConst(startOffset, endOffset)
        }

        val functionSymbol = typeParameterSymbol.owner.parents.firstIsInstanceOrNull<IrFunction>()?.symbol
        if (functionSymbol == null) {
            diagnosticReporter.at(typeParameterSymbol.owner)
                .report(KTNIrErrors.KTN_FUNCTION_SYMBOL_NULL)
            return null.toIrConst(startOffset, endOffset)
        }

        val parameter = transformedFunctions[functionSymbol]?.let { generatedParameters ->
            generatedParameters.firstOrNull { it.typeParameter.symbol == typeParameterSymbol }
        }?.let { parameterSelector(it) }
        if (parameter == null) {
            diagnosticReporter.at(typeParameterSymbol.owner)
                .report(KTNIrErrors.KTN_MISSING_TYPE_PARAM_ANNOTATION, expectedAnnotation.shortClassName.identifier)
            return null.toIrConst(startOffset, endOffset)
        }

        return IrGetValueImpl(
            startOffset = startOffset,
            endOffset = endOffset,
            symbol = parameter.symbol,
        )
    }

    private fun String?.toIrConst(startOffset: Int, endOffset: Int) = if (this != null) {
        IrConstImpl.string(startOffset, endOffset, stringType, this)
    } else {
        IrConstImpl.constNull(startOffset, endOffset, nullableNothingType)
    }

}
