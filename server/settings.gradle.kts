rootProject.name = "prushka"

sequenceOf(
    "server-common",
    "server-database",
    "server-auth",
    "server-shared",
    "server-broker",
    "server-bukkit",
    "server-log",
).forEach { include(it) }
