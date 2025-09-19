package server.bukkit.gameplay.player

import org.bukkit.Bukkit
import server.bukkit.BukkitPlugin
import server.bukkit.gameplay.player.DataConvertor.getOrCache
import server.common.logger.Logger
import server.common.message.FieldMessage
import server.common.message.PlayerDataTransactionMessage
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.reflect.KProperty1

class DataAction(val type: DataActionType, val data: Any, val fields: Array<String>?)

enum class DataActionType {
    INSERT, UPDATE, DELETE
}


fun <T : Any> GamePlayer.update(data: T, vararg properties: KProperty1<T, *>) {
    writer.add(DataAction(DataActionType.UPDATE, data, DataConvertor.getProperty(data, properties)))
}

fun <T: Any> GamePlayer.insert(data: T): T {
    writer.add(DataAction(DataActionType.INSERT, data, null))
    return data
}

fun <T: Any> GamePlayer.delete(data: T) {
    writer.add(DataAction(DataActionType.DELETE, data, null))
}

class DataWriter(private val player: GamePlayer) {

    private val writeQueue = ConcurrentLinkedQueue<DataAction>()

    fun add(info: DataAction) {
        writeQueue.add(info)
    }

    // 玩家数据写入 DBServer
    fun write() {
        if (writeQueue.isEmpty()) return
        // 确保每条数据 只会产生一次提交
        val map = IdentityHashMap<Any, DataAction>()
        while (!writeQueue.isEmpty()) {
            var dataAction = writeQueue.poll()
            if (map.containsKey(dataAction.data)) {
                val old = map.remove(dataAction.data)
                if (old!!.type == DataActionType.DELETE || dataAction.type == DataActionType.INSERT) {
                    // 通常是代码写的有问题 对象删除之后就不能操作了 在新增前不能有任何操作
                    Logger.error(
                        "Invalid player data operation {} before {}, dataClass: {}",
                        old.type,
                        dataAction.type,
                        dataAction.data.javaClass.name
                    )
                    kickPlayerIfPossible(player)
                    return  // 跳过本次的操作
                }

                if (dataAction.type == DataActionType.UPDATE) {
                    if (old.type == DataActionType.UPDATE) {
                        // 都是更新则进行合并
                        val mergedField = mutableSetOf<String>()
                        mergedField.addAll(listOf(*dataAction.fields!!))
                        mergedField.addAll(listOf(*old.fields!!))
                        dataAction = DataAction(DataActionType.UPDATE, dataAction.data, mergedField.toTypedArray<String>())
                    } else if (old.type == DataActionType.INSERT) {
                        // 之前是新增则直接新增
                        dataAction = DataAction(DataActionType.INSERT, dataAction.data, null)
                    }
                }

                // 这种情况产生一条删除提交 覆盖掉新增 不会出现报错
                // if (writeDBInfo.getType() == WriteDBInfo.Type.DELETE && old.getType() == WriteDBInfo.Type.INSERT);
            }
            map[dataAction.data] = dataAction
        }
        BukkitPlugin.bizThread.submit {
            try {
                val transaction = PlayerDataTransactionMessage(player.playerId)
                for (dataAction in map.values) {
                    val dataClassCache = getOrCache(dataAction.data.javaClass)
                    val tableName = dataClassCache.tableName

                    val uniqueFields = mutableListOf<FieldMessage>()
                    for (uniqueFieldName in dataClassCache.uniqueFields) {
                        val field = dataClassCache.tableFields[uniqueFieldName]
                        val fieldMessage = FieldMessage(uniqueFieldName, field!![dataAction.data])
                        uniqueFields.add(fieldMessage)
                    }
                    when (dataAction.type) {
                        DataActionType.UPDATE -> {
                            val updateFields = mutableListOf<FieldMessage>()
                            for (key in dataAction.fields!!) {
                                val value = dataClassCache.updateFields[key]!!
                                val fieldMessage = FieldMessage(key, value[dataAction.data])
                                updateFields.add(fieldMessage)
                            }
                            transaction.addUpdate(tableName, uniqueFields, updateFields)
                        }
                        DataActionType.INSERT -> {
                            for ((key, value) in dataClassCache.updateFields) {
                                val fieldMessage = FieldMessage(key, value[dataAction.data])
                                uniqueFields.add(fieldMessage)
                            }
                            transaction.addInsert(tableName, uniqueFields)
                        }
                        DataActionType.DELETE -> {
                            transaction.addDelete(tableName, uniqueFields)
                        }
                    }
                }
                player.dataService.transaction(transaction)
            } catch (e: Exception) {
                Logger.error("Failed to write database for player {}", player.playerId, e)
                kickPlayerIfPossible(player)
            }
        }
    }

    private fun kickPlayerIfPossible(player: GamePlayer) {
        val bukkitPlayer = Bukkit.getPlayer(player.uniqueId) ?: return
        if (Bukkit.isPrimaryThread()) bukkitPlayer.kick()
        else Bukkit.getScheduler().runTask(BukkitPlugin, Runnable { bukkitPlayer.kick() })
    }
}
