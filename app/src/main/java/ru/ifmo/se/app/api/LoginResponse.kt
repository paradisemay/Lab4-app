package ru.ifmo.se.app.api

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String? // например, JWT токен
)
