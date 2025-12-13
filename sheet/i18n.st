table i18n {
    @akey(key, subkey=lang)

    key: string   // 国际化键
    lang: string  // 语言代码
    text: string  // 翻译文本

    @data
    welcome.title, zh_cn, 欢迎
    welcome.title, en_us, Welcome
    welcome.title, ja_jp, ようこそ
}
