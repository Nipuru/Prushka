package server.bukkit.time

import server.bukkit.constant.DAY
import server.common.logger.Logger
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.*


object TimeManager {
    /** 当前的时间戳（毫秒） */
    var now: Long = 0
        private set
    /** 本日 0:00 的时间戳（毫秒） */
    var dayZero: Long = 0
        private set
    /** 周一 0:00 的时间戳（毫秒） */
    var weekZero: Long = 0
        private set
    /** 本月1号 0:00 的时间戳（毫秒） */
    var monthZero: Long = 0
        private set
    /** 当前的时间戳 用于改时间 */
    var debugTime: Long = 0
    /** 新的一天钩子函数（实在不想用event） */
    var newDayFunc: (Long) -> Unit = {}

    private lateinit var timer: Timer

    fun init() {
        setTime(debugTime)
        timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (debugTime != now) {
                    setTime(debugTime)
                }
                val nextDay = now + DAY
                now += 1
                debugTime = now
                if (now >= nextDay) {
                    Logger.info("set dayZero: $nextDay")
                    dayZero = nextDay
                    val instant = Instant.ofEpochMilli(now)
                    val dateTime = instant.atZone(ZoneId.systemDefault())
                    if (dateTime.dayOfWeek == DayOfWeek.MONDAY) {
                        Logger.info("set weekZero: $nextDay")
                        weekZero = nextDay
                    }
                    if (dateTime.dayOfMonth == 1) {
                        Logger.info("set monthZero: $nextDay")
                        monthZero = nextDay
                    }
                    newDayFunc.invoke(nextDay)
                }
            }
        }, 0, 1000)
    }

    fun cancel() {
        if (::timer.isInitialized) {
            timer.cancel()
        }
    }

    private fun setTime(time: Long) {
        var debugTime = time
        if (debugTime == 0L) {
            if (now == 0L) {
                // 第一次设置
                debugTime = System.currentTimeMillis()
            } else {
                // 后续就不管了
                return
            }
        }
        now = debugTime
        var dayZero = dayZero
        val instant = Instant.ofEpochMilli(now)
        val dateTime = instant.atZone(ZoneId.systemDefault())

        val newDayZero = dateTime
            .toLocalDate().atStartOfDay(dateTime.zone).toInstant().toEpochMilli()
        val newWeekZero = dateTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .toLocalDate().atStartOfDay(dateTime.zone).toInstant().toEpochMilli()
        val newMonthZero = dateTime.withDayOfMonth(1)
            .toLocalDate().atStartOfDay(dateTime.zone).toInstant().toEpochMilli()

        if (dayZero < newDayZero) {
            dayZero += DAY
            newDayFunc.invoke(dayZero)
        }
        this.dayZero = newDayZero
        weekZero = newWeekZero
        monthZero = newMonthZero

        Logger.info("Server time changed to ${DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(dateTime)}")
    }

}