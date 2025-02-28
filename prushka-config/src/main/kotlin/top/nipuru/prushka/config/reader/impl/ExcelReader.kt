package top.nipuru.prushka.config.reader.impl

import cn.idev.excel.FastExcel
import cn.idev.excel.context.AnalysisContext
import cn.idev.excel.read.listener.ReadListener
import top.nipuru.prushka.config.reader.IReader
import java.io.File


class ExcelReader : IReader {

    override val extension: List<String> = listOf("xls", "xlsx")

    override fun <T> readFile(file: File, targetType: Class<T>): List<T> {
        val back = mutableListOf<T>()
        FastExcel.read(file, targetType, object : ReadListener<T> {
            override fun invoke(data: T, context: AnalysisContext) {
                back.add(data)
            }
            override fun doAfterAllAnalysed(context: AnalysisContext) {}
        }).doReadAll()
        return back
    }

}
