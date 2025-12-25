package server.auth.http

import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.header
import io.ktor.server.request.receive
import io.ktor.server.routing.RoutingContext
import server.auth.util.JWTUtil
import server.auth.util.fail
import server.auth.util.success
import kotlin.random.Random


/**
 * @author Nipuru
 * @since 2025/12/24 17:07
 */
private val fakeUserList = listOf(
    FakeUser(
        username = "admin",
        password = "123456",
        token = ""  // token 将在登录时动态生成
    )
)

// 存储有效的token集合
val validTokens = mutableSetOf<String>()

// 假用户数据
data class FakeUser(
    val username: String,
    val password: String,
    val token: String,
)

// 登录请求体
data class LoginRequest(val username: String, val password: String)

// 表格数据项
data class TableItem(
    val id: Int,
    val name: String,
    val sex: String,
    val phone: Long,
    val education: String,
    val married: Int,
    val forbid: Boolean,
    val hobby: List<String>
)

// 分页响应数据
data class PageData<T>(val list: List<T>, val total: Int)

private val prefixList = listOf(135, 136, 137, 138, 139, 155, 158, 183, 185, 189)
private val educationList = listOf("小学", "初中", "高中", "专科", "本科", "研究生")
private val hobbyList = listOf("羽毛球", "乒乓球", "篮球", "排球", "网球", "游泳", "滑雪", "跳高", "滑翔", "潜水")
private val nameList = listOf("张三", "李四", "王五", "赵六", "钱七", "孙八", "周九", "吴十", "郑十一", "陈十二",
    "刘芳", "杨华", "黄丽", "周杰", "吴婷", "赵敏", "孙强", "李娜", "王伟", "陈静")

private fun getPhone(): Long {
    val prefix = prefixList.random()
    val suffix = (10000000..99999999).random()
    return (prefix.toString() + suffix.toString()).toLong()
}

private fun getEducation() = educationList.random()

private fun getMarried() = Random.nextInt(4)

private fun getHobby(): List<String> {
    val len = if (Random.nextBoolean()) 3 else 4
    return hobbyList.shuffled().take(len)
}


suspend fun RoutingContext.login() {
    val request = call.receive<LoginRequest>()
    val user = fakeUserList.find { it.username == request.username && it.password == request.password }
    if (user == null) {
        call.fail("账号或密码错误!")
        return
    }
    val token = JWTUtil.makeToken(user.username)
    validTokens.add(token)

    call.success("登录成功", mapOf(
        "token" to token,
    ))
}

private fun genTableList(): List<TableItem> {
    return (0 until 100).map { index ->
        TableItem(
            id = 1001 + index,
            name = nameList.random(),
            sex = if (Random.nextBoolean()) "男" else "女",
            phone = getPhone(),
            education = getEducation(),
            married = getMarried(),
            forbid = Random.nextBoolean(),
            hobby = getHobby()
        )
    }
}

private val tableList = genTableList()

suspend fun RoutingContext.getUserInfo() {
    val principal = call.principal<JWTPrincipal>()
    val username = principal?.payload?.getClaim("username")?.asString()
    if (username == null) {
        call.fail("无效的token!")
        return
    }
    val user = fakeUserList.find { it.username == username }
    if (user == null) {
        call.fail("未找到对应的用户信息!")
        return
    }
    call.success("获取成功", mapOf(
        "username" to user.username,
    ))
}

suspend fun RoutingContext.logout() {
    val token = call.request.header("Authorization")
    if (token != null) {
        validTokens.remove(token)
    }
    call.success("Token已销毁!")
}

suspend fun RoutingContext.getTableList() {
    val current = call.parameters["current"]?.toIntOrNull() ?: 1
    val pageSize = call.parameters["pageSize"]?.toIntOrNull() ?: 10

    val offset = (current - 1) * pageSize
    val pageData = if (offset + pageSize >= tableList.size) {
        tableList.subList(offset.coerceAtMost(tableList.size), tableList.size)
    } else {
        tableList.subList(offset, offset + pageSize)
    }

    call.success("获取成功", PageData(pageData, tableList.size))
}