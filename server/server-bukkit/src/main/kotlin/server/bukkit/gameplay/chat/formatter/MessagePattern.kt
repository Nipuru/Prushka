package server.bukkit.gameplay.chat.formatter

import java.util.regex.Pattern

abstract class MessagePattern(val pattern: Pattern) : MessageFormatter
