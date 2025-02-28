plugins {
    application
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":prushka-common"))
    implementation(libs.afybroker.client)
    implementation(libs.fastexcel)
    implementation(libs.snakeyaml)
}

val mainClazz = "top.nipuru.prushka.config.ConfigServerKt"

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
