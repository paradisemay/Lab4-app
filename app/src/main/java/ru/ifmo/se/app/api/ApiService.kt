package ru.ifmo.se.app.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("register")
    fun registerUser(@Body registrationRequest: RegistrationRequest): Call<RegistrationResponse>

    @POST("login")
    fun loginUser(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @POST("point")
    fun checkPoint(@Body pointRequest: PointRequest): Call<PointResponse>

    @GET("history")
    fun getHistory(): Call<List<ResultResponse>>

    @DELETE("history")
    fun removeHistory(): Call<HistoryRemoveResponse>
}
