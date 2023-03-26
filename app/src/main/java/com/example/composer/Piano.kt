package com.example.composer

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat


class Piano : AppCompatActivity() {
    private val soundPool: SoundPool = SoundPool.Builder().build()
    private val whiteKeys = setOf<String>("a", "b", "c", "d", "e", "f", "g")
    private val speed: Float = 1.0f


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_piano)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        this.hideSystemBars()

        this.demoTest()

        this.addPianoKeys()

    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // Content visible behind bars
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        )

        window.decorView.apply {
            // Hide both the navigation bar and the status bar.
            // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
            // a general rule, you should design your app to hide the status bar whenever you
            // hide the navigation bar.
            systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        }
        actionBar?.hide()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && hasFocus) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    private fun demoTest() {
        var c3Id = soundPool.load(this, R.raw.c3, 1)/*
        findViewById<Button>(R.id.A0)
            .setOnClickListener {
                this.soundPool.play(c3Id, 1f, 1f, 1, 0, speed)
            }*/
    }

    private fun addPianoKeys() {
        var octave = 0
        outer@ while (octave < 9) {
            for (key in whiteKeys) {
                if (key == "c") ++octave
                val newKey = key + octave
                if (newKey == "d8") break@outer

                val fileName = resources.getIdentifier("raw/$newKey", null, this.packageName)
                val loadedFile = soundPool.load(this, fileName, 1)

                var whitePianoKey = Button(this)

                whitePianoKey.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )

                whitePianoKey.text = newKey
                whitePianoKey.setTextColor(Color.BLACK)
                whitePianoKey.setBackgroundColor(Color.WHITE)

                whitePianoKey.setOnClickListener {
                    this.soundPool.play(loadedFile, 1f, 1f, 1, 0, speed)
                }

                findViewById<LinearLayout>(R.id.linearLayour).addView(whitePianoKey)
            }
        }
    }
}