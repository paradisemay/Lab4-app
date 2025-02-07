package ru.ifmo.se.app.model

import ru.ifmo.se.app.view.PointStatus

data class GraphData(
    var x: Float,
    var y: Float,
    var radius: Float,
    var status: PointStatus
)
