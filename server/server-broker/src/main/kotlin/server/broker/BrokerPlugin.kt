package server.broker

import net.afyer.afybroker.server.plugin.Plugin
import server.broker.listener.PlayerListener
import server.broker.processor.*

object BrokerPlugin : Plugin() {
    override fun onEnable() {
        server.pluginManager.registerListener(this, PlayerListener())
        server.registerUserProcessor(PlayerDataTransferBrokerProcessor())
        server.registerUserProcessor(PlayerOfflineDataBrokerProcessor())
        server.registerUserProcessor(PlayerChatBrokerProcessor())
        server.registerUserProcessor(PlayerPrivateChatBrokerProcessor())
        server.registerUserProcessor(GetTimeBrokerProcessor())
        server.registerUserProcessor(DebugTimeBrokerProcessor())
        server.registerUserProcessor(TeleportInvokeBrokerProcessor())
        server.registerUserProcessor(AudienceBrokerProcessor())
    }
}
