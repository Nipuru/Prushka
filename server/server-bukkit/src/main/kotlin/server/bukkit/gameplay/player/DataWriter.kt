package server.bukkit.gameplay.player

import net.afyer.afybroker.client.Broker
import org.bukkit.Bukkit
import server.bukkit.gameplay.player.DataConvertor.getOrCache
import server.common.logger.logger
import server.bukkit.plugin
import server.bukkit.util.submit
import server.common.message.FieldMessage
import server.common.service.PlayerDataService
import server.common.service.PlayerDataService.Transaction
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class DataWriter(private val player: GamePlayer) {

    private val writeQueue = ConcurrentLinkedQueue<DataAction>()
    private val dataService = Broker.getService(PlayerDataService::class.java, player.dbId.toString())

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
                    logger.error(
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
        submit {
            try {
                val transaction = Transaction(player.playerId)
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
                dataService.transaction(transaction)
            } catch (e: Exception) {
                logger.error("Failed to write database for player {}", player.playerId, e)
                kickPlayerIfPossible(player)
            }
        }
    }

    private fun kickPlayerIfPossible(player: GamePlayer) {
        val bukkitPlayer = Bukkit.getPlayer(player.uniqueId) ?: return
        if (Bukkit.isPrimaryThread()) bukkitPlayer.kick()
        else Bukkit.getScheduler().runTask(plugin, Runnable { bukkitPlayer.kick() })
    }
}
