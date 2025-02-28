package top.nipuru.prushka.common.config

@ConfigReader("test_excel")
data class TestExcelConfig(
    val id: String,
    val name: String,
)
