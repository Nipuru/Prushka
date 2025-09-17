package server.bukkit.util.text

import net.kyori.adventure.key.Keyed
import net.kyori.adventure.text.*
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import org.bukkit.plugin.Plugin
import server.bukkit.util.text.font.Bitmap
import server.bukkit.util.text.font.Font
import server.bukkit.util.text.font.FontRepository
import server.bukkit.util.text.resolver.FixedWidthResolver.Position
import server.bukkit.util.text.resolver.BitmapResolver
import server.bukkit.util.text.resolver.FixedWidthResolver
import server.bukkit.util.text.resolver.SplitResolver


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
fun String.component(): TextComponent {
    return Component.text().let {
        it.append(TextFactory.instance.miniMessage.deserialize(this))
        it.style(
            Style.style()
                .decoration(TextDecoration.ITALIC, false)
                .build()
        )
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

/**
 * 文本位移
 * @param pixel 像素
 */
fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.split(pixel: Int): ComponentBuilder<C, B> {
    return this.append("<split:$pixel>".component())
}

/**
 * 添加图片
 * @param name 图片名称 st_bitmap config_id
 * @param row 行
 * @param column 列
 */
fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.bitmap(
    name: String,
    row: Int = 0,
    column: Int = 0
): ComponentBuilder<C, B> {
    return this.append("<bitmap:$name:$row:$column>".component())
}

/**
 * 固定宽度
 * @param text 文本
 * @param width 宽度
 * @param position 对其方式
 */
fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.fixedWidth(
    text: String,
    width: Int,
    position: Position = Position.LEFT
): ComponentBuilder<C, B> {
    return this.append("<fixed_width:$position:$width>${text}</fixed_width>".component())
}

/**
 * 获取组件宽度
 * @param parentBold 父组件是否粗体
 * @param parentItalic 父组件是否斜体
 * @param parentFont 父组件字体
 */
fun ComponentLike.getWidth(
    parentBold: Boolean = false,
    parentItalic: Boolean = false,
    parentFont: Keyed = Font.DEFAULT
): Int {
    return TextFactory.instance.font.getTotalWidth(asComponent(), parentBold, parentItalic, parentFont)
}

class TextFactory private constructor(val plugin: Plugin, splits: List<String>, bitmaps: List<Bitmap>) {
    val bitmap: BitmapResolver = BitmapResolver(bitmaps.associateBy { it.name })
    val split: SplitResolver = SplitResolver(splits.map { bitmap.resolve(it) })
    val font: FontRepository = FontRepository { fileName: String ->
        plugin.getResource("font/$fileName")
    }.also { repository ->
        // 注册 bitmap
        for (bitmap in bitmaps) {
            Font.fonts(bitmap, bitmap.width).forEach {
                repository.register(it)
            }
        }
    }

    val fixedWidth: FixedWidthResolver = FixedWidthResolver(split, font)
    val miniMessage: MiniMessage = MiniMessage.builder().tags(
        TagResolver.resolver(
            StandardTags.defaults(),
            split.resolver(),
            bitmap.resolver(),
            fixedWidth.resolver()
        )
    ).build()

    companion object {
        val instance: TextFactory get() = _instance ?: throw IllegalStateException("TextFactory not initialized")

        private var _instance: TextFactory? = null

        fun init(plugin: Plugin, splits: List<String>, bitmaps: () -> List<Bitmap>) {
            _instance = TextFactory(plugin, splits, bitmaps())
        }
    }
}