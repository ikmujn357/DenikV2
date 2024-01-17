package com.example.denikv1.controller.statistics

import android.content.Context
import com.example.denikv1.model.statistics.AllStatisticsModel
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint

interface AllStatisticsController {
    fun getDataGraph(context: Context): BarGraphSeries<DataPoint>
    fun getXLabelsGraph(context: Context): Array<String>
    fun getUniqueDifficulties(context: Context): List<String>
}

// implementace kontroléru pro celkové statistiky
class AllStatisticsControllerImpl(
    private val model: AllStatisticsModel
) : AllStatisticsController {

    override fun getDataGraph(context: Context): BarGraphSeries<DataPoint> {
        return model.getDataGraph(context)
    }

    override fun getXLabelsGraph(context: Context): Array<String> {
        return model.getXLabelsGraph(context)
    }

    override fun getUniqueDifficulties(context: Context): List<String> {
        return model.getUniqueDifficulties(context)
    }
}
