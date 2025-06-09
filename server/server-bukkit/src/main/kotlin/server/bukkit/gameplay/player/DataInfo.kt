package server.bukkit.gameplay.player

import server.bukkit.gameplay.player.DataConvertor.pack
import server.bukkit.gameplay.player.DataConvertor.unpack
import server.bukkit.gameplay.player.DataConvertor.unpackList
import server.common.message.database.FieldMessage

class DataInfo(val tables: MutableMap<String, MutableList<List<FieldMessage>>>) {

    fun <T : Data> unpack(dataClass: Class<T>): T? {
        return unpack(tables, dataClass)
    }

    fun <T : Data> unpackList(dataClass: Class<T>): List<T> {
        return unpackList(tables, dataClass)
    }

    fun <T: Data> pack(data: T) {
        pack(this.tables, data)
    }
}
