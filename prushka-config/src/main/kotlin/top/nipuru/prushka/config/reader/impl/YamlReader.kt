package top.nipuru.prushka.config.reader.impl

import org.yaml.snakeyaml.Yaml
import top.nipuru.prushka.config.reader.IReader
import java.io.File
import java.io.InputStreamReader

class YamlReader : IReader {

    override val extension: List<String> = listOf("yaml", "yml")

    override fun <T> readFile(file: File, targetType: Class<T>): List<T> {
        file.inputStream().use { inputStream ->
            InputStreamReader(inputStream).use { reader ->
                val yaml = Yaml()
                return listOf(yaml.loadAs(reader, targetType))
            }
        }
    }

}
