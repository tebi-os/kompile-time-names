# Kompile-time names
Kompile-time names is a Kotlin Multiplatform compiler plugin which inserts the simple or qualified name of a class
at compile time. This preserves the name of the class even if it gets obfuscated using e.g. R8. This also works for
targets where qualified names normally aren't available through reflection such as Kotlin/JS.

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

## Usage
This plugin can only be added using Gradle:
```kotlin
plugins {
    id("com.tebi.kompile-time-names") version "0.0.1-alpha"
}
```
