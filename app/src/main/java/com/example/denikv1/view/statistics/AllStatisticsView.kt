package com.example.denikv1

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.GridLabelRenderer
import com.jjoe64.graphview.helper.StaticLabelsFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries

interface AllStatisticsView {
    fun displayGraph(view: View)
}

class AllStatisticsFragment : Fragment(), AllStatisticsView {
    private lateinit var controller: AllStatisticsController
    private lateinit var cestaModel: CestaModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.celkova, container, false)
        cestaModel = CestaModelImpl(requireContext())
        val statisticsModel = AllStatisticsModelImpl(cestaModel)
        controller = AllStatisticsControllerImpl(statisticsModel, requireContext())
        displayGraph(view)
        return view
    }

    override fun displayGraph(view: View) {
        val graphView = view.findViewById<GraphView>(R.id.graph_obtiznost)
        val series = controller.getDataGraph(requireContext())

        graphView.viewport.isXAxisBoundsManual = true
        graphView.viewport.isYAxisBoundsManual = true


        val maxY = series.highestValueY
        graphView.viewport.setMaxX(series.highestValueX + 0.5)
        graphView.viewport.setMaxY(maxY + 1.0)

        if (series.isEmpty) {
            graphView.viewport.isXAxisBoundsManual = true
            graphView.viewport.isYAxisBoundsManual = true

            graphView.gridLabelRenderer.numHorizontalLabels = 0
            graphView.gridLabelRenderer.numVerticalLabels = 0
            val staticLabelsFormatter = StaticLabelsFormatter(graphView)
            staticLabelsFormatter.setHorizontalLabels(arrayOf("", ""))
            graphView.gridLabelRenderer.labelFormatter = staticLabelsFormatter
            graphView.viewport.setMinX(0.0)
            graphView.viewport.setMinY(0.0)

        } else {
            graphView.addSeries(series)

            val numLabels = controller.getXLabelsGraph(requireContext()).size
            graphView.gridLabelRenderer.numHorizontalLabels = numLabels
            graphView.gridLabelRenderer.numVerticalLabels = (maxY.toInt() + 2) / 2

            graphView.gridLabelRenderer.labelHorizontalHeight = 50
            graphView.gridLabelRenderer.verticalLabelsAlign = Paint.Align.CENTER
            graphView.gridLabelRenderer.gridStyle = GridLabelRenderer.GridStyle.NONE
            graphView.gridLabelRenderer.setHorizontalLabelsAngle(-25)

            val staticLabelsFormatter = StaticLabelsFormatter(graphView)
            staticLabelsFormatter.setHorizontalLabels(controller.getXLabelsGraph(requireContext()))
            graphView.gridLabelRenderer.labelFormatter = staticLabelsFormatter

            // Přidat následující řádek pro posunutí labelů o jedno místo doprava
            graphView.viewport.setMinX(0.5)

            val barWidthPx = 25
            series.spacing = barWidthPx
        }
    }
}
