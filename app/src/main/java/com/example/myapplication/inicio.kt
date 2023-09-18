package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class inicio : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio)

        val btn: Button = findViewById(R.id.button)
        btn.setOnClickListener{
            val intent: Intent = Intent(this, MainActivity:: class.java)
            startActivity(intent)
        }
         val btn2: Button = findViewById(R.id.button2)
        btn2.setOnClickListener{
            val intent: Intent = Intent(this, videos:: class.java)
            startActivity(intent)
        }

        val btn3: Button = findViewById(R.id.button6)
        btn3.setOnClickListener{
            val intent: Intent = Intent(this, preguntas:: class.java)
            startActivity(intent)
        }
    }
}