table bitmap {  // 位图表
    @key(config_id)

    config_id: string  // 配置id
    row: int32  // 行数
    column: int32  // 列数
    ascent: int32  // 基线偏移
    height: int32  // 高度
    img_height: int32  // 图片高度
    img_width: int32  // 图片宽度
    file: string  // 材质文件路径

    @data
    split_pos_1, 1, 1, -32768, 0, 256, 256, prushka:ui/pixel_split.png, 
    split_pos_2, 1, 1, -32768, 1, 256, 256, prushka:ui/pixel_split.png, 
    split_pos_4, 1, 1, -32768, 3, 256, 256, prushka:ui/pixel_split.png, 
    split_pos_8, 1, 1, -32768, 7, 256, 256, prushka:ui/pixel_split.png, 
    split_pos_16, 1, 1, -32768, 15, 256, 256, prushka:ui/pixel_split.png, 
    split_pos_32, 1, 1, -32768, 31, 256, 256, prushka:ui/pixel_split.png, 
    split_pos_64, 1, 1, -32768, 63, 256, 256, prushka:ui/pixel_split.png, 
    split_pos_128, 1, 1, -32768, 127, 256, 256, prushka:ui/pixel_split.png, 
    split_neg_1, 1, 1, -32768, -3, 256, 256, prushka:ui/pixel_split.png, 
    split_neg_2, 1, 1, -32768, -4, 256, 256, prushka:ui/pixel_split.png, 
    split_neg_4, 1, 1, -32768, -6, 256, 256, prushka:ui/pixel_split.png, 
    split_neg_8, 1, 1, -32768, -10, 256, 256, prushka:ui/pixel_split.png, 
    split_neg_16, 1, 1, -32768, -18, 256, 256, prushka:ui/pixel_split.png, 
    split_neg_32, 1, 1, -32768, -34, 256, 256, prushka:ui/pixel_split.png, 
    split_neg_64, 1, 1, -32768, -66, 256, 256, prushka:ui/pixel_split.png, 
    split_neg_128, 1, 1, -32768, -130, 256, 256, prushka:ui/pixel_split.png, 
    emoji, 5, 12, 8, 9, 85, 204, prushka:ui/emojis.png, 
    system_prefix, 1, 5, 7, 8, 16, 80, prushka:ui/system_prefix.png,
}
