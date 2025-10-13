package com.tebi.ktn.compiler.fir

import com.tebi.ktn.compiler.BuildConfig
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies.WHOLE_ELEMENT
import org.jetbrains.kotlin.diagnostics.error0
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.psi.KtElement


object KTNErrors : KtDiagnosticsContainer() {

    val KTN_TYPE_PARAM_NOT_REIFIED by error0<KtElement>(WHOLE_ELEMENT)
    val KTN_TYPE_PARAM_NULLABLE_BOUND by error0<KtElement>(WHOLE_ELEMENT)

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = DefaultErrorMessageMappie

}


object DefaultErrorMessageMappie : BaseDiagnosticRendererFactory() {
    override val MAP: KtDiagnosticFactoryToRendererMap by KtDiagnosticFactoryToRendererMap(BuildConfig.DISPLAY_NAME) { map ->
        map.put(KTNErrors.KTN_TYPE_PARAM_NOT_REIFIED, "Type parameters annotated with @WithKompileTimeNames must be reified")
        map.put(KTNErrors.KTN_TYPE_PARAM_NULLABLE_BOUND, "Type parameters annotated with @WithKompileTimeNames must have a non-nullable bound")
    }
}