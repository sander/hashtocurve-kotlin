# Hashing to Elliptic Curves for Kotlin

**Hashing to Elliptic Curves for Kotlin** implements [RFC 9380](https://www.rfc-editor.org/rfc/rfc9380.html) algorithms for encoding or hashing an arbitrary byte array to a point on an elliptic curve.

> [!WARNING]
> This project has not been independently audited. Help is welcome to increase security or to make risks explicit. Also see the [Security policy](https://github.com/sander/hashtocurve-kotlin/security/policy).

## How to build

Run the tests. On POSIX:

    ./gradlew test

On Windows:

    gradlew test

See [HashToCurveSpec](src/test/kotlin/HashToCurveSpec.kt) for example usage.

Instead of `test`, use `jar` to create a JAR in `build/libs`.

## How to use in an application

The easiest is to copy-paste [`hashtocurve.kt`](src/main/kotlin/hashtocurve.kt) into your project and maintain your fork there. Check back regularly if this source project has relevant changes for you, and publish changes you consider useful for others.

Alternatively, add the JAR that you have built as a dependency to your project.

Make the `implementation` dependencies from [`build.gradle.kts`](build.gradle.kts) available in your project.

After including the code and its dependencies in your project, select a `HashToCurve` suite and configure a domain separation tag (see [Domain Separation Requirements](https://www.rfc-editor.org/rfc/rfc9380.html#name-domain-separation-requireme)). With the resulting function, you can hash any byte array to the related elliptic curve. The result is encoded as a `Point(x: ByteArray, y: ByteArray)` with components that contain the big-endian encoding of affine curve point coordinates.

## Test vectors

Some test vectors from [RFC 9380](https://www.rfc-editor.org/rfc/rfc9380.html) are used.

## Related resources

- [License](LICENSE.md)
- [Security policy](SECURITY.md)
