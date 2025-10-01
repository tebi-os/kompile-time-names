@file:OptIn(KompileTimeNames::class)

package foo.bar

import com.tebi.ktn.KompileTimeNames
import com.tebi.ktn.WithQualifiedName
import com.tebi.ktn.kompileTimeQualifiedName


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
    return if (actual == expect) {
        "OK"
    } else {
        "Fail: $actual"
    }
}

@KompileTimeNames
inline fun <@WithQualifiedName reified T : Any> someFunction(someValue: Int): String? {
    return someFunction2<T>(someValue)
}

@KompileTimeNames
inline fun <@WithQualifiedName reified U : Any> someFunction2(someValue2: Int): String? {
    return kompileTimeQualifiedName<U>()
}
