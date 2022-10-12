import org.jetbrains.compose.desktop.application.dsl.TargetFormat

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    kotlin("plugin.serialization")
}

group = "net.oleg"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        withJava()
    }

    @Suppress("LocalVariableName")
    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
        }
        val jvmMain by getting {
            dependencies {
                val kotlin_version: String by project
                val ktor_version: String by project
                val coroutines_version: String by project
                val kodein_log_version: String by project
                val kotlinx_serialization_version: String by project

                implementation(compose.desktop.currentOs)

                implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")
                implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlin_version")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinx_serialization_version")

                implementation("io.ktor:ktor-client-core:$ktor_version")
                implementation("io.ktor:ktor-client-cio:$ktor_version")
                implementation("io.ktor:ktor-client-serialization:$ktor_version")
                implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
                implementation("io.ktor:ktor-client-logging:$ktor_version")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")

                implementation("org.kodein.log:kodein-log:$kodein_log_version")
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "net.oleg.app.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Dictionary to Anki"
            packageVersion = "1.0.0"
            description = "Small useful tool for adding new words to Anki application"
            copyright = "Copyright 2022 Oleg Okhotnikov"
            licenseFile.set(project.file("LICENSE"))
            macOS {
                iconFile.set(project.file("dictionary.icns"))
            }
        }
    }
}
