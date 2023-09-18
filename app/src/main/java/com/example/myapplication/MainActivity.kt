package com.example.myapplication

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import androidx.camera.core.ImageProxy
import androidx.camera.core.impl.utils.MatrixExt.postRotate
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.ingenieriiajhr.jhrCameraX.BitmapResponse
import com.ingenieriiajhr.jhrCameraX.CameraJhr
import com.ingenieriiajhr.jhrCameraX.ImageProxyResponse
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
   private lateinit var auth: FirebaseAuth
   private lateinit var binding: ActivityMainBinding

   private lateinit var firestore: FirebaseFirestore

   private var preguntaAleatoria: String? = null

   private lateinit var txPregun: TextView

    // Tiempo en milisegundos para cambiar la pregunta (5 segundos)
    private val tiempoCambioPregunta = 10000L

    // Handler para ejecutar la tarea periódica
    private val handler = Handler()

  //  lateinit var binding : ActivityMainBinding
    lateinit var cameraJhr: CameraJhr

    lateinit var classifyTf: ClassifyTf
    companion object{
        const val INPUT_SIZE = 224
    }

    val classes = arrayOf("A", "B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        classifyTf=ClassifyTf(this)

        txPregun = findViewById(R.id.txtPregunta)

        //init cameraJHR
        cameraJhr = CameraJhr(this)

        firestore = FirebaseFirestore.getInstance()

        iniciarTareaPeriodica()
    }
    private fun iniciarTareaPeriodica() {
        // Define la tarea que se ejecutará periódicamente
        val tareaCambioPregunta = object : Runnable {
            override fun run() {
                obtenerPreguntaAleatoria()
                handler.postDelayed(this, tiempoCambioPregunta)
            }
        }

        // Ejecuta la tarea por primera vez
        handler.post(tareaCambioPregunta)
    }
    private fun obtenerPreguntaAleatoria() {
        val db = FirebaseFirestore.getInstance()
        val preguntasRef = db.collection("preguntas")

        preguntasRef.get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    val randomIndex = Random.nextInt(0, documents.size())
                    val preguntaAleatoria = documents.documents[randomIndex].getString("pregunta")
                    // Muestra la pregunta aleatoria en el TextView
                    txPregun.text = preguntaAleatoria
                    // Actualiza la variable preguntaAleatoria
                    this.preguntaAleatoria = preguntaAleatoria
                } else {
                    // No se encontraron preguntas en la colección

                    txPregun.text = "No hay preguntas disponibles."
                }
            }
            .addOnFailureListener { exception ->
                // Ocurrió un error al obtener las preguntas
                txPregun.text = "Error al obtener las preguntas: ${exception.message}"
            }
    }
private fun signOut(){
    Firebase.auth.signOut()
    val intent =Intent(this, SignUpActivity::class.java)
    startActivity(intent)

}
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (cameraJhr.allpermissionsGranted() && !cameraJhr.ifStartCamera){
            startCameraJhr()
        }else{
            cameraJhr.noPermissions()
        }
    }

    private fun startCameraJhr() {
        cameraJhr.addlistenerBitmap(object : BitmapResponse {
            override fun bitmapReturn(bitmap: Bitmap?) {
                if (bitmap!=null){
                    classifyImage(bitmap)


                }
            }
        })

        cameraJhr.initBitmap()
        cameraJhr.initImageProxy()
        //selector camera LENS_FACING_FRONT = 0;    LENS_FACING_BACK = 1;
        //aspect Ratio  RATIO_4_3 = 0; RATIO_16_9 = 1;  false returImageProxy, true return bitmap
        cameraJhr.start(0,0,binding.cameraPreview,true,false,true)
    }

    private fun classifyImage(bitmap: Bitmap) {
       //224*2024o
        val bitmapScale= Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false)

        classifyTf.listenerInterpreter(object: ReturnInterpreter{
            override fun classify(confidence: FloatArray, maxConfidence: Int) {
            runOnUiThread {
                binding.txtResult.text = /*"${classes[0]}: ${confidence[0]}\n"+
                        "${classes[1]}: ${confidence[1]}\n"+
                        "${classes[2]}: ${confidence[2]}\n"+*/

                        "Result: ${classes[maxConfidence]}"

                obtenerPreguntaYComparar(classes[maxConfidence])
            }
            }

        })

        classifyTf.classify(bitmapScale)
        runOnUiThread {
            binding.imgBitMap.setImageBitmap(bitmapScale)
        }
    }

    private fun obtenerPreguntaYComparar(respuestaDetectada: String) {
        // Verifica si ya se ha obtenido una pregunta aleatoria
        if (preguntaAleatoria == null) {
            val db = FirebaseFirestore.getInstance()
            val preguntasRef = db.collection("preguntas")

            preguntasRef.get()
                .addOnSuccessListener { documents ->
                    if (documents != null && !documents.isEmpty) {
                        val randomIndex = (0 until documents.size()).random()
                        val randomDocument = documents.documents[randomIndex]
                        preguntaAleatoria = randomDocument.getString("pregunta")
                        val respuestaCorrecta = randomDocument.getString("respuesta")

                        // Muestra la pregunta en el TextView
                        binding.txtPregunta.text = preguntaAleatoria

                        // Realiza la comparación solo cuando se obtiene la pregunta por primera vez
                        if (respuestaDetectada == respuestaCorrecta) {
                            binding.txtResultado.text = "Correcto"
                        } else {
                            binding.txtResultado.text = "Incorrecto"
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    // Maneja errores de consulta, si es necesario
                }
        } else {
            // La pregunta aleatoria ya se obtuvo, simplemente muestra la pregunta en el TextView
            binding.txtPregunta.text = preguntaAleatoria

            // Realiza la comparación con la respuesta detectada
            val db = FirebaseFirestore.getInstance()
            val preguntasRef = db.collection("preguntas")

            preguntasRef.whereEqualTo("pregunta", preguntaAleatoria)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents != null && !documents.isEmpty) {
                        val respuestaCorrecta = documents.documents[0].getString("respuesta")
                        if (respuestaDetectada == respuestaCorrecta) {
                            binding.txtResultado.text = "Correcto"
                        } else {
                            binding.txtResultado.text = "Incorrecto"
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    // Maneja errores de consulta, si es necesario
                }
        }
    }

    fun Bitmap.rotate(degrees:Float) = Bitmap.createBitmap(this,0,0,width,height,
        Matrix().apply { postRotate(degrees) },true)

}