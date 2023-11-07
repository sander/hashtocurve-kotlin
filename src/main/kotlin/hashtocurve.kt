/**
 * Implementation of RFC 9380 algorithms for encoding or hashing an arbitrary
 * byte array to a point on an elliptic curve.
 *
 * http://sanderdijkhuis.nl/2023/hashtocurve-kotlin/
 *
 * MIT License
 *
 * Copyright (c) 2023 Sander Dijkhuis <mail@sanderdijkhuis.nl>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */
@file:Risk(
    "Since this project has not been independently audited, attackers may find and exploit weaknesses before you do",
    treatment = "Assess the risks before using this for real, and help out to increase security or make risks explicit"
)
@file:Risk(
    "More API surface is exposed than needed",
    treatment = "For now, encourage users to just rely on HashToCurve.P256(tag)(message) and ignore the rest"
)
@file:DesignDecision(
    "Keep all in a single file that provides only a limited external interface",
    rationale = "Enable inclusion in apps using copy-paste instead of more expensive library management",
    consequence = "Time saved on publishing a library can be spent on making copy-paste and contributing back easy"
)

package nl.sanderdijkhuis.hashtocurve

import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.math.ec.ECPoint
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*
import kotlin.experimental.xor
import kotlin.math.ceil

@DesignDecision(
    "Use byte arrays on the interface instead of concrete big integer or point types",
    rationale = "Enable porting to Kotlin Multiplatform",
    consequence = "Users need to parse big-endian integer values (OS2IP)"
)
class Point(val x: ByteArray, val y: ByteArray)

@Risk(
    "Some functions are hard-coded for the P-256 suite, potentially introducing errors in other implementations",
    treatment = "Accept and pay extra attention when implementing other suites"
)
sealed class Suite {
    operator fun invoke(message: ByteArray): Point = hash(message).bytes()

    internal fun hashToField(message: ByteArray, count: Int): List<Element> {
        require(count > 0)
        if (curve.field.extensionDegree != 1) TODO()
        val size = Size(count * curve.field.extensionDegree * hashToFieldSize.bytes)
        val uniformBytes = expandMessage(message, tag, size)
        return (0..<count).map { i ->
            val j = 0
            val elmOffset = hashToFieldSize.bytes * (j + i * curve.field.extensionDegree)
            val tv = uniformBytes.copyOfRange(elmOffset, elmOffset + hashToFieldSize.bytes)
            val eJ = Integer(tv).run { modulo(this, curve.field.characteristic) }
            Element(curve.field, eJ)
        }
    }

    internal abstract val mapToCurve: MapToCurve

    protected abstract val id: String
    protected abstract val curve: Curve
    protected abstract val hashToFieldSize: Size
    protected abstract val clearCofactorParameter: Int /* TODO */

    protected abstract val expandMessage: ExpandMessage

    protected abstract val tag: DomainSeparationTag

    internal abstract fun hash(message: ByteArray): CurvePoint
}

sealed class HashToCurve : Suite() {
    override fun hash(message: ByteArray): CurvePoint =
        hashToField(message, 2).map { mapToCurve(it) }.reduceRight { a, b -> a + b }.clearCofactor()

    class P256 private constructor(override val tag: DomainSeparationTag) : HashToCurve() {
        override val id = "P256_XMD:SHA-256_SSWU_RO_"
        override val curve = Curve.p256
        override val hashToFieldSize get() = Size(48)
        override val clearCofactorParameter = 1
        override val expandMessage = ExpandMessage.Xmd(HashFunction.SHA256)
        override val mapToCurve = MapToCurve.SimpleSwu(curve, Element(curve.field, Integer(-10)))

        constructor(tag: String) : this(DomainSeparationTag(tag))
    }

    private fun CurvePoint.clearCofactor(): CurvePoint = this * Element(curve.field, Integer(clearCofactorParameter))
}

