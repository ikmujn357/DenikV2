package com.example.denikv1.model.statistics

import android.content.Context
import com.example.denikv1.model.CestaModel
import com.github.mikephil.charting.data.BarEntry
import kotlinx.coroutines.runBlocking

// Rozhraní pro model statistik se sloupcovým grafem
interface AllStatisticsModel {
    fun getDataGraph(context: Context): List<BarEntry>
    fun getXLabelsGraph(context: Context): Array<String>
    fun getUniqueDifficulties(context: Context): List<String>
}

// Implementace rozhraní pro demonstraci dat
class AllStatisticsModelImpl(private val cestaModel: CestaModel) : AllStatisticsModel {

    //seřazení obtížností
    private val difficultyComparator = Comparator<String?> { difficulty1, difficulty2 ->
        val orderMap = mapOf(
            "3-" to 1, "3a" to 2, "3" to 3, "3a+" to 4, "3+" to 5, "3b" to 6,
            "4-" to 7, "3b+" to 8, "3c" to 9, "4" to 10, "3c+" to 11, "4a" to 12,
            "4+" to 13, "4a+" to 14, "4b" to 15, "5-" to 16, "4b+" to 17, "5" to 18,
            "4c" to 19, "4c+" to 20, "5+" to 21, "5a" to 22, "5a+" to 23, "6-" to 24,
            "5b" to 25, "5b+" to 26, "6" to 27, "5c" to 28, "5c+" to 29, "6+" to 30,
            "6a" to 31, "6a+" to 32, "7-" to 33, "6b" to 34, "7" to 35, "6b+" to 36,
            "7+" to 37, "6c" to 38, "8-" to 39, "6c+" to 40, "7a" to 41, "8" to 42,
            "7a+" to 43, "8+" to 44, "7b" to 45, "9-" to 46, "7b+" to 47, "9" to 48,
            "7c" to 49, "7c+" to 50, "9+" to 51, "8a" to 52, "10-" to 53, "8a+" to 54,
            "10" to 55, "8b" to 56, "10+" to 57, "8b+" to 58, "8c" to 59, "11-" to 60,
            "8c+" to 61, "11" to 62, "9a" to 63, "11+" to 64, "9a+" to 65, "12-" to 66,
            "9b" to 67, "12" to 68, "9b+" to 69, "12+" to 70, "9c" to 71
        )

        val order1 = orderMap[difficulty1] ?: 0
        val order2 = orderMap[difficulty2] ?: 0

        order1.compareTo(order2)
    }

    override fun getDataGraph(context: Context): List<BarEntry> {
        val allCesta = runBlocking { cestaModel.getAllCesta() }
        val distinctDifficulties = getUniqueDifficulties(context).sortedWith(difficultyComparator)

        val barEntries = distinctDifficulties.mapIndexed { index, difficulty ->
            val count = allCesta.count { it.gradeNum == difficulty }
            BarEntry(index.toFloat(), count.toFloat())
        }

        return barEntries
    }

    override fun getXLabelsGraph(context: Context): Array<String> {
        val labels = getUniqueDifficulties(context).sortedWith(difficultyComparator).toTypedArray()
        return arrayOf(*labels)
    }

    override fun getUniqueDifficulties(context: Context): List<String> {
        val allCesta = runBlocking { cestaModel.getAllCesta() }
        return allCesta
            .map { it.gradeNum }
            .filter { it != "x" } // Pokud "x" bylo označením pro chybějící hodnotu gradeNum, tento filtr by měl zůstat nezměněn.
            .distinct()
    }
}
