package nl.sanderdijkhuis.hashtocurve

import java.security.MessageDigest

sealed interface HashFunction {
    val output: Size
    val block: Size
    operator fun invoke(input: ByteArray): ByteArray

    data object SHA256 : HashFunction {
        override val output = Size(32)
        override val block = Size(64)

        override operator fun invoke(input: ByteArray) = with (MessageDigest.getInstance("SHA-256")) {
            update(input)
            digest()!!
        }
    }
}
