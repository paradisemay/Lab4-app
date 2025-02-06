package ru.ifmo.se.app.api

data class LoginResponse(
    val message: String,
    val token: String? = null
)
