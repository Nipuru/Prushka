package server.bukkit.gameplay.player

import server.common.message.FieldMessage

class DataInfo(val tables: MutableMap<String, MutableList<List<FieldMessage>>>) {

    inline fun <reified T : Data> unpack(): T? {
        return DataConvertor.unpack(tables)
    }

    inline fun <reified T : Data> unpackList(): List<T> {
        return DataConvertor.unpackList(tables)
    }

    fun <T: Data> pack(data: T) {
        DataConvertor.pack(this.tables, data)
    }
}
