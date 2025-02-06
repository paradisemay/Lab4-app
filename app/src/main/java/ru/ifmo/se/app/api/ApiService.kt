package ru.ifmo.se.app.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("register")
    fun registerUser(@Body registrationRequest: RegistrationRequest): Call<RegistrationResponse>

    @POST("login")
    fun loginUser(@Body loginRequest: LoginRequest): Call<LoginResponse>
}
