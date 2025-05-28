package top.nipuru.prushka.server.game.util

import net.kyori.adventure.text.format.TextColor

fun String.tag(tag: String) = "<$tag:$this/>"

fun java.awt.Color.textColor() = TextColor.color(this.rgb)

fun java.awt.Color.textColorTag() = "<${TextColor.color(this.rgb).asHexString()}>"

fun org.bukkit.Color.textColor() = TextColor.color(this.asRGB())

fun org.bukkit.Color.textColorTag() = "<${TextColor.color(this.asRGB()).asHexString()}>"