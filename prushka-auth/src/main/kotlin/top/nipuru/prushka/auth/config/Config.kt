package top.nipuru.prushka.auth.config

import org.yaml.snakeyaml.Yaml
import top.nipuru.prushka.common.util.ResourceUtil
import java.io.InputStreamReader


/**
 * @author Nipuru
 * @since 2024/11/08 11:27
 */
class Config {
    var broker: Broker? = null
    var datasource: DataSource? = null
}

class Broker {
    var host: String? = null
    var port: Int? = null
}

class DataSource {
    var host: String? = null
    var port: Int? = null
    var database: String? = null
    var username: String? = null
    var password: String? = null
    var pool: List<Int> = emptyList()
}

fun loadConfig(): Config {
    ResourceUtil.getResourceOrExtract("config.yml").use { inputStream ->
        InputStreamReader(inputStream).use { reader ->
            val yaml = Yaml()
            return yaml.loadAs(reader, Config::class.java)
        }
    }
}
