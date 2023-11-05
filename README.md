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

Add the JAR as a dependency to your project.

Then select a `Suite` and configure a `DomainSeparationTag` (see [Domain Separation Requirements](https://www.rfc-editor.org/rfc/rfc9380.html#name-domain-separation-requireme)). With the resulting function, you can hash any byte array to the related elliptic curve.

## Design decisions

- Abstract over big integer implementations, enabling porting to [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html).
- Use the Go [math/big](https://pkg.go.dev/math/big) style of handling big integers, enabling memory control in some implementations.
    - Consequence: this may be counterintuitive to JVM users.

## Test vectors

Some test vectors from [RFC 9380](https://www.rfc-editor.org/rfc/rfc9380.html) are used.

## Related resources

- [License](LICENSE.md)
- [Security policy](SECURITY.md)
