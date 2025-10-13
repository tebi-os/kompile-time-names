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
        val KompileTimeSimpleName = CallableId(PackageNames.KompileTimeNames, Name.identifier("kompileTimeSimpleName"))
    }

    object ClassIDs {
        val WithKompileTimeNames = ClassId(PackageNames.KompileTimeNames, Name.identifier("WithKompileTimeNames"))
    }

    object PluginKey : GeneratedDeclarationKey()

    val PluginOrigin = IrDeclarationOrigin.GeneratedByPlugin(PluginKey)

}
