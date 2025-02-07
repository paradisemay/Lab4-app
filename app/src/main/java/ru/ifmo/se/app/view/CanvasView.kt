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

    private val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
    }

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

    var graphData: GraphData = GraphData(0f, 0f, 0f, PointStatus.UNKNOWN)

    // Флаг, указывающий, нужно ли отрисовывать точку
    private var showPoint: Boolean = true

    override fun onDraw(canvas: Canvas) {
        centerX = width / 2f
        centerY = height / 2f
        stepX = ((width - 100f) / 4.0).toFloat()
        stepY = ((height - 100f) / 4.0).toFloat()

        super.onDraw(canvas)
        // Рисуем фигуру
        drawFigure(canvas)
        // Рисуем оси координат
        drawAxes(canvas)

        // Отрисовываем точку только если showPoint равен true и радиус не равен нулю
        if (showPoint && graphData.radius != 0f) {
            val pointX = graphData.x / graphData.radius * (2 * stepX)
            val pointY = graphData.y / graphData.radius * (2 * stepY)
            drawPoint(pointX, pointY, canvas)
        }
    }

    private fun drawPoint(x: Float, y: Float, canvas: Canvas) {
        Log.d("CanvasView", "pointX = $x, pointY = $y")
        canvas.drawCircle(centerX + x, centerY - y, 20f, Paint().apply {
            color = graphData.status.color.toInt()
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

    // Обновление данных графа и перерисовка View
    fun updateGraphData(newData: GraphData) {
        graphData = newData
        invalidate()
    }

    // Управление отрисовкой точки:
    // Если show = true, точка отрисовывается; если false – пропускается.
    fun setShowPoint(show: Boolean) {
        showPoint = show
        invalidate()
    }

    // Метод-заглушка для обработки касания по графу.
    // Принимает координаты нажатия (x, y) и может быть дополнен логикой в будущем.
    fun onGraphTouch(x: Float, y: Float): Pair<Float, Float> {
        val pointX = (x - centerX) * graphData.radius / (2 * stepX)
        val pointY = (centerY - y) * graphData.radius / (2 * stepY)
        return Pair(pointX, pointY)
    }

    // Переопределяем performClick для целей доступности.
    override fun performClick(): Boolean {
        // Вызываем базовую реализацию (например, для генерации событий доступности)
        return super.performClick()
    }
}
