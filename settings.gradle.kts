pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
    }

    plugins {
        kotlin("multiplatform").version(extra["kotlin_version"] as String)
        id("org.jetbrains.compose").version(extra["compose_version"] as String)
        kotlin("plugin.serialization").version(extra["kotlin_version"] as String)
        id("gradle-plugin").version(extra["kotlin_version"] as String)
        id("serialization").version(extra["kotlin_version"] as String)
    }
}
rootProject.name = "lang-card"
