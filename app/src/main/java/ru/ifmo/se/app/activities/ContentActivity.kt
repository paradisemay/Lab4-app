package ru.ifmo.se.app.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.ifmo.se.app.R
import ru.ifmo.se.app.api.ApiService
import ru.ifmo.se.app.api.PointRequest
import ru.ifmo.se.app.api.PointResponse
import ru.ifmo.se.app.model.GraphData
import ru.ifmo.se.app.util.AuthInterceptor
import ru.ifmo.se.app.util.TokenManager
import ru.ifmo.se.app.viewmodel.ContentViewModel
import ru.ifmo.se.app.view.CanvasView
import ru.ifmo.se.app.view.PointStatus

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

            val yText = etY.text.toString()
            var yValue: Float? = null
            try {
                yValue = yText.toFloat()
            } catch (_: NumberFormatException) {
                etY.error = "Некорректное значение Y"
                return@setOnClickListener
            }

            if (yValue < -3 || yValue > 3) {
                etY.error = "Значение Y вне диапазона [-3; 3]"
                return@setOnClickListener
            }

            if (yText.length > 4 && yText.contains('.') && yText.substring(yText.indexOf('.') + 1).length > 4) {
                etY.error = "Много знаков после запятой"
                return@setOnClickListener
            }

            checkPoint(xValue, yValue, rValue) { result ->
                result?.let { hit ->
                    if (hit) {
                        viewModel.updateGraph(GraphData(xValue, yValue, rValue, PointStatus.HIT))
                        canvasView.setShowPoint(true)
                    } else {
                        viewModel.updateGraph(GraphData(xValue, yValue, rValue, PointStatus.MISS))
                        canvasView.setShowPoint(true)
                    }
                }
            }
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
            if (event.action == MotionEvent.ACTION_DOWN) {
                // Вызываем performClick для корректной работы с доступностью
                view.performClick()
                // Вызываем метод-заглушку onGraphTouch с координатами касания
                val (xPoint, yPoint) = canvasView.onGraphTouch(event.x, event.y)
                val rPoint = activeRButton?.text.toString().toFloatOrNull() ?: 1f

                checkPoint(xPoint, yPoint, rPoint) { result ->
                    result?.let { hit ->
                        if (hit) {
                            viewModel.updateGraph(GraphData(xPoint, yPoint, rPoint, PointStatus.HIT))
                            canvasView.setShowPoint(true)
                        } else {
                            viewModel.updateGraph(GraphData(xPoint, yPoint, rPoint, PointStatus.MISS))
                            canvasView.setShowPoint(true)
                        }
                    }
                }
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
        val currentGraphData = viewModel.graphData.value ?: GraphData(0f, 0f, 0f, PointStatus.UNKNOWN)
        val updatedGraphData = GraphData(currentGraphData.x, currentGraphData.y, newRadius, currentGraphData.status)
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
        val currentGraphData = viewModel.graphData.value ?: GraphData(0f, 0f, 0f, PointStatus.UNKNOWN)
        val updatedGraphData = GraphData(currentGraphData.x, currentGraphData.y, defaultR, currentGraphData.status)
        viewModel.updateGraph(updatedGraphData)
        canvasView.setShowPoint(false)
    }

    private fun checkPoint(x: Float, y: Float, r: Float, callback: (Boolean?) -> Unit){
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(this))
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://45.134.12.67:8080/server2-1.0-SNAPSHOT/api/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        val request = PointRequest(x, y, r)

        apiService.checkPoint(request).enqueue(object : Callback<PointResponse> {
            override fun onResponse(call: Call<PointResponse>, response: Response<PointResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    callback(body?.message == "hit") // Возвращаем true или false
                } else {
                    handleErrorResponse(response)
                    callback(null) // Ошибка -> null
                }
            }

            override fun onFailure(call: Call<PointResponse>, t: Throwable) {
                showToast("Ошибка сети. Попробуйте позже.")
                callback(null) // Ошибка сети -> null
            }
        })
    }

    private fun handleErrorResponse(response: Response<PointResponse>) {
        try {
            val errorJson = response.errorBody()?.string()
            if (!errorJson.isNullOrEmpty()) {
                val jsonObj = JSONObject(errorJson)
                val errorMessage = jsonObj.getString("error")

                if (errorMessage == "Неверный или истекший токен") {
                    showToast("Сессия истекла. Пожалуйста, войдите в систему заново.")
                    TokenManager.saveToken(this, "")
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    showToast(errorMessage)
                }
            } else {
                showToast("Ошибка сервера")
            }
        } catch (e: Exception) {
            showToast("Ошибка обработки ответа")
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
