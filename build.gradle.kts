plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2" apply false
    id("org.jetbrains.kotlin.jvm") version "1.8.22" apply false
    id("io.papermc.paperweight.userdev") version "1.7.2" apply false
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

    dependencies {
        testImplementation ("org.junit.jupiter:junit-jupiter-api:5.8.2")
        testRuntimeOnly ("org.junit.jupiter:junit-jupiter-engine:5.8.2")
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





