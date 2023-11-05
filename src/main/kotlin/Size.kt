package nl.sanderdijkhuis.hashtocurve

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
