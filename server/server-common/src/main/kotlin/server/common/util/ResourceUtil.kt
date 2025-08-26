package server.common.util

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.file.Files

/**
 * 将 jar 包内的某个文件提取到 jar 包所在目录
 */
fun Any.getResourceOrExtract(name: String): InputStream {
    val destFile = File(name)
    if (!destFile.exists()) {
        val inputStream = javaClass.classLoader.getResourceAsStream(name) ?: throw FileNotFoundException(name)
        Files.copy(inputStream, destFile.toPath())
        inputStream.close()
    }
    return FileInputStream(destFile)
}