// This file is auto-generated. DO NOT EDIT.
// Generated by tool
package top.nipuru.prushka.common.sheet

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

lateinit var stRewardAMap: Map<Int/* rewardId */, List<StReward>>
    private set

data class StReward(
    /** 配置id */ 
    val rewardId: Int,
    /** 类型 */ 
    val type: Int,
    /** 物品id */ 
    val id: Int,
    /** 数量 */
    val num: Int,
)

internal fun loadStReward(gson: Gson, tablePath: String) {
    val jsonFile = File(tablePath, "st_reward.json")
    val jsonString = jsonFile.readText()
    val type = object : TypeToken<List<StReward>>() {}.type
    val list = gson.fromJson<List<StReward>>(jsonString, type)
    val aMap = mutableMapOf<Int, MutableList<StReward>>()
    for (data in list) {
        aMap.getOrPut(data.rewardId) { mutableListOf() }.add(data)
    }
    stRewardAMap = aMap
}