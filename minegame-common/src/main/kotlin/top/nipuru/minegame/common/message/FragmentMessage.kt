package top.nipuru.minegame.common.message

import java.io.Serializable

class FragmentMessage(val formatterIdx: Int, val args: Array<out Serializable?>) : Serializable