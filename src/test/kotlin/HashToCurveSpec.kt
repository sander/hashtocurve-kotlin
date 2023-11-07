package nl.sanderdijkhuis.hashtocurve

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@OptIn(ExperimentalStdlibApi::class)
class HashToCurveSpec {
    @Test
    fun demo() {
        val result = HashToCurve.P256("DEMO-V01")("Hello, World!".toByteArray())
        assertEquals("aa3e89cb54d29100e04f3395bd7bbe78e0548fe5173e0dfecdfff7efeddccccf", result.x.toHexString())
        assertEquals("4c1defc59f6ac0efaaa80284f7d36bb82041c03d5b1b5e073f4081d77c1e3e8f", result.y.toHexString())
    }

    @Test
    fun `test vector`() {
        val hash = HashToCurve.P256("QUUX-V01-CS02-with-P256_XMD:SHA-256_SSWU_RO_")
        val message = ByteArray(0)
        val element = hash.hashToField(message, 2).map { it.bytes }
        assertContentEquals(
            ("ad5342c66a6dd0ff080df1da0ea1c04b96e0330dd89406465eeba1" + "1582515009").hexToByteArray(),
            element[0]
        )
        assertContentEquals(
            ("8c0f1d43204bd6f6ea70ae8013070a1518b43873bcd850aafa0a9e" + "220e2eea5a").hexToByteArray(),
            element[1]
        )
        val mapped = hash.hashToField(message, 2).map { hash.mapToCurve(it) }.map { it.point }
        assertContentEquals(
            ("ab640a12220d3ff283510ff3f4b1953d09fad35795140b1c5d64f3" + "13967934d5").hexToByteArray(),
            mapped[0].x
        )
        assertContentEquals(
            ("dccb558863804a881d4fff3455716c836cef230e5209594ddd33d8" + "5c565b19b1").hexToByteArray(),
            mapped[0].y
        )
        assertContentEquals(
            ("51cce63c50d972a6e51c61334f0f4875c9ac1cd2d3238412f84e31" + "da7d980ef5").hexToByteArray(),
            mapped[1].x
        )
        assertContentEquals(
            ("b45d1a36d00ad90e5ec7840a60a4de411917fbe7c82c3949a6e699" + "e5a1b66aac").hexToByteArray(),
            mapped[1].y
        )
        val hashed = hash(message)
        assertContentEquals(
            ("2c15230b26dbc6fc9a37051158c95b79656e17a1a920b11394ca91" + "c44247d3e4").hexToByteArray(),
            hashed.x
        )
        assertContentEquals(
            ("8a7a74985cc5c776cdfe4b1f19884970453912e9d31528c060be9a" + "b5c43e8415").hexToByteArray(),
            hashed.y
        )
    }
}
