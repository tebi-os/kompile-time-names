package com.tebi.ktn.compiler.fir

import com.tebi.ktn.compiler.KTNIDs
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirTypeParameterChecker
import org.jetbrains.kotlin.fir.declarations.FirTypeParameter
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.types.coneTypeOrNull
import org.jetbrains.kotlin.fir.types.isMarkedNullable


class WithKompileTimeNamesUsageChecker : FirTypeParameterChecker(MppCheckerKind.Common) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirTypeParameter) {
        if (!declaration.hasAnnotation(KTNIDs.ClassIDs.WithKompileTimeNames, context.session)) return
        if (!declaration.isReified) {
            reporter.reportOn(declaration.source, KTNErrors.KTN_TYPE_PARAM_NOT_REIFIED)
        }
        if (declaration.bounds.none { it.coneTypeOrNull?.isMarkedNullable == false }) {
            reporter.reportOn(declaration.source, KTNErrors.KTN_TYPE_PARAM_NULLABLE_BOUND)
        }
    }

}
