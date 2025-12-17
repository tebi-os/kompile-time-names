package com.tebi.ktn.testing

import com.tebi.ktn.testing.runners.AbstractJvmBoxTest
import org.jetbrains.kotlin.generators.dsl.junit5.generateTestGroupSuiteWithJUnit5


fun main() {
    generateTestGroupSuiteWithJUnit5 {
        testGroup(testDataRoot = "compiler-plugin/src/testData", testsRoot = "compiler-plugin/src/test-gen") {
            testClass<AbstractJvmBoxTest> {
                model("box")
            }
        }
    }
}
