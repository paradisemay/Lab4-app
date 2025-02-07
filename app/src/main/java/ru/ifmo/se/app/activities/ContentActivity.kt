package ru.ifmo.se.app.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.ifmo.se.app.R
import ru.ifmo.se.app.model.GraphData
import ru.ifmo.se.app.util.TokenManager
import ru.ifmo.se.app.viewmodel.ContentViewModel
import ru.ifmo.se.app.view.CanvasView

class ContentActivity : AppCompatActivity() {

    private val viewModel: ContentViewModel by viewModels()
    private lateinit var canvasView: CanvasView
    private lateinit var etY: EditText
    private lateinit var tableLayout: TableLayout

    // Переменные для хранения активной кнопки для X и R
    private var activeXButton: Button? = null
    private var activeRButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content)

        canvasView = findViewById(R.id.canvasView)
        etY = findViewById(R.id.etY)
        tableLayout = findViewById(R.id.tableLayout)

        // Устанавливаем активные кнопки по умолчанию:
        // для X – кнопка со значением "0" (btnX_0), для R – кнопка со значением "1" (btnR_1)
        activeXButton = findViewById(R.id.btnX_0)
        activeRButton = findViewById(R.id.btnR_1)
        activeXButton?.apply {
            backgroundTintList = ColorStateList.valueOf(Color.parseColor("#800000"))
            setTextColor(Color.parseColor("#FFFFFF"))
        }
        activeRButton?.apply {
            backgroundTintList = ColorStateList.valueOf(Color.parseColor("#800000"))
            setTextColor(Color.parseColor("#FFFFFF"))
        }

        // Назначаем обработчики кликов для всех кнопок выбора X
        val xButtons = listOf<Button>(
            findViewById(R.id.btnX_2),
            findViewById(R.id.btnX_1_5),
            findViewById(R.id.btnX_1),
            findViewById(R.id.btnX_0_5),
            findViewById(R.id.btnX_0),
            findViewById(R.id.btnX_0_5_pos),
            findViewById(R.id.btnX_1_pos),
            findViewById(R.id.btnX_1_5_pos),
            findViewById(R.id.btnX_2_pos)
        )
        for (button in xButtons) {
            button.setOnClickListener {
                setActiveX(button)
            }
        }

        // Назначаем обработчики кликов для всех кнопок выбора R
        val rButtons = listOf<Button>(
            findViewById(R.id.btnR_0_5),
            findViewById(R.id.btnR_1),
            findViewById(R.id.btnR_1_5),
            findViewById(R.id.btnR_2),
            findViewById(R.id.btnR_2_5),
            findViewById(R.id.btnR_3)
        )
        for (button in rButtons) {
            button.setOnClickListener {
                setActiveR(button)
            }
        }

        // Подписываемся на изменения LiveData из ViewModel
        viewModel.graphData.observe(this) { newGraphData ->
            // Обновляем CanvasView и таблицу при изменении данных
            canvasView.updateGraphData(newGraphData)
            updateTable(newGraphData)
        }

        // Обработка нажатия кнопки "Нарисовать"
        findViewById<Button>(R.id.btnDraw).setOnClickListener {
            val xValue = activeXButton?.text.toString().toFloatOrNull() ?: 0f
            val rValue = activeRButton?.text.toString().toFloatOrNull() ?: 0f
            val yValue = etY.text.toString().toFloatOrNull() ?: 0f
            val newGraphData = GraphData(xValue, yValue, rValue)
            viewModel.updateGraph(newGraphData)
            // При нажатии на "Нарисовать" точка отобразится
            canvasView.setShowPoint(true)
        }

        // Обработка нажатия кнопки "Выйти"
        findViewById<Button>(R.id.logout).setOnClickListener {
            TokenManager.saveToken(this, "")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Обработка касания по CanvasView (графу)
        canvasView.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                // Вызываем performClick для корректной работы с доступностью
                view.performClick()
                // Вызываем метод-заглушку onGraphTouch с координатами касания
                canvasView.onGraphTouch(event.x, event.y)
                true
            } else {
                false
            }
        }

        // При первом открытии отправляем значение r из выбранной по умолчанию кнопки (без точки)
        updateGraphWithDefaultR()
    }

    // Метод для обновления активной кнопки для группы X
    private fun setActiveX(selectedButton: Button) {
        activeXButton?.apply {
            backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F5F5F5"))
            setTextColor(Color.parseColor("#000000"))
        }
        selectedButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#800000"))
        selectedButton.setTextColor(Color.parseColor("#FFFFFF"))
        activeXButton = selectedButton
    }

    // Метод для обновления активной кнопки для группы R.
    // При нажатии обновляется только радиус (сохранены текущие x и y),
    // а CanvasView обновляется без отрисовки точки (только подписи обновляются).
    private fun setActiveR(selectedButton: Button) {
        activeRButton?.apply {
            backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F5F5F5"))
            setTextColor(Color.parseColor("#000000"))
        }
        selectedButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#800000"))
        selectedButton.setTextColor(Color.parseColor("#FFFFFF"))
        activeRButton = selectedButton

        // Обновляем данные графа: сохраняем текущие x и y, а радиус берём из выбранной кнопки
        val newRadius = selectedButton.text.toString().toFloatOrNull() ?: 0f
        val currentGraphData = viewModel.graphData.value ?: GraphData(0f, 0f, 0f)
        val updatedGraphData = GraphData(currentGraphData.x, currentGraphData.y, newRadius)
        viewModel.updateGraph(updatedGraphData)

        // Обновляем CanvasView без отрисовки точки – обновятся только подписи и оси
        canvasView.setShowPoint(false)
    }

    // Метод для обновления таблицы с данными на основе нового состояния графа
    private fun updateTable(graphData: GraphData) {
        tableLayout.removeAllViews()
        val data = listOf(
            listOf("X", "Y", "Radius"),
            listOf(graphData.x.toString(), graphData.y.toString(), graphData.radius.toString())
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

    // Метод для отправки значения r при первом открытии Activity.
    // Обновляем данные графа, оставляя x и y без изменений, и не отрисовываем точку.
    private fun updateGraphWithDefaultR() {
        val defaultR = activeRButton?.text.toString().toFloatOrNull() ?: 0f
        val currentGraphData = viewModel.graphData.value ?: GraphData(0f, 0f, 0f)
        val updatedGraphData = GraphData(currentGraphData.x, currentGraphData.y, defaultR)
        viewModel.updateGraph(updatedGraphData)
        canvasView.setShowPoint(false)
    }
}
