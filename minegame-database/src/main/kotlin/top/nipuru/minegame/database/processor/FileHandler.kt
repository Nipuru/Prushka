package top.nipuru.minegame.database.processor

import top.nipuru.minegame.common.message.database.LoadFileRequest
import top.nipuru.minegame.common.message.database.SaveFileRequest
import top.nipuru.minegame.common.processor.RequestDispatcher
import top.nipuru.minegame.database.file.FileManager

class LoadFileHandler : RequestDispatcher.Handler<LoadFileRequest> {

    override fun handle(asyncCtx: RequestDispatcher.ResponseContext, request: LoadFileRequest) {
        val data: ByteArray = FileManager.getFile(request.filename)
        asyncCtx.sendResponse(data)
    }

    override fun interest(): Class<LoadFileRequest> {
        return LoadFileRequest::class.java
    }
}

class SaveFileHandler : RequestDispatcher.Handler<SaveFileRequest> {

    override fun handle(asyncCtx: RequestDispatcher.ResponseContext, request: SaveFileRequest) {
        FileManager.saveFile(request.filename, request.data)
        asyncCtx.sendResponse(true) // response
    }

    override fun interest(): Class<SaveFileRequest> {
        return SaveFileRequest::class.java
    }
}
