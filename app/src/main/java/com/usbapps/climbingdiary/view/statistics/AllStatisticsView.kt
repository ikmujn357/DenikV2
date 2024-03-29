package com.usbapps.climbingdiary.view.statistics

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.usbapps.climbingdiary.R
import com.usbapps.climbingdiary.controller.statistics.AllStatisticsController
import com.usbapps.climbingdiary.controller.statistics.AllStatisticsControllerImpl
import com.usbapps.climbingdiary.model.CestaModel
import com.usbapps.climbingdiary.model.CestaModelImpl
import com.usbapps.climbingdiary.model.statistics.AllStatisticsModelImpl
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.launch

interface AllStatisticsView {
    fun displayGraph(view: View)
}

class AllStatisticsFragment : Fragment(), AllStatisticsView {
    private lateinit var controller: AllStatisticsController
    private lateinit var cestaModel: CestaModel
    private lateinit var layoutCelkova: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.celkova, container, false)
        cestaModel = CestaModelImpl(requireContext())
        val statisticsModel = AllStatisticsModelImpl(cestaModel)
        controller = AllStatisticsControllerImpl(statisticsModel)
        layoutCelkova = view.findViewById(R.id.layout_celkova)
        graphVisibility ()
        displayGraph(view)
        return view
    }

    private fun graphVisibility () {
        lifecycleScope.launch {
            Log.d("data", cestaModel.getAllCesta().size.toString())
            if (cestaModel.getAllCesta().isNotEmpty()) {
                // Pokud jsou k dispozici data, zobrazíme layout grafu
                layoutCelkova.visibility = View.VISIBLE
            } else {
                // Pokud nejsou k dispozici žádná data, skryjeme layout grafu
                layoutCelkova.visibility = View.GONE
            }
        }
    }

    override fun displayGraph(view: View) {
        val barChart = view.findViewById<BarChart>(R.id.graph_obtiznost)
        val barEntries = controller.getDataGraph(requireContext())

        val barDataSet = BarDataSet(barEntries, null)

        barDataSet.color = ContextCompat.getColor(requireContext(), R.color.grafy)

        val barData = BarData(barDataSet)

        if (barData.dataSetCount == 0) {
            // Set empty graph
            barChart.setNoDataText("No data available")
            barChart.invalidate()
        } else {
            barChart.data = barData

            // Set X-axis labels
            val xLabels = controller.getXLabelsGraph(requireContext())

            val xAxis = barChart.xAxis
            xAxis.valueFormatter = IndexAxisValueFormatter(xLabels)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false)
            xAxis.axisMaximum = xLabels.size.toFloat()
            xAxis.labelCount = xLabels.size

            val yAxis = barChart.axisLeft
            yAxis.axisMinimum = 0f
            yAxis.granularity = 1f
            yAxis.setDrawGridLines(false)

            // Set Y-axis labels
            barChart.axisLeft.granularity = 1f

            barDataSet.valueFormatter = object : ValueFormatter() {
                override fun getBarLabel(barEntry: BarEntry?): String {
                    return barEntry?.y?.toInt().toString()
                }
            }

            barDataSet.valueTextSize=10f


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


}
