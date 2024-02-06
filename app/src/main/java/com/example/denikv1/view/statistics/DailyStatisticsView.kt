package com.example.denikv1.view.statistics

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.EventDay
import com.example.denikv1.R
import com.example.denikv1.controller.statistics.DailyStatisticsController
import com.example.denikv1.controller.statistics.DailyStatisticsControllerImpl
import com.example.denikv1.custom.CestaAdapter
import com.example.denikv1.model.CestaEntity
import com.example.denikv1.model.CestaModel
import com.example.denikv1.model.CestaModelImpl
import com.example.denikv1.model.statistics.DailyStatisticsModelImpl
import com.example.denikv1.view.AddActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.launch
import java.util.Calendar

interface DailyStatisticsView {
    fun updateGraph1(barEntries: List<BarEntry>, startDate: Long, endDate: Long)
    fun updateGraph2(barEntries: List<BarEntry>, startDate: Long, endDate: Long)
}

class DailyStatisticsFragment : Fragment(), DailyStatisticsView {
    private lateinit var controller: DailyStatisticsController
    private lateinit var cestaModel: CestaModel
    private lateinit var calendarView: CalendarView
    private lateinit var barChart1: BarChart
    private lateinit var barChart2: BarChart

    private lateinit var layoutObtiznost: LinearLayout
    private lateinit var layoutStylPrelezu: LinearLayout // Added the missing type

    private val calendar: Calendar = Calendar.getInstance()

    private lateinit var recyclerViewRoutes: RecyclerView

    private val events: MutableList<EventDay> = ArrayList()

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
        recyclerViewRoutes = view.findViewById(R.id.recyclerViewRoutesDailyStats)

        calendarView = view.findViewById(R.id.calendarView)
        // Sample events
        val eventCalendar = Calendar.getInstance()
        eventCalendar.add(Calendar.DAY_OF_MONTH, 1)

        // Set events to CalendarView
        calendarView.setEvents(events)

        // Initialize views
        layoutObtiznost = view.findViewById(R.id.layout_obtiznost)
        layoutStylPrelezu = view.findViewById(R.id.layout_styl_prelezu)

        calendarView.setOnDayClickListener(object : com.applandeo.materialcalendarview.listeners.OnDayClickListener {
            override fun onDayClick(eventDay: EventDay) {
                val selectedDate = eventDay.calendar.time
                val startDate = Calendar.getInstance()
                startDate.time = selectedDate
                startDate.set(Calendar.HOUR_OF_DAY, 0)
                startDate.set(Calendar.MINUTE, 0)
                startDate.set(Calendar.SECOND, 0)

                val endDate = Calendar.getInstance()
                endDate.time = selectedDate
                endDate.set(Calendar.HOUR_OF_DAY, 23)
                endDate.set(Calendar.MINUTE, 59)
                endDate.set(Calendar.SECOND, 59)

                (controller as DailyStatisticsControllerImpl).onDateChanged(
                    startDate.get(Calendar.YEAR),
                    startDate.get(Calendar.MONTH),
                    startDate.get(Calendar.DAY_OF_MONTH)
                )
            }
        })

        // Set initial date for the controller
        (controller as DailyStatisticsControllerImpl).onDateChanged(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Nastavení aktivních a neaktivních dnů
        lifecycleScope.launch {
            val disabledDays = getDisabledDays()
            calendarView.setDisabledDays(disabledDays)
        }

        return view
    }

    private suspend fun getDisabledDays(): List<Calendar> {
        val startDate = Calendar.getInstance()
        startDate.set(2024, Calendar.FEBRUARY, 1)
        val endDate = Calendar.getInstance()
        endDate.set(2024, Calendar.FEBRUARY, 29)

        val disabledDays = mutableListOf<Calendar>()

        while (startDate.before(endDate)) {
            if (!hasRouteOnDay(startDate)) {
                // Přidat do seznamu neaktivních dnů
                disabledDays.add(startDate.clone() as Calendar)
            }
            startDate.add(Calendar.DAY_OF_MONTH, 1)
        }
        return disabledDays
    }

    private suspend fun hasRouteOnDay(date: Calendar): Boolean {
        val startDate = date.clone() as Calendar
        startDate.set(Calendar.HOUR_OF_DAY, 0)
        startDate.set(Calendar.MINUTE, 0)
        startDate.set(Calendar.SECOND, 0)

        val endDate = date.clone() as Calendar
        endDate.set(Calendar.HOUR_OF_DAY, 23)
        endDate.set(Calendar.MINUTE, 59)
        endDate.set(Calendar.SECOND, 59)

        return cestaModel.hasRouteInDateRange(startDate.timeInMillis, endDate.timeInMillis)
    }

    override fun updateGraph1(barEntries: List<BarEntry>, startDate: Long, endDate: Long) {
        val xLabels = controller.getXLabelsGraph1(requireContext(), startDate, endDate).toList()
        setupBarChart(barChart1, barEntries, xLabels)

        if (barEntries.isNotEmpty()) {
            layoutObtiznost.visibility = View.VISIBLE
            updateRecyclerViewCesty(startDate, endDate)
        } else {
            layoutObtiznost.visibility = View.GONE
            recyclerViewRoutes.adapter = null
        }
    }

    override fun updateGraph2(barEntries: List<BarEntry>, startDate: Long, endDate: Long) {
        val xLabels = controller.getXLabelsGraph2(requireContext(), startDate, endDate).toList()
        setupBarChart(barChart2, barEntries, xLabels)

        if (barEntries.isNotEmpty()) {
            layoutStylPrelezu.visibility = View.VISIBLE
            updateRecyclerViewCesty(startDate, endDate)
        } else {
            layoutStylPrelezu.visibility = View.GONE
            recyclerViewRoutes.adapter = null
        }
    }

    private fun updateRecyclerViewCesty(startDate: Long, endDate: Long) {
        lifecycleScope.launch {
            try {
                val cesty = cestaModel.getAllCestaForDateRange(startDate, endDate)
                setupRecyclerViewCesty(cesty)
            } catch (e: Exception) {
                Log.e("DailyStatisticsFragment", "Nelze načíst cesty", e)
            }
        }
    }

    private fun setupRecyclerViewCesty(cesty: List<CestaEntity>) {
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerViewRoutes.layoutManager = layoutManager
        recyclerViewRoutes.adapter = CestaAdapter(cesty) { cestaId ->
            val intent = Intent(requireContext(), AddActivity::class.java)
            intent.putExtra("cestaId", cestaId)
            startActivity(intent)
        }
    }

    private fun setupBarChart(barChart: BarChart, barEntries: List<BarEntry>, xLabels: List<String>) {
        val barDataSet = BarDataSet(barEntries, null)

        barDataSet.color = ContextCompat.getColor(requireContext(), R.color.purple_700)

        barChart.data = BarData(barDataSet)

        barDataSet.valueFormatter = object : ValueFormatter() {
            override fun getBarLabel(barEntry: BarEntry?): String {
                return barEntry?.y?.toInt().toString()
            }
        }

        barDataSet.valueTextSize = 10f
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
        yAxis.textSize = 12f

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