package com.tebi.ktn.compiler.fir

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirTypeParameterChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension


class KTNAdditionalCheckersExtension(session: FirSession) : FirAdditionalCheckersExtension(session) {

    override val declarationCheckers = object : DeclarationCheckers() {
        override val typeParameterCheckers: Set<FirTypeParameterChecker> = setOf(
            WithKompileTimeNamesUsageChecker(),
        )
    }

}
