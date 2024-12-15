package top.nipuru.minegame.game.gameplay.chat

import java.io.Serializable

class Fragment(vararg val args: Serializable?) {
    @Suppress("UNCHECKED_CAST")
    fun <T> getArg(index: Int): T {
        return args[index] as T
    }
}
