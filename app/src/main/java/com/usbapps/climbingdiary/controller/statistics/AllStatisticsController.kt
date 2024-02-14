package com.usbapps.climbingdiary.denikv1.controller.statistics

import android.content.Context
import com.usbapps.climbingdiary.denikv1.model.statistics.AllStatisticsModel
import com.github.mikephil.charting.data.BarEntry

/**
 * Rozhraní pro kontrolér všech statistik.
 * Definuje metody pro získání dat a popisků osy X pro graf a pro získání jedinečných obtížností.
 */
interface AllStatisticsController {
    fun getDataGraph(context: Context): List<BarEntry>
    fun getXLabelsGraph(context: Context): Array<String>
    fun getUniqueDifficulties(context: Context): List<String>
}

/**
 * Implementace kontroléru pro všechny statistiky.
 * Tato třída implementuje rozhraní AllStatisticsController a zajišťuje získání dat a manipulaci s nimi.
 *
 * @param model Reference na model, který poskytuje data a operace pro zobrazení všech statistik.
 */
class AllStatisticsControllerImpl(
    private val model: AllStatisticsModel
) : AllStatisticsController {

    /**
     * Přepisuje metodu pro získání dat pro graf.
     * Získává data pro graf pomocí modelu.
     *
     * @param context Kontext aplikace.
     * @return Seznam BarEntry představujících data pro graf.
     */
    override fun getDataGraph(context: Context): List<BarEntry> {
        return model.getDataGraph(context)
    }

    /**
     * Přepisuje metodu pro získání popisků osy X pro graf.
     * Získává popisky osy X pro graf pomocí modelu.
     *
     * @param context Kontext aplikace.
     * @return Pole řetězců představujících popisky osy X pro graf.
     */
    override fun getXLabelsGraph(context: Context): Array<String> {
        return model.getXLabelsGraph(context)
    }

    /**
     * Přepisuje metodu pro získání jedinečných obtížností.
     * Získává jedinečné obtížnosti pomocí modelu.
     *
     * @param context Kontext aplikace.
     * @return Seznam jedinečných obtížností.
     */
    override fun getUniqueDifficulties(context: Context): List<String> {
        return model.getUniqueDifficulties(context)
    }
}