package ru.ifmo.se.app.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.security.MessageDigest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.ifmo.se.app.R
import ru.ifmo.se.app.api.ApiService
import ru.ifmo.se.app.api.LoginRequest
import ru.ifmo.se.app.api.LoginResponse

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

            // Хэширование пароля перед отправкой на сервер
            val hashedPassword = hashPassword(password)

            // Авторизация на сервере
            loginUser(username, hashedPassword)
        }
    }

    private fun loginUser(username: String, hashedPassword: String) {
        // Логика для отправки данных на сервер
        val retrofit = Retrofit.Builder()
            .baseUrl("https://yourapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        val loginRequest = LoginRequest(username, hashedPassword)

        apiService.loginUser(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    showToast("Авторизация прошла успешно!")
                    // Переход на главную страницу или страницу с контентом
                    val intent = Intent(this@LoginActivity, ContentActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    showToast("Ошибка авторизации. Неверный логин или пароль.")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                showToast("Ошибка сети. Попробуйте позже.")
            }
        })
    }

    private fun hashPassword(password: String): String {
        try {
            // Используем SHA-256 для хэширования пароля
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
            return hashBytes.joinToString("") { String.format("%02x", it) }
        } catch (e: Exception) {
            e.printStackTrace()
            return password // В случае ошибки возвращаем пароль без изменений
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
