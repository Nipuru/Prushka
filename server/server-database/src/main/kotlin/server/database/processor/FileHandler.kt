package server.database.processor

import server.common.message.database.FileLoadRequest
import server.common.message.database.FileSaveRequest
import server.common.processor.RequestDispatcher
import server.database.service.FileService

class FileLoadHandler : RequestDispatcher.Handler<FileLoadRequest> {

    override fun handle(asyncCtx: RequestDispatcher.ResponseContext, request: FileLoadRequest) {
        val data: ByteArray = FileService.getFile(request.filename)
        asyncCtx.sendResponse(data)
    }

    override fun interest(): Class<FileLoadRequest> {
        return FileLoadRequest::class.java
    }
}

class FileSaveHandler : RequestDispatcher.Handler<FileSaveRequest> {

    override fun handle(asyncCtx: RequestDispatcher.ResponseContext, request: FileSaveRequest) {
        FileService.saveFile(request.filename, request.data)
        asyncCtx.sendResponse(true) // response
    }

    override fun interest(): Class<FileSaveRequest> {
        return FileSaveRequest::class.java
    }
}
