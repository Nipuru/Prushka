# .st 模板语法规范

## 概述

`.st` (Sheet Template) 是一种声明式的配置表结构定义语言，用于：
- 描述配置表的字段结构
- 定义索引类型和查询方式
- 自动生成 Kotlin 代码和 JSON 数据

## 语法结构

```
table <表名> {
    // 索引配置
    @key(<字段名>)                     // 主键索引
    @vkey(<字段名>)                    // 虚拟键索引
    @akey(<字段名>, subkey=<字段名>)   // 数组键索引，可选子键

    // 其他配置
    @unique([<字段1>, ...])  // 唯一性约束 (索引本身具有唯一性约束)

    // 字段定义
    <字段名>: <类型> [-> <关联表>.<字段>] // <注释>

    // 预制数据（可选）
    @data
    <值1>, <值2>, ...  // CSV格式，按字段顺序
}
```

## 数据类型

### 基础类型

| 类型 | Kotlin 映射 | 说明 |
|------|-------------|------|
| `int32` | `Int` | 32 位整数 |
| `int64` | `Long` | 64 位整数 |
| `float32` | `Float` | 32 位浮点数 |
| `float64` | `Double` | 64 位浮点数 |
| `bool` | `Boolean` | 布尔值 |
| `string` | `String` | 字符串 |

### 数组类型

使用 `[]` 前缀表示数组：

```
[]int32    → List<Int>
[]string   → List<String>
[]float64  → List<Double>
```

### 引用类型

使用 `ref<表名.字段名>` 表示引用，类型自动与目标字段一致：

```
name: ref<i18n.key>         → 类型与 i18n.key 相同 (String)
item_id: ref<item.config_id> → 类型与 item.config_id 相同
```

## 索引配置

### @key - 主键索引

生成 `Map<KeyType, Entity>` 和查询方法：

```kotlin
// 配置
@key(config_id)

// 生成的 API
fun Sheet.getStBitmap(configId: String): StBitmap?
fun Sheet.getAllStBitmap(): Map<String, StBitmap>
```

### @vkey - 虚拟键索引

额外的查询键，生成独立的 Map：

```kotlin
// 配置
@key(config_id)
@vkey(item_id)

// 生成的 API
fun Sheet.getStItem(configId: String): StItem?
fun Sheet.getStItemByItemId(itemId: Int): StItem?
```

### @akey - 数组键索引

一对多关系，生成 `Map<KeyType, List<Entity>>`：

```kotlin
// 配置
@akey(reward_id)

// 生成的 API
fun Sheet.getStRewards(rewardId: Int): List<StReward>
```

### @akey 复合键索引

使用 `subkey` 参数实现数组 + 子键组合，生成 `Map<Pair<AkeyType, SubkeyType>, Entity>`：

```kotlin
// 配置
@akey(quest_id, subkey=stage)

// 生成的 API
fun Sheet.getStQuest(questId: Int, stage: Int): StQuest?
```

## 其他配置

### @unique - 唯一性约束

定义字段唯一性约束（用于数据校验）：

```
@unique([config_id])           // 单字段唯一
@unique([guild_id, member_id])  // 组合字段唯一
```

## 字段定义

### 基本格式

```
<字段名>: <类型>  // <注释>
```

示例：
```
config_id: string       // 配置ID
level: int32            // 等级
exp: []int32            // 经验值列表
name: ref<i18n.key>     // 引用i18n.key，类型自动推断
```

## 预制数据

### @data - 预制数据块

在表定义中可以使用 `@data` 块定义预制数据，这些数据会与从 Excel 读取的数据合并。

#### 基本语法

```
table <表名> {
    // 字段定义
    field1: type1
    field2: type2

    // 预制数据
    @data
    value1, value2  // 第一行数据
    value3, value4  // 第二行数据
}
```

#### 数据格式规则

1. **CSV 格式**：每行一条记录，字段用逗号分隔
2. **字段顺序**：必须按照字段定义的顺序填写值
3. **注释**：支持行尾注释 `//`
4. **字符串**：简单字符串无需引号，包含逗号或特殊字符时使用引号
5. **数组**：使用方括号表示，如 `[1, 2, 3]`

#### 数据类型示例

```
// 基础类型
100           // int32
3.14          // float64
true          // bool
hello         // string（无特殊字符）
"hello,world" // string（包含逗号）

// 数组类型
[1, 2, 3]           // []int32
["a", "b", "c"]     // []string
[1.5, 2.5]          // []float64
```

## 设计原则

1. **声明式优于命令式**：只描述"是什么"，而非"怎么做"
2. **类型安全**：明确的类型系统，避免运行时错误
3. **可读性优先**：清晰的语法，易于理解和维护
4. **扩展性**：预留关联引用等高级特性的语法空间

## 未来扩展

- [ ] 枚举类型：`enum ItemType { WEAPON, ARMOR }`
- [ ] 嵌套对象：`reward: Reward { type, id, amount }`
- [ ] 字段验证：`@range(1, 100)`, `@regex("^[A-Z]+$")`
- [ ] 表继承：`table item extends base`
- [ ] 计算字段：`total: int32 = base + bonus`
