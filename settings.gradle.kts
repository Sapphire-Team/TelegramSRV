pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.kikugie.dev/releases")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.7.10"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

stonecutter {
    create(rootProject) {
        versions("1.16.5", "1.17.1", "1.18.2", "1.19.4", "1.20.1", "1.21.1")
        vcsVersion = "1.21.1"
    }
}

rootProject.name = "TelegramSRV"