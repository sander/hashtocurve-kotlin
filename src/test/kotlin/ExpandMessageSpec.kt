package nl.sanderdijkhuis.hashtocurve

import kotlin.test.Test
import kotlin.test.assertContentEquals

@OptIn(ExperimentalStdlibApi::class)
class ExpandMessageSpec {
    @Test
    fun `test vectors for expand_message_xmd(SHA-256)`() {
        val expand = ExpandMessage.Xmd(HashFunction.SHA256)
        val tag = DomainSeparationTag("QUUX-V01-CS02-with-expander")
        val message = ByteArray(0)
        val size = Size(0x20)
        val result = expand(message, tag, size)
        assertContentEquals(
            ("f659819a6473c1835b25ea59e3d38914c98b374f0970b7e4" +
                    "c92181df928fca88").hexToByteArray(), result
        )
    }
}
