plugins {
    application
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":server-common"))
    implementation(libs.afybroker.client)
    implementation(libs.hikari)
    implementation(libs.snakeyaml)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.logging)
    implementation(libs.ktor.server.headers)
    implementation(libs.ktor.server.content)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.serialization.gson)
    implementation(libs.slf4j.api)
    implementation(libs.guava)


    // pgsql驱动
    runtimeOnly(libs.postgresql)
    // logback
    runtimeOnly(libs.logback.classic)
}

val mainClazz = "server.auth.AuthServerKt"

application {
    mainClass.set(mainClazz)
}

tasks.shadowJar {
    archiveFileName.set("AuthServer.jar")
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