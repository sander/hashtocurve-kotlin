plugins {
    kotlin("jvm") version "1.9.0"
}

group = "nl.sanderdijkhuis"
version = "0.1.0-SNAPSHOT"

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

kotlin {
    jvmToolchain(8)
}
