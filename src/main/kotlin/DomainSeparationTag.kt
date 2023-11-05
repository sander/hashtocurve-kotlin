package nl.sanderdijkhuis.hashtocurve

@JvmInline
value class DomainSeparationTag private constructor(private val bytes: ByteArray) {
    private val size get() = Size(bytes.size)
    val sizeByte get() = size.bytes(Size.one)!!
    operator fun plus(bytes: ByteArray) = this.bytes + bytes

    companion object {
        private const val MAXIMUM_SIZE = 255
        operator fun invoke(bytes: ByteArray): DomainSeparationTag {
            require (bytes.size <= MAXIMUM_SIZE)
            return DomainSeparationTag(bytes)
        }
        operator fun invoke(string: String) = invoke(string.toByteArray())
    }
}
