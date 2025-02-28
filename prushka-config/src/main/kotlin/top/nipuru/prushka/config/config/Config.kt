package top.nipuru.prushka.config.config

import org.yaml.snakeyaml.Yaml
import top.nipuru.prushka.common.util.ResourceUtil
import java.io.InputStreamReader


/**
 * @author Nipuru
 * @since 2024/11/08 12:38
 */

class Config {
    var broker: Broker? = null
    var configServer: ConfigServer? = null
}

class Broker {
    var host: String? = null
    var port: Int? = null
}

class ConfigServer {
    var basePath: String? = null
}

fun loadConfig(): Config {
    ResourceUtil.getResourceOrExtract("config.yml").use { inputStream ->
        InputStreamReader(inputStream).use { reader ->
            val yaml = Yaml()
            return yaml.loadAs(reader, Config::class.java)
        }
    }
}
