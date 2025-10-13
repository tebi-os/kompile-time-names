package com.tebi.ktn.compiler.ir

import com.tebi.ktn.compiler.KTNIDs
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrValueParameterSymbolImpl
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.visitors.IrTransformer
import org.jetbrains.kotlin.name.Name


class WithKompileTimeNamesFunctionTransformer(
    private val context: IrPluginContext,
) : IrTransformer<MutableMap<IrSimpleFunctionSymbol, List<GeneratedQualifiedNameParameter>>>() {

    override fun visitSimpleFunction(
        declaration: IrSimpleFunction,
        data: MutableMap<IrSimpleFunctionSymbol, List<GeneratedQualifiedNameParameter>>,
    ): IrStatement {
        if (declaration.typeParameters.none { it.hasAnnotation(KTNIDs.ClassIDs.WithKompileTimeNames) }) {
            return super.visitFunction(declaration, data)
        }

        val annotatedTypeParameters = declaration.typeParameters
            .filter { it.hasAnnotation(KTNIDs.ClassIDs.WithKompileTimeNames) }

        val additionalParameters = annotatedTypeParameters.map { typeParameter ->
            GeneratedQualifiedNameParameter(
                typeParameter = typeParameter,
                qualifiedNameParameter = context.irFactory.createValueParameter(
                    startOffset = SYNTHETIC_OFFSET,
                    endOffset = SYNTHETIC_OFFSET,
                    origin = KTNIDs.PluginOrigin,
                    kind = IrParameterKind.Regular,
                    name = Name.identifier($$"$${typeParameter.name.identifier}$qualifiedName"),
                    type = context.irBuiltIns.stringType.makeNullable(),
                    isAssignable = false,
                    symbol = IrValueParameterSymbolImpl(),
                    varargElementType = null,
                    isCrossinline = false,
                    isNoinline = false,
                    isHidden = true,
                ).also {
                    it.parent = declaration
                },
            )
        }

        declaration.parameters += additionalParameters.map { it.qualifiedNameParameter }

        data[declaration.symbol] = additionalParameters

        return super.visitFunction(declaration, data)
    }

}