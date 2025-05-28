package top.nipuru.prushka.common.message

import java.io.Serializable

class FragmentMessage(val formatterIdx: Int, val args: Array<out Serializable?>) : Serializable