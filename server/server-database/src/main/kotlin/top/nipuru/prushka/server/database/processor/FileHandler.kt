package top.nipuru.prushka.server.database.processor

import top.nipuru.prushka.server.common.message.database.LoadFileRequest
import top.nipuru.prushka.server.common.message.database.SaveFileRequest
import top.nipuru.prushka.server.common.processor.RequestDispatcher
import top.nipuru.prushka.server.database.service.FileService

class LoadFileHandler : RequestDispatcher.Handler<LoadFileRequest> {

    override fun handle(asyncCtx: RequestDispatcher.ResponseContext, request: LoadFileRequest) {
        val data: ByteArray = FileService.getFile(request.filename)
        asyncCtx.sendResponse(data)
    }

    override fun interest(): Class<LoadFileRequest> {
        return LoadFileRequest::class.java
    }
}

class SaveFileHandler : RequestDispatcher.Handler<SaveFileRequest> {

    override fun handle(asyncCtx: RequestDispatcher.ResponseContext, request: SaveFileRequest) {
        FileService.saveFile(request.filename, request.data)
        asyncCtx.sendResponse(true) // response
    }

    override fun interest(): Class<SaveFileRequest> {
        return SaveFileRequest::class.java
    }
}
