package top.nipuru.prushka.config.reader

import org.ehcache.impl.internal.concurrent.ConcurrentHashMap
import top.nipuru.prushka.common.config.ConfigReader
import top.nipuru.prushka.config.config.Config
import top.nipuru.prushka.config.reader.impl.ExcelReader
import java.io.File
import java.nio.file.Files

object FileReader {

    private lateinit var basePath: String
    val files = ConcurrentHashMap<String, File>()
    val fileData = ConcurrentHashMap<String, List<Any>>()
    val readers = mutableListOf<IReader>()

    fun init(config: Config) {
        basePath = config.configServer!!.basePath!!
        readFiles()
        readers.add(ExcelReader())
    }

    fun reload(config: Config) {
        files.clear()
        fileData.clear()
        basePath = config.configServer!!.basePath!!
        readFiles()
    }

    fun readFiles() {
        val file = File(basePath)
        if (!file.exists()) {
            throw RuntimeException("File not found")
        }
        // 解析file的路径 文件实际路径 去掉 basePath 去掉文件类型 然后/转换为_ 就是key
        Files.walk(file.toPath()).forEach {
            val tempFile = it.toFile()
            if (tempFile.isFile) {
                val key = convertToKey(tempFile)
                files[key] = tempFile
            }
        }
    }

    fun <T : Any> readFile(targetClass: Class<T>): List<T> {
        // 获取targetClass 上面的注解
        val annotation = targetClass.getAnnotation(ConfigReader::class.java) ?: throw RuntimeException("Annotation not found")
        val key = annotation.bindKey
        return readFile(key, targetClass)
    }

    fun <T : Any> readFile(key: String, targetType: Class<T>): List<T> {
        if (fileData.containsKey(key)) {
            return fileData[key] as List<T>
        }
        val file = files[key] ?: throw RuntimeException("File not found")
        val reader = readers.find { it.extension.contains(file.extension) } ?: throw RuntimeException("Reader not found")
        val data = reader.readFile(file, targetType)
        fileData[key] = data
        return data
    }

    /**
     * 将文件的绝对路径转换为基于基准路径的规范化键值
     *
     * 生成规则：
     * 1. 计算文件相对于基准路径 [basePath] 的相对路径 (自动去掉基准路径部分)
     * 2. 递归移除每个路径段的文件扩展名
     * 3. 用下划线连接所有路径段 (例：abc/def/ghi.txt → abc_def_ghi)
     *
     * @param relative 目标文件的绝对路径对象，必须位于 [basePath] 目录下
     * @return 生成的规范化键值，路径段以下划线连接且不含扩展名
     * @throws IllegalArgumentException 如果文件不在基准路径下 (通过 [File.relativeTo] 隐式检查)
     *
     * ### 典型用例
     * ```
     * basePath = "/config"
     * 文件路径 = "/config/abc/def/file.name.txt"
     * 生成键值 = "abc_def_file.name"
     * ```
     *
     * @see File.relativeTo 实际路径关系计算
     * @see File.nameWithoutExtension 扩展名处理逻辑
     */
    private fun convertToKey(relative: File): String {
        // 1. 确保 basePath 是目录的规范路径（处理可能的末尾斜杠问题）
        val baseFile = File(basePath).canonicalFile
        // 2. 获取文件相对于 basePath 的相对路径（自动去掉 basePath 部分）
        val relativeTo = relative.canonicalFile.relativeTo(baseFile)
        // 3. 分解路径段并处理扩展名
        val parts = mutableListOf<String>()
        var current: File? = relativeTo
        while (current != null) {
            if (current.name.isNotEmpty()) {
                parts.add(0, current.nameWithoutExtension)
            }
            current = current.parentFile
        }
        // 4. 拼接为下划线分隔的键
        return parts.joinToString("_")
    }


}
