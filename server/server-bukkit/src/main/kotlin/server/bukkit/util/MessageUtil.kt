package server.bukkit.util

import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import net.kyori.adventure.text.*
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import server.bukkit.BukkitPlugin
import server.bukkit.util.text.*
import server.bukkit.util.text.FixedWidthResolver.Position
import server.common.logger.Logger
import server.common.sheet.Sheet
import server.common.sheet.getAllStBitmap

/**
 * 目前支持
 *
 * 全部默认标签：（略）
 *
 * bitmap: 获取名为 name 的字符
 * <bitmap:name>               第一个字符
 * <bitmap:name:index>         下标为 index 的字符
 * <bitmap:name:from-to>       下标 from 到 to 前闭后开一连串字符
 * <bitmap:name:row:col>       row 行 col 列的字符
 * <bitmap:name:row:from-to>   row 行 from 到 to 列前闭后开一连串字符
 *
 * split: 像素分隔符
 * <split:10>                  右移动 10 像素
 * <split:-200>                回退 -200 像素
 *
 * fixed_width: 固定像素
 * <fixed_width:right:100>1234</fixed_width>        往右对齐
 * <fixed_width:left:100>1234</fixed_width>         往左对齐
 * <fixed_width:center:100>1234</fixed_width>       居中对齐
 */
fun String.component() : TextComponent {
    return Component.text().let {
        it.append(MessageHolder.miniMessage.deserialize(this))
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

fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.split(pixel: Int) : ComponentBuilder<C, B> {
    return this.append("<split:$pixel>".component())
}

fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.bitmap(name: String, row: Int = 0, column: Int = 0): ComponentBuilder<C, B> {
    return this.append("<bitmap:$name:$row:$column>".component())
}

fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.fixedWidth(text: String, width: Int, position: Position = Position.LEFT): ComponentBuilder<C, B> {
    return this.append("<fixed_width:$position:$width>${text}</fixed_width>".component())
}

fun ComponentLike.getWidth(parentBold: Boolean = false, parentItalic: Boolean = false, font: Keyed = Font.DEFAULT): Float {
    return MessageHolder.font.getTotalWidth(asComponent(), parentBold, parentItalic, font)
}

private object MessageHolder {

    val bitmaps = bitmaps()
    val bitmap = BitmapResolver(bitmaps)
    val split = split(bitmap)
    val font = font(bitmaps)
    val fixedWidth = FixedWidthResolver(split, font)
    val miniMessage = MiniMessage.builder().tags(
        TagResolver.resolver(
            StandardTags.defaults(),
            split.resolver(),
            bitmap.resolver(),
            fixedWidth.resolver()
        )
    ).build()

    fun font(bitmaps: Map<String, Bitmap>): FontRepository {
        val repository = FontRepository { fileName: String ->
            BukkitPlugin.getResource("font/$fileName")
        }
        // 注册 bitmap
        for (bitmap in bitmaps.values) {
            Font.fonts(bitmap, bitmap.width).forEach{
                repository.register(it)
            }
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
                    ((cfg.height shr 31) and 1) + 1
            val bitmap = Bitmap(
                cfg.configId,
                Key.key("prushka:bitmap"),
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