package com.tebi.ktn.compiler.ir

import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.declarations.IrValueParameter


class GeneratedQualifiedNameParameter(
    val typeParameter: IrTypeParameter,
    val qualifiedNameParameter: IrValueParameter
)
