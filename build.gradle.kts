plugins {
    java
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.paperweight) apply false
}

subprojects {

    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    group = "top.nipuru.prushka"
    version = "0.1"

    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://repo.tabooproject.org/repository/releases/")
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    }

    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }

    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

tasks.withType<Jar> {
    enabled = false
}





