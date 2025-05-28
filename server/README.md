# server
游戏服务器实现

- common: 公共模块
- game: bukkit 插件，游戏的功能实现
- broker: broker-server 插件，负责消息的转发
- database: 数据库服务器程序，存储玩家数据
- shared: 公共服务器程序，处理公共业务逻辑，如拍卖，排行榜，玩家信息
- auth: 认证服务器，玩家uid，dbId分配、三方sdk对接、webhook接口、管理后端等
- log: 日志服务器，记录集群内产生的日志