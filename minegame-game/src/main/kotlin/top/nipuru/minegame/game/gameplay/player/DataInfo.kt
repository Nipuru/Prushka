package top.nipuru.minegame.game.gameplay.player

import top.nipuru.minegame.common.message.database.FieldMessage
import top.nipuru.minegame.game.gameplay.player.DataConvertor.pack
import top.nipuru.minegame.game.gameplay.player.DataConvertor.unpack
import top.nipuru.minegame.game.gameplay.player.DataConvertor.unpackList

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
