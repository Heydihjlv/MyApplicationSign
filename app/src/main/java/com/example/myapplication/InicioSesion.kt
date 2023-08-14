package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.myapplication.databinding.ActivityInicioSesionBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class InicioSesion : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    //instanciar componente desde interfaz
    private lateinit var binding: ActivityInicioSesionBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio_sesion)

        binding = ActivityInicioSesionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth= Firebase.auth
        binding.signInAppCompatButton.setOnClickListener{
            val nEmail = binding.emailEditText.text.toString()
            val nPassword = binding.passwordEditText.text.toString()
           when{
               nEmail.isEmpty() || nPassword.isEmpty() -> {
                   Toast.makeText(baseContext,"correo o contrasenia incorrecto",
                        Toast.LENGTH_SHORT).show()
               }
               else ->{
               signIn(nEmail,nPassword)
           }
           }
        }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){task ->
                if (task.isSuccessful) {
                    Log.d("TAG", "signInWithEmail:success")
                    reload()
                } else {
                    Log.w("TAG", "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Email o contraseña o incorrectos.",
                        Toast.LENGTH_SHORT).show()
                }

            }
    }
    private fun reload(){
        val intent=Intent(this, MainActivity::class.java)
        this.startActivity(intent)
    }



}