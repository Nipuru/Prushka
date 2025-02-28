plugins {
    `java-library`
}

dependencies {
    api(kotlin("stdlib"))
    compileOnly(libs.gson)
    compileOnly(libs.afybroker.core)
}
