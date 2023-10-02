package com.example.mycloudmap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class AutenticacionActivity : AppCompatActivity() {

    private lateinit var mEditTextEmail: EditText
    private lateinit var mEditTextPassword: EditText
    private lateinit var mBtnSignIn: Button

    private var correo: String = ""
    private var contrasena: String = ""

    private val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_autenticacion)

        mBtnSignIn = findViewById<View>(R.id.btnSignIn) as Button
        mEditTextEmail = findViewById(R.id.editTextEmail)
        mEditTextPassword = findViewById(R.id.editTextPassword)


        mBtnSignIn.setOnClickListener { view ->
            //Toast.makeText(this, "Inicio exitoso", Toast.LENGTH_SHORT).show()
            correo = mEditTextEmail.text.toString()
            contrasena = mEditTextPassword.text.toString()

            if (correo.isNotBlank() && contrasena.isNotBlank()) {
                if (contrasena.length >= 6) {
                    signInUser()
                }

            } else {
                Toast.makeText(
                    this@AutenticacionActivity,
                    "Los campos se deben rellenar",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun signInUser() {
        mAuth.signInWithEmailAndPassword(correo, contrasena).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val intent = Intent(this@AutenticacionActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(
                    this@AutenticacionActivity,
                    "No se pudo iniciar sesion y compruebe los datos nuevamente",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}