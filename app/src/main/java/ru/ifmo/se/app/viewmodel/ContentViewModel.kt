package ru.ifmo.se.app.viewmodel

import androidx.lifecycle.ViewModel
import ru.ifmo.se.app.model.GraphData

class ContentViewModel : ViewModel() {

    // Храним данные для рисования
    var graphData = GraphData(0f, 0f, 0f)

    // Метод для обновления данных круга
    fun updateCircle(newGraphData: GraphData) {
        graphData = newGraphData
    }
}
