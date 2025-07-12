package server.common.service

import server.common.message.PlayerInfoMessage


/**
 * @author Nipuru
 * @since 2025/07/12 17:45
 */
interface PlayerInfoService {

    fun getByIds(playerIds: List<Int>): Map<Int, PlayerInfoMessage>

    fun getByName(name: String): PlayerInfoMessage?

    fun insertOrUpdate(playerInfo: PlayerInfoMessage)
}