package com.tebi.ktn.compiler

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name


object KTNIDs {

    object PackageNames {
        val KompileTimeNames = FqName.fromSegments(listOf("com", "tebi", "ktn"))
    }

    object CallableIDs {
        val KompileTimeQualifiedName = CallableId(PackageNames.KompileTimeNames, Name.identifier("kompileTimeQualifiedName"))
    }

    object ClassIDs {
        val NotImplementedError = ClassId.fromString("kotlin/NotImplementedError")
        val KompileTimeNamesUsage = ClassId(PackageNames.KompileTimeNames, Name.identifier("KompileTimeNamesUsage"))
        val WithKompileTimeNames = ClassId(PackageNames.KompileTimeNames, Name.identifier("WithKompileTimeNames"))
    }

    fun makeQualifiedNameValueParameterName(typeParameterName: Name): Name {
        return Name.identifier(typeParameterName.identifier + $$"$qualifiedName")
    }

    fun transformWithKompileTimeNamesFunctionName(functionName: Name): Name {
        return Name.identifier(functionName.identifier + $$"$wktn")
    }

    fun originalWithKompileTimeNamesFunctionName(functionName: Name): Name {
        check(functionName.identifier.endsWith($$"$wktn"))
        return Name.identifier(functionName.identifier.dropLast(5))
    }

    object PluginKey : GeneratedDeclarationKey()

    val PluginOrigin = IrDeclarationOrigin.GeneratedByPlugin(PluginKey)

}
