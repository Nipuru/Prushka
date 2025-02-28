package top.nipuru.prushka.common.config

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigReader(
    val bindKey: String,
)
