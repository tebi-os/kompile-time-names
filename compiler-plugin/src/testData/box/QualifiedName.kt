package foo.bar

import com.tebi.ktn.KompileTimeNamesUsage
import com.tebi.ktn.kompileTimeQualifiedName


class SomeClass {
    companion object
}

@OptIn(KompileTimeNamesUsage::class)
fun box(): String {
    val actual = listOf(
        kompileTimeQualifiedName<String>(),
        kompileTimeQualifiedName<SomeClass>(),
        kompileTimeQualifiedName<SomeClass.Companion>(),
    )
    val expect = listOf(
        "kotlin.String",
        "foo.bar.SomeClass",
        "foo.bar.SomeClass.Companion",
    )
    return if (actual == expect) { "OK" } else { "Fail: $actual" }
}
