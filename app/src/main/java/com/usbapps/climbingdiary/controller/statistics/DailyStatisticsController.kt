package com.usbapps.climbingdiary.denikv1.controller.statistics

import android.content.Context
import com.usbapps.climbingdiary.denikv1.model.statistics.DailyStatisticsModel
import com.usbapps.climbingdiary.denikv1.view.statistics.DailyStatisticsFragment
import com.github.mikephil.charting.data.BarEntry
import java.util.Calendar

/**
 * Rozhraní pro kontrolér denních statistik.
 * Definuje metody pro získání dat a popisků osy X pro Graf1 a Graf2,
 * a také metodu pro zpracování změny data.
 */
interface DailyStatisticsController {
    fun getDataGraph1(context: Context, startDate: Long, endDate: Long = startDate):List<BarEntry>
    fun getXLabelsGraph1(context: Context, startDate: Long, endDate: Long): Array<String>
    fun getDataGraph2(context: Context, startDate: Long, endDate: Long = startDate): List<BarEntry>
    fun getXLabelsGraph2(context: Context, startDate: Long, endDate: Long): Array<String>
    fun onDateChanged(year: Int, month: Int, dayOfMonth: Int)
}

/**
 * Implementace kontroléru pro denní statistiky.
 * Tato třída se stará o řízení interakcí mezi pohledem (fragmentem) a modelem pro denní statistiky.
 *
 * @param view Reference na pohled (fragment), který tento kontrolér ovládá.
 * @param model Reference na model, který poskytuje data a operace pro zobrazení denních statistik.
 */
class DailyStatisticsControllerImpl(
    private val view: DailyStatisticsFragment,
    private val model: DailyStatisticsModel
) : DailyStatisticsController {
    private val calendar: Calendar = Calendar.getInstance()

    /**
     * Funkce getDataGraph1 načte data pro první graf.
     *
     * @param context aktuální kontext aplikace
     * @param startDate počáteční datum datového rozsahu
     * @param endDate koncové datum datového rozsahu
     * @return seznam objektů BarEntry obsahující data pro graf
     */
    override fun getDataGraph1(
        context: Context,
        startDate: Long,
        endDate: Long
    ): List<BarEntry> {
        return model.getDataGraph1(context, startDate, endDate)
    }

    /**
     * Vrací pole řetězců představujících popisky osy X pro Graf1.
     * Popisky jsou generovány na základě poskytnutých data počátku a konce.
     *
     * @param context Kontext aplikace.
     * @param startDate Počáteční datum v milisekundách od epochy.
     * @param endDate Konečné datum v milisekundách od epochy.
     * @return Pole řetězců představujících popisky osy X.
     */
    override fun getXLabelsGraph1(context: Context, startDate: Long, endDate: Long): Array<String> {
        return model.getXLabelsGraph1(context, startDate, endDate)
    }

    /**
     * Override funkce pro získání dat pro Graf2.
     * Tato funkce získává seznam BarEntry, který představuje data pro vykreslení Graf2.
     * Data jsou získána z modelu na základě poskytnutých data počátku a konce.
     *
     * @param context Kontext aplikace.
     * @param startDate Počáteční datum v milisekundách od epochy.
     * @param endDate Konečné datum v milisekundách od epochy.
     * @return Seznam BarEntry představujících data pro Graf2.
     */
    override fun getDataGraph2(
        context: Context,
        startDate: Long,
        endDate: Long
    ): List<BarEntry> {
        return model.getDataGraph2(context, startDate, endDate)
    }

    /**
     * Přepisuje metodu pro získání popisků osy X pro Graf2.
     * Tato metoda získává pole řetězců představujících popisky osy X pro Graf2.
     * Popisky jsou generovány z modelu na základě poskytnutých data počátku a konce.
     *
     * @param context Kontext aplikace.
     * @param startDate Počáteční datum v milisekundách od epochy.
     * @param endDate Konečné datum v milisekundách od epochy.
     * @return Pole řetězců představujících popisky osy X pro Graf2.
     */
    override fun getXLabelsGraph2(context: Context, startDate: Long, endDate: Long): Array<String> {
        return model.getXLabelsGraph2(context, startDate, endDate)
    }

    /**
     * Metoda volaná při změně data.
     * Aktualizuje vybrané datum a načte a zobrazí statistiky pro dané datum.
     *
     * @param year Rok.
     * @param month Měsíc (0-11).
     * @param dayOfMonth Den v měsíci (1-31).
     */
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

    /**
     * Soukromá metoda pro načtení a zobrazení statistik pro zadané období.
     * Načte data pro Graf1 pomocí modelu a aktualizuje zobrazení grafu ve view.
     *
     * @param startDate Počáteční datum v milisekundách od epochy.
     * @param endDate Konečné datum v milisekundách od epochy.
     */
    private fun loadAndDisplayStatistics(startDate: Long, endDate: Long) {
        val barEntries1 = model.getDataGraph1(view.requireContext(), startDate, endDate)

        view.updateGraph(barEntries1, startDate, endDate)
    }


}