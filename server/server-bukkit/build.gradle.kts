plugins {
    `java-library`
    alias(libs.plugins.shadow)
    alias(libs.plugins.paperweight)
}

dependencies {
    implementation(project(":server-common"))
    paperweight.paperDevBundle(libs.versions.paper)
    compileOnly(libs.afybroker.client)

    // Kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
    archiveFileName.set("BukkitPlugin.jar")
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