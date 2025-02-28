package top.nipuru.prushka.game.util

import net.afyer.afybroker.client.Broker
import top.nipuru.prushka.common.config.TestExcelConfig
import top.nipuru.prushka.common.message.config.ConfigMessage
import kotlin.reflect.KProperty

object ConfigGetter {

    inline operator fun <reified T> getValue(ref: Any?, property: KProperty<*>): List<T> {
        return Broker.invokeSync(ConfigMessage(T::class.java))
    }


}


object Test {

    val test: List<TestExcelConfig> by ConfigGetter

}
