package nl.sanderdijkhuis.hashtocurve

import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.math.ec.ECPoint
import java.math.BigInteger

data class Point(val curve: Curve, val x: Element, val y: Element) {
    private val spec = ECNamedCurveTable.getParameterSpec("secp256r1")

    init {
        if (curve != Curve.p256) TODO()
        require(curve.field == x.field)
        require(curve.field == y.field)
    }

    operator fun plus(other: Point) = toECPoint().add(other.toECPoint()).normalize().toPoint(curve)
    operator fun times(scalar: Element): Point {
        require(x.field == scalar.field)
        return toECPoint().multiply(scalar.integer.toBigInteger()).toPoint(curve)
    }

    private fun toECPoint() = spec.curve.createPoint(x.integer.toBigInteger(), y.integer.toBigInteger())!!
    private fun ECPoint.toPoint(curve: Curve) = normalize().run {
        Point(
            curve,
            Element(curve.field, affineXCoord.toBigInteger().toInteger()),
            Element(curve.field, affineYCoord.toBigInteger().toInteger())
        )
    }

    private fun Integer.toBigInteger() = BigInteger(1, bytes(Size(32)))
    private fun BigInteger.toInteger(): Integer {
        require(signum() != -1)
        return Integer(toByteArray())
    }
}
