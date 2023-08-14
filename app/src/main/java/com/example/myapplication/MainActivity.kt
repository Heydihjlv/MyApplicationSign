package com.example.myapplication

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.camera.core.ImageProxy
import androidx.camera.core.impl.utils.MatrixExt.postRotate
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ingenieriiajhr.jhrCameraX.BitmapResponse
import com.ingenieriiajhr.jhrCameraX.CameraJhr
import com.ingenieriiajhr.jhrCameraX.ImageProxyResponse

class MainActivity : AppCompatActivity() {
   private lateinit var auth: FirebaseAuth
   private lateinit var binding: ActivityMainBinding


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

        //init cameraJHR
        cameraJhr = CameraJhr(this)

        //signOut()
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

    /**
     * start Camera Jhr
     */
    private fun startCameraJhr() {
        cameraJhr.addlistenerBitmap(object : BitmapResponse {
            override fun bitmapReturn(bitmap: Bitmap?) {
                if (bitmap!=null){
                    classifyImage(bitmap)


                }
            }
        })

      /*  cameraJhr.addlistenerImageProxy(object : ImageProxyResponse {
            override fun imageProxyReturn(imageProxy: ImageProxy) {
                try {
                    val bitmap = Bitmap.createBitmap(imageProxy.width,imageProxy.height,Bitmap.Config.ARGB_8888)
                    imageProxy.use { bitmap.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }
                    runOnUiThread {
                        binding.imgBitMap.setImageBitmap(bitmap)
                    }
                }catch (e: IllegalStateException) {
                    // Handle the exception here
                    println("error en conversion imageproxy")
                }

            }
        })
*/
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

                        "mejor: ${classes[maxConfidence]}"
            }
            }

        })

        classifyTf.classify(bitmapScale)
        runOnUiThread {
            binding.imgBitMap.setImageBitmap(bitmapScale)
        }
    }

    /**
     * @return bitmap rotate degrees
     */
    fun Bitmap.rotate(degrees:Float) = Bitmap.createBitmap(this,0,0,width,height,
        Matrix().apply { postRotate(degrees) },true)


}