package com.example.denikv1

import android.content.Context
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Calendar

// Rozhraní pro kontrolér denních statistik
interface DailyStatisticsController {
    // Metoda pro získání dat pro první sloupcový graf
    fun getDataGraph1(context: Context, startDate: Long, endDate: Long = startDate): BarGraphSeries<DataPoint>

    // Metoda pro získání osy X pro první sloupcový graf
    fun getXLabelsGraph1(context: Context, startDate: Long, endDate: Long): Array<String>

    // Metoda pro získání dat pro druhý sloupcový graf
    fun getDataGraph2(context: Context, startDate: Long, endDate: Long = startDate): BarGraphSeries<DataPoint>

    // Metoda pro získání osy X pro druhý sloupcový graf
    fun getXLabelsGraph2(context: Context, startDate: Long, endDate: Long): Array<String>

    // Metoda volaná při změně data v CalendarView
    fun onDateChanged(year: Int, month: Int, dayOfMonth: Int)
}

// Implementace rozhraní pro kontrolér denních statistik
class DailyStatisticsControllerImpl(
    private val view: DailyStatisticsFragment,
    private val model: DailyStatisticsModel
) : DailyStatisticsController {
    private val calendar: Calendar = Calendar.getInstance()

    // Metoda pro získání dat pro první sloupcový graf
    override fun getDataGraph1(
        context: Context,
        startDate: Long,
        endDate: Long
    ): BarGraphSeries<DataPoint> {
        return model.getDataGraph1(context, startDate, endDate)
    }

    // Metoda pro získání osy X pro první sloupcový graf
    override fun getXLabelsGraph1(context: Context, startDate: Long, endDate: Long): Array<String> {
        return model.getXLabelsGraph1(context,startDate, endDate)
    }

    // Metoda pro získání dat pro druhý sloupcový graf
    override fun getDataGraph2(
        context: Context,
        startDate: Long,
        endDate: Long
    ): BarGraphSeries<DataPoint> {
        return model.getDataGraph2(context, startDate, endDate)
    }

    // Metoda pro získání osy X pro první sloupcový graf
    override fun getXLabelsGraph2(context: Context, startDate: Long, endDate: Long): Array<String> {
        return model.getXLabelsGraph2(context,startDate, endDate)
    }

    // Metoda volaná při změně data v CalendarView
    override fun onDateChanged(year: Int, month: Int, dayOfMonth: Int) {
        // Nastavení začátku a konce vybraného dne
        val selectedDateStart = calendar.apply {
            set(year, month, dayOfMonth, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val selectedDateEnd = calendar.apply {
            set(year, month, dayOfMonth, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        // Načtení a zobrazení statistik pro vybraný den
        loadAndDisplayStatistics(selectedDateStart, selectedDateEnd)
    }

    // Privátní metoda pro načtení a zobrazení statistik pro vybraný den
    private fun loadAndDisplayStatistics(startDate: Long, endDate: Long) {
        // Získání dat o cestách pro vybraný den
        val series1 = model.getDataGraph1(view.requireContext(), startDate, endDate)
        val series2 = model.getDataGraph2(view.requireContext(), startDate, endDate)

        // Aktualizace grafu v přidruženém pohledu (fragmentu)
        view.updateGraph1(series1,startDate,endDate)
        view.updateGraph2(series2,startDate,endDate)
    }
}
