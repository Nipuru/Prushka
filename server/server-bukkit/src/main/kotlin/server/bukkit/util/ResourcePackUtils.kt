package server.bukkit.util

import com.google.gson.JsonObject
import server.bukkit.MessageType
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse


/**
 * 将 resourcepack_server 的资源包信息进行提取
 * http://localhost:8080/api/packs/resourcepack
 *
 * @author Nipuru
 * @since 2025/08/11 10:11
 */
class ResourcePack(val url: String, val hash: String) {

    companion object {
        fun parse(url: String): ResourcePack {
            val uri = URI.create(url)
            val client = HttpClient.newHttpClient()
            val request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            client.close()

            if (response.statusCode() == 200) {
                val jsonResponse = gson.fromJson(response.body(), JsonObject::class.java)

                if (jsonResponse.get("success").asBoolean) {
                    val data = jsonResponse.getAsJsonObject("data")
                    val downloadUrl = data.get("download_url").asString
                    val hash = data.get("hash").asString

                    // 构建完整的下载URL
                    val baseUrl = "${uri.scheme}://${uri.host}:${uri.port}"
                    val fullDownloadUrl = baseUrl + downloadUrl

                    return ResourcePack(fullDownloadUrl, hash)
                }
            }
            throw IOException("Failed to parse resource pack URL")
        }
    }
}