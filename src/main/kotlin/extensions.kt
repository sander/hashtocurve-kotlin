package nl.sanderdijkhuis.hashtocurve

fun String.toTag() = DomainSeparationTag(this)

fun ByteArray.toInteger() = Integer(this)
