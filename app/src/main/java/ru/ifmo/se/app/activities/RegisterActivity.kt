package ru.ifmo.se.app.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import ru.ifmo.se.app.api.RegistrationRequest
import ru.ifmo.se.app.api.RegistrationResponse
import ru.ifmo.se.app.util.TokenManager

class RegisterActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        Log.d("RegisterActivity", "onCreate")

        // Инициализация полей
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)

        // Обработчик для кнопки "Зарегистрироваться"
        btnRegister.setOnClickListener {
            Log.d("RegisterActivity", "btnRegister onClick")
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

            // Согласно примерам отправляется открытый пароль:
            registerUser(username, password)
        }
    }

    private fun registerUser(username: String, password: String) {
        Log.d("RegisterActivity", "registerUser $username $password")
        // Создаем экземпляр Retrofit с указанным базовым URL
        val retrofit = Retrofit.Builder()
            .baseUrl("http://45.134.12.67:8080/server2-1.0-SNAPSHOT/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        // Создаем запрос регистрации с полями "login" и "password"
        val registrationRequest = RegistrationRequest(username, password)

        apiService.registerUser(registrationRequest).enqueue(object : Callback<RegistrationResponse> {
            override fun onResponse(call: Call<RegistrationResponse>, response: Response<RegistrationResponse>) {
                Log.d("RegisterActivity", "onResponse ${response.code()} ${response.message()}")
                if (response.isSuccessful) {
                    // Если статус 200, получаем сообщение и токен
                    val registrationResponse = response.body()
                    registrationResponse?.let {
                        showToast(it.message)
                        // Сохраняем токен для дальнейшего использования
                        it.token?.let { token ->
                            TokenManager.saveToken(this@RegisterActivity, token)
                        }
                        // Переход на страницу авторизации или главную страницу
                        val intent = Intent(this@RegisterActivity, ContentActivity::class.java)
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
                            "Ошибка регистрации. Попробуйте еще раз."
                        }
                    } catch (e: Exception) {
                        "Ошибка регистрации. Попробуйте еще раз."
                    }
                    showToast(errorMessage)
                }
            }

            override fun onFailure(call: Call<RegistrationResponse>, t: Throwable) {
                Log.d("RegisterActivity", "onFailure", t)
                showToast("Ошибка сети. Попробуйте позже.")
            }
        })
    }

    private fun showToast(message: String) {
        Log.d("RegisterActivity", "showToast $message")
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
