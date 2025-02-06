package top.nipuru.prushka.broker

import net.afyer.afybroker.server.plugin.Plugin
import top.nipuru.prushka.broker.listener.PlayerListener
import top.nipuru.prushka.broker.processor.*

class BrokerPlugin : Plugin() {
    override fun onEnable() {
        server.pluginManager.registerListener(this, PlayerListener())
        server.registerUserProcessor(RequestMessageRouter())
        server.registerUserProcessor(PlayerDataTransferBrokerProcessor())
        server.registerUserProcessor(PlayerOfflineDataBrokerProcessor())
        server.registerUserProcessor(PlayerChatBrokerProcessor())
        server.registerUserProcessor(PlayerPrivateChatBrokerProcessor())
        server.registerUserProcessor(GetTimeBrokerProcessor())
        server.registerUserProcessor(DebugTimeBrokerProcessor())
        server.registerUserProcessor(TeleportInvokeBrokerProcessor())
    }
}
