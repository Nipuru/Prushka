package server.bukkit.config

import server.bukkit.plugin


/**
 * config.yml 配置
 * 不希望由此文件记录太多东西 而仅记录服务端运行所需的基本配置
 *
 * @author Nipuru
 * @since 2025/06/27 16:57
 */
enum class Config(val path: String) {
    /**
     * 是否开启调试模式
     */
    DEBUG("debug"),

    /**
     * 资源包服务器地址
     */
    RESOURCEPACK_URL("resourcepack-server.url"),

    /**
     * 资源包名称
     */
    RESOURCEPACK_PACK("resourcepack-server.pack");



    fun int(def: Int = 0): Int {
        return plugin.config.getInt(path, def)
    }

    fun boolean(def: Boolean = false): Boolean {
        return plugin.config.getBoolean(path, def)
    }

    fun string(def: String = ""): String {
        return plugin.config.getString(path, def)!!
    }
}