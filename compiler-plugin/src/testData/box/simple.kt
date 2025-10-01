package foo.bar

import com.tebi.ktn.kompileTimeQualifiedName
import kotlin.reflect.KClass


fun box(): String {
    val result = kompileTimeQualifiedName()
    return if (result == "Hey there!") { "OK" } else { "Fail: $result" }
}
