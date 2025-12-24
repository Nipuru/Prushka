package server.auth.http

import io.ktor.server.auth.*
import io.ktor.server.routing.*
import server.auth.util.success


/**
 * 路由配置
 *
 * @author Nipuru
 * @since 2025/01/22 15:55
 */
fun Route.configureRouting() {
    get("/") { call.success("Hello World!") }

    // 登录
    post("/api/login") { login() }

    // 需要JWT验证的
    authenticate {
        // 获取用户信息
        get("/api/getUserInfo") { getUserInfo() }
        // 登出接口
        get("/api/logout") { logout() }
        // 表格数据
        get("/api/table/getTableList") { getTableList() }
        // 表格元数据
        get("/api/sheet/getSheetMetadata") { getSheetMetadata() }
        // 获取指定表格数据
        get("/api/sheet/getSheetList") { getSheetList() }
        // 更新表格数据
        post("/api/sheet/updateSheet") { updateSheet() }
        // 新增表格数据
        post("/api/sheet/insertSheet") { insertSheet() }
        // 删除表格数据
        get("/api/sheet/deleteSheet") { deleteSheet() }
    }
}

