package com.example.denikv1.view.statistics

import android.content.Intent
import android.graphics.Color
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
import com.example.denikv1.R
import com.example.denikv1.controller.statistics.DailyStatisticsController
import com.example.denikv1.controller.statistics.DailyStatisticsControllerImpl
import com.example.denikv1.custom.CestaAdapter
import com.example.denikv1.custom.customCalendar.date.DatePickerDialog
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

class DailyStatisticsFragment : Fragment(), DailyStatisticsView, DatePickerDialog.OnDateSetListener {
    private lateinit var controller: DailyStatisticsController
    private lateinit var cestaModel: CestaModel
    private lateinit var barChart1: BarChart
    private lateinit var barChart2: BarChart

    private lateinit var layoutObtiznost: LinearLayout
    private lateinit var layoutStylPrelezu: LinearLayout

    private lateinit var recyclerViewRoutes: RecyclerView

    private lateinit var dpd: DatePickerDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.denni, container, false)
        cestaModel = CestaModelImpl(requireContext())
        val statisticsModel = DailyStatisticsModelImpl(cestaModel)
        controller = DailyStatisticsControllerImpl(this, statisticsModel)

        // Initialize views
        barChart1 = view.findViewById(R.id.graph_obtiznost_denni)
        barChart2 = view.findViewById(R.id.graph_styl_prelezu)
        recyclerViewRoutes = view.findViewById(R.id.recyclerViewRoutesDailyStats)
        layoutObtiznost = view.findViewById(R.id.layout_obtiznost)
        layoutStylPrelezu = view.findViewById(R.id.layout_styl_prelezu)

        // Initialize the date picker dialog
        initializeDatePicker()

        showDatePicker()

        return view
    }

    // Method to initialize the date picker dialog
    private fun initializeDatePicker() {
        val now: Calendar = Calendar.getInstance()
        dpd = DatePickerDialog.newInstance(
            this,
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        )

        dpd.setThemeDark(false)
        dpd.accentColor = Color.parseColor("#9C27B0")

        val days = Array<Calendar>(13) { Calendar.getInstance() }
        for (i in -6..6) {
            val day: Calendar = Calendar.getInstance()
            day.add(Calendar.DAY_OF_MONTH, i * 2)
            days[i + 6] = day
        }
        dpd.setSelectableDays(days)

        dpd.setOnCancelListener {
            Log.d("DatePickerDialog", "Dialog was cancelled")
        }

        //dpd.onDateSetListener = this
    }

    // Method to show the date picker dialog
    private fun showDatePicker() {
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.datePickerContainer, dpd)
            ?.addToBackStack(null)
            ?.commit()
    }

    override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val selectedDate = Calendar.getInstance()
        selectedDate.set(Calendar.YEAR, year)
        selectedDate.set(Calendar.MONTH, monthOfYear)
        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)

        val startDate = selectedDate.clone() as Calendar
        startDate.set(Calendar.HOUR_OF_DAY, 0)
        startDate.set(Calendar.MINUTE, 0)
        startDate.set(Calendar.SECOND, 0)

        val endDate = selectedDate.clone() as Calendar
        endDate.set(Calendar.HOUR_OF_DAY, 23)
        endDate.set(Calendar.MINUTE, 59)
        endDate.set(Calendar.SECOND, 59)

        (controller as DailyStatisticsControllerImpl).onDateChanged(
            startDate.get(Calendar.YEAR),
            startDate.get(Calendar.MONTH),
            startDate.get(Calendar.DAY_OF_MONTH)
        )
    }

    override fun updateGraph1(barEntries: List<BarEntry>, startDate: Long, endDate: Long) {
        val xLabels = controller.getXLabelsGraph1(requireContext(), startDate, endDate).toList()
        setupBarChart(barChart1, barEntries, xLabels)

        if (barEntries.isNotEmpty()) {
            layoutObtiznost.visibility = View.VISIBLE
            lifecycleScope.launch {
                val cesty = cestaModel.getAllCestaForDateRange(startDate, endDate)
                updateRecyclerViewCesty(cesty)
            }
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
            lifecycleScope.launch {
                val cesty = cestaModel.getAllCestaForDateRange(startDate, endDate)
                updateRecyclerViewCesty(cesty)
            }
        } else {
            layoutStylPrelezu.visibility = View.GONE
            recyclerViewRoutes.adapter = null
        }
    }

    private fun updateRecyclerViewCesty(cesty: List<CestaEntity>) {
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
