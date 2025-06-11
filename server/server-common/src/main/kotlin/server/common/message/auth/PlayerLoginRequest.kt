package server.common.message.auth

import java.io.Serializable
import java.util.UUID


/**
 * @author Nipuru
 * @since 2025/06/11 16:39
 */
class PlayerLoginRequest(val name: String, val uniqueId: UUID, val ip: String) : Serializable
