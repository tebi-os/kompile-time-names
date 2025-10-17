package com.tebi.ktn.compiler.fir

import com.tebi.ktn.compiler.KTNIDs
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.getContainingClassSymbol
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.declarations.utils.isInline
import org.jetbrains.kotlin.fir.declarations.utils.isSuspend
import org.jetbrains.kotlin.fir.declarations.utils.isTailRec
import org.jetbrains.kotlin.fir.declarations.utils.modality
import org.jetbrains.kotlin.fir.declarations.utils.visibility
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.plugin.createTopLevelFunction
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.types.typeContext
import org.jetbrains.kotlin.fir.types.withNullability
import org.jetbrains.kotlin.name.CallableId


class WithKompileTimeNamesFunctionGenerator(session: FirSession) : FirDeclarationGenerationExtension(session) {

    private val kompileTimeNamesUsagePredicate = LookupPredicate.AnnotatedWith(
        annotations = setOf(KTNIDs.ClassIDs.KompileTimeNamesUsage.asSingleFqName()),
    )

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(kompileTimeNamesUsagePredicate)
    }

    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirNamedFunctionSymbol> {
        val originalFunctionSymbols = session.symbolProvider.getTopLevelFunctionSymbols(
            packageFqName = callableId.packageName,
            name = KTNIDs.originalWithKompileTimeNamesFunctionName(callableId.callableName),
        ).filterWithKompileTimeNames()

        return originalFunctionSymbols.map { functionSymbol ->
            createTopLevelFunction(
                key = KTNIDs.PluginKey,
                callableId = callableId,
                returnType = functionSymbol.resolvedReturnType,
                config = {
                    visibility = functionSymbol.visibility
                    modality = functionSymbol.modality
                    status {
                        isInline = functionSymbol.isInline
                        isTailRec = functionSymbol.isTailRec
                        isSuspend = functionSymbol.isSuspend
                    }
                    functionSymbol.receiverParameterSymbol?.resolvedType?.let {
                        extensionReceiverType(it)
                    }
                    for (valueParameterSymbol in functionSymbol.valueParameterSymbols) {
                        valueParameter(
                            name = valueParameterSymbol.name,
                            type = valueParameterSymbol.resolvedReturnType,
                            isCrossinline = valueParameterSymbol.isCrossinline,
                            isNoinline = valueParameterSymbol.isNoinline,
                            isVararg = valueParameterSymbol.isVararg,
                            hasDefaultValue = valueParameterSymbol.hasDefaultValue,
                        )
                    }
                    for (typeParameterSymbol in functionSymbol.typeParameterSymbols) {
                        typeParameter(
                            name = typeParameterSymbol.name,
                            variance = typeParameterSymbol.variance,
                            isReified = typeParameterSymbol.isReified,
                            config = {
                                for (bound in typeParameterSymbol.resolvedBounds) {
                                    bound(bound.coneType)
                                }
                            }
                        )
                        if (typeParameterSymbol.hasAnnotation(KTNIDs.ClassIDs.WithKompileTimeNames, session)) {
                            valueParameter(
                                name = KTNIDs.makeQualifiedNameValueParameterName(typeParameterSymbol.name),
                                type = session.builtinTypes.stringType.coneType.withNullability(true, session.typeContext),
                            )
                        }
                    }

                },
            ).apply {
                replaceAnnotations(functionSymbol.resolvedAnnotationsWithArguments)
                functionSymbol.typeParameterSymbols.forEachIndexed { index, typeParameterSymbol ->
                    typeParameters[index].replaceAnnotations(typeParameterSymbol.resolvedAnnotationsWithArguments)
                }
                functionSymbol.valueParameterSymbols.forEachIndexed { index, valueParameterSymbol ->
                    valueParameters[index].replaceAnnotations(valueParameterSymbol.resolvedAnnotationsWithArguments)
                }
            }.symbol
        }
    }

    override fun getTopLevelCallableIds(): Set<CallableId> {
        return session.predicateBasedProvider.getSymbolsByPredicate(kompileTimeNamesUsagePredicate)
            .filterIsInstance<FirNamedFunctionSymbol>()
//            .filterWithKompileTimeNames()
            .mapTo(mutableSetOf()) { functionSymbol ->
                functionSymbol.callableId.copy(
                    callableName = KTNIDs.transformWithKompileTimeNamesFunctionName(functionSymbol.callableId.callableName),
                )
            }
    }

    private fun Iterable<FirNamedFunctionSymbol>.filterWithKompileTimeNames() = filter { functionSymbol ->
        functionSymbol.getContainingClassSymbol() == null && functionSymbol.typeParameterSymbols.any {
            it.hasAnnotation(KTNIDs.ClassIDs.WithKompileTimeNames, session)
        }
    }

}
