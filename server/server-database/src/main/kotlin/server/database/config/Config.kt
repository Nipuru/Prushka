package server.database.config

import org.yaml.snakeyaml.Yaml
import server.common.util.ResourceUtil
import java.io.InputStreamReader


/**
 * @author Nipuru
 * @since 2024/11/08 12:38
 */
class Broker {
    var host: String = "localhost"
    var port: Int = 11200
}

class DataSource {
    var host: String = "localhost"
    var port: Int = 5432
    var database: String = "prushka"
    var username: String = "postgres"
    var password: String = "123456"
}

class Config {
    var broker: Broker = Broker()
    var datasource: DataSource = DataSource()
    var dbId: Int = 1

    companion object {
        fun load(): Config {
            ResourceUtil.getResourceOrExtract("config.yml").use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    val yaml = Yaml()
                    return yaml.loadAs(reader, Config::class.java)
                }
            }
        }
    }
}