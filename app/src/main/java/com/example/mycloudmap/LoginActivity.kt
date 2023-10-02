package com.example.mycloudmap


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class LoginActivity: AppCompatActivity() {

    private lateinit var mEditTextName: EditText
    private lateinit var mEditTextEmail: EditText
    private lateinit var mEditTextPassword: EditText
    private lateinit var mBtnRegister: Button
    private lateinit var mBtnSignIn: Button

    //variables que vamos a registrar

    private var nombre: String = ""
    private var correo: String = ""
    private var contrasena: String = ""

    private val mAuth = FirebaseAuth.getInstance()
    private val mDatabase = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mBtnRegister = findViewById<View>(R.id.btnRegister) as Button
        mBtnSignIn = findViewById<View>(R.id.btnSendtoSignIn) as Button
        mEditTextName = findViewById(R.id.editTextName)
        mEditTextEmail = findViewById(R.id.editTextEmail)
        mEditTextPassword = findViewById(R.id.editTextPassword)

        mBtnRegister.setOnClickListener { view ->
            //Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
            nombre = mEditTextName.text.toString()
            correo = mEditTextEmail.text.toString()
            contrasena = mEditTextPassword.text.toString()

            if (nombre.isNotBlank() && correo.isNotBlank() && contrasena.isNotBlank()) {
                    if (contrasena.length >= 6) {
                        registerUser()
                    }

                } else {
                Toast.makeText(
                    this@LoginActivity,
                    "Los campos se deben rellenar",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        mBtnSignIn.setOnClickListener {
            val intent = Intent(this@LoginActivity, AutenticacionActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun registerUser(){
        mAuth.createUserWithEmailAndPassword(correo, contrasena).addOnCompleteListener(this)
        { task ->
            if (task.isSuccessful) {
                //el usuario se registró exitosamente
                mAuth.currentUser

                val map = HashMap<String, Any>()
                map["nombre"] = nombre
                map["email"] = correo
                map["password"] = contrasena

                val userId = mAuth.currentUser?.uid

                if (userId != null) {
                    val usersReference = mDatabase.child("Users").child(userId)

                    usersReference.setValue(map).addOnCompleteListener { task2 ->
                        if (task2.isSuccessful) {
                            // La escritura en la base de datos se completó con éxito
                            // Puedes realizar acciones adicionales aquí, si es necesario
                            val intent = Intent(this@LoginActivity, ProfileActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            task2.exception?.message
                            // Handle error aquí, si es necesario
                            Toast.makeText(this@LoginActivity, "No se pudieron crear los usuarios correctamente", Toast.LENGTH_SHORT).show()
                        }
                    }
                }


            } else {

                task.exception?.message
            }

        }
    }

     override fun onStart() {
        super.onStart()
        if(mAuth.currentUser != null){
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


}



