plugins {
    `java-library`
    alias(libs.plugins.shadow)
    alias(libs.plugins.paperweight)
}

dependencies {
    implementation(project(":server-common"))
    paperweight.paperDevBundle(libs.versions.paper)
    compileOnly(libs.afybroker.client)
}

tasks.assemble {
    dependsOn(tasks.reobfJar)
}

tasks.reobfJar {
    outputJar = File(layout.buildDirectory.get().asFile, "/libs/BukkitPlugin.jar")
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