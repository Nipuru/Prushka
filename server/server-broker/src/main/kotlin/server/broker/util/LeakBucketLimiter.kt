package server.broker.util

import kotlin.math.max

class LeakBucketLimiter(private val leaksPerSecond: Double, capacity: Int) {
    private val capacity = capacity.toDouble()

    private var water = 0.0

    private var lastOutTime = System.currentTimeMillis()

    @get:Synchronized
    val isLimit: Boolean
        get() {
            if (water == 0.0) {
                lastOutTime = System.currentTimeMillis()
                water += 1.0
                return false
            }
            // 执行漏水
            val waterLeaked = ((System.currentTimeMillis() - lastOutTime) * leaksPerSecond / 1000)
            val waterLeft = water - waterLeaked
            water = max(0.0, waterLeft)
            lastOutTime = System.currentTimeMillis()
            if (water < capacity) {
                water += 1.0
                return false
            } else {
                return true
            }
        }
}
