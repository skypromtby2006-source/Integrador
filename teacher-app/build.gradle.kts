import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")                   version "1.9.23"
    id("org.jetbrains.compose")     version "1.6.2"
    kotlin("plugin.serialization")  version "1.9.23"
}

group   = "com.didactai"
version = "1.0.0"

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    // ── UI ──────────────────────────────────────────────
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.0")

    // ── Base de datos ────────────────────────────────────
    implementation("org.jetbrains.exposed:exposed-core:0.44.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.44.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.44.1")
    implementation("org.jetbrains.exposed:exposed-java-time:0.44.1")
    implementation("org.postgresql:postgresql:42.7.1")

    // ── Servidor HTTP embebido ───────────────────────────
    implementation("io.ktor:ktor-server-core:2.3.8")
    implementation("io.ktor:ktor-server-netty:2.3.8")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.8")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.8")

    // ── JSON ─────────────────────────────────────────────
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    // ── Hashing de contraseñas (MVP: SHA-256) ────────────
    implementation("commons-codec:commons-codec:1.16.1")

    // ── Exportación PDF ───────────────────────────────────
    implementation("com.itextpdf:itext7-core:7.2.5")

    // ── Email SMTP ────────────────────────────────────────
    implementation("com.sun.mail:jakarta.mail:2.0.1")

    // ── Importación Excel ─────────────────────────────────
    implementation("org.apache.poi:poi-ooxml:5.2.5")

    // ── TOTP 2FA ──────────────────────────────────────────
    implementation("dev.turingcomplete:kotlin-onetimepassword:2.4.0")
    // ── QR Code PNG ───────────────────────────────────────
    implementation("io.github.g0dkar:qrcode-kotlin:4.1.1")
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Deb, TargetFormat.Rpm)
            packageName    = "didactai-teacher"
            packageVersion = "1.0.0"
            description    = "Panel del Maestro · Didactai"
        }
    }
}
