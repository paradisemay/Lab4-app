package ru.ifmo.se.app.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.ifmo.se.app.R
import ru.ifmo.se.app.api.ApiService
import ru.ifmo.se.app.api.LoginRequest
import ru.ifmo.se.app.api.LoginResponse
import ru.ifmo.se.app.util.TokenManager
import java.security.MessageDigest

class LoginActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Инициализация полей
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)

        // Обработчик для кнопки "Войти"
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                showToast("Все поля должны быть заполнены!")
                return@setOnClickListener
            }

            // По примеру отправляем открытый пароль (хэширование можно использовать, если требуется)
            loginUser(username, password)
        }
    }

    private fun loginUser(username: String, password: String) {
        // Создаем экземпляр Retrofit с указанным базовым URL
        val retrofit = Retrofit.Builder()
            .baseUrl("http://45.134.12.67:8080/server2-1.0-SNAPSHOT/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        val loginRequest = LoginRequest(username, password)

        apiService.loginUser(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    // Если статус 200, получаем сообщение и токен
                    val loginResponse = response.body()
                    loginResponse?.let {
                        showToast(it.message)
                        // Сохраняем токен для дальнейшего использования
                        it.token?.let { token ->
                            TokenManager.saveToken(this@LoginActivity, token)
                        }
                        // Переход на главную страницу или страницу с контентом
                        val intent = Intent(this@LoginActivity, ContentActivity::class.java)
                        startActivity(intent)
                        finish()
                    } ?: run {
                        showToast("Пустой ответ сервера")
                    }
                } else {
                    // Если статус ошибки (4XX), пытаемся получить поле "error" из errorBody
                    val errorMessage = try {
                        val errorJson = response.errorBody()?.string()
                        if (!errorJson.isNullOrEmpty()) {
                            val jsonObj = JSONObject(errorJson)
                            jsonObj.getString("error")
                        } else {
                            "Ошибка авторизации. Попробуйте еще раз."
                        }
                    } catch (e: Exception) {
                        "Ошибка авторизации. Попробуйте еще раз."
                    }
                    showToast(errorMessage)
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                showToast("Ошибка сети. Попробуйте позже.")
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
