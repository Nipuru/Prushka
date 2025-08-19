package server.bukkit.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import server.bukkit.plugin
import server.bukkit.util.font.Bitmap
import server.bukkit.util.font.Font
import server.bukkit.util.font.FontRepository
import server.bukkit.util.font.resolver.BitmapResolver
import server.bukkit.util.font.resolver.FixedWidthResolver
import server.bukkit.util.font.resolver.SplitResolver
import server.common.sheet.Sheet
import server.common.sheet.getAllStBitmap

/**
 * 目前支持
 *
 * 全部默认标签：（略）
 *
 * bitmap: 获取名为 name 的字符
 * <bitmap:name/>               第一个字符
 * <bitmap:name;index/>         下标为 index 的字符
 * <bitmap:name;from-to/>       下标 from 到 to 前闭后开一连串字符
 * <bitmap:name;row;col/>       row 行 col 列的字符
 * <bitmap:name;row;from-to/>   row 行 from 到 to 列前闭后开一连串字符
 *
 * split: 像素分隔符
 * <split:10/>                  右移动 10 像素
 * <split:-200/>                回退 -200 像素
 *
 * fixed_width: 固定像素
 * <fixed_width:right:100>1234</fixed_width>        往右对齐
 * <fixed_width:left:100>1234</fixed_width>         往左对齐
 * <fixed_width:center:100>1234</fixed_width>       居中对齐
 */
fun String.component() : TextComponent {
    return Component.text().let {
        it.append(MiniMessageHolder.miniMessage.deserialize(this))
        it.style(Style.style()
            .decoration(TextDecoration.ITALIC, false)
            .build())
        it.build()
    }
}

/**
 * 追加字符串
 */
fun Component.append(str: String): Component {
    return this.append(str.component())
}

/**
 * 追加 HoverEvent
 */
fun Component.hoverEvent(str: String): Component {
    return this.hoverEvent(str.component())
}

private object MiniMessageHolder {

    val miniMessage = initMiniMessage()

    fun initMiniMessage(): MiniMessage {
        val bitmaps = bitmaps()
        val bitmap = BitmapResolver(bitmaps)
        val split = split(bitmap)
        val font = font(bitmaps)
        val fixedWidth = FixedWidthResolver(split, font)
        return MiniMessage.builder().tags(TagResolver.resolver(
            StandardTags.defaults(),
            split.resolver(),
            bitmap.resolver(),
            fixedWidth.resolver()
        )).build()
    }

    fun font(bitmaps: Map<String, Bitmap>): FontRepository {
        val repository = FontRepository()
        // 初始化默认字体
        val fonts = sequenceOf("prushka_font")
        val bytes = plugin.getResource("glyph_sizes.bin")!!.readAllBytes()
        for (font in fonts) {
            // 起始 unicode
            var unicode = 0x0000
            for (byte in bytes) {
                // 高位 (高 4 位)
                val start = (byte.toInt() shr 4) and 0x0F
                // 低位 (低 4 位)
                val end = byte.toInt() and 0x0F
                val width = end - start + 2
                repository.register(Font.slim(font, unicode.toChar(), width.toFloat()))
                unicode += 1
            }
        }
        // 覆写 bitmap
        for (bitmap in bitmaps.values) {
            val width = bitmap.width.toFloat()
            repository.register(*Font.fonts(bitmap, width, width))
        }
        return repository
    }

    fun bitmaps(): Map<String, Bitmap> {
        // 这里要确保和 python 工具使用一样的算法 /tool/export_bitmap.py
        val bitmaps = mutableMapOf<String, Bitmap>()
        var unicode = 0x1000
        for (cfg in Sheet.getAllStBitmap().values) {
            val chars = mutableListOf<String>()
            for (i in 0 until cfg.row) {
                val builder = StringBuilder()
                for (j in 0 until cfg.column) {
                    builder.append(unicode.toChar())
                    unicode += 1
                }
                chars.add(builder.toString())
            }
            val width = (cfg.imgWidth * cfg.height * cfg.row) / (cfg.imgHeight * cfg.column) +
                    ((cfg.height shr  31) and  1)  + 1
            val bitmap = Bitmap(
                cfg.configId,
                cfg.font,
                width,
                *chars.toTypedArray()
            )
            bitmaps[cfg.configId] = bitmap
        }
        return bitmaps
    }

    fun split(bitmap: BitmapResolver): SplitResolver {
        val splits = listOf(
            "split_pos_1", "split_pos_2", "split_pos_4", "split_pos_8",
            "split_pos_16", "split_pos_32", "split_pos_64", "split_pos_128",
            "split_neg_1", "split_neg_2", "split_neg_4", "split_neg_8",
            "split_neg_16", "split_neg_32", "split_neg_64", "split_neg_128"
        ).map { bitmap.resolve(it) }
        return SplitResolver(splits)
    }
}

