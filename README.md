# Kompile-time names
Kompile-time names is a Kotlin Multiplatform compiler plugin which inserts the simple or qualified name of a class
at compile time. This preserves the name of the class even if it gets obfuscated using e.g. R8. This also works for
targets where qualified names normally aren't available through reflection such as Kotlin/JS.


## Usage
This plugin can only be added using Gradle:
```kotlin
plugins {
    id("com.tebi.kompile-time-names") version "1.0.1"
}
```


## Example
This snippet shows how the plugin can be used. The invocation of `kompileTimeQualifiedName<SomeClass>()` gets replaced
at compile time with the string constant `"foo.bar.SomeClass"`.
```kotlin
package foo.bar

data class SomeClass(
    val greeting: String,
)

println(kompileTimeQualifiedName<SomeClass>()) // This will print: foo.bar.SomeClass
println(kompileTimeSimpleName<SomeClass>()) // This will print: SomeClass
```


## Wrapping
The `kompileTime*Name()` functions are defined as inline functions with a reified type parameter. This is
needed because the type needs to be known at compile time and can not be resolved dynamically at runtime.

It is possible to wrap these functions in your own function, but with the following restrictions:
1. The wrapper function must itself also be inline and have the type parameter be reified.
2. The wrapper function must be annotated with `@KompileTimeNames`, and your type parameter with either
   `@WithQualifiedName` or `@WithSimpleName` (or both), depending on which function you are going to use.
3. The type parameter must have a non-nullable bound (e.g. `Any`).
4. If you are exposing the wrapper function as part of a library, then both the library module and the
   module that is using your library must apply this plugin.

If any of the above requirements are not met you will get an error during compilation.

It is possible to nest multiple wrapper functions as long as the above requirements are met.

### Wrapping example
```kotlin
@KompileTimeNames
inline fun <@WithQualifiedName reified T : Any> someWrapperFunction(): String? {
    return "wrapped:${kompileTimeQualifiedName<T>()}"
}
```
