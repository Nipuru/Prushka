package server.bukkit.gameplay.player

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.kyori.adventure.resource.ResourcePackInfo
import server.bukkit.util.gson
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.UUID


/**
 * 将 resourcepack_server 的资源包信息进行提取
 *
 * @author Nipuru
 * @since 2025/08/11 10:11
 */
class ResourcePack(
    /** 名称 */
    val name: String,

    /** 描述 */
    val description: String,

    /** 包格式 */
    val packFormat: Int,

    /** 大小 */
    val size: Long,

    /** 哈希值 */
    val hash: String,

    /** 最后修改时间(秒) */
    val lastModified: Long,

    /** 是否是目录 */
    val isDirectory: Boolean,

    /** 下载地址 */
    val url: String
) {

    /**
     * 通过资源包名创建 UUID
     *
     * @return UUID
     */
    fun createUUID(): UUID {
        return UUID.nameUUIDFromBytes(name.toByteArray())
    }

    /**
     * 创建 URI
     */
    fun createURI(): URI {
        return URI.create(url)
    }

    /**
     * 转换为 [ResourcePackInfo]
     */
    fun toResourcePackInfo(): ResourcePackInfo {
        return ResourcePackInfo.resourcePackInfo(createUUID(), createURI(), hash)
    }

    companion object {

        /**
         * 获取服务器的资源包列表
         *
         * @param serverAddress 资源包服务器地址
         * @return 资源包列表
         */
        fun listPacks(serverAddress: String): List<ResourcePack> {
            val url = "$serverAddress/api/packs"
            val response = getData(url)
            val packs = mutableListOf<ResourcePack>()
            if (response.get("success").asBoolean) {
                response.getAsJsonArray("data").forEach {
                    packs.add(parseResourcePack(serverAddress, it))
                }
            }
            return packs
        }

        /**
         * 获取指定名称的资源包信息
         *
         * @param serverAddress 资源包服务器地址
         * @param packName 资源包名称
         * @return 资源包信息
         */
        fun getPack(serverAddress: String, packName: String): ResourcePack? {
            val url = "$serverAddress/api/packs/$packName"
            val response = getData(url)
            if (response.get("success").asBoolean) {
                return parseResourcePack(serverAddress, response.get("data"))
            }
            return null
        }

        private fun getData(url: String): JsonObject {
            val uri = URI.create(url)
            val request = HttpRequest.newBuilder().uri(uri).GET().build()
            val response = HttpClient.newHttpClient().use {
                it.send(request, HttpResponse.BodyHandlers.ofString())
            }
            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), JsonObject::class.java)
            }
            throw IOException("Failed to get data, url $url, statusCode: ${response.statusCode()}")
        }

        private fun parseResourcePack(serverAddress: String, data: JsonElement): ResourcePack {
            data as JsonObject
            return ResourcePack(
                name = data.get("name").asString,
                description = data.get("description").asString,
                packFormat = data.get("pack_format").asInt,
                size = data.get("size").asLong,
                hash = data.get("hash").asString,
                lastModified = data.get("last_modified").asLong,
                isDirectory = data.get("is_directory").asBoolean,
                url = parseUrl(
                    serverAddress = serverAddress,
                    downloadUrl = data.get("download_url").asString
                ),
            )
        }

        private fun parseUrl(serverAddress: String, downloadUrl: String): String {
            if (downloadUrl.startsWith("/download")) {
                return "$serverAddress$downloadUrl"
            }
            return downloadUrl
        }
    }
}