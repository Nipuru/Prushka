package top.nipuru.minegame.broker

import net.afyer.afybroker.server.plugin.Plugin
import top.nipuru.minegame.broker.listener.PlayerListener
import top.nipuru.minegame.broker.processor.*

class BrokerPlugin : Plugin() {
    override fun onEnable() {
        server.pluginManager.registerListener(this, PlayerListener())
        server.registerUserProcessor(RequestMessageRouter())
    }
}
