package foo.bar

import com.tebi.ktn.KompileTimeNamesUsage
import com.tebi.ktn.kompileTimeSimpleName


class SomeClass {
    companion object
}

@OptIn(KompileTimeNamesUsage::class)
fun box(): String {
    val actual = listOf(
        kompileTimeSimpleName<String>(),
        kompileTimeSimpleName<SomeClass>(),
        kompileTimeSimpleName<SomeClass.Companion>(),
    )
    val expect = listOf(
        "String",
        "SomeClass",
        "Companion",
    )
    return if (actual == expect) { "OK" } else { "Fail: $actual" }
}
