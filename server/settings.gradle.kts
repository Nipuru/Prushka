rootProject.name = "prushka"

sequenceOf(
    "server-common",
    "server-database",
    "server-auth",
    "server-shared",
    "server-broker",
    "server-game",
    "server-log",
).forEach { include(it) }

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

