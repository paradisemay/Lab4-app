package ru.ifmo.se.app.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import ru.ifmo.se.app.R
import ru.ifmo.se.app.util.TokenManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Проверяем наличие сохраненного токена
        val token = TokenManager.getToken(this)
        if (!token.isNullOrEmpty()) {
            // Если токен есть, сразу переходим к ContentActivity
            val intent = Intent(this, ContentActivity::class.java)
            startActivity(intent)
            finish() // Завершаем MainActivity, чтобы пользователь не мог вернуться на этот экран кнопкой "Назад"
        } else {
            // Если токена нет, отображаем экран с кнопками входа и регистрации
            setContentView(R.layout.activity_main)

            val btnLogin = findViewById<Button>(R.id.btnLogin)
            val btnRegister = findViewById<Button>(R.id.btnRegister)

            // Обработчик для кнопки "Войти"
            btnLogin.setOnClickListener {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }

            // Обработчик для кнопки "Зарегистрироваться"
            btnRegister.setOnClickListener {
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
