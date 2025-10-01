package com.tebi.ktn.compiler.fir

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirTypeParameterChecker
import org.jetbrains.kotlin.fir.declarations.FirTypeParameter
import org.jetbrains.kotlin.mpp.SimpleFunctionSymbolMarker


class WithKompileTimeNamesUsageChecker : FirTypeParameterChecker(MppCheckerKind.Common) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirTypeParameter) = context(context.session) {
        if (!declaration.hasWithKompileTimeNameAnnotation()) return

        val containingDeclarationSymbol = declaration.containingDeclarationSymbol
        if (containingDeclarationSymbol !is SimpleFunctionSymbolMarker) {
            reporter.reportOn(declaration.source, KTNFirErrors.KTN_TYPE_PARAM_NOT_ON_SIMPLE_FUNCTION)
        } else {
            if (!declaration.isReified) {
                reporter.reportOn(declaration.source, KTNFirErrors.KTN_TYPE_PARAM_NOT_REIFIED)
            }
            if (!declaration.hasNonNullableBounds()) {
                reporter.reportOn(declaration.source, KTNFirErrors.KTN_TYPE_PARAM_NULLABLE_BOUND)
            }
            if (!containingDeclarationSymbol.hasKompileTimeNamesAnnotation()) {
                reporter.reportOn(declaration.containingDeclarationSymbol.source, KTNFirErrors.KTN_MISSING_FUNCTION_ANNOTATION)
            }
        }
    }

}
