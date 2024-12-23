package top.nipuru.prushka.common.message

import java.io.Serializable


/**
 * @author Nipuru
 * @since 2024/11/28 10:07
 */

/**
 * 时间通知
 */
class DebugTimeNotify(val time: Long) : Serializable

/**
 * 查询时间
 */
class GetTimeRequest : Serializable