rootProject.name = "minegame"

sequenceOf(
    "minegame-common",
    "minegame-database",
    "minegame-auth",
    "minegame-shared",
    "minegame-broker",
    "minegame-game"
).forEach { include(it) }

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

