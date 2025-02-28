package top.nipuru.prushka.common.message.config

import java.io.Serializable

class ConfigMessageType(val name: String, val configType: Class<*>) : Serializable
class ConfigMessage(val configType: Class<*>) : Serializable
