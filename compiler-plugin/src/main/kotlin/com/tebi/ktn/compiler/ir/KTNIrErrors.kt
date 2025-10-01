package com.tebi.ktn.compiler.ir

import com.tebi.ktn.compiler.BuildConfig
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies.WHOLE_ELEMENT
import org.jetbrains.kotlin.diagnostics.error0
import org.jetbrains.kotlin.diagnostics.error1
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.CommonRenderers
import org.jetbrains.kotlin.psi.KtElement


object KTNIrErrors : KtDiagnosticsContainer() {

    val KTN_TYPE_ARG_NULL by error0<KtElement>(WHOLE_ELEMENT)
    val KTN_TYPE_PARAM_SYMBOL_NULL by error0<KtElement>(WHOLE_ELEMENT)
    val KTN_FUNCTION_SYMBOL_NULL by error0<KtElement>(WHOLE_ELEMENT)
    val KTN_MISSING_TYPE_PARAM_ANNOTATION by error1<KtElement, String>(WHOLE_ELEMENT)

    override fun getRendererFactory() = KTNIrErrorsRendererFactory

}


object KTNIrErrorsRendererFactory : BaseDiagnosticRendererFactory() {
    override val MAP: KtDiagnosticFactoryToRendererMap by KtDiagnosticFactoryToRendererMap(BuildConfig.DISPLAY_NAME) { map ->
        map.put(KTNIrErrors.KTN_TYPE_ARG_NULL, "Type argument is missing")
        map.put(KTNIrErrors.KTN_TYPE_PARAM_SYMBOL_NULL, "Could not determine type parameter symbol")
        map.put(KTNIrErrors.KTN_FUNCTION_SYMBOL_NULL, "Could not find type parameter's function symbol")
        map.put(KTNIrErrors.KTN_MISSING_TYPE_PARAM_ANNOTATION, "Type parameter should be annotated with @{0}", CommonRenderers.STRING)
    }
}
