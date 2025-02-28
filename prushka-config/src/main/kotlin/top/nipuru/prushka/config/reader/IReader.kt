package top.nipuru.prushka.config.reader

import java.io.File

interface IReader {

    // 拓展名
    val extension: List<String>

    // 读取文件 并返回对象
    fun <T> readFile(file: File, targetType: Class<T>): List<T>
}
