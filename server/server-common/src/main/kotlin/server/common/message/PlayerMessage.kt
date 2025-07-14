package server.common.message

import java.io.Serializable


/**
 * @author Nipuru
 * @since 2025/07/14 15:40
 */
class PlayerMessage(val playerId: Int, val dbId: Int) : Serializable