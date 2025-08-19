package server.bukkit.gameplay.misc

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.kyori.adventure.resource.ResourcePackInfo
import net.kyori.adventure.resource.ResourcePackRequest
import server.bukkit.config.Config
import server.bukkit.gameplay.player.GamePlayer
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.UUID
import java.util.concurrent.CompletableFuture

fun GamePlayer.setResourcePack(pack: ResourcePack) {
    val request = ResourcePackRequest.resourcePackRequest()
        .packs(pack.toResourcePackInfo())
        .replace(true)
        .required(true)
    bukkitPlayer.sendResourcePacks(request)
}

/**
 * 将 resourcepack_server 的资源包信息进行提取
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
        fun listPacks(): CompletableFuture<List<ResourcePack>> {
            return CompletableFuture.supplyAsync {
                val serverAddress = Config.RESOURCEPACK_URL.string()
                val url = "$serverAddress/api/packs"
                val response = getData(url)
                val packs = mutableListOf<ResourcePack>()
                if (response.get("success").asBoolean) {
                    response.getAsJsonArray("data").forEach {
                        packs.add(parseResourcePack(serverAddress, it))
                    }
                }
                packs
            }
        }

        /**
         * 获取服务器资源包信息
         *
         * @return 资源包信息
         */
        fun getServerPack(): CompletableFuture<ResourcePack?> {
            return CompletableFuture.supplyAsync {
                val serverAddress = Config.RESOURCEPACK_URL.string()
                val packName = Config.RESOURCEPACK_PACK.string()
                val url = "$serverAddress/api/packs/$packName"
                val response = getData(url)
                if (response.get("success").asBoolean) {
                    parseResourcePack(serverAddress, response.get("data"))
                }
                null
            }
        }

        private fun getData(url: String): JsonObject {
            val uri = URI.create(url)
            val request = HttpRequest.newBuilder().uri(uri).GET().build()
            val response = HttpClient.newHttpClient().use {
                it.send(request, HttpResponse.BodyHandlers.ofString())
            }
            if (response.statusCode() == 200) {
                return JsonParser.parseString(response.body()).asJsonObject
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