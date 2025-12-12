table rank {
    @key(config_id)

    config_id: int32  // 配置id
    name: string  // 名称
    name_color: string  // 聊天名字颜色
    chat_color: string  // 聊天文本颜色

    @data
    0, 玩家, #ACACAC, #D3D3D3
    1, 会员I, #6BD266, #A2F99D
    2, 会员II, #4FD2BF, #91F8EA
    3, 会员III, #B173E2, #E2A5FF
    7, 管理员, #FF6565, #FFCACD
}
