package server.common.util

import org.yaml.snakeyaml.Yaml


/**
 * @author Nipuru
 * @since 2025/08/31 22:43
 */
fun Any?.toYaml(): String {
    return YamlHolder.yaml.dump( this)
}

inline fun <reified T> String.fromYaml(): T {
    return YamlHolder.yaml.loadAs(this, T::class.java)
}

@PublishedApi
internal object YamlHolder {
    val yaml = Yaml()
}