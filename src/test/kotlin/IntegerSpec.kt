package nl.sanderdijkhuis.hashtocurve

import kotlin.test.Test
import kotlin.test.assertEquals

class IntegerSpec {
    @Test
    fun roundTrip() {
        for (integer in listOf(Integer.zero, Integer.one))
            assertEquals(integer, Integer(integer.bytes(Size(32))!!))
    }
}
