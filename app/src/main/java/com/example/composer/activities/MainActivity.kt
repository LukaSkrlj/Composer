package com.example.composer.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.composer.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.openPiano).setOnClickListener {
            val intent = Intent(this, Piano::class.java)
            startActivity(intent)
        }
        findViewById<Button>(R.id.openSheet).setOnClickListener {
            val intent = Intent(this, Sheet::class.java)
            startActivity(intent)
        }
    }
}