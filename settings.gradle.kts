pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    // Multi-version orchestration: https://stonecutter.kikugie.dev/
    id("dev.kikugie.stonecutter") version "0.9.6"

    // Picks the right Loom variant per version (official-mapped 26.1+ vs remapped/Yarn-era older MC)
    id("dev.kikugie.loom-back-compat") version "0.3"

    // Lets Gradle auto-provision the JDK toolchain required by older Minecraft versions
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

stonecutter {
    create(rootProject) {
        versions("1.21.4", "1.21.11", "26.1.2")
        vcsVersion = "26.1.2"
    }
}

rootProject.name = "runal"
