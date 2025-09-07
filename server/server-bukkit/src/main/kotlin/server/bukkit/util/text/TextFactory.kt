package server.bukkit.util.text

import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import org.bukkit.plugin.Plugin


/**
 * @author Nipuru
 * @since 2025/09/07 13:16
 *
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
class TextFactory(val plugin: Plugin, bitmaps: List<Bitmap>) {
    val bitmapMap: Map<String, Bitmap> = bitmaps.associate { it.name to it }
    val bitmap: BitmapResolver = BitmapResolver(bitmapMap)
    val split: SplitResolver = split(bitmap)
    val font: FontRepository = font(bitmapMap)
    val fixedWidth: FixedWidthResolver = FixedWidthResolver(split, font)
    val miniMessage: MiniMessage = MiniMessage.builder().tags(
        TagResolver.resolver(
            StandardTags.defaults(),
            split.resolver(),
            bitmap.resolver(),
            fixedWidth.resolver()
        )
    ).build()

    private fun font(bitmaps: Map<String, Bitmap>): FontRepository {
        val repository = FontRepository { fileName: String ->
            plugin.getResource("font/$fileName")
        }
        // 注册 bitmap
        for (bitmap in bitmaps.values) {
            Font.fonts(bitmap, bitmap.width).forEach{
                repository.register(it)
            }
        }
        return repository
    }

    private fun split(bitmap: BitmapResolver): SplitResolver {
        val splits = listOf(
            "split_pos_1", "split_pos_2", "split_pos_4", "split_pos_8",
            "split_pos_16", "split_pos_32", "split_pos_64", "split_pos_128",
            "split_neg_1", "split_neg_2", "split_neg_4", "split_neg_8",
            "split_neg_16", "split_neg_32", "split_neg_64", "split_neg_128"
        ).map { bitmap.resolve(it) }
        return SplitResolver(splits)
    }
}