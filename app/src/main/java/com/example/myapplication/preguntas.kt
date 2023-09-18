package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityPreguntasBinding
import com.google.firebase.firestore.FirebaseFirestore


class preguntas : AppCompatActivity() {

    private var mfirestore: FirebaseFirestore? = null

    lateinit var binding: ActivityPreguntasBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreguntasBinding.inflate(layoutInflater)

        setContentView(binding.root)
        supportActionBar?.title = "Preguntas"

        mfirestore = FirebaseFirestore.getInstance()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val preg: EditText = findViewById(R.id.txtpreg)
        val resp: EditText = findViewById(R.id.txtresp)
        val btn_add: Button = findViewById(R.id.btn_add)

        val db : FirebaseFirestore = FirebaseFirestore.getInstance()


        btn_add.setOnClickListener {
            val pregpet = preg.text.toString().trim()
            val respet = resp.text.toString().trim()

            try {
                if (pregpet.isEmpty() || respet.isEmpty()) {
                    Toast.makeText(applicationContext, "Ingresar los datos", Toast.LENGTH_SHORT).show()
                } else {
                    postPet(pregpet, respet)
                }
            } catch (e: NumberFormatException) {
                // Maneja la excepción si el valor ingresado no es un número válido
                Toast.makeText(applicationContext, "No válido", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btConsultar.setOnClickListener {
            var datos = ""
            db.collection("preguntas")
                .get()
                .addOnSuccessListener { resultado ->
                    for (documento in resultado) {
                        val pregunta = documento.getString("pregunta")
                        val respuesta = documento.getString("respuesta")
                        datos += "Pregunta: $pregunta\nRespuesta: $respuesta\n\n"
                    }
                    binding.tvConsulta.text = datos
                }
                .addOnFailureListener { exception ->
                    binding.tvConsulta.text = "No se ha podido conectar"
                }
        }

    }

    private fun postPet(pregpet: String, respet: String) {
        val map: MutableMap<String, Any> = HashMap()
        map["pregunta"] = pregpet
        map["respuesta"] = respet

        mfirestore!!.collection("preguntas").add(map).addOnSuccessListener {
            Toast.makeText(applicationContext, "Creado exitosamente", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener { e ->
            Toast.makeText(applicationContext, "Error al ingresar: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("FirestoreError", "Error al ingresar", e)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return false
    }
}