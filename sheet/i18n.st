table i18n {
    @akey(key, subkey=lang)

    key: string   // 国际化键
    lang: string  // 语言代码
    text: string  // 翻译文本

    @data
    welcome.title, zh_cn, 欢迎
    welcome.title, en_us, Welcome
    welcome.title, ja_jp, ようこそ
    property.gold, zh_cn, 金币
    property.points, zh_cn, 点券
    rank.player, zh_cn, 玩家
    rank.vip1, zh_cn, 会员I
    rank.vip2, zh_cn, 会员II
    rank.vip3, zh_cn, 会员III
    rank.admin, zh_cn, 管理员
}
