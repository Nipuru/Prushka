plugins {
    id("com.github.johnrengelman.shadow")
}

dependencies {
    compileOnly("net.afyer.afybroker:afybroker-server:2.3")
    implementation(project(":prushka-common"))
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