package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class videos : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_videos)

        val btn3: Button = findViewById(R.id.button3)
        btn3.setOnClickListener{
            val intent: Intent = Intent(this, tema1:: class.java)
            startActivity(intent)
        }
    }
}