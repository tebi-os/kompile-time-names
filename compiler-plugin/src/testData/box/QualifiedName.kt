@file:OptIn(KompileTimeNamesUsage::class)

package foo.bar

import com.tebi.ktn.KompileTimeNamesUsage
import com.tebi.ktn.kompileTimeQualifiedName
import com.tebi.ktn.WithKompileTimeNames


class SomeClass {
    companion object
}

fun box(): String {
    val actual = listOf(
        kompileTimeQualifiedName<String>(),
        kompileTimeQualifiedName<SomeClass>(),
        kompileTimeQualifiedName<SomeClass.Companion>(),
        someFunction<SomeClass>(2),
    )
    val expect = listOf(
        "kotlin.String",
        "foo.bar.SomeClass",
        "foo.bar.SomeClass.Companion",
        "foo.bar.SomeClass",
    )
    return if (actual == expect) { "OK" } else { "Fail: $actual" }
}

@KompileTimeNamesUsage
inline fun <@WithKompileTimeNames reified T : Any> someFunction(someValue: Int): String? {
    return someFunction2<T>(someValue)
}

@KompileTimeNamesUsage
inline fun <@WithKompileTimeNames reified U : Any> someFunction2(someValue2: Int): String? {
    return kompileTimeQualifiedName<U>()
}
