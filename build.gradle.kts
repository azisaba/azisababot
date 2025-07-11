plugins {
    kotlin("jvm") version libs.versions.kotlin.get()
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
}

group = "net.azisaba"
version = "0.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.adventure.api)
    implementation(libs.adventure.gson)
    implementation(libs.cronutils)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.hikari.cp)
    implementation(libs.kaml)
    implementation(libs.kord)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.kandy)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.logback.classic)
    implementation(libs.mariadb.jdbc)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}