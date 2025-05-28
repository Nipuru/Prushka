rootProject.name = "prushka"

sequenceOf(
    "prushka-common",
    "prushka-database",
    "prushka-auth",
    "prushka-shared",
    "prushka-broker",
    "prushka-game",
    "prushka-log",
).forEach { include(it) }

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

