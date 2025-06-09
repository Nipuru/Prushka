package server.database.processor

import server.common.message.database.LoadFileRequest
import server.common.message.database.SaveFileRequest
import server.common.processor.RequestDispatcher

class LoadFileHandler : RequestDispatcher.Handler<LoadFileRequest> {

    override fun handle(asyncCtx: RequestDispatcher.ResponseContext, request: LoadFileRequest) {
        val data: ByteArray = server.database.service.FileService.getFile(request.filename)
        asyncCtx.sendResponse(data)
    }

    override fun interest(): Class<LoadFileRequest> {
        return LoadFileRequest::class.java
    }
}

class SaveFileHandler : RequestDispatcher.Handler<SaveFileRequest> {

    override fun handle(asyncCtx: RequestDispatcher.ResponseContext, request: SaveFileRequest) {
        server.database.service.FileService.saveFile(request.filename, request.data)
        asyncCtx.sendResponse(true) // response
    }

    override fun interest(): Class<SaveFileRequest> {
        return SaveFileRequest::class.java
    }
}
