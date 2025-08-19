package server.bukkit.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder



fun Any?.toJson(): String {
    return GsonHolder.gson.toJson(this)
}

inline fun <reified T> String.fromJson(): T {
    return GsonHolder.gson.fromJson(this, T::class.java)
}

@PublishedApi
internal object GsonHolder {
    val gson: Gson = GsonBuilder().create()
}
