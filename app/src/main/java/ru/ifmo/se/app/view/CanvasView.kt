package ru.ifmo.se.app.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.View
import ru.ifmo.se.app.model.GraphData
import kotlin.properties.Delegates

class CanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val figurePaint = Paint().apply {
        // Цвет в формате ARGB (например, прозрачный синий)
        color = 0x6F34ebdb
        style = Paint.Style.FILL
    }

    private val axisPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 5f
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 40f
        textAlign = Paint.Align.CENTER
    }

    private var centerX by Delegates.notNull<Float>()
    private var centerY by Delegates.notNull<Float>()
    private var stepX by Delegates.notNull<Float>()
    private var stepY by Delegates.notNull<Float>()

    // Текущие данные (например, для текущей точки и текущего радиуса)
    var graphData: GraphData = GraphData(0f, 0f, 0f, PointStatus.UNKNOWN)

    // Флаг, управляющий отрисовкой текущей точки
    private var showPoint: Boolean = true

    // Список точек из истории
    private val historyPoints = mutableListOf<GraphData>()

    override fun onDraw(canvas: Canvas) {
        centerX = width / 2f
        centerY = height / 2f
        stepX = ((width - 100f) / 4.0).toFloat()
        stepY = ((height - 100f) / 4.0).toFloat()

        super.onDraw(canvas)

        // Рисуем фигуру и оси
        drawFigure(canvas)
        drawAxes(canvas)

        // Отрисовываем точки из истории с учетом текущего радиуса (graphData.radius)
        for (point in historyPoints) {
            if (point.radius != 0f) {
                val screenX = point.x / graphData.radius * (2 * stepX)
                val screenY = point.y / graphData.radius * (2 * stepY)
                // Если радиус точки совпадает с текущим, используем её статус,
                // иначе считаем статус UNKNOWN (например, серым)
                val drawStatus = if (point.radius == graphData.radius) point.status else PointStatus.UNKNOWN
                drawPoint(screenX, screenY, canvas, drawStatus.color.toInt())
            }
        }

        // Отрисовываем текущую точку (она всегда рисуется по текущему радиусу)
        if (showPoint && graphData.radius != 0f) {
            val screenX = graphData.x / graphData.radius * (2 * stepX)
            val screenY = graphData.y / graphData.radius * (2 * stepY)
            drawPoint(screenX, screenY, canvas, graphData.status.color.toInt())
        }
    }

    private fun drawPoint(x: Float, y: Float, canvas: Canvas, color: Int) {
        Log.d("CanvasView", "pointX = $x, pointY = $y")
        canvas.drawCircle(centerX + x, centerY - y, 20f, Paint().apply {
            this.color = color
            style = Paint.Style.FILL
        })
    }

    private fun drawFigure(canvas: Canvas) {
        canvas.drawRect(centerX, centerY - 2 * stepY, centerX + stepX, centerY, figurePaint)
        canvas.drawArc(
            centerX - 2 * stepX,
            centerY - 2 * stepY,
            centerX + 2 * stepX,
            centerY + 2 * stepY,
            90f,
            90f,
            true,
            figurePaint
        )
        val path = Path()
        path.moveTo(centerX, centerY)
        path.lineTo(centerX, centerY - stepY)
        path.lineTo(centerX - 2 * stepX, centerY)
        path.lineTo(centerX, centerY)
        path.close()
        canvas.drawPath(path, figurePaint)
    }

    private fun drawAxes(canvas: Canvas) {
        // Горизонтальная ось
        canvas.drawLine(0f, centerY, width.toFloat(), centerY, axisPaint)
        // Вертикальная ось
        canvas.drawLine(centerX, 0f, centerX, height.toFloat(), axisPaint)

        // Стрелки на осях
        drawArrowX(canvas)
        drawArrowY(canvas)

        val labelLength = 20f
        canvas.drawLine(
            centerX - 2 * stepX,
            centerY - labelLength,
            centerX - 2 * stepX,
            centerY,
            axisPaint
        )
        canvas.drawLine(centerX - stepX, centerY - labelLength, centerX - stepX, centerY, axisPaint)
        canvas.drawLine(centerX + stepX, centerY - labelLength, centerX + stepX, centerY, axisPaint)
        canvas.drawLine(
            centerX + 2 * stepX,
            centerY - labelLength,
            centerX + 2 * stepX,
            centerY,
            axisPaint
        )

        canvas.drawLine(
            centerX - labelLength,
            centerY - 2 * stepY,
            centerX,
            centerY - 2 * stepY,
            axisPaint
        )
        canvas.drawLine(centerX - labelLength, centerY - stepY, centerX, centerY - stepY, axisPaint)
        canvas.drawLine(centerX - labelLength, centerY + stepY, centerX, centerY + stepY, axisPaint)
        canvas.drawLine(
            centerX - labelLength,
            centerY + 2 * stepY,
            centerX,
            centerY + 2 * stepY,
            axisPaint
        )

        // Подписи осей с учетом текущего радиуса
        canvas.drawText("-${graphData.radius}", centerX - 2 * stepX, centerY + 40f, textPaint)
        canvas.drawText("-${graphData.radius / 2f}", centerX - stepX, centerY + 40f, textPaint)
        canvas.drawText("${graphData.radius / 2f}", centerX + stepX, centerY + 40f, textPaint)
        canvas.drawText("${graphData.radius}", centerX + 2 * stepX, centerY + 40f, textPaint)

        canvas.drawText("${graphData.radius}", centerX + 40f, centerY - 2 * stepY, textPaint)
        canvas.drawText("${graphData.radius / 2f}", centerX + 40f, centerY - stepY, textPaint)
        canvas.drawText("-${graphData.radius / 2f}", centerX + 40f, centerY + stepY, textPaint)
        canvas.drawText("-${graphData.radius}", centerX + 40f, centerY + 2 * stepY, textPaint)

        // Подписи осей
        canvas.drawText("X", width - 40f, centerY - 40f, textPaint)
        canvas.drawText("Y", centerX + 40f, 40f, textPaint)
    }

    private fun drawArrowX(canvas: Canvas) {
        val cornerX = width.toFloat()
        val cornerY = height / 2f
        val arrowLength = 20f
        val dArrowLength = arrowLength / Math.sqrt(2.0).toFloat()
        canvas.drawLine(cornerX, cornerY, cornerX - dArrowLength, cornerY - dArrowLength, axisPaint)
        canvas.drawLine(cornerX, cornerY, cornerX - dArrowLength, cornerY + dArrowLength, axisPaint)
    }

    private fun drawArrowY(canvas: Canvas) {
        val cornerX = width / 2f
        val cornerY = 0f
        val arrowLength = 20f
        val dArrowLength = arrowLength / Math.sqrt(2.0).toFloat()
        canvas.drawLine(cornerX, cornerY, cornerX - dArrowLength, cornerY + dArrowLength, axisPaint)
        canvas.drawLine(cornerX, cornerY, cornerX + dArrowLength, cornerY + dArrowLength, axisPaint)
    }

    // Обновление данных графа и перерисовка холста
    fun updateGraphData(newData: GraphData) {
        graphData = newData
        invalidate()
    }

    // Управление отрисовкой текущей точки
    fun setShowPoint(show: Boolean) {
        showPoint = show
        invalidate()
    }

    // Метод для обработки касания по холсту
    fun onGraphTouch(x: Float, y: Float): Pair<Float, Float> {
        val pointX = (x - centerX) * graphData.radius / (2 * stepX)
        val pointY = (centerY - y) * graphData.radius / (2 * stepY)
        return Pair(pointX, pointY)
    }

    // Метод для добавления новой точки в историю и перерисовки холста
    fun addHistoryPoint(point: GraphData) {
        historyPoints.add(point)
        invalidate()
    }

    // Метод для обновления списка точек истории (например, при загрузке истории с сервера)
    fun updateHistoryPoints(points: List<GraphData>) {
        historyPoints.clear()
        historyPoints.addAll(points)
        invalidate()
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }
}
