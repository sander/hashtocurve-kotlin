package nl.sanderdijkhuis.hashtocurve

import nl.sanderdijkhuis.hashtocurve.Integer.Companion.four
import nl.sanderdijkhuis.hashtocurve.Integer.Companion.integer
import nl.sanderdijkhuis.hashtocurve.Integer.Companion.one
import nl.sanderdijkhuis.hashtocurve.Integer.Companion.three

class Element private constructor(val field: FiniteField, val integer: Integer) {
    fun squareRoot(): Element =
        if (integer { modulo(field.order, four) } == three) let {
            val c1 = integer { add(field.order, one) }.run { div(this, four) }
            return invoke(field, integer { exponent(integer, c1, field.order) })
        }
        else TODO()

    override fun toString() = integer.toString()

    companion object {
        operator fun invoke(field: FiniteField, integer: Integer): Element =
            if (field.contains(integer)) Element(field, integer)
            else Element(field, integer { modulo(integer, field.order) })
    }
}
