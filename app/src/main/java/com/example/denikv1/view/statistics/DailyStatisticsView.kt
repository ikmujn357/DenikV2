package com.example.denikv1.view.statistics

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.denikv1.R
import com.example.denikv1.controller.statistics.DailyStatisticsController
import com.example.denikv1.controller.statistics.DailyStatisticsControllerImpl
import com.example.denikv1.model.CestaModel
import com.example.denikv1.model.CestaModelImpl
import com.example.denikv1.model.statistics.DailyStatisticsModelImpl
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.util.Calendar

// Rozhraní
// Rozhraní
interface DailyStatisticsView {
    fun updateGraph1(barEntries: List<BarEntry>, startDate: Long, endDate: Long)
    fun updateGraph2(barEntries: List<BarEntry>, startDate: Long, endDate: Long)
}


// Třída DailyStatisticsFragment implementující rozhraní
class DailyStatisticsFragment : Fragment(), DailyStatisticsView {
    private lateinit var controller: DailyStatisticsController
    private lateinit var cestaModel: CestaModel
    private lateinit var calendarView: CalendarView
    private lateinit var barChart1: BarChart
    private lateinit var barChart2: BarChart

    private val calendar: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.denni, container, false)
        cestaModel = CestaModelImpl(requireContext())
        val statisticsModel = DailyStatisticsModelImpl(cestaModel)
        controller = DailyStatisticsControllerImpl(this, statisticsModel)

        // Inicializace pohledů
        barChart1 = view.findViewById(R.id.graph_obtiznost_denni)
        barChart2 = view.findViewById(R.id.graph_styl_prelezu)
        calendarView = view.findViewById(R.id.calendarView)

        // Nastavení Listener pro změnu data v CalendarView
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            (controller as DailyStatisticsControllerImpl).onDateChanged(year, month, dayOfMonth)
        }

        (controller as DailyStatisticsControllerImpl).onDateChanged(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        return view
    }

    // Aktualizace prvního grafu
    override fun updateGraph1(barEntries: List<BarEntry>, startDate: Long, endDate: Long) {
        val xLabels = controller.getXLabelsGraph1(requireContext(), startDate, endDate).toList()
        setupBarChart(barChart1, barEntries, xLabels)
    }

    // Aktualizace druhého grafu
    override fun updateGraph2(barEntries: List<BarEntry>, startDate: Long, endDate: Long) {
        val xLabels = controller.getXLabelsGraph2(requireContext(), startDate, endDate).toList()
        setupBarChart(barChart2, barEntries, xLabels)
    }

    private fun setupBarChart(barChart: BarChart, barEntries: List<BarEntry>, xLabels: List<String>) {
        val barDataSet = BarDataSet(barEntries, null)

        barDataSet.color = ContextCompat.getColor(requireContext(), R.color.purple_700)

        barChart.data = BarData(barDataSet)

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawAxisLine(true)
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)

        // Nastavení popisků osy X pod každým sloupcem
        xAxis.valueFormatter = IndexAxisValueFormatter(xLabels)
        xAxis.labelCount = xLabels.size

        val yAxis = barChart.axisLeft
        yAxis.axisMinimum = 0f
        yAxis.granularity = 1f
        yAxis.setDrawGridLines(false)



        // Customize other properties as needed
        barChart.isHighlightPerDragEnabled = false
        barChart.isHighlightPerTapEnabled = false
        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        barChart.isDoubleTapToZoomEnabled = false
        barChart.isScaleYEnabled = false
        barChart.isScaleXEnabled = false
        barChart.isClickable = false
        barChart.legend.isEnabled = false
        barChart.invalidate()
    }

}
