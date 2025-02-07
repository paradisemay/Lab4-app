package ru.ifmo.se.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.ifmo.se.app.model.GraphData

class ContentViewModel : ViewModel() {

    // Используем MutableLiveData для хранения состояния графа
    private val _graphData = MutableLiveData<GraphData>().apply {
        value = GraphData(0f, 0f, 0f)
    }
    // Экспонируем неизменяемый LiveData для наблюдения из UI
    val graphData: LiveData<GraphData> get() = _graphData

    // Метод для обновления данных графа
    fun updateGraph(newGraphData: GraphData) {
        _graphData.value = newGraphData
    }
}
