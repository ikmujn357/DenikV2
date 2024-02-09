package com.example.denikv1.controller.statistics

import android.content.Context
import com.example.denikv1.model.statistics.DailyStatisticsModel
import com.example.denikv1.view.statistics.DailyStatisticsFragment
import com.github.mikephil.charting.data.BarEntry
import java.util.Calendar

// Rozhraní pro kontrolér denních statistik
interface DailyStatisticsController {
    fun getDataGraph1(context: Context, startDate: Long, endDate: Long = startDate):List<BarEntry>
    fun getXLabelsGraph1(context: Context, startDate: Long, endDate: Long): Array<String>
    fun getDataGraph2(context: Context, startDate: Long, endDate: Long = startDate): List<BarEntry>
    fun getXLabelsGraph2(context: Context, startDate: Long, endDate: Long): Array<String>
    fun onDateChanged(year: Int, month: Int, dayOfMonth: Int)
}

// Implementace rozhraní pro kontrolér denních statistik
class DailyStatisticsControllerImpl(
    private val view: DailyStatisticsFragment,
    private val model: DailyStatisticsModel
) : DailyStatisticsController {
    private val calendar: Calendar = Calendar.getInstance()

    override fun getDataGraph1(
        context: Context,
        startDate: Long,
        endDate: Long
    ): List<BarEntry> {
        return model.getDataGraph1(context, startDate, endDate)
    }

    override fun getXLabelsGraph1(context: Context, startDate: Long, endDate: Long): Array<String> {
        return model.getXLabelsGraph1(context, startDate, endDate)
    }

    override fun getDataGraph2(
        context: Context,
        startDate: Long,
        endDate: Long
    ): List<BarEntry> {
        return model.getDataGraph2(context, startDate, endDate)
    }

    override fun getXLabelsGraph2(context: Context, startDate: Long, endDate: Long): Array<String> {
        return model.getXLabelsGraph2(context, startDate, endDate)
    }

    override fun onDateChanged(year: Int, month: Int, dayOfMonth: Int) {
        val selectedDateStart = calendar.apply {
            set(year, month, dayOfMonth, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val selectedDateEnd = calendar.apply {
            set(year, month, dayOfMonth, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        loadAndDisplayStatistics(selectedDateStart, selectedDateEnd)
    }

    private fun loadAndDisplayStatistics(startDate: Long, endDate: Long) {
        val barEntries1 = model.getDataGraph1(view.requireContext(), startDate, endDate)

        view.updateGraph(barEntries1, startDate, endDate)
    }


}