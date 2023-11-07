package nl.sanderdijkhuis.hashtocurve

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class ElementSpec {
    @Test
    fun squareRoot() {
        val field = Curve.p256.field
        repeat(100) {
            val integer = Integer(Random.nextBytes(40)).run { modulo(this, field.order) }
            val squared = Integer { exponent(integer, Integer.two, field.order) }
            val element = Element(field, squared)
            val sqrt = element.squareRoot()
            val squared2 = Integer { exponent(sqrt.integer, Integer.two, field.order) }
            assertEquals(squared, squared2)
        }
    }

    @Test
    fun exposure() {
        nl.sanderdijkhuis.hashtocurve.Size
    }
}
