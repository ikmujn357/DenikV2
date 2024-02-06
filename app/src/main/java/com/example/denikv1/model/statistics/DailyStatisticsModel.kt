package com.example.denikv1.model.statistics

import android.content.Context
import com.example.denikv1.model.CestaEntity
import com.example.denikv1.model.CestaModel
import com.github.mikephil.charting.data.BarEntry
import kotlinx.coroutines.runBlocking

// Interface for the model handling daily statistics
interface DailyStatisticsModel {
    fun getDataGraph1(context: Context, startDate: Long, endDate: Long): List<BarEntry>
    fun getXLabelsGraph1(context: Context, startDate: Long, endDate: Long): Array<String>
    fun getDataByDate(startDate: Long, endDate: Long): List<CestaEntity>
    fun getUniqueDifficulty(context: Context, startDate: Long, endDate: Long): List<String>
    fun getUniqueStyle(context: Context, startDate: Long, endDate: Long): List<String>
    fun getDataGraph2(context: Context, startDate: Long, endDate: Long): List<BarEntry>
    fun getXLabelsGraph2(context: Context, startDate: Long, endDate: Long): Array<String>
}

// Implementation of the model for daily statistics
class DailyStatisticsModelImpl(private val cestaModel: CestaModel) : DailyStatisticsModel {
    override fun getDataByDate(startDate: Long, endDate: Long): List<CestaEntity> {
        return runBlocking { cestaModel.getAllCestaForDateRange(startDate, endDate) }
    }

    override fun getUniqueDifficulty(context: Context, startDate: Long, endDate: Long): List<String> {
        val allCesta = getDataByDate(startDate, endDate)
        return allCesta.map { it.gradeNum }.filter { it != "x" }.distinct()
    }

    override fun getUniqueStyle(context: Context, startDate: Long, endDate: Long): List<String> {
        val allCesta = getDataByDate(startDate, endDate)
        return allCesta.map { it.climbStyle }.distinct()
    }

    private val difficultyComparator = Comparator<String> { difficulty1, difficulty2 ->
        val orderMap = mapOf(
            "3-" to 1, "3" to 2, "3+" to 3,
            "4-" to 4, "4" to 5, "4+" to 6, "5-" to 7, "5" to 8, "5+" to 9,
            "6-" to 10, "6" to 11, "6+" to 12, "7-" to 13, "7" to 14, "7+" to 15,
            "8-" to 16, "8" to 17, "8+" to 18, "9-" to 19, "9" to 20, "9+" to 21,
            "10-" to 22, "10" to 23, "10+" to 24, "11-" to 25, "11" to 26,
            "11+" to 27, "12-" to 28, "12" to 29, "12+" to 30
        )
        val order1 = orderMap[difficulty1] ?: 0
        val order2 = orderMap[difficulty2] ?: 0

        order1.compareTo(order2)
    }

    private val climbingStyleComparator = Comparator<String> { style1, style2 ->
        val orderMap = mapOf(
            "Flash" to 1, "On sight" to 2, "Top rope" to 3,
            "Red point" to 4, "Pink point" to 5, "All free" to 6
        )

        orderMap[style1]!!.compareTo(orderMap[style2]!!)
    }

    override fun getDataGraph1(context: Context, startDate: Long, endDate: Long): List<BarEntry> {
        val allCesta = getDataByDate(startDate, endDate)
        val distinctDifficulties = getUniqueDifficulty(context, startDate, endDate)
            .sortedWith(difficultyComparator)
            .filter { it != "x" }

        val entries = distinctDifficulties.mapIndexed { index, difficulty ->
            val count = allCesta.count { it.gradeNum == difficulty }
            BarEntry(index.toFloat(), count.toFloat())
        }

        return entries
    }

    override fun getXLabelsGraph1(context: Context, startDate: Long, endDate: Long): Array<String> {
        val labels = getUniqueDifficulty(context, startDate, endDate)
            .sortedWith(difficultyComparator)
            .filter { it != "x" }
            .toTypedArray()
        return arrayOf(*labels, "")
    }

    override fun getDataGraph2(context: Context, startDate: Long, endDate: Long): List<BarEntry> {
        val allCesta = getDataByDate(startDate, endDate)
        val distinctStyle = getUniqueStyle(context, startDate, endDate).sortedWith(climbingStyleComparator)

        val entries = distinctStyle.mapIndexed { index, style ->
            val count = allCesta.count { it.climbStyle == style }
            BarEntry(index.toFloat(), count.toFloat())
        }

        return entries
    }

    override fun getXLabelsGraph2(context: Context, startDate: Long, endDate: Long): Array<String> {
        val labels = getUniqueStyle(context, startDate, endDate)
            .sortedWith(climbingStyleComparator)
            .toTypedArray()
        return arrayOf(*labels, "")
    }


}
