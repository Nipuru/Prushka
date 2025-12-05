plugins {
    `java-library`
    alias(libs.plugins.shadow)
    alias(libs.plugins.paperweight)
}

dependencies {
    compileOnly(libs.afybroker.client)
    paperweight.paperDevBundle(libs.versions.paper)
    implementation(project(":server-common"))
    implementation(libs.kotlinx.coroutines.core)
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