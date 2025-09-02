package server.log.config

import server.common.util.fromYaml
import server.common.util.getResourceOrExtract


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
    var database: String = "prushka_log"
    var username: String = "postgres"
    var password: String = "123456"
}

class Config {
    var broker: Broker = Broker()
    var datasource: DataSource = DataSource()

    companion object {
        fun load(): Config {
            return javaClass.getResourceOrExtract("config.yml").bufferedReader().use { reader ->
                reader.readText()
            }.fromYaml<Config>()
        }
    }
}
