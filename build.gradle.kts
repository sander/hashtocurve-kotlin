plugins {
    kotlin("jvm") version "1.9.0"
    `maven-publish`
}

group = "nl.sanderdijkhuis"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.bouncycastle:bcprov-jdk18on:1.76")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

java {
    withJavadocJar()
    withSourcesJar()
}

kotlin {
    jvmToolchain(8)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "hashtocurve-kotlin"
            from(components["java"])
            pom {
                name.set("Hashing to Elliptic Curves for Kotlin")
                description.set("Implementation of RFC 9380 algorithms for encoding or hashing an arbitrary " +
                        "byte array to a point on an elliptic curve.")
                url.set("https://sanderdijkhuis.nl/2023/hashtocurve-kotlin/")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://sanderdijkhuis.nl/2023/hashtocurve-kotlin/blob/main/LICENSE.md")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/sander/hashtocurve-kotlin.git")
                    developerConnection.set("scm:git:ssh://github.com/sander/hashtocurve-kotlin.git")
                    url.set("https://github.com/sander/hashtocurve-kotlin")
                }
                developers {
                    developer {
                        id.set("sander")
                        name.set("Sander Dijkhuis")
                        email.set("mail@sanderdijkhuis.nl")
                    }
                }
            }
        }
    }
}
