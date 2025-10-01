package com.tebi.ktn

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name


object KTNIDs {

    object GeneratorKey : GeneratedDeclarationKey()

    object PackageNames {
        val KompileTimeNames = FqName.fromSegments(listOf("com", "tebi", "ktn"))
    }

    object ClassIDs {
        val NotImplementedError = ClassId.fromString("kotlin/NotImplementedError")
    }

    object CallableIDs {
        val KompileTimeQualifiedName = CallableId(PackageNames.KompileTimeNames, Name.identifier("kompileTimeQualifiedName"))
        val KompileTimeSimpleName = CallableId(PackageNames.KompileTimeNames, Name.identifier("kompileTimeSimpleName"))
    }

    object TypeParameterNames {
        val T = Name.identifier("T")
    }

}
