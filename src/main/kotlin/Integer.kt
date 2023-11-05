package nl.sanderdijkhuis.hashtocurve

import java.math.BigInteger
import java.util.*

/** Go-inspired big.Int interface to enable memory control in some implementations */
class Integer private constructor(private var value: BigInteger) {
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
    fun multiply(x: Integer, y: Integer, m: Integer): Integer = apply { value = x.value.multiply(y.value).mod(m.value) }
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

        fun integer(f: Integer.() -> Integer): Integer = zero.run(f)

        val oneByte get() = one.bytes(Size.one)!!
        fun zeroBytes(size: Size) = zero.bytes(size)!!

        operator fun invoke(bytes: ByteArray) = Integer(BigInteger(1, bytes))
        operator fun invoke(value: Int) = Integer(BigInteger.valueOf(value.toLong()))
        operator fun invoke(value: UByte) = Integer(value.toInt())

        fun UByte.singleByte() = Integer(this).bytes(Size.one)!!
    }
}
