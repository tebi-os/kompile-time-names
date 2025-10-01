package com.tebi.ktn.fir

import com.tebi.ktn.KTNIDs
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.ExperimentalTopLevelDeclarationsGenerationApi
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.plugin.createTopLevelFunction
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName


class KTNFirGenerator(
    session: FirSession,
) : FirDeclarationGenerationExtension(session) {

    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?,
    ): List<FirNamedFunctionSymbol> {
        return when (callableId) {
            KTNIDs.CallableIDs.KompileTimeQualifiedName -> {
                listOf(
                    createTopLevelFunction(
                        key = KTNIDs.GeneratorKey,
                        callableId = callableId,
                        returnType = session.builtinTypes.stringType.coneType,
                    ).symbol
                )
            }
            else -> emptyList()
        }
    }

    override fun hasPackage(packageFqName: FqName): Boolean {
        return packageFqName == KTNIDs.PackageNames.KompileTimeNames
    }

    @ExperimentalTopLevelDeclarationsGenerationApi
    override fun getTopLevelCallableIds(): Set<CallableId> {
        return setOf(KTNIDs.CallableIDs.KompileTimeQualifiedName)
    }

}
