plugins {
    id("dev.kikugie.stonecutter")
    id("io.papermc.paperweight.userdev") version "1.7.5" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
    id("xyz.jpenilla.run-paper") version "2.3.1" apply false
}

stonecutter active "1.21.1"

stonecutter parameters {
    val version = node.metadata.version

    constants["use_adventure"] = eval(version, ">=1.19")
    constants["modern_commands"] = eval(version, ">=1.20.6")
    constants["modern_chat_event"] = eval(version, ">=1.19")
}