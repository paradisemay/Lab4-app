package ru.ifmo.se.app.view

enum class PointStatus(val color: Long) {
    HIT(0xff00ff00),
    MISS(0xffff0000),
    UNKNOWN(0xff808080)
}