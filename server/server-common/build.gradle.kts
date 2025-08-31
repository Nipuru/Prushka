plugins {
    `java-library`
}

dependencies {
    api(kotlin("stdlib"))
    compileOnly(libs.gson)
    compileOnly(libs.afybroker.core)

    // 存放部分公用代码 尽管部分模块不需要
    compileOnly(libs.exposed.core)
    compileOnly(libs.hikari)
}
