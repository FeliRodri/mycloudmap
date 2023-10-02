package com.example.mycloudmap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.content.Intent


class ProfileActivity : AppCompatActivity() {

    private val SPLASH_TIME_OUT = 3000 // 1 segundo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)


        Handler().postDelayed({
            // Crea un Intent para redirigir a la próxima actividad (por ejemplo, LoginActivity)
            val intent = Intent(this@ProfileActivity, MainActivity::class.java)
            startActivity(intent)
            finish() // Cierra esta actividad para que no vuelva atrás
        }, SPLASH_TIME_OUT.toLong())
    }
}