plugins {
    `java-library`
    alias(libs.plugins.shadow)
    alias(libs.plugins.paperweight)
}

dependencies {
    implementation(project(":prushka-common"))
    paperweight.paperDevBundle("1.20.4-R0.1-SNAPSHOT")
    paperweight.paperDevBundle(libs.versions.paper)
    compileOnly(libs.afybroker.client)
}

tasks.assemble {
    dependsOn(tasks.reobfJar)
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version
    )
    inputs.properties(props)
    filesMatching("plugin.yml") {
        expand(props)
    }
}