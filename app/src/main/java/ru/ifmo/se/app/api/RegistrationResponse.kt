package ru.ifmo.se.app.api

data class RegistrationResponse(
    val message: String,
    val token: String? = null
)
