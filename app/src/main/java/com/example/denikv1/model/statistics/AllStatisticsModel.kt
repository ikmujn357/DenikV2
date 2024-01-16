package com.example.denikv1

import android.content.Context
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import kotlinx.coroutines.runBlocking

// Rozhraní pro model statistik se sloupcovým grafem
interface AllStatisticsModel {
    fun getDataGraph(context: Context): BarGraphSeries<DataPoint>
    fun getXLabelsGraph(context: Context): Array<String>
    fun getUniqueDifficulties(context: Context): List<String>
}

// Implementace rozhraní pro demonstraci dat
class AllStatisticsModelImpl(private val cestaModel: CestaModel) : AllStatisticsModel {

    //seřazení obtížností
    private val difficultyComparator = Comparator<String?> { difficulty1, difficulty2 ->
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

    override fun getDataGraph(context: Context): BarGraphSeries<DataPoint> {
        val allCesta = runBlocking { cestaModel.getAllCesta() }
        val distinctDifficulties = getUniqueDifficulties(context).sortedWith(difficultyComparator)

        val dataPoints = distinctDifficulties.mapIndexed { index, difficulty ->
            val count = allCesta.count { it.gradeNum + it.gradeSign == difficulty }
            DataPoint(index + 1.0, count.toDouble())
        }.toTypedArray()

        return BarGraphSeries(dataPoints)
    }

    override fun getXLabelsGraph(context: Context): Array<String> {
        val labels = getUniqueDifficulties(context).sortedWith(difficultyComparator).toTypedArray()
        return arrayOf(*labels, "")
    }

    override fun getUniqueDifficulties(context: Context): List<String> {
        val allCesta = runBlocking { cestaModel.getAllCesta() }
        return allCesta
            .map { it.gradeNum + it.gradeSign }
            .filter { it != "x" }
            .distinct()
    }
}
