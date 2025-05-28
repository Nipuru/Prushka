package top.nipuru.prushka.shared.time


/**
 * @author Nipuru
 * @since 2024/11/28 10:25
 */
object TimeManager {
    private var deltaTime: Int = 0

    fun now(): Long {
        return System.currentTimeMillis()
    }

    fun debugTime(): Long {
        return if (deltaTime == 0) {
            0
        } else {
            now()
        }
    }
}