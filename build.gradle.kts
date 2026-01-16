import dev.kikugie.stonecutter.StonecutterAPI

plugins {
    `java-library`
    id("com.github.johnrengelman.shadow")
    id("xyz.jpenilla.run-paper")
}

group = "net.weever"
version = "1.1.4+${stonecutter.current.version}"

val targetJavaVersion = when {
    stonecutter.eval(stonecutter.current.version, ">=1.20.5") -> 21
    stonecutter.eval(stonecutter.current.version, ">=1.18") -> 17
    stonecutter.eval(stonecutter.current.version, ">=1.17") -> 16
    else -> 11
}

val paperGroup = if (stonecutter.eval(stonecutter.current.version, ">=1.17")) "io.papermc.paper" else "com.destroystokyo.paper"
val paperDependency = "$paperGroup:paper-api:${stonecutter.current.version}-R0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    implementation("org.telegram:telegrambots:6.9.7.1")

    compileOnly(paperDependency)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(targetJavaVersion)
    }

    shadowJar {
        archiveBaseName.set("TelegramSRV")
        archiveClassifier.set("")
        // relocate("org.telegram", "net.weever.telegramSRV.libs.telegram") 
    }

    runServer {
        minecraftVersion(stonecutter.current.version)
    }

    processResources {
        val props = mapOf(
            "version" to project.version,
            "api_version" to if (stonecutter.eval(stonecutter.current.version, ">=1.20")) "1.20" else "1.16"
        )
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }
}