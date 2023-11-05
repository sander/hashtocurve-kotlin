package nl.sanderdijkhuis.hashtocurve

import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalStdlibApi::class)
class HashToCurveSpec {
    @Test
    fun `test vector`() {
        val toTag = "QUUX-V01-CS02-with-P256_XMD:SHA-256_SSWU_RO_".toTag()
        val hash = Suite.HashToCurve.P256(toTag)
        val message = ByteArray(0)
        val element = hash.hashToField(message, 2)
        assertEquals(
            ("ad5342c66a6dd0ff080df1da0ea1c04b96e0330dd89406465eeba1" + "1582515009").hexToByteArray().toInteger(),
            element[0].integer
        )
        assertEquals(
            ("8c0f1d43204bd6f6ea70ae8013070a1518b43873bcd850aafa0a9e" + "220e2eea5a").hexToByteArray().toInteger(),
            element[1].integer
        )
        val mapped = element.map { hash.mapToCurve(it) }
        assertEquals(
            ("ab640a12220d3ff283510ff3f4b1953d09fad35795140b1c5d64f3" + "13967934d5").hexToByteArray().toInteger(),
            mapped[0].x.integer
        )
        assertEquals(
            ("dccb558863804a881d4fff3455716c836cef230e5209594ddd33d8" + "5c565b19b1").hexToByteArray().toInteger(),
            mapped[0].y.integer
        )
        val Q1 = hash.mapToCurve(element[1])
        assertEquals(
            ("51cce63c50d972a6e51c61334f0f4875c9ac1cd2d3238412f84e31" + "da7d980ef5").hexToByteArray().toInteger(),
            mapped[1].x.integer
        )
        assertEquals(
            ("b45d1a36d00ad90e5ec7840a60a4de411917fbe7c82c3949a6e699" + "e5a1b66aac").hexToByteArray().toInteger(),
            mapped[1].y.integer
        )
        val hashed = hash(message)
        assertEquals(
            ("2c15230b26dbc6fc9a37051158c95b79656e17a1a920b11394ca91" + "c44247d3e4").hexToByteArray().toInteger(),
            hashed.x.integer
        )
        assertEquals(
            ("8a7a74985cc5c776cdfe4b1f19884970453912e9d31528c060be9a" + "b5c43e8415").hexToByteArray().toInteger(),
            hashed.y.integer
        )
    }
}
