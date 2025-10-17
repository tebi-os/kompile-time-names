package com.tebi.ktn.compiler.ir

import com.tebi.ktn.compiler.KTNIDs
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.lower.irThrow
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irCallConstructor
import org.jetbrains.kotlin.ir.builders.irExprBody
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.callableId
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.hasDefaultValue
import org.jetbrains.kotlin.ir.util.setDeclarationsParent
import org.jetbrains.kotlin.ir.util.toIrConst
import org.jetbrains.kotlin.ir.visitors.IrTransformer


class WithKompileTimeNamesFunctionTransformer(
    private val context: IrPluginContext,
) : IrTransformer<WithKompileTimeNamesTransformations>() {

    /* Properties */

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    private val notImplementedErrorConstructorSymbol: IrConstructorSymbol by lazy {
        val errorSymbol = context.referenceClass(KTNIDs.ClassIDs.NotImplementedError)
            ?: error("Could not resolve symbol for ${KTNIDs.ClassIDs.NotImplementedError.asFqNameString()}")
        val constructorSymbol = errorSymbol.constructors.firstOrNull { constructorSymbol ->
            val parameter = constructorSymbol.owner.parameters.singleOrNull() ?: return@firstOrNull false
            parameter.kind == IrParameterKind.Regular && parameter.type == context.irBuiltIns.stringType
        } ?: error("Could not resolve constructor symbol for ${KTNIDs.ClassIDs.NotImplementedError.asFqNameString()}(String)")
        constructorSymbol
    }


    /* Public functions */

    override fun visitSimpleFunction(
        declaration: IrSimpleFunction,
        data: WithKompileTimeNamesTransformations,
    ): IrStatement {
        val origin = declaration.origin
        if (origin !is IrDeclarationOrigin.GeneratedByPlugin || origin.pluginKey != KTNIDs.PluginOrigin.pluginKey) {
            return super.visitSimpleFunction(declaration, data)
        }
        check(declaration.body == null) { "Expected body of ${declaration.fqNameWhenAvailable} to be null" }

        val original = findOriginal(declaration)
            ?: error("Unable to find original function of ${declaration.fqNameWhenAvailable}")

        declaration.setThrowNotImplementedErrorBody(
            message = "This invocation of ${original.fqNameWhenAvailable} should have been transformed at " +
                    "compile-time. Make sure you have applied the com.tebi.kompile-time-names Gradle plugin to " +
                    "every module that uses this function either directly or indirectly using an inline function.",
        )

        val annotatedTypeParameters = original.typeParameters
            .filter { it.hasAnnotation(KTNIDs.ClassIDs.WithKompileTimeNames) }

        val additionalParameters = annotatedTypeParameters.mapIndexed { index, typeParameter ->
            GeneratedQualifiedNameParameter(
                typeParameter = typeParameter,
                qualifiedNameParameter = declaration.parameters[original.parameters.size + index],
            )
        }

        swapNames(declaration, original)
        moveAdditionalParameters(declaration, original, additionalParameters)

        data.register(declaration, original, additionalParameters)

        return super.visitSimpleFunction(declaration, data)
    }


    /* Private functions */

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    private fun findOriginal(declaration: IrSimpleFunction): IrSimpleFunction? {
        val originalCallableId = declaration.callableId.copy(
            callableName = KTNIDs.originalWithKompileTimeNamesFunctionName(declaration.callableId.callableName),
        )
        return context.referenceFunctions(originalCallableId)
            .map { it.owner }
            .firstOrNull { declaration.matchesOriginal(it) }
    }

    private fun IrSimpleFunction.matchesOriginal(original: IrSimpleFunction): Boolean {
        if (
            original.visibility != visibility ||
            original.modality != modality ||
            original.returnType != returnType ||
            original.typeParameters.size != typeParameters.size
        ) return false

        val annotatedTypeParameters = mutableListOf<IrTypeParameter>()
        for ((index, originalTypeParameter) in original.typeParameters.withIndex()) {
            val typeParameter = typeParameters[index]
            if (
                originalTypeParameter.name != typeParameter.name ||
                originalTypeParameter.variance != typeParameter.variance ||
                originalTypeParameter.isReified != typeParameter.isReified ||
                originalTypeParameter.superTypes != typeParameter.superTypes
            ) return false
            if (originalTypeParameter.hasAnnotation(KTNIDs.ClassIDs.WithKompileTimeNames)) {
                annotatedTypeParameters.add(originalTypeParameter)
            }
        }

        if (original.parameters.size + annotatedTypeParameters.size != parameters.size) return false

        for ((index, originalParameter) in original.parameters.withIndex()) {
            val parameter = parameters[index]
            if (
                originalParameter.name != parameter.name ||
                originalParameter.type != parameter.type ||
                originalParameter.kind != parameter.kind ||
                originalParameter.varargElementType != parameter.varargElementType ||
                originalParameter.hasDefaultValue() != parameter.hasDefaultValue()
            ) return false
        }

        for ((index, annotatedTypeParameter) in annotatedTypeParameters.withIndex()) {
            val parameter = parameters[original.parameters.size + index]
            if (parameter.name != KTNIDs.makeQualifiedNameValueParameterName(annotatedTypeParameter.name)) return false
        }

        return true
    }

    private fun swapNames(
        declaration: IrSimpleFunction,
        original: IrSimpleFunction,
    ) {
        val temp = declaration.name
        declaration.name = original.name
        original.name = temp

        declaration.file
    }

    private fun moveAdditionalParameters(
        declaration: IrSimpleFunction,
        original: IrSimpleFunction,
        additionalParameters: List<GeneratedQualifiedNameParameter>,
    ) {
        declaration.parameters = declaration.parameters.dropLast(additionalParameters.size)
        original.parameters += additionalParameters.map { it.qualifiedNameParameter.setDeclarationsParent(original) }
    }

    private fun IrSimpleFunction.setThrowNotImplementedErrorBody(
        message: String,
    ) = with(DeclarationIrBuilder(context, symbol)) {
        body = irExprBody(
            value = irThrow(
                arg = irCallConstructor(notImplementedErrorConstructorSymbol, emptyList()).apply {
                    arguments[0] = message.toIrConst(context.irBuiltIns.stringType)
                },
            ),
        )
    }

}