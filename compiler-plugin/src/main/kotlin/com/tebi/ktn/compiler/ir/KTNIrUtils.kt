package com.tebi.ktn.compiler.ir

import com.tebi.ktn.compiler.KTNIDs
import org.jetbrains.kotlin.ir.declarations.IrAnnotationContainer
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.classId
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.name.Name


@UnsafeDuringIrConstructionAPI
fun IrTypeParameter.hasWithKompileTimeNameAnnotation() = annotations.any { annotation ->
    val annotationClassId = annotation.symbol.owner.parentAsClass.classId
    annotationClassId == KTNIDs.ClassIDs.WithQualifiedName || annotationClassId == KTNIDs.ClassIDs.WithSimpleName
}

fun IrAnnotationContainer.hasKompileTimeNamesAnnotation() =
    hasAnnotation(KTNIDs.ClassIDs.KompileTimeNames)

fun Name.makeQualifiedNameParameterName() =
    Name.identifier($$"$$identifier$qualifiedName")

fun Name.makeSimpleNameParameterName() =
    Name.identifier($$"$$identifier$simpleName")

fun IrDeclaration.isExternalDeclarationStub(): Boolean {
    val origin = origin
    return origin == IrDeclarationOrigin.IR_EXTERNAL_DECLARATION_STUB ||
        origin == IrDeclarationOrigin.IR_EXTERNAL_JAVA_DECLARATION_STUB
}
