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
        val KompileTimeNames = ClassId(PackageNames.KompileTimeNames, Name.identifier("KompileTimeNames"))
        val WithQualifiedName = ClassId(PackageNames.KompileTimeNames, Name.identifier("WithQualifiedName"))
        val WithSimpleName = ClassId(PackageNames.KompileTimeNames, Name.identifier("WithSimpleName"))
        val HiddenFromObjC = ClassId.fromString("kotlin/native/HiddenFromObjC")
    }

    object PluginKey : GeneratedDeclarationKey()

    val PluginOrigin = IrDeclarationOrigin.GeneratedByPlugin(PluginKey)

}
