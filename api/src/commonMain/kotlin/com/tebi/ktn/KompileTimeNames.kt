package com.tebi.ktn


@RequiresOptIn(
    message = "To use this function, make sure you have applied the com.tebi.kompile-time-names Gradle plugin to " +
            "your module. If you apply the plugin you do not need to manually opt-in.",
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
public annotation class KompileTimeNamesUsage


@KompileTimeNamesUsage
@Suppress("unused", "RedundantNullableReturnType")
public inline fun <reified T : Any> kompileTimeQualifiedName(): String? {
    throw NotImplementedError(
        message = "This invocation of kompileTimeQualifiedName() should have been replaced at compile-time with a " +
                "String constant. Make sure you have applied the com.tebi.kompile-time-names Gradle plugin to " +
                "every module that uses this function either directly or indirectly using an inline function.",
    )
}

@KompileTimeNamesUsage
@Suppress("unused", "RedundantNullableReturnType")
public inline fun <reified T : Any> kompileTimeSimpleName(): String? {
    throw NotImplementedError(
        message = "This invocation of kompileTimeSimpleName() should have been replaced at compile-time with a " +
                "String constant. Make sure you have applied the com.tebi.kompile-time-names Gradle plugin to " +
                "every module that uses this function either directly or indirectly using an inline function.",
    )
}
