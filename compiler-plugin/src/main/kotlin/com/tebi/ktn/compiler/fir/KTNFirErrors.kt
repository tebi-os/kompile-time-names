package com.tebi.ktn.compiler.fir

import com.tebi.ktn.compiler.BuildConfig
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies.WHOLE_ELEMENT
import org.jetbrains.kotlin.diagnostics.error0
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.psi.KtElement


object KTNFirErrors : KtDiagnosticsContainer() {

    val KTN_TYPE_PARAM_NOT_ON_SIMPLE_FUNCTION by error0<KtElement>(WHOLE_ELEMENT)
    val KTN_TYPE_PARAM_NOT_REIFIED by error0<KtElement>(WHOLE_ELEMENT)
    val KTN_TYPE_PARAM_NULLABLE_BOUND by error0<KtElement>(WHOLE_ELEMENT)
    val KTN_MISSING_FUNCTION_ANNOTATION by error0<KtElement>(WHOLE_ELEMENT)

    override fun getRendererFactory() = KTNFirErrorsRendererFactory

}


object KTNFirErrorsRendererFactory : BaseDiagnosticRendererFactory() {
    override val MAP: KtDiagnosticFactoryToRendererMap by KtDiagnosticFactoryToRendererMap(BuildConfig.DISPLAY_NAME) { map ->
        map.put(KTNFirErrors.KTN_TYPE_PARAM_NOT_ON_SIMPLE_FUNCTION, "Only type parameters of simple functions may be annotated with @WithQualifiedName or @WithSimpleName")
        map.put(KTNFirErrors.KTN_TYPE_PARAM_NOT_REIFIED, "Type parameters annotated with @WithQualifiedName or @WithSimpleName must be reified")
        map.put(KTNFirErrors.KTN_TYPE_PARAM_NULLABLE_BOUND, "Type parameters annotated with @WithQualifiedName or @WithSimpleName must have a non-nullable bound")
        map.put(KTNFirErrors.KTN_MISSING_FUNCTION_ANNOTATION, "Functions with type parameters annotated with @WithQualifiedName or @WithSimpleName must themselves be annotated with @KompileTimeNames")
    }
}
