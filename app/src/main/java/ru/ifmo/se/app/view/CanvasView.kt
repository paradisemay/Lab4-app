package ru.ifmo.se.app.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
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
        color = Color.BLUE
        style = Paint.Style.FILL
    }

    private val figurePaint = Paint().apply {
        color = Color.RED
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

    var graphData: GraphData = GraphData(0f, 0f, 0f)

    override fun onDraw(canvas: Canvas) {
        centerX = width / 2f
        centerY = height / 2f
        stepX = (((width - 100f)) / 4.0).toFloat()
        stepY = (((height - 100f)) / 4.0).toFloat()

        Log.d("CanvasView", "centerX = $centerX, centerY = $centerY, stepX = $stepX, stepY = $stepY")

        super.onDraw(canvas)
        // Рисуем фигуру
        drawFigure(canvas)
        // Рисуем оси координат
        drawAxes(canvas)

        // Рисуем точку
        val x = graphData.x / graphData.radius * (2 * stepX)
        val y = graphData.y / graphData.radius * (2 * stepY)
        drawPoint(x, y, canvas)
    }

    private fun drawPoint(x: Float, y: Float, canvas: Canvas) {
        canvas.drawCircle(centerX + x, centerY - y, 20f, paint)
    }

    private fun drawFigure(canvas: Canvas) {
        canvas.drawRect(centerX, centerY - 2 * stepY, centerX + stepX, centerY, figurePaint)

        canvas.drawArc(centerX - 2 * stepX, centerY - 2 * stepY, centerX + 2 * stepX, centerY + 2 * stepY, 90f, 90f, true, figurePaint)

        val path = Path()
        path.moveTo(centerX, centerY)
        path.lineTo(centerX, centerY - stepY)
        path.lineTo(centerX - 2 * stepX, centerY)
        path.lineTo(centerX, centerY)
        path.close()
        canvas.drawPath(path, figurePaint)
    }

    private fun drawAxes(canvas: Canvas) {
        // Рисуем горизонтальную ось
        canvas.drawLine(0f, centerY, width.toFloat(), centerY, axisPaint)

        // Рисуем вертикальную ось
        canvas.drawLine(centerX, 0f, centerX, height.toFloat(), axisPaint)

        // Рисуем стрелку на горизонтальной оси (ось X) - направлена вправо
        drawArrowX(canvas)
        // Рисуем стрелку на вертикальной оси (ось Y) - направлена вверх
        drawArrowY(canvas)

        val labelLenght = 20f
        canvas.drawLine(centerX - 2 * stepX, centerY - labelLenght, centerX - 2 * stepX, centerY, axisPaint)
        canvas.drawLine(centerX - stepX, centerY - labelLenght, centerX - stepX, centerY, axisPaint)
        canvas.drawLine(centerX + stepX, centerY - labelLenght, centerX + stepX, centerY, axisPaint)
        canvas.drawLine(centerX + 2 * stepX, centerY - labelLenght, centerX + 2 * stepX, centerY, axisPaint)

        canvas.drawLine(centerX - labelLenght, centerY - 2 * stepY, centerX, centerY - 2 * stepY, axisPaint)
        canvas.drawLine(centerX - labelLenght, centerY - stepY, centerX, centerY - stepY, axisPaint)
        canvas.drawLine(centerX - labelLenght, centerY + stepY, centerX, centerY + stepY, axisPaint)
        canvas.drawLine(centerX - labelLenght, centerY + 2 * stepY, centerX, centerY + 2 * stepY, axisPaint)

        canvas.drawText("-${graphData.radius}", centerX - 2 * stepX, centerY + 40f, textPaint)
        canvas.drawText("-${graphData.radius / 2f}", centerX - stepX, centerY + 40f, textPaint)
        canvas.drawText("${graphData.radius / 2f}", centerX + stepX, centerY + 40f, textPaint)
        canvas.drawText("${graphData.radius}", centerX + 2 * stepX, centerY + 40f, textPaint)

        canvas.drawText("-${graphData.radius}", centerX + 40f, centerY - 2 * stepY, textPaint)
        canvas.drawText("-${graphData.radius / 2f}", centerX + 40f, centerY - stepY, textPaint)
        canvas.drawText("${graphData.radius / 2f}", centerX + 40f, centerY + stepY, textPaint)
        canvas.drawText("${graphData.radius}", centerX + 40f, centerY + 2 * stepY, textPaint)


        // Рисуем надписи X и Y
        canvas.drawText("X", width - 40f, centerY - 40f, textPaint) // Надпись для оси X
        canvas.drawText("Y", centerX + 40f, 40f, textPaint) // Надпись для оси Y
    }

    private fun drawArrowX(canvas: Canvas) {
        val cornerX = width.toFloat()
        val cornerY = height / 2f
        val arrowLength = 20f

        val sq2 = Math.sqrt(2.0).toFloat()
        val dArrowLength = arrowLength / sq2

        canvas.drawLine(cornerX, cornerY, cornerX - dArrowLength, cornerY - dArrowLength, axisPaint)
        canvas.drawLine(cornerX, cornerY, cornerX - dArrowLength, cornerY + dArrowLength, axisPaint)
    }

    private fun drawArrowY(canvas: Canvas) {
        val cornerX = width / 2f
        val cornerY = 0f
        val arrowLength = 20f

        val sq2 = Math.sqrt(2.0).toFloat()
        val dArrowLength = arrowLength / sq2

        canvas.drawLine(cornerX, cornerY, cornerX - dArrowLength, cornerY + dArrowLength, axisPaint)
        canvas.drawLine(cornerX, cornerY, cornerX + dArrowLength, cornerY + dArrowLength, axisPaint)
    }


    fun updateGraphData(newData: GraphData) {
        graphData = newData
        invalidate() // Перерисовываем канвас
    }
}
