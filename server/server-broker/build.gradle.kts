plugins {
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":server-common"))
    compileOnly(libs.afybroker.server)
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
    archiveFileName.set("BrokerPlugin.jar")
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version
    )
    inputs.properties(props)
    filesMatching("broker.yml") {
        expand(props)
    }
}