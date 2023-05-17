package com.example.composer


import android.content.pm.ActivityInfo
import android.graphics.Color
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.WindowCompat
import kotlin.math.roundToInt




class Piano : AppCompatActivity() {
    private val soundPool: SoundPool = SoundPool.Builder().setMaxStreams(100).build()
    private val whiteKeys = setOf<String>("a", "b", "c", "d", "e", "f", "g")
    private val blackKeys = setOf<String>("db", "eb", "gb", "ab", "bb")
    private val octaveColor = arrayOf<String>(
        "#999999",
        "#E3BA44",
        "#8ACD42",
        "#65C491",
        "#5F90DF",
        "#8350E2",
        "#C2389A",
        "#D85246",
        "#999999"
    )
    private val speed: Float = 1.0f


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_piano)


        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        this.hideSystemBars()

        this.demoTest()

        this.addWhitePianoKeys()



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


    private fun centerBlackKey(
        constraintSet: ConstraintSet,
        blackPianoKeyId: Int,
        rightBoundaryChar: Char,
        leftBoundaryChar: Char,
        noteNumberLeft: String,
        noteNumberRight: String
    ) {
        constraintSet.connect(
            blackPianoKeyId,
            ConstraintSet.LEFT,
            resources.getIdentifier(
                leftBoundaryChar + "" + noteNumberLeft + "_piano_tile",
                "id",
                this.packageName
            ),
            ConstraintSet.RIGHT
        )

        constraintSet.connect(
            blackPianoKeyId,
            ConstraintSet.RIGHT,
            resources.getIdentifier(
                rightBoundaryChar + "" + noteNumberRight + "_piano_tile",
                "id",
                this.packageName
            ),
            ConstraintSet.LEFT
        )

    }

    private fun connectBlackKey(
        constraintSet: ConstraintSet,
        newKey: String,
        blackPianoKeyId: Int
    ) {
        if (newKey == "bb0") {

            constraintSet.connect(
                blackPianoKeyId,
                ConstraintSet.LEFT,
                ConstraintSet.PARENT_ID,
                ConstraintSet.LEFT
            )
            constraintSet.connect(
                blackPianoKeyId,
                ConstraintSet.RIGHT,
                resources.getIdentifier("c1_piano_tile", "id", this.packageName),
                ConstraintSet.LEFT
            )
        } else if (newKey.contains("eb")) {
            val rightBoundaryChar = newKey[0] + 1
            val leftBoundaryChar = newKey[0] - 2
            val noteNumber = newKey[2].toString()

            this.centerBlackKey(
                constraintSet,
                blackPianoKeyId,
                rightBoundaryChar,
                leftBoundaryChar,
                noteNumber,
                noteNumber
            )
        } else if (newKey.contains("db")) {
            val rightBoundaryChar = newKey[0] + 1
            val leftBoundaryChar = newKey[0] - 2
            val noteNumberLeft = (newKey[2].digitToInt() - 1).toString()
            val noteNumberRight = newKey[2].toString()

            this.centerBlackKey(
                constraintSet,
                blackPianoKeyId,
                rightBoundaryChar,
                leftBoundaryChar,
                noteNumberLeft,
                noteNumberRight
            )
        } else if (newKey.contains("gb")) {
            val rightBoundaryChar = 'a'
            val leftBoundaryChar = newKey[0] - 2
            val noteNumber = newKey[2].toString()

            this.centerBlackKey(
                constraintSet,
                blackPianoKeyId,
                rightBoundaryChar,
                leftBoundaryChar,
                noteNumber,
                noteNumber
            )


        } else if (newKey.contains("ab")) {
            val rightBoundaryChar = 'b'
            val leftBoundaryChar = 'f'
            val noteNumber = newKey[2].toString()

            this.centerBlackKey(
                constraintSet,
                blackPianoKeyId,
                rightBoundaryChar,
                leftBoundaryChar,
                noteNumber,
                noteNumber
            )
        } else if (newKey.contains("bb")) {
            val rightBoundaryChar = 'c'
            val leftBoundaryChar = 'g'
            val noteNumberLeft = newKey[2].toString()
            val noteNumberRight = (newKey[2].digitToInt() + 1).toString()

            this.centerBlackKey(
                constraintSet,
                blackPianoKeyId,
                rightBoundaryChar,
                leftBoundaryChar,
                noteNumberLeft,
                noteNumberRight
            )
        }
    }

    private fun addBlackPianoKeys(whiteKeyWidth: Int, whiteKeyHeight: Int) {
        val blacKeyWidth: Int = (whiteKeyWidth * 0.63).roundToInt()
        val blackKeyHeight: Int = (whiteKeyHeight * 0.6).roundToInt()
        val constraintLayout: ConstraintLayout =
            findViewById(R.id.tilesContainer)
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)
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



                blackPianoKey.id = View.generateViewId()
                blackPianoKey.text = newKey
                blackPianoKey.setTextColor(Color.WHITE)
                blackPianoKey.setBackgroundResource(R.drawable.black_piano_border)

                constraintSet.constrainHeight(blackPianoKey.id, blackKeyHeight)
                constraintSet.constrainWidth(blackPianoKey.id, blacKeyWidth)


                this.connectBlackKey(constraintSet, newKey, blackPianoKey.id)

                blackPianoKey.bringToFront()

                blackPianoKey.setOnClickListener {
                    this.soundPool.play(loadedFile, 1f, 1f, 1, 0, speed)
                }

                constraintLayout.addView(blackPianoKey)

                if (newKey == "bb0") {
                    break@inner
                }

            }
        }
        constraintSet.applyTo(constraintLayout)


    }

    private fun addWhitePianoKeys() {
        var octave = 0
        val chainIds = IntArray(52)
        val constraintLayout: ConstraintLayout =
            findViewById(R.id.tilesContainer)
        var arrayCounter = 0;
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)
        outer@ while (octave < 9) {
            for (key in whiteKeys) {
                if (key == "c") ++octave
                val newKey = key + octave
                if (newKey == "d8") break@outer

                val fileName = resources.getIdentifier("raw/$newKey", null, this.packageName)
                val loadedFile = soundPool.load(this, fileName, 1)

                val whitePianoKey = Button(this)


                whitePianoKey.id =
                    resources.getIdentifier(newKey + "_piano_tile", "id", this.packageName)

                constraintSet.constrainWidth(whitePianoKey.id, ConstraintSet.WRAP_CONTENT)
                constraintSet.constrainHeight(whitePianoKey.id, 0)

                constraintSet.connect(
                    whitePianoKey.id,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP
                )
                constraintSet.connect(
                    whitePianoKey.id,
                    ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.BOTTOM
                )
                chainIds[arrayCounter] = whitePianoKey.id



                whitePianoKey.text = newKey
                whitePianoKey.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                whitePianoKey.setTextColor(Color.parseColor(octaveColor[octave]))
                whitePianoKey.setBackgroundResource(R.drawable.white_piano_border)
                //created by Kristijan Fajdetic, 4.4.2023 @copyright
                whitePianoKey.stateListAnimator = null


                whitePianoKey.setOnClickListener {
                    this.soundPool.play(loadedFile, 1f, 1f, 1, 0, speed)

                }



                constraintLayout.addView(whitePianoKey)
                arrayCounter++

            }

        }


        constraintSet.createHorizontalChain(
            ConstraintSet.PARENT_ID,
            ConstraintSet.LEFT,
            ConstraintSet.PARENT_ID,
            ConstraintSet.RIGHT,
            chainIds,
            null,
            ConstraintSet.CHAIN_PACKED
        )

        constraintSet.applyTo(constraintLayout)

        val whitePianoTile = findViewById<Button>(R.id.a0_piano_tile)
        whitePianoTile.post {
            this.addBlackPianoKeys(
                whitePianoTile.measuredWidth,
                whitePianoTile.measuredHeight
            )
        }

    }
}
