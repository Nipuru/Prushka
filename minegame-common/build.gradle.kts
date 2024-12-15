plugins {
    `java-library`
}

dependencies {
    api(kotlin("stdlib"))
    compileOnly("com.google.code.gson:gson:2.8.9")
    compileOnly("net.afyer.afybroker:afybroker-core:2.3")
}