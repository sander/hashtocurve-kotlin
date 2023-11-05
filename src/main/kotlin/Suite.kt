package nl.sanderdijkhuis.hashtocurve

import nl.sanderdijkhuis.hashtocurve.Integer.Companion.one

sealed interface Suite {
    val id: String
    val curve: Curve
    val hashToFieldSize: Size
    val clearCofactorParameter: Element

    val expandMessage: ExpandMessage
    val mapToCurve: MapToCurve

    val tag: DomainSeparationTag

    operator fun invoke(message: ByteArray): Point

    fun hashToField(message: ByteArray, count: Int): List<Element> {
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

    sealed interface HashToCurve : Suite {
        override fun invoke(message: ByteArray): Point =
            hashToField(message, 2).map { mapToCurve(it) }.reduceRight { a, b -> a + b }.clearCofactor()

        class P256(override val tag: DomainSeparationTag) : HashToCurve {
            override val id = "P256_XMD:SHA-256_SSWU_RO_"
            override val curve = Curve.p256
            override val hashToFieldSize get() = Size(48)
            override val clearCofactorParameter = Element(curve.field, one)
            override val expandMessage = ExpandMessage.Xmd(HashFunction.SHA256)
            override val mapToCurve = MapToCurve.SimpleSwu(curve, Element(curve.field, Integer(-10)))
        }

        private fun Point.clearCofactor(): Point = this * clearCofactorParameter
    }
}
