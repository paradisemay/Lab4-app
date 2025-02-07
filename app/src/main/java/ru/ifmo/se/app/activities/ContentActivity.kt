package ru.ifmo.se.app.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
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
import ru.ifmo.se.app.api.HistoryRemoveResponse
import ru.ifmo.se.app.api.PointRequest
import ru.ifmo.se.app.api.PointResponse
import ru.ifmo.se.app.api.ResultResponse
import ru.ifmo.se.app.model.GraphData
import ru.ifmo.se.app.util.AuthInterceptor
import ru.ifmo.se.app.util.TokenManager
import ru.ifmo.se.app.viewmodel.ContentViewModel
import ru.ifmo.se.app.view.CanvasView
import ru.ifmo.se.app.view.PointStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

        // Обработчики кликов для кнопок X
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

        // Обработчики кликов для кнопок R
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

        // Подписка на изменения LiveData из ViewModel
        viewModel.graphData.observe(this) { newGraphData ->
            // Обновляем холст и таблицу при изменении данных
            canvasView.updateGraphData(newGraphData)
            updateTable()
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
                    val status = if (hit) PointStatus.HIT else PointStatus.MISS
                    val newPoint = GraphData(xValue, yValue, rValue, status)
                    viewModel.updateGraph(newPoint)
                    // Добавляем точку в историю холста
                    canvasView.addHistoryPoint(newPoint)
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
                view.performClick()
                val (xPoint, yPoint) = canvasView.onGraphTouch(event.x, event.y)
                val rPoint = activeRButton?.text.toString().toFloatOrNull() ?: 1f

                checkPoint(xPoint, yPoint, rPoint) { result ->
                    result?.let { hit ->
                        val status = if (hit) PointStatus.HIT else PointStatus.MISS
                        val newPoint = GraphData(xPoint, yPoint, rPoint, status)
                        viewModel.updateGraph(newPoint)
                        canvasView.addHistoryPoint(newPoint)
                    }
                }
                true
            } else {
                false
            }
        }

        // Обработка кнопки "Очистить историю" (btnClear)
        findViewById<Button>(R.id.btnClear).setOnClickListener {
            val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(this))
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl("http://45.134.12.67:8080/server2-1.0-SNAPSHOT/api/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val apiService = retrofit.create(ApiService::class.java)
            apiService.removeHistory().enqueue(object : Callback<HistoryRemoveResponse> {
                override fun onResponse(
                    call: Call<HistoryRemoveResponse>,
                    response: Response<HistoryRemoveResponse>
                ) {
                    if (response.isSuccessful) {
                        // Очищаем локальную историю: холст и таблица
                        canvasView.updateHistoryPoints(emptyList())
                        tableLayout.removeAllViews()
                        // Выводим надпись "История отсутствует"
                        showNoHistoryMessage()
                        // Выводим тост с сообщением из ответа
                        val toastMessage = response.body()?.message ?: "История очищена"
                        showToast(toastMessage)
                    } else {
                        handleErrorResponse(response)
                    }
                }

                override fun onFailure(call: Call<HistoryRemoveResponse>, t: Throwable) {
                    showToast("Ошибка сети. Попробуйте позже.")
                }
            })
        }

        // Загрузка истории через API (обновляет таблицу и холст)
        updateTable()

        // При первом открытии Activity обновляем граф с выбранным значением R (без точки)
        updateGraphWithDefaultR()
    }

    // Метод для обновления активной кнопки группы X
    private fun setActiveX(selectedButton: Button) {
        activeXButton?.apply {
            backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F5F5F5"))
            setTextColor(Color.parseColor("#000000"))
        }
        selectedButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#800000"))
        selectedButton.setTextColor(Color.parseColor("#FFFFFF"))
        activeXButton = selectedButton
    }

    // Метод для обновления активной кнопки группы R
    private fun setActiveR(selectedButton: Button) {
        activeRButton?.apply {
            backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F5F5F5"))
            setTextColor(Color.parseColor("#000000"))
        }
        selectedButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#800000"))
        selectedButton.setTextColor(Color.parseColor("#FFFFFF"))
        activeRButton = selectedButton

        val newRadius = selectedButton.text.toString().toFloatOrNull() ?: 0f
        val currentGraphData = viewModel.graphData.value ?: GraphData(0f, 0f, 0f, PointStatus.UNKNOWN)
        val updatedGraphData = GraphData(currentGraphData.x, currentGraphData.y, newRadius, currentGraphData.status)
        viewModel.updateGraph(updatedGraphData)

        // Обновляем холст (без отображения текущей точки, только оси и подписи)
        canvasView.setShowPoint(false)
    }

    // Загрузка истории через API и обновление таблицы и холста
    private fun updateTable() {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(this))
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://45.134.12.67:8080/server2-1.0-SNAPSHOT/api/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        apiService.getHistory().enqueue(object : Callback<List<ResultResponse>> {
            override fun onResponse(call: Call<List<ResultResponse>>, response: Response<List<ResultResponse>>) {
                if (response.isSuccessful) {
                    val history = response.body()
                    if (history.isNullOrEmpty()) {
                        showNoHistoryMessage()
                    } else {
                        updateTableWithData(history)
                    }
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<List<ResultResponse>>, t: Throwable) {
                Log.e("NetworkError", "Error: ${t.message}")
                showToast("Ошибка сети. Попробуйте позже.")
            }
        })
    }

    private fun updateTableWithData(history: List<ResultResponse>) {
        tableLayout.removeAllViews()

        // Заголовки таблицы
        val headerRow = TableRow(this)
        val headers = listOf("Время", "X", "Y", "R", "Результат")
        for (header in headers) {
            val textView = TextView(this).apply {
                text = header
                setPadding(16, 8, 16, 8)
                setTextColor(Color.BLACK)
                textSize = 16f
            }
            headerRow.addView(textView)
        }
        tableLayout.addView(headerRow)

        val dateFormat = SimpleDateFormat("HH:mm d MMM", Locale.getDefault())
        val historyPointsList = mutableListOf<GraphData>()

        for (point in history) {
            val row = TableRow(this)
            val formattedTime = dateFormat.format(Date(point.time))
            val values = listOf(
                formattedTime,
                String.format("%.2f", point.x),
                String.format("%.2f", point.y),
                String.format("%.1f", point.r),
                if (point.result == "hit") "Попадание" else "Мимо"
            )

            for (value in values) {
                val textView = TextView(this).apply {
                    text = value
                    setPadding(16, 8, 16, 8)
                    setTextColor(Color.DKGRAY)
                    textSize = 14f
                }
                row.addView(textView)
            }
            tableLayout.addView(row)

            // Формируем список точек для холста
            val status = if (point.result == "hit") PointStatus.HIT else PointStatus.MISS
            historyPointsList.add(GraphData(point.x, point.y, point.r, status))
        }

        // Обновляем холст: отображаем точки из истории
        canvasView.updateHistoryPoints(historyPointsList)
    }

    private fun showNoHistoryMessage() {
        tableLayout.removeAllViews()
        val textView = TextView(this).apply {
            text = "История отсутствует"
            textSize = 18f
            setTextColor(Color.GRAY)
            setPadding(16, 16, 16, 16)
            gravity = Gravity.CENTER
        }
        tableLayout.addView(textView)
    }

    // При первом открытии Activity обновляем граф с выбранным значением R (без точки)
    private fun updateGraphWithDefaultR() {
        val defaultR = activeRButton?.text.toString().toFloatOrNull() ?: 0f
        val currentGraphData = viewModel.graphData.value ?: GraphData(0f, 0f, 0f, PointStatus.UNKNOWN)
        val updatedGraphData = GraphData(currentGraphData.x, currentGraphData.y, defaultR, currentGraphData.status)
        viewModel.updateGraph(updatedGraphData)
        canvasView.setShowPoint(false)
    }

    private fun checkPoint(x: Float, y: Float, r: Float, callback: (Boolean?) -> Unit) {
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
                    callback(body?.message == "hit")
                } else {
                    handleErrorResponse(response)
                    callback(null)
                }
            }

            override fun onFailure(call: Call<PointResponse>, t: Throwable) {
                showToast("Ошибка сети. Попробуйте позже.")
                callback(null)
            }
        })
    }

    private fun handleErrorResponse(response: Response<*>) {
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
