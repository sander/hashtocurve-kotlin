package nl.sanderdijkhuis.hashtocurve

import nl.sanderdijkhuis.hashtocurve.Integer.Companion.integer
import nl.sanderdijkhuis.hashtocurve.Integer.Companion.two
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class ElementSpec {
    @Test
    fun squareRoot() {
        val field = Curve.p256.field
        repeat(100) {
            val integer = Integer(Random.nextBytes(40)).run { modulo(this, field.order) }
            val squared = integer { exponent(integer, two, field.order) }
            val element = Element(field, squared)
            val sqrt = element.squareRoot()
            val squared2 = integer { exponent(sqrt.integer, two, field.order) }
            assertEquals(squared, squared2)
        }
    }
}
