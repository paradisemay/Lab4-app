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
import ru.ifmo.se.app.api.RegistrationRequest
import ru.ifmo.se.app.api.RegistrationResponse

class RegisterActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Инициализация полей
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)

        // Обработчик для кнопки "Зарегистрироваться"
        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                showToast("Все поля должны быть заполнены!")
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                showToast("Пароли не совпадают!")
                return@setOnClickListener
            }

            // Хэширование пароля перед отправкой на сервер
            val hashedPassword = hashPassword(password)

            // Регистрация на сервере
            registerUser(username, hashedPassword)
        }
    }

    private fun registerUser(username: String, hashedPassword: String) {
        // Логика для отправки данных на сервер
        val retrofit = Retrofit.Builder()
            .baseUrl("https://yourapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        val registrationRequest = RegistrationRequest(username, hashedPassword)

        apiService.registerUser(registrationRequest).enqueue(object : Callback<RegistrationResponse> {
            override fun onResponse(call: Call<RegistrationResponse>, response: Response<RegistrationResponse>) {
                if (response.isSuccessful) {
                    showToast("Регистрация прошла успешно!")
                    // Переход на страницу авторизации или главную страницу
                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    showToast("Ошибка регистрации. Попробуйте еще раз.")
                }
            }

            override fun onFailure(call: Call<RegistrationResponse>, t: Throwable) {
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
