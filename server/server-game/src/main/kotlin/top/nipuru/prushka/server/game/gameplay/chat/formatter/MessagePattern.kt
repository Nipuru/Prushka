package top.nipuru.prushka.server.game.gameplay.chat.formatter

import java.util.regex.Pattern

abstract class MessagePattern(val pattern: Pattern) : MessageFormatter