sealed class Curve {
    internal abstract val field: FiniteField

    class Weierstrass internal constructor(internal val a: Element, internal val b: Element) : Curve() {
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

internal class CurvePoint(val curve: Curve, private val x: Element, private val y: Element) {
    private val spec = ECNamedCurveTable.getParameterSpec("secp256r1")

    init {
        if (curve != Curve.p256) TODO()
        require(curve.field == x.field)
        require(curve.field == y.field)
    }

    val point get() = Point(x.bytes, y.bytes)

    operator fun plus(other: CurvePoint) = toECPoint().add(other.toECPoint()).normalize().toPoint(curve)
    operator fun times(scalar: Element): CurvePoint {
        require(x.field == scalar.field)
        return toECPoint().multiply(scalar.integer.toBigInteger()).toPoint(curve)
    }

    fun bytes() = Point(x.integer.bytes(Size(32))!!, y.integer.bytes(Size(32))!!)

    private fun toECPoint() = spec.curve.createPoint(x.integer.toBigInteger(), y.integer.toBigInteger())!!
    private fun ECPoint.toPoint(curve: Curve) = normalize().run {
        CurvePoint(
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

@JvmInline
value class DomainSeparationTag private constructor(private val bytes: ByteArray) {
    private val size get() = Size(bytes.size)
    val sizeByte get() = size.bytes(Size.one)!!
    operator fun plus(bytes: ByteArray) = this.bytes + bytes

    companion object {
        private const val MAXIMUM_SIZE = 255
        operator fun invoke(bytes: ByteArray): DomainSeparationTag {
            require(bytes.size <= MAXIMUM_SIZE)
            return DomainSeparationTag(bytes)
        }

        operator fun invoke(string: String) = invoke(string.toByteArray())
    }
}

internal class Element private constructor(val field: FiniteField, val integer: Integer) {
    val bytes get() = integer.bytes(Size(32))!! /* TODO */

    fun squareRoot(): Element =
        if (Integer { modulo(field.order, Integer.four) } == Integer.three) let {
            val c1 = Integer { add(field.order, Integer.one) }.run { div(this, Integer.four) }
            return invoke(field, Integer { exponent(integer, c1, field.order) })
        }
        else TODO()

    override fun toString() = integer.toString()

    companion object {
        operator fun invoke(field: FiniteField, integer: Integer): Element =
            if (field.contains(integer)) Element(field, integer)
            else Element(field, Integer { modulo(integer, field.order) })
    }
}

sealed interface ExpandMessage {
    operator fun invoke(message: ByteArray, tag: DomainSeparationTag, size: Size): ByteArray

    data class Xmd(val hash: HashFunction) : ExpandMessage {
        override operator fun invoke(message: ByteArray, tag: DomainSeparationTag, size: Size): ByteArray {
            val iterations = ceil(size / hash.output).toInt().let {
                require(it <= 255)
                it.toUByte()
            }
            val suffixedTag = tag + tag.sizeByte
            val input =
                Integer.zeroBytes(hash.block) + message + size.twoBytes() + Integer.zeroBytes(Size.one) + suffixedTag
            val uniformBytes = hash(input).let { first ->
                (2.toUByte()..iterations).fold(listOf(hash(first + Integer.oneByte + suffixedTag))) { list, i ->
                    list + hash((first xor list.last()) + Integer(i.toInt()).bytes(Size.one)!! + suffixedTag)
                }
            }.reduce { b1, b2 -> b1 + b2 }
            return uniformBytes.copyOfRange(0, size.bytes)
        }

        private infix fun ByteArray.xor(other: ByteArray) = zip(other).map { (a, b) -> a xor b }.toByteArray()
    }
}

@OptIn(ExperimentalStdlibApi::class)
internal data class FiniteField(val characteristic: Integer, val extensionDegree: Int) {
    init {
        require(!characteristic.lessThan(Integer.four))
    }

    val order = (1..extensionDegree).fold(Integer.one) { result, _ -> Integer { multiply(result, characteristic) } }

    fun contains(integer: Integer): Boolean =
        extensionDegree == 1 && (integer == Integer.zero || Integer.zero lessThan integer) && integer lessThan order

    fun element(hex: String) = Element(this, Integer(hex.hexToByteArray()))
    fun element(integer: Integer) = Element(this, integer)

    companion object {
        operator fun invoke(characteristicHex: String, extensionDegree: Int) =
            FiniteField(Integer(characteristicHex.hexToByteArray()), extensionDegree)
    }
}

sealed interface HashFunction {
    val output: Size
    val block: Size
    operator fun invoke(input: ByteArray): ByteArray

    data object SHA256 : HashFunction {
        override val output = Size(32)
        override val block = Size(64)

        override operator fun invoke(input: ByteArray) = with(MessageDigest.getInstance("SHA-256")) {
            update(input)
            digest()!!
        }
    }
}

@DesignDecision(
    "Use the Go math/big style of handling big integers",
    rationale = "Enable memory control in some implementations",
    consequence = "This may be counterintuitive to JVM users"
)
internal class Integer private constructor(private var value: BigInteger) {
    fun bytes(to: Size): ByteArray? = if (value.signum() == -1) null else value.toByteArray().let {
        if (it[0] == 0x00.toByte()) Arrays.copyOfRange(it, 1, it.size) else it
    }.run {
        when (size) {
            to.bytes -> this
            in 0..<to.bytes -> ByteArray(to.bytes - size) + this
            else -> null
        }
    }

    override fun toString(): String = value.toString()
    override fun equals(other: Any?) = other is Integer && value == other.value
    override fun hashCode() = value.hashCode()

    fun add(x: Integer, y: Integer): Integer = apply { value = x.value.add(y.value) }
    fun subtract(x: Integer, y: Integer): Integer = apply { value = x.value.subtract(y.value) }
    fun multiply(x: Integer, y: Integer): Integer = apply { value = x.value.multiply(y.value) }
    fun multiply(x: Integer, y: Integer, m: Integer): Integer =
        apply { value = x.value.multiply(y.value).mod(m.value) }

    fun moduloInverse(x: Integer, m: Integer): Integer = apply { value = x.value.modInverse(m.value) }
    fun exponent(x: Integer, y: Integer, m: Integer): Integer = apply { value = x.value.modPow(y.value, m.value) }
    fun negate(x: Integer): Integer = apply { value = x.value.negate() }
    fun compareMove(a: Integer, b: Integer, c: Boolean): Integer = apply { value = if (!c) a.value else b.value }
    fun modulo(x: Integer, m: Integer): Integer = apply { value = x.value.mod(m.value) }
    fun div(x: Integer, y: Integer): Integer = apply { value = x.value.divide(y.value) }

    infix fun lessThan(other: Integer) = value.compareTo(other.value) == -1

    companion object {
        val zero: Integer get() = Integer(BigInteger.ZERO)
        val one: Integer get() = Integer(BigInteger.ONE)
        val two: Integer get() = Integer(2)
        val three: Integer get() = Integer(3)
        val four: Integer get() = Integer(4)

        operator fun invoke(f: Integer.() -> Integer): Integer = zero.run(f)

        val oneByte get() = one.bytes(Size.one)!!
        fun zeroBytes(size: Size) = zero.bytes(size)!!

        operator fun invoke(bytes: ByteArray) = Integer(BigInteger(1, bytes))
        operator fun invoke(value: Int) = Integer(BigInteger.valueOf(value.toLong()))
        operator fun invoke(value: UByte) = Integer(value.toInt())
    }
}

internal interface MapToCurve {
    operator fun invoke(element: Element): CurvePoint

    class SimpleSwu(private val curve: Curve.Weierstrass, private val z: Element) : MapToCurve {
        init {
            require(z.field == curve.field)
        }

        private val constant =
            Integer { subtract(curve.field.order, Integer.three) }.run { div(this, Integer.four) }

        override operator fun invoke(element: Element): CurvePoint {
            require(element.field == curve.field)
            val field = curve.field
            val tv1 = Integer { exponent(element.integer, Integer.two, field.order) }
                .run { multiply(z.integer, this) }
            val tv2 = Integer { exponent(tv1, Integer.two, field.order) }.run { add(this, tv1) }
            val tv3 = Integer { add(tv2, Integer.one) }.run { multiply(curve.b.integer, this) }
            val tv4 = Integer {
                compareMove(
                    z.integer,
                    Integer { negate(tv2) },
                    tv2 != Integer.zero
                )
            }
                .run { multiply(curve.a.integer, this) }
            tv2.exponent(tv3, Integer.two, field.order)
            val tv6 = Integer { exponent(tv4, Integer.two, field.order) }
            val tv5 = Integer { multiply(curve.a.integer, tv6) }
            tv2.add(tv2, tv5).run { multiply(tv2, tv3, field.order) }
            tv6.multiply(tv6, tv4)
            tv5.multiply(curve.b.integer, tv6)
            tv2.add(tv2, tv5)
            val x = Integer { multiply(tv1, tv3) }
            val (isGx1Square, y1) = squareRootRatio(field.element(tv2), field.element(tv6))
            val y = Integer { multiply(tv1, element.integer, field.order) }.run { multiply(this, y1.integer) }
            x.compareMove(x, tv3, isGx1Square)
            y.compareMove(y, y1.integer, isGx1Square)
            val e1 = sign(element.integer) == sign(y)
            y.compareMove(Integer { negate(y) }, y, e1)
            x.multiply(x, Integer { moduloInverse(tv4, field.order) })
            return CurvePoint(curve, Element(field, x), Element(field, y))
        }

        private fun sign(x: Integer) =
            if (curve.field.extensionDegree == 1) Integer { modulo(x, Integer.two) }
            else TODO()

        private fun squareRootRatio(u: Element, v: Element) =
            if (Integer { modulo(curve.field.order, Integer.four) } == Integer.three) squareRootRatio3mod4(u, v)
            else TODO()

        private fun squareRootRatio3mod4(u: Element, v: Element): Pair<Boolean, Element> {
            require(u.field == curve.field)
            require(v.field == curve.field)
            val field = curve.field
            val c2 = Element(curve.field, Integer { negate(z.integer) }).squareRoot().integer
            val tv1 = Integer { exponent(v.integer, Integer.two, field.order) }
            val tv2 = Integer { multiply(u.integer, v.integer, field.order) }
            tv1.multiply(tv1, tv2)
            val y1 = Integer { exponent(tv1, constant, field.order) }.run { multiply(this, tv2, field.order) }
            val y2 = Integer { multiply(y1, c2, field.order) }
            val tv3 = Integer { exponent(y1, Integer.two, field.order) }
                .run { multiply(this, v.integer, field.order) }
            val isQR = tv3 == u.integer
            val y = Integer { compareMove(y2, y1, isQR) }
            return isQR to Element(field, y)
        }
    }
}

@JvmInline
value class Size(val bytes: Int) {
    init {
        require(bytes in 0..maximumValue)
    }

    fun twoBytes() = Integer(bytes).bytes(two)!!
    fun bytes(size: Size) = Integer(bytes).bytes(size)
    operator fun div(other: Size) = bytes.toDouble() / other.bytes

    companion object {
        private val maximumValue = UShort.MAX_VALUE.toInt()
        val one = Size(1)
        val two = Size(2)
    }
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Repeatable
internal annotation class DesignDecision(val summary: String, val rationale: String, val consequence: String)

@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Repeatable
internal annotation class Risk(val scenario: String, val treatment: String)
