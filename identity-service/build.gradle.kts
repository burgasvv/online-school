
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(ktorLibs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
}

group = "org.burgas"
version = "1.0.0-SNAPSHOT"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(25)
}
dependencies {
    implementation(ktorLibs.serialization.kotlinx.json)
    implementation(ktorLibs.server.config.yaml)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.netty)
    implementation(libs.logback.classic)
    implementation("io.ktor:ktor-server-status-pages:3.5.0")
    implementation("io.ktor:ktor-server-sessions:3.5.0")
    implementation("io.ktor:ktor-server-cors:3.5.0")
    implementation("io.ktor:ktor-server-csrf:3.5.0")

    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)
}