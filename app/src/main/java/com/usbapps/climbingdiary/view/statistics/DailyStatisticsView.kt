package com.usbapps.climbingdiary.view.statistics

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.usbapps.climbingdiary.R
import com.usbapps.climbingdiary.controller.statistics.DailyStatisticsController
import com.usbapps.climbingdiary.controller.statistics.DailyStatisticsControllerImpl
import com.usbapps.climbingdiary.custom.CestaAdapter
import com.usbapps.climbingdiary.custom.customCalendar.date.DatePickerDialog
import com.usbapps.climbingdiary.model.CestaEntity
import com.usbapps.climbingdiary.model.CestaModel
import com.usbapps.climbingdiary.model.CestaModelImpl
import com.usbapps.climbingdiary.model.statistics.DailyStatisticsModelImpl
import com.usbapps.climbingdiary.view.AddActivity
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
    fun updateGraph(barEntries: List<BarEntry>, startDate: Long, endDate: Long)
}

class DailyStatisticsFragment : Fragment(), DailyStatisticsView, DatePickerDialog.OnDateSetListener {
    private lateinit var controller: DailyStatisticsController
    private lateinit var cestaModel: CestaModel
    private lateinit var barChart: BarChart
    private lateinit var layoutObtiznost: LinearLayout
    private lateinit var recyclerViewRoutes: RecyclerView
    private lateinit var dpd: DatePickerDialog
    private lateinit var buttonObtiznost: Button
    private lateinit var buttonStylPrelzeu: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.denni, container, false)
        cestaModel = CestaModelImpl(requireContext())
        val statisticsModel = DailyStatisticsModelImpl(cestaModel)
        controller = DailyStatisticsControllerImpl(this, statisticsModel)

        // Initialize views
        barChart = view.findViewById(R.id.graph_obtiznost_denni)
        recyclerViewRoutes = view.findViewById(R.id.recyclerViewRoutesDailyStats)
        layoutObtiznost = view.findViewById(R.id.layout_obtiznost)

        // Initialize the date picker dialog
        initializeDatePicker()
        showDatePicker()

        buttonObtiznost = view.findViewById(R.id.button_obtížnost)
        buttonStylPrelzeu = view.findViewById(R.id.button_stylprelezu)

        // Call the method to setup bar chart with empty data initially to set the correct XAxis properties
        setupBarChart(barChart, emptyList(), emptyList())

        return view
    }

    private fun initializeDatePicker() {
        val now: Calendar = Calendar.getInstance()
        dpd = DatePickerDialog.newInstance(
            this,
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        )

        dpd.setThemeDark(false)
        val accentColor = ContextCompat.getColor(requireContext(), R.color.grafy)
        dpd.accentColor = accentColor

        lifecycleScope.launch {
            try {
                val daysWithRoutes = cestaModel.getAllDatesWithData()
                val selectableDays = daysWithRoutes.map { day ->
                    Calendar.getInstance().apply { timeInMillis = day }
                }.toTypedArray()

                dpd.setSelectableDays(selectableDays)
            } catch (e: Exception) {
                Log.e("DatePickerDialog", "Error fetching selectable days: ${e.message}")
            }
        }
    }

    private fun showDatePicker() {
        lifecycleScope.launch {
            try {
                val closestDateWithData = cestaModel.getClosestDateWithData()
                closestDateWithData?.let { closestDate ->
                    val calendar = Calendar.getInstance().apply { timeInMillis = closestDate }

                    dpd.initialize(
                        this@DailyStatisticsFragment,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )

                    activity?.supportFragmentManager?.beginTransaction()
                        ?.replace(R.id.datePickerContainer, dpd)
                        ?.commit()
                }
            } catch (e: Exception) {
                Log.e("DatePickerDialog", "Error showing date picker: ${e.message}")
            }
        }
    }

    override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val selectedDate = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthOfYear)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
        }

        val startDate = selectedDate.clone() as Calendar
        val endDate = selectedDate.clone() as Calendar

        startDate.set(Calendar.HOUR_OF_DAY, 0)
        startDate.set(Calendar.MINUTE, 0)
        startDate.set(Calendar.SECOND, 0)

        endDate.set(Calendar.HOUR_OF_DAY, 23)
        endDate.set(Calendar.MINUTE, 59)
        endDate.set(Calendar.SECOND, 59)

        (controller as DailyStatisticsControllerImpl).onDateChanged(
            startDate.get(Calendar.YEAR),
            startDate.get(Calendar.MONTH),
            startDate.get(Calendar.DAY_OF_MONTH)
        )

        lifecycleScope.launch {
            try {
                val cesty =
                    cestaModel.getAllCestaForDateRange(startDate.timeInMillis, endDate.timeInMillis)
                updateRecyclerViewCesty(cesty)
            } catch (e: Exception) {
                Log.e("DailyStatisticsFragment", "Error updating RecyclerView: ${e.message}")
            }
        }
    }

    override fun updateGraph(barEntries: List<BarEntry>, startDate: Long, endDate: Long) {
        val xLabels = controller.getXLabelsGraph1(requireContext(), startDate, endDate).toList()
        setupBarChart(barChart, barEntries, xLabels)
        buttonObtiznost.setBackgroundResource(R.drawable.rectangle_grade_button)
        buttonStylPrelzeu.setBackgroundResource(android.R.color.transparent)

        buttonObtiznost.setOnClickListener {
            buttonObtiznost.setBackgroundResource(R.drawable.rectangle_grade_button)
            buttonStylPrelzeu.setBackgroundResource(android.R.color.transparent)
            // Zde aktualizujeme graf pomocí dat pro obtížnost
            val barEntries1 = controller.getDataGraph1(requireContext(), startDate, endDate)
            val xLabels1 =
                controller.getXLabelsGraph1(requireContext(), startDate, endDate).toList()
            setupBarChart(barChart, barEntries1, xLabels1)

            // Zde můžete aktualizovat RecyclerView, pokud je to potřeba
            lifecycleScope.launch {
                val cesty = cestaModel.getAllCestaForDateRange(startDate, endDate)
                updateRecyclerViewCesty(cesty)
            }
        }

        buttonStylPrelzeu.setOnClickListener {
            buttonStylPrelzeu.setBackgroundResource(R.drawable.rectangle_grade_button)
            buttonObtiznost.setBackgroundResource(android.R.color.transparent)
            val barEntries2 = controller.getDataGraph2(requireContext(), startDate, endDate)
            val xLabels2 =
                controller.getXLabelsGraph2(requireContext(), startDate, endDate).toList()
            setupBarChart(barChart, barEntries2, xLabels2)

            if (barEntries2.isNotEmpty()) {
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
    }

    private fun updateRecyclerViewCesty(cesty: List<CestaEntity>) {
        val reversedCesty = cesty.reversed()
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerViewRoutes.layoutManager = layoutManager
        recyclerViewRoutes.adapter = CestaAdapter(reversedCesty) { cestaId ->
            val intent = Intent(requireContext(), AddActivity::class.java)
            intent.putExtra("cestaId", cestaId)
            startActivity(intent)
        }
    }

    private fun setupBarChart(
        barChart: BarChart,
        barEntries: List<BarEntry>,
        xLabels: List<String>
    ) {
        val barDataSet = BarDataSet(barEntries, null)

        barDataSet.color = ContextCompat.getColor(requireContext(), R.color.grafy)

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

        xAxis.valueFormatter = IndexAxisValueFormatter(xLabels)
        xAxis.labelCount = xLabels.size  // Nastavte počet zobrazených značek na ose x

        val yAxis = barChart.axisLeft
        yAxis.axisMinimum = 0f
        yAxis.granularity = 1f
        yAxis.setDrawGridLines(false)
        yAxis.textSize = 12f

        barChart.apply {
            isHighlightPerDragEnabled = false
            isHighlightPerTapEnabled = false
            axisRight.isEnabled = false
            description.isEnabled = false
            isDoubleTapToZoomEnabled = false
            isScaleYEnabled = false
            isScaleXEnabled = false
            isClickable = false
            legend.isEnabled = false
            invalidate()
        }
    }

}
