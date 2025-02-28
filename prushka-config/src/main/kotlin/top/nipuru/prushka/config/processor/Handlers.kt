package top.nipuru.prushka.config.processor

import top.nipuru.prushka.common.message.config.ConfigMessage
import top.nipuru.prushka.common.message.config.ConfigMessageType
import top.nipuru.prushka.common.processor.RequestDispatcher
import top.nipuru.prushka.config.reader.FileReader

class ConfigMessageTypeHandler : RequestDispatcher.Handler<ConfigMessageType> {

    override fun handle(asyncCtx: RequestDispatcher.ResponseContext, request: ConfigMessageType) {
        asyncCtx.sendResponse(FileReader.readFile(request.name, request.configType))
    }

    override fun interest(): Class<ConfigMessageType> {
        return ConfigMessageType::class.java
    }
}

class ConfigMessageHandler : RequestDispatcher.Handler<ConfigMessage> {

    override fun handle(asyncCtx: RequestDispatcher.ResponseContext, request: ConfigMessage) {
        asyncCtx.sendResponse(FileReader.readFile(request.configType))
    }

    override fun interest(): Class<ConfigMessage> {
        return ConfigMessage::class.java
    }
}
