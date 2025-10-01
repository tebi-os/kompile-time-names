@file:OptIn(KompileTimeNames::class)

package foo.bar

import com.tebi.ktn.KompileTimeNames
import com.tebi.ktn.WithQualifiedName
import com.tebi.ktn.WithSimpleName
import com.tebi.ktn.kompileTimeQualifiedName
import com.tebi.ktn.kompileTimeSimpleName


class SomeClass

fun box(): String {
    val actual = listOf(
        someFunction<SomeClass>(),
    )
    val expect = listOf(
        "foo.bar.SomeClass:SomeClass",
    )
    return if (actual == expect) {
        "OK"
    } else {
        "Fail: $actual"
    }
}

@KompileTimeNames
inline fun <
    @WithQualifiedName @WithSimpleName
    reified T : Any,
    > someFunction(): String? {
    return "${kompileTimeQualifiedName<T>()}:${kompileTimeSimpleName<T>()}"
}
