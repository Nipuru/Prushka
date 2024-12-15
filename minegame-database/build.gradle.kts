plugins {
    application
    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation(project(":minegame-common"))
    implementation("net.afyer.afybroker:afybroker-client:2.3")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.yaml:snakeyaml:2.2")

    // pgsql驱动
    runtimeOnly("org.postgresql:postgresql:42.7.4")
    // logback
    runtimeOnly("ch.qos.logback:logback-classic:1.2.11")
}

val mainClazz = "top.nipuru.minegame.database.DatabaseServerKt"

application {
    mainClass.set(mainClazz)
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to mainClazz,
        )
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}