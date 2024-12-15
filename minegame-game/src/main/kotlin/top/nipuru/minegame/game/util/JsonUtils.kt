package top.nipuru.minegame.game.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder


/**
 * @author Nipuru
 * @since 2024/11/26 17:45
 */
val gson: Gson = GsonBuilder().create()

fun Any?.toJson(): String {
    return gson.toJson(this)
}

inline fun <reified T> String.fromJson(): T {
    return gson.fromJson(this, T::class.java)
}
