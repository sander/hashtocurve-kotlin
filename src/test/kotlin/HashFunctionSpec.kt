package nl.sanderdijkhuis.hashtocurve

import kotlin.test.Test

class HashFunctionSpec {
    @Test
    fun test() {
        HashFunction.SHA256("Hello, World!".toByteArray())
    }
}
