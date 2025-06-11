package server.common.message.shared

import java.io.Serializable


/**
 * @author Nipuru
 * @since 2025/06/11 16:47
 */
class GetPlayerInfosRequest(val playerIds: List<Int>) : Serializable
