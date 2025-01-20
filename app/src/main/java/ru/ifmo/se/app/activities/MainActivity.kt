package ru.ifmo.se.app.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import ru.ifmo.se.app.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        // Обработчик для кнопки "Войти"
        btnLogin.setOnClickListener {
            // Логика для перехода на страницу входа
//            val intent = Intent(this, LoginActivity::class.java)
            val intent = Intent(this, ContentActivity::class.java)
            startActivity(intent)
        }

        // Обработчик для кнопки "Зарегистрироваться"
        btnRegister.setOnClickListener {
            // Логика для перехода на страницу регистрации
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
