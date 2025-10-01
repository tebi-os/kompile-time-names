package com.tebi.ktn


/**
 * Every function that uses [WithQualifiedName] or [WithSimpleName] annotations on its type arguments must itself
 * be annotated with this annotation. This function requires opt-in, but applying the `com.tebi.kompile-time-names`
 * Gradle plugin will automatically take care of this opt-in. Thus if an opt-in error does appear it is an indication
 * that the Gradle plugin is not correctly applied.
 */
@RequiresOptIn(
    message = "To use this function, make sure you have applied the com.tebi.kompile-time-names Gradle plugin to " +
        "your module. If you apply the plugin you do not need to manually opt-in.",
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
public annotation class KompileTimeNames


@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.TYPE_PARAMETER)
@KompileTimeNames
public annotation class WithQualifiedName

@KompileTimeNames
@Suppress("unused", "RedundantNullableReturnType")
public inline fun <@WithQualifiedName reified T : Any> kompileTimeQualifiedName(): String? {
    throw NotImplementedError(
        message = "This invocation of kompileTimeQualifiedName() should have been replaced at compile-time with a " +
            "String constant. Make sure you have applied the com.tebi.kompile-time-names Gradle plugin to " +
            "every module that uses this function either directly or indirectly using an inline function.",
    )
}


@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.TYPE_PARAMETER)
@KompileTimeNames
public annotation class WithSimpleName

@KompileTimeNames
@Suppress("unused", "RedundantNullableReturnType")
public inline fun <@WithSimpleName reified T : Any> kompileTimeSimpleName(): String? {
    throw NotImplementedError(
        message = "This invocation of kompileTimeSimpleName() should have been replaced at compile-time with a " +
            "String constant. Make sure you have applied the com.tebi.kompile-time-names Gradle plugin to " +
            "every module that uses this function either directly or indirectly using an inline function.",
    )
}
