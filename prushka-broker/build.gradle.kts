plugins {
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":prushka-common"))
    compileOnly(libs.afybroker.server)
}

tasks.build {
    dependsOn(tasks.shadowJar)
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