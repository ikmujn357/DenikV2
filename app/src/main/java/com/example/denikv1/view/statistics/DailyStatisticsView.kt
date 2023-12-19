package com.example.denikv1

import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.GridLabelRenderer
import com.jjoe64.graphview.helper.StaticLabelsFormatter
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Calendar

// Rozhraní
interface DailyStatisticsView {
    fun updateGraph1(series: BarGraphSeries<DataPoint>, startDate: Long, endDate: Long)
    fun updateGraph2(series: BarGraphSeries<DataPoint>, startDate: Long, endDate: Long)
}

// Třída DailyStatisticsFragment implementující rozhraní
class DailyStatisticsFragment : Fragment(), DailyStatisticsView {
    private lateinit var controller: DailyStatisticsController
    private lateinit var cestaModel: CestaModel
    private lateinit var calendarView: CalendarView
    private lateinit var graphView1: GraphView
    private lateinit var graphView2: GraphView

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
        graphView1 = view.findViewById(R.id.graph_obtiznost_denni)
        graphView2 = view.findViewById(R.id.graph_styl_prelezu)
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
    override fun updateGraph1(series: BarGraphSeries<DataPoint>, startDate: Long, endDate: Long) {
        graphView1.viewport.isXAxisBoundsManual = true
        graphView1.viewport.isYAxisBoundsManual = true

        val maxY = series.highestValueY
        graphView1.viewport.setMaxX(series.highestValueX + 0.5)
        graphView1.viewport.setMaxY(maxY + 1.0)

        // Odebrání předchozích dat a přidání nových
        graphView1.removeAllSeries()
        graphView1.addSeries(series)

        // Nastavení počtu horizontálních a vertikálních popisků
        val labelsGraph1 = controller.getXLabelsGraph1(requireContext(), startDate, endDate)

        if (labelsGraph1.size > 1) {
            graphView1.viewport.isXAxisBoundsManual = true
            graphView1.viewport.isYAxisBoundsManual = true
            graphView1.viewport.setMinX(0.5)
            graphView1.viewport.setMinY(0.0)
            graphView1.gridLabelRenderer.numVerticalLabels = maxY.toInt() + 2

            // Nastavení popisků
            graphView1.gridLabelRenderer.labelHorizontalHeight = 50
            graphView1.gridLabelRenderer.verticalLabelsAlign = Paint.Align.CENTER
            graphView1.gridLabelRenderer.gridStyle = GridLabelRenderer.GridStyle.NONE
            graphView1.gridLabelRenderer.setHorizontalLabelsAngle(-25)

            // Nastavení statických popisků pro osu X
            val staticLabelsFormatter = StaticLabelsFormatter(graphView1)
            staticLabelsFormatter.setHorizontalLabels(labelsGraph1)
            graphView1.gridLabelRenderer.labelFormatter = staticLabelsFormatter

            // Nastavení minimální hodnoty pro osu X
            graphView1.viewport.setMinX(0.5)
            graphView1.viewport.setMinY(0.0)

            // Nastavení mezer mezi sloupci
            val barWidthPx = 25
            series.spacing = barWidthPx
        }
        else {
            graphView1.viewport.isXAxisBoundsManual = true
            graphView1.viewport.isYAxisBoundsManual = true
            // Clear any existing labels or formatting
            graphView1.gridLabelRenderer.numHorizontalLabels = 0
            graphView1.gridLabelRenderer.numVerticalLabels = 0
            val staticLabelsFormatter = StaticLabelsFormatter(graphView1)
            staticLabelsFormatter.setHorizontalLabels(arrayOf("",""))
            graphView1.gridLabelRenderer.labelFormatter = staticLabelsFormatter
            graphView1.viewport.setMinX(0.0)
            graphView1.viewport.setMinY(0.0)
        }
    }

    override fun updateGraph2(series: BarGraphSeries<DataPoint>, startDate: Long, endDate: Long) {
        graphView2.viewport.isXAxisBoundsManual = true
        graphView2.viewport.isYAxisBoundsManual = true

        val maxY = series.highestValueY
        graphView2.viewport.setMaxX(series.highestValueX + 0.5)
        graphView2.viewport.setMaxY(maxY + 1.0)

        // Odebrání předchozích dat a přidání nových
        graphView2.removeAllSeries()
        graphView2.addSeries(series)

        // Nastavení počtu horizontálních a vertikálních popisků
        val labelsGraph2 = controller.getXLabelsGraph2(requireContext(), startDate, endDate)

        if (labelsGraph2.size > 1) {
            graphView2.viewport.isXAxisBoundsManual = true
            graphView2.viewport.isYAxisBoundsManual = true
            graphView2.viewport.setMinX(0.5)
            graphView2.viewport.setMinY(0.0)
            graphView2.gridLabelRenderer.numVerticalLabels = maxY.toInt() + 2 // Změna na celočíselné hodnoty

            // Nastavení popisků
            graphView2.gridLabelRenderer.labelHorizontalHeight = 50
            graphView2.gridLabelRenderer.verticalLabelsAlign = Paint.Align.CENTER
            graphView2.gridLabelRenderer.gridStyle = GridLabelRenderer.GridStyle.NONE
            graphView2.gridLabelRenderer.setHorizontalLabelsAngle(-25)

            // Nastavení statických popisků pro osu X
            val staticLabelsFormatter = StaticLabelsFormatter(graphView2)
            staticLabelsFormatter.setHorizontalLabels(labelsGraph2)
            graphView2.gridLabelRenderer.labelFormatter = staticLabelsFormatter

            // Nastavení minimální hodnoty pro osu X
            graphView2.viewport.setMinX(0.5)
            graphView2.viewport.setMinY(0.0)

            // Nastavení mezer mezi sloupci
            val barWidthPx = 25
            series.spacing = barWidthPx
        } else {
            graphView2.viewport.isXAxisBoundsManual = true
            graphView2.viewport.isYAxisBoundsManual = true
            // Clear any existing labels or formatting
            graphView2.gridLabelRenderer.numHorizontalLabels = 0
            graphView2.gridLabelRenderer.numVerticalLabels = 0
            val staticLabelsFormatter = StaticLabelsFormatter(graphView2)
            staticLabelsFormatter.setHorizontalLabels(arrayOf("", ""))
            graphView2.gridLabelRenderer.labelFormatter = staticLabelsFormatter
            graphView2.viewport.setMinX(0.0)
            graphView2.viewport.setMinY(0.0)
        }
    }
}
