package com.tebi.ktn.fir

import com.tebi.ktn.KTNIDs
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.ExperimentalTopLevelDeclarationsGenerationApi
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.plugin.createTopLevelFunction
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.types.typeContext
import org.jetbrains.kotlin.fir.types.withNullability
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName


class KTNFunctionFirGenerator(
    session: FirSession,
) : FirDeclarationGenerationExtension(session) {

    private val builtinTypes = session.builtinTypes

    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?,
    ): List<FirNamedFunctionSymbol> = when (callableId) {
        KTNIDs.CallableIDs.KompileTimeQualifiedName, KTNIDs.CallableIDs.KompileTimeSimpleName -> {
            listOf(
                createTopLevelFunction(
                    key = KTNIDs.GeneratorKey,
                    callableId = callableId,
                    returnType = builtinTypes.stringType.coneType.withNullability(true, session.typeContext),
                    config = {
                        typeParameter(
                            name = KTNIDs.TypeParameterNames.T,
                            isReified = true,
                            config = {
                                bound(builtinTypes.anyType.coneType)
                            }
                        )
                    }
                ).symbol
            )
        }
        else -> emptyList()
    }

    override fun hasPackage(packageFqName: FqName): Boolean {
        return packageFqName == KTNIDs.PackageNames.KompileTimeNames
    }

    @ExperimentalTopLevelDeclarationsGenerationApi
    override fun getTopLevelCallableIds(): Set<CallableId> {
        return setOf(
            KTNIDs.CallableIDs.KompileTimeQualifiedName,
            KTNIDs.CallableIDs.KompileTimeSimpleName,
        )
    }

}
