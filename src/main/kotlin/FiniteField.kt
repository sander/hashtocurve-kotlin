package nl.sanderdijkhuis.hashtocurve

import nl.sanderdijkhuis.hashtocurve.Integer.Companion.four
import nl.sanderdijkhuis.hashtocurve.Integer.Companion.integer
import nl.sanderdijkhuis.hashtocurve.Integer.Companion.one
import nl.sanderdijkhuis.hashtocurve.Integer.Companion.zero

@OptIn(ExperimentalStdlibApi::class)
data class FiniteField(val characteristic: Integer, val extensionDegree: Int) {
    init {
        require(!characteristic.lessThan(four))
    }

    val order = (1..extensionDegree).fold(one) { result, _ -> integer { multiply(result, characteristic) } }

    fun contains(integer: Integer): Boolean =
        extensionDegree == 1 && (integer == zero || zero lessThan integer) && integer lessThan order

    fun element(hex: String) = Element(this, hex.hexToByteArray().toInteger())
    fun element(integer: Integer) = Element(this, integer)

    companion object {
        operator fun invoke(characteristicHex: String, extensionDegree: Int) =
            FiniteField(characteristicHex.hexToByteArray().toInteger(), extensionDegree)
    }
}
