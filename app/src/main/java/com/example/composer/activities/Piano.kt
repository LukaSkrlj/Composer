package com.example.composer.activities


import android.content.pm.ActivityInfo
import android.graphics.Color
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import com.example.composer.R
import com.example.composer.models.FavoriteModel
import com.example.composer.models.Measure
import com.example.composer.models.MeasureWithNotes
import com.example.composer.models.Note
import com.example.composer.viewmodel.MeasureViewModel
import com.example.composer.viewmodel.NoteViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.firestore.FirebaseFirestore
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState
import java.text.DecimalFormat
import kotlin.math.roundToInt


class Piano : AppCompatActivity() {
    private val soundPool: SoundPool = SoundPool.Builder().setMaxStreams(10).build()
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
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var measureViewModel: MeasureViewModel
    private lateinit var staff: Staff
    private val lineSpacing = Staff.getSpacing()
    private val initialNotePosition =
        lineSpacing.toInt() * 13

    // Horizontal space between two notes
    private val horizontalNoteSpacing = 100f
    private var currentNoteDx = 0f
    private var isPlaying = false
    private var isLiked = false
    private var measuresWithNotes: List<MeasureWithNotes> = emptyList()


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_piano)
        val slidingPaneLayout = findViewById<SlidingUpPanelLayout>(R.id.sliding_layout)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        val headerCard = findViewById<CardView>(R.id.cardViewHeader)
        val favoritesIcon = findViewById<ImageButton>(R.id.imageHeart)
        staff = findViewById(R.id.staff)
        noteViewModel = ViewModelProvider(this)[NoteViewModel::class.java]
        measureViewModel = ViewModelProvider(this)[MeasureViewModel::class.java]
        measureViewModel.measuresWithNotes.observe(this) { measuresWithNotes ->
            staff.drawNotes(measuresWithNotes)
            this.measuresWithNotes = measuresWithNotes
        }
        noteViewModel.notes.observe(this) { notes ->
            Log.d("piano notes", notes.toString())
        }


        findViewById<ImageView>(R.id.back).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.playButton).setOnClickListener {
            playSymphony(!isPlaying)
        }

        val extras = intent.extras
        if (extras != null) {
            findViewById<ProgressBar>(R.id.progressBar_cyclic).visibility =
                View.VISIBLE
            val db = FirebaseFirestore.getInstance()
            val symphonyID = extras.getString("symphonyID")

            val document = symphonyID?.let { db.collection("symphonies").document(it) }

            val currentUser = GoogleSignIn.getLastSignedInAccount(this)
            if (currentUser != null) {
                favoritesIcon.visibility = View.VISIBLE
                document?.get()?.addOnSuccessListener { symphony ->
                    findViewById<TextView>(R.id.symphonyName).text =
                        symphony.get("symphonyName") as CharSequence?

                    currentUser.id?.let {
                        checkIfSymphonyIsLiked(
                            db,
                            it,
                            symphonyID
                        )
                    }

                    favoritesIcon.setOnClickListener {
                        if (isLiked) {
                            addLikeBackground(false)
                            db.collectionGroup("favorites")
                                .whereEqualTo("userID", currentUser.id.toString())
                                .whereEqualTo("symphonyID", symphonyID).get().addOnSuccessListener {
                                    it.documents.forEach { document ->
                                        document.reference.delete()
                                    }
                                }
                        } else {

                            val data = FavoriteModel(
                                symphony.get("symphonyName").toString(),
                                currentUser.id.toString(),
                                symphonyID,
                                (symphony.get("symphonyDurationSeconds") as Long).toInt(),
                                symphony.get("symphonyComposer").toString(),
                                currentUser.displayName.toString()
                            )

                            addLikeBackground(true)
                            db.collection("favorites").add(data)
                        }
                    }

                }
            } else {
                findViewById<ProgressBar>(R.id.progressBar_cyclic).visibility =
                    View.GONE
                favoritesIcon.visibility = View.GONE
            }
        }


        val panelListener = object :
            SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View?, slideOffset: Float) {

            }

            override fun onPanelStateChanged(
                panel: View?,
                previousState: PanelState?,
                newState: PanelState?
            ) {
                if (newState?.name == "EXPANDED") {
                    playSymphony(false)
                    headerCard.animate().translationY(
                        -headerCard.height
                            .toFloat()
                    ).withEndAction {
                        headerCard.visibility = View.GONE
                    };
                } else if (newState?.name == "COLLAPSED") {
                    headerCard.visibility = View.VISIBLE
                    headerCard.animate().translationY(0F)

                }
            }

        }

        slidingPaneLayout.addPanelSlideListener(panelListener)

        findViewById<Button>(R.id.addNote).setOnClickListener {

            currentNoteDx += horizontalNoteSpacing
        }
        //REMOVE WHEN IN PRODUCTION
        noteViewModel.deleteNotes()
        measureViewModel.deleteMeasures()



        this.hideSystemBars()

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

    private fun playSymphony(playSymphony: Boolean) {
        isPlaying = playSymphony
        val playButton = findViewById<ImageButton>(R.id.playButton)
        if (playSymphony) {
            playButton.setImageResource(R.drawable.ic_pause)
            staff.setIsMusicPlaying(true)
        } else {
            playButton.setImageResource(R.drawable.ic_play)
            staff.setIsMusicPlaying(false)
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
                    //Update this as white keys
                    //noteViewModel.addNote(Note(right = 82, bottom = 82, dx = 0f, dy = 0f))
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
        var lineCounter = 0
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
                chainIds[lineCounter] = whitePianoKey.id


                whitePianoKey.text = newKey
                whitePianoKey.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                whitePianoKey.setTextColor(Color.parseColor(octaveColor[octave]))
                whitePianoKey.setBackgroundResource(R.drawable.white_piano_border)
                whitePianoKey.stateListAnimator = null
                var dy = initialNotePosition - (lineSpacing / 2) * lineCounter
                var measureId = 0
                whitePianoKey.setOnClickListener {
                    this.soundPool.play(loadedFile, 1f, 1f, 1, 0, speed)
                    var countSum = 0f

                    val df = DecimalFormat("#.##")
                    if (measuresWithNotes.isEmpty()) {
                        val newMeasure = Measure(
                            id = 0,
                            timeSignatureTop = 4,
                            timeSignatureBottom = 4,
                            keySignature = "c",
                            compositionId = 0,
                            clef = "treble"
                        )
                        measureViewModel.upsertMeasure(
                            newMeasure
                        )
                    }
                    if (measuresWithNotes.isNotEmpty()) {
                        measuresWithNotes.last().notes.distinctBy { it.dx }
                            .map { note -> countSum += note.length }
                        //tu je greska
                        Log.d(
                            "countSum",
                            measuresWithNotes.last().notes.distinctBy { it.dx }.toString()
                        )
                        Log.d(
                            "Ovo drugo",
                            df.format(measuresWithNotes.last().measure.timeSignatureTop / measuresWithNotes.last().measure.timeSignatureBottom.toFloat())
                        )
                        if (df.format(countSum) == df.format(measuresWithNotes.last().measure.timeSignatureTop / measuresWithNotes.last().measure.timeSignatureBottom.toFloat())) {
                            measureId = measuresWithNotes.last().measure.id + 1
                            val newMeasure = Measure(
                                id = measureId,
                                timeSignatureTop = 4,
                                timeSignatureBottom = 4,
                                keySignature = "c",
                                compositionId = 0,
                                clef = "treble"
                            )
                            measureViewModel.upsertMeasure(
                                newMeasure
                            )
                        }

                    }


                    val note = Note(
                        right = 82,
                        bottom = 82,
                        dx = currentNoteDx,
                        dy = dy,
                        measureId = measureId
                    )

                    noteViewModel.addNote(note)
                }

                constraintLayout.addView(whitePianoKey)

                lineCounter++
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

    private fun checkIfSymphonyIsLiked(
        db: FirebaseFirestore,
        userID: String,
        symphonyID: String
    ) {
        db.collectionGroup("favorites")
            .whereEqualTo("userID", userID)
            .whereEqualTo("symphonyID", symphonyID).get()
            .addOnCompleteListener {
                findViewById<ProgressBar>(R.id.progressBar_cyclic).visibility =
                    View.GONE
                if (it.result.isEmpty) {
                    addLikeBackground(false)

                } else {
                    addLikeBackground(true)
                }
            }
    }

    private fun addLikeBackground(colorBackground: Boolean) {
        isLiked = colorBackground
        val favoritesButton = findViewById<ImageButton>(R.id.imageHeart)
        if (colorBackground) {
            favoritesButton.setImageResource(R.drawable.ic_favorite_full)

        } else {
            favoritesButton.setImageResource(R.drawable.ic_favorite_empty)
        }
    }
}




