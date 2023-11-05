package nl.sanderdijkhuis.hashtocurve

interface Curve {
    val field: FiniteField

    data class Weierstrass(val a: Element, val b: Element) : Curve {
        init {
            require(a.field == b.field)
            require(a.integer != Integer.zero)
            require(b.integer != Integer.zero)
        }

        override val field get() = a.field
    }

    companion object {
        val p256 = FiniteField("ffffffff00000001000000000000000000000000ffffffffffffffffffffffff", 1).run {
            Weierstrass(
                element("ffffffff00000001000000000000000000000000fffffffffffffffffffffffc"),
                element("5ac635d8aa3a93e7b3ebbd55769886bc651d06b0cc53b0f63bce3c3e27d2604b")
            )
        }
    }
}
