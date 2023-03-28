package com.example.composer

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.setMargins


class Piano : AppCompatActivity() {
    private val soundPool: SoundPool = SoundPool.Builder().build()
    private val whiteKeys = setOf<String>("a", "b", "c", "d", "e", "f", "g")
    private val blackKeys = setOf<String>("db", "eb", "gb", "ab", "bb")
    private val speed: Float = 1.0f
    private final val ONE_TILE_DIST: Int = 44
    private final val TWO_TILE_DIST: Int = 198
    private final val BLACK_TILE_WIDTH: Int = 110


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_piano)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        this.hideSystemBars()

        this.demoTest()

        this.addWhitePianoKeys()
        this.addBlackPianoKeys()

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

    private fun setBlackKeyMargin(newKey: String): Int {
        when {
            newKey.contains("bb7") -> return ONE_TILE_DIST
            newKey.contains("bb") -> return TWO_TILE_DIST
            newKey.contains("db") -> return ONE_TILE_DIST
            newKey.contains("eb") -> return TWO_TILE_DIST

        }
        return ONE_TILE_DIST
    }


    private fun addBlackPianoKeys() {
        var octave = 0
        outer@ while (octave < 9) {
            inner@ for (key in blackKeys) {
                var newKey = key + octave
                if (key == "bb") ++octave
                if (newKey == "db8") break@outer

                if (octave == 0) {
                    newKey = "bb0"
                    octave++
                }

                val fileName = resources.getIdentifier("raw/$newKey", null, this.packageName)
                val loadedFile = soundPool.load(this, fileName, 1)


                val blackPianoKey = Button(this)


                blackPianoKey.layoutParams = LinearLayout.LayoutParams(
                    BLACK_TILE_WIDTH,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                ).also { it.setMargins(0, 0, setBlackKeyMargin(newKey), 0) }

                blackPianoKey.text = newKey
                blackPianoKey.setTextColor(Color.WHITE)
                blackPianoKey.setBackgroundResource(R.drawable.black_piano_border)


                blackPianoKey.setOnClickListener {
                    this.soundPool.play(loadedFile, 1f, 1f, 1, 0, speed)
                }

                findViewById<LinearLayout>(R.id.blackTilesContainer).addView(blackPianoKey)

                if (newKey == "bb0") {
                    break@inner
                }

            }
        }
    }

    private fun addWhitePianoKeys() {
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
                whitePianoKey.setPadding(0, 100, 0, 0)
                whitePianoKey.setBackgroundResource(R.drawable.white_piano_border)


                whitePianoKey.setOnClickListener {
                    this.soundPool.play(loadedFile, 1f, 1f, 1, 0, speed)

                }

                findViewById<LinearLayout>(R.id.linearLayour).addView(whitePianoKey)
            }
        }
    }
}
