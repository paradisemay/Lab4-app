package ru.ifmo.se.app.activities

import android.os.Bundle
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.ifmo.se.app.R
import ru.ifmo.se.app.model.GraphData
import ru.ifmo.se.app.viewmodel.ContentViewModel
import ru.ifmo.se.app.view.CanvasView

class ContentActivity : AppCompatActivity() {

    private val viewModel: ContentViewModel by viewModels()
    private lateinit var canvasView: CanvasView
    private lateinit var etX: EditText
    private lateinit var etY: EditText
    private lateinit var etR: EditText
    private lateinit var tableLayout: TableLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content)

        canvasView = findViewById(R.id.canvasView)
        etX = findViewById(R.id.etX)
        etY = findViewById(R.id.etY)
        etR = findViewById(R.id.etR)
        tableLayout = findViewById(R.id.tableLayout)

        findViewById<Button>(R.id.btnDraw).setOnClickListener {
            val x = etX.text.toString().toFloatOrNull() ?: 0f
            val y = etY.text.toString().toFloatOrNull() ?: 0f
            val r = etR.text.toString().toFloatOrNull() ?: 0f
            val newGraphData = GraphData(x, y, r)
            viewModel.updateCircle(newGraphData)
            canvasView.updateGraphData(newGraphData)
        }

        fillTable()
    }

    private fun fillTable() {
        val data = listOf(
            listOf("X", "Y", "Radius"),
            listOf(viewModel.graphData.x.toString(), viewModel.graphData.y.toString(), viewModel.graphData.radius.toString())
        )

        for (row in data) {
            val tableRow = TableRow(this)
            for (cell in row) {
                val textView = TextView(this)
                textView.text = cell
                textView.setPadding(8, 8, 8, 8)
                tableRow.addView(textView)
            }
            tableLayout.addView(tableRow)
        }
    }
}
