package nl.sanderdijkhuis.hashtocurve

import nl.sanderdijkhuis.hashtocurve.Integer.Companion.singleByte
import nl.sanderdijkhuis.hashtocurve.Integer.Companion.zeroBytes
import kotlin.experimental.xor
import kotlin.math.ceil

sealed interface ExpandMessage {
    operator fun invoke(message: ByteArray, tag: DomainSeparationTag, size: Size): ByteArray

    data class Xmd(val hash: HashFunction) : ExpandMessage {
        override operator fun invoke(message: ByteArray, tag: DomainSeparationTag, size: Size): ByteArray {
            val iterations = ceil(size / hash.output).toInt().let {
                require(it <= 255)
                it.toUByte()
            }
            val suffixedTag = tag + tag.sizeByte
            val input = zeroBytes(hash.block) + message + size.twoBytes() + zeroBytes(Size.one) + suffixedTag
            val uniformBytes = hash(input).let { first ->
                (2.toUByte()..iterations).fold(listOf(hash(first + Integer.oneByte + suffixedTag))) { list, i ->
                    list + hash((first xor list.last()) + i.toUByte().singleByte() + suffixedTag)
                }
            }.reduce { b1, b2 -> b1 + b2 }
            return uniformBytes.copyOfRange(0, size.bytes)
        }

        private infix fun ByteArray.xor(other: ByteArray) = zip(other).map { (a, b) -> a xor b }.toByteArray()
    }
}
