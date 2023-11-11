# Hashing to Elliptic Curves for Kotlin

[![](https://jitpack.io/v/nl.sanderdijkhuis/hashtocurve-kotlin.svg)](https://jitpack.io/#nl.sanderdijkhuis/hashtocurve-kotlin)

**Hashing to Elliptic Curves for Kotlin** implements [RFC 9380](https://www.rfc-editor.org/rfc/rfc9380.html) algorithms for encoding or hashing an arbitrary byte array to a point on an elliptic curve.

> [!WARNING]
> This project has not been independently audited. Help is welcome to increase security or to make risks explicit. Also see the [Security policy](https://github.com/sander/hashtocurve-kotlin/security/policy).

## How to build

Run the tests. On POSIX:

    ./gradlew test

On Windows:

    gradlew test

Instead of `test`, use `jar` to create a JAR in `build/libs`.

## How to use in an application

Add the [`hashtocurve-kotlin` dependency](https://jitpack.io/#nl.sanderdijkhuis/hashtocurve-kotlin) to your project.

Select a `HashToCurve` suite and configure a domain separation tag (see [Domain Separation Requirements](https://www.rfc-editor.org/rfc/rfc9380.html#name-domain-separation-requireme)). With the resulting function, you can hash any byte array to the related elliptic curve. The result is encoded as a `Point(x: ByteArray, y: ByteArray)` with components that contain the big-endian encoding of affine curve point coordinates.

See [HashToCurveSpec](src/test/kotlin/HashToCurveSpec.kt) for example usage.

## Test vectors

Some test vectors from [RFC 9380](https://www.rfc-editor.org/rfc/rfc9380.html) are used.

## Related resources

- [License](LICENSE.md)
- [Security policy](SECURITY.md)
