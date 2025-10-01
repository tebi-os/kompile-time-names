package com.tebi.ktn.compiler.fir

import com.tebi.ktn.compiler.KTNIDs
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirTypeParameter
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.declarations.toAnnotationClassId
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.types.coneTypeOrNull
import org.jetbrains.kotlin.fir.types.isMarkedNullable


context(session: FirSession)
fun FirTypeParameter.hasWithKompileTimeNameAnnotation() = annotations.any { annotation ->
    val annotationClassId = annotation.toAnnotationClassId(session)
    annotationClassId == KTNIDs.ClassIDs.WithQualifiedName || annotationClassId == KTNIDs.ClassIDs.WithSimpleName
}

context(session: FirSession)
fun FirBasedSymbol<*>.hasKompileTimeNamesAnnotation() =
    resolvedAnnotationsWithClassIds.hasAnnotation(KTNIDs.ClassIDs.KompileTimeNames, session)

fun FirTypeParameter.hasNonNullableBounds() = bounds.any {
    it.coneTypeOrNull?.isMarkedNullable == false
}
