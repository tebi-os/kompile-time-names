@file:OptIn(KompileTimeNames::class)

package foo.bar

import com.tebi.ktn.KompileTimeNames
import com.tebi.ktn.WithSimpleName
import com.tebi.ktn.kompileTimeSimpleName


class SomeClass {
    companion object
}

fun box(): String {
    val actual = listOf(
        kompileTimeSimpleName<String>(),
        kompileTimeSimpleName<SomeClass>(),
        kompileTimeSimpleName<SomeClass.Companion>(),
        someFunction<SomeClass>(2),
    )
    val expect = listOf(
        "String",
        "SomeClass",
        "Companion",
        "SomeClass",
    )
    return if (actual == expect) {
        "OK"
    } else {
        "Fail: $actual"
    }
}

@KompileTimeNames
inline fun <@WithSimpleName reified T : Any> someFunction(someValue: Int): String? {
    return someFunction2<T>(someValue)
}

@KompileTimeNames
inline fun <@WithSimpleName reified U : Any> someFunction2(someValue2: Int): String? {
    return kompileTimeSimpleName<U>()
}
