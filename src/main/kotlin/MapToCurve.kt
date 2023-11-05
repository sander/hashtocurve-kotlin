package nl.sanderdijkhuis.hashtocurve

import nl.sanderdijkhuis.hashtocurve.Integer.Companion.four
import nl.sanderdijkhuis.hashtocurve.Integer.Companion.integer
import nl.sanderdijkhuis.hashtocurve.Integer.Companion.one
import nl.sanderdijkhuis.hashtocurve.Integer.Companion.three
import nl.sanderdijkhuis.hashtocurve.Integer.Companion.two
import nl.sanderdijkhuis.hashtocurve.Integer.Companion.zero

interface MapToCurve {
    operator fun invoke(element: Element): Point

    class SimpleSwu(private val curve: Curve.Weierstrass, private val Z: Element) : MapToCurve {
        init {
            require(Z.field == curve.field)
        }

        private val constant =
            integer { subtract(curve.field.order, Integer.three) }.run { div(this, Integer.four) }

        override operator fun invoke(element: Element): Point {
            require(element.field == curve.field)
            val field = curve.field
            val tv1 = integer { exponent(element.integer, two, field.order) }
                .run { multiply(Z.integer, this) }
            val tv2 = integer { exponent(tv1, two, field.order) }.run { add(this, tv1) }
            val tv3 = integer { add(tv2, one) }.run { multiply(curve.b.integer, this) }
            val tv4 = integer { compareMove(Z.integer, integer { negate(tv2) }, tv2 != zero) }
                .run { multiply(curve.a.integer, this) }
            tv2.exponent(tv3, two, field.order)
            val tv6 = integer { exponent(tv4, two, field.order) }
            val tv5 = integer { multiply(curve.a.integer, tv6) }
            tv2.add(tv2, tv5).run { multiply(tv2, tv3, field.order) }
            tv6.multiply(tv6, tv4)
            tv5.multiply(curve.b.integer, tv6)
            tv2.add(tv2, tv5)
            val x = integer { multiply(tv1, tv3) }
            val (isGx1Square, y1) = squareRootRatio(field.element(tv2), field.element(tv6))
            val y = integer { multiply(tv1, element.integer, field.order) }.run { multiply(this, y1.integer) }
            x.compareMove(x, tv3, isGx1Square)
            y.compareMove(y, y1.integer, isGx1Square)
            val e1 = sign(element.integer) == sign(y)
            y.compareMove(integer { negate(y) }, y, e1)
            x.multiply(x, integer { moduloInverse(tv4, field.order) })
            return Point(curve, Element(field, x), Element(field, y))
        }

        private fun sign(x: Integer) =
            if (curve.field.extensionDegree == 1) integer { modulo(x, two) }
            else TODO()

        private fun squareRootRatio(u: Element, v: Element) =
            if (integer { modulo(curve.field.order, four) } == three) squareRootRatio3mod4(u, v)
            else TODO()

        private fun squareRootRatio3mod4(u: Element, v: Element): Pair<Boolean, Element> {
            require(u.field == curve.field)
            require(v.field == curve.field)
            val field = curve.field
            val c2 = Element(curve.field, integer { negate(Z.integer) }).squareRoot().integer
            val tv1 = integer { exponent(v.integer, two, field.order) }
            val tv2 = integer { multiply(u.integer, v.integer, field.order) }
            tv1.multiply(tv1, tv2)
            val y1 = integer { exponent(tv1, constant, field.order) }.run { multiply(this, tv2, field.order) }
            val y2 = integer { multiply(y1, c2, field.order) }
            val tv3 = integer { exponent(y1, two, field.order) }
                .run { multiply(this, v.integer, field.order) }
            val isQR = tv3 == u.integer
            val y = integer { compareMove(y2, y1, isQR) }
            return isQR to Element(field, y)
        }
    }
}
