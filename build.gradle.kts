plugins {
    kotlin("jvm") version "2.0.21"
}

group = "net.azisaba"
version = "0.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.hikari.cp)
    implementation(libs.mariadb.jdbc)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}