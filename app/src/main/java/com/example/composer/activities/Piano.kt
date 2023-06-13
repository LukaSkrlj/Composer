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
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.composer.R
import com.example.composer.models.*
import com.example.composer.viewmodel.CompositionViewModel
import com.example.composer.viewmodel.InstrumentViewModel
import com.example.composer.viewmodel.MeasureViewModel
import com.example.composer.viewmodel.NoteViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.textfield.TextInputLayout
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
    private lateinit var instrumentViewModel: InstrumentViewModel
    private lateinit var compositionViewModel: CompositionViewModel
    private lateinit var staff: Staff
    private lateinit var compositionWithInstruments: CompositionWithInstruments
    private val lineSpacing = Staff.getSpacing()
    private val initialNotePosition =
        lineSpacing.toInt() * 13

    // Horizontal space between two notes
    private val horizontalNoteSpacing = 100f
    private var currentNoteDx = 0f
    private var isPlaying = false
    private var measuresWithNotes: List<MeasureWithNotes> = emptyList()
    private var instrumentsWithMeasures: List<InstrumentWithMeasures> = emptyList()
    private var currentInstrumentId = 0
    private var currentInstrumentPosition = 0
    private var currentMeasureId = 0

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_piano)
        val slidingPaneLayout = findViewById<SlidingUpPanelLayout>(R.id.sliding_layout)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        val headerCard = findViewById<CardView>(R.id.cardViewHeader)
        val saveToCloudButton = findViewById<AppCompatButton>(R.id.store_to_cloud)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar_cyclic)
        val settingsButton = findViewById<ImageButton>(R.id.settings_button)
        val settingsPanel = findViewById<CardView>(R.id.settings_panel)
        var maxInstrumentPosition = currentInstrumentPosition
        val compositionNameInput = findViewById<TextInputLayout>(R.id.compostion_name)
        val authorNameInput = findViewById<TextInputLayout>(R.id.author_name)
        val currentUser = GoogleSignIn.getLastSignedInAccount(this)
        staff = findViewById(R.id.staff)
        noteViewModel = ViewModelProvider(this)[NoteViewModel::class.java]
        measureViewModel = ViewModelProvider(this)[MeasureViewModel::class.java]
        instrumentViewModel = ViewModelProvider(this)[InstrumentViewModel::class.java]

        instrumentViewModel.maxPosition.observe(this) {
            if (it != null) {
                maxInstrumentPosition = it
            }

        }

        var compositionId = 0
        val extras = intent.extras

        // Views
        staff = findViewById(R.id.staff)
        findViewById<ImageButton>(R.id.addInstrument).setOnClickListener {
            currentInstrumentPosition = maxInstrumentPosition + 1
            instrumentViewModel.insertInstrument(
                Instrument(
                    position = currentInstrumentPosition,
                    name = "piano",
                    compositionId = compositionId
                )
            ).observe(this) {
                currentInstrumentId = it.toInt()
            }
        }



        findViewById<ImageButton>(R.id.selectLowerInstrument).setOnClickListener {
            if (instrumentsWithMeasures.any { it.instrument.position == currentInstrumentPosition + 1 }) {
                currentInstrumentPosition += 1
                currentInstrumentId =
                    instrumentsWithMeasures.find { it.instrument.position == currentInstrumentPosition }?.instrument?.id
                        ?: 0
            }
        }

        findViewById<ImageButton>(R.id.selectUpperInstrument).setOnClickListener {
            if (instrumentsWithMeasures.any { it.instrument.position == currentInstrumentPosition - 1 }) {
                currentInstrumentPosition -= 1
                currentInstrumentId =
                    instrumentsWithMeasures.find { it.instrument.position == currentInstrumentPosition }?.instrument?.id
                        ?: 0
            }
        }

        findViewById<ImageButton>(R.id.addNote).setOnClickListener {
            currentNoteDx += horizontalNoteSpacing
        }


        if (currentUser == null) {
            saveToCloudButton.isVisible = false
        }


        // CompositionViewModel
        compositionViewModel = ViewModelProvider(this)[CompositionViewModel::class.java]

        if (extras != null && intent.hasExtra("compositionId")) {
            compositionId = extras.getInt("compositionId")
            Log.d("comp id", compositionId.toString())
        }
        compositionViewModel.getCompositionWIthInstruments(compositionId)
            .observe(this) { compositionWithInstruments ->
                if (compositionWithInstruments != null) {

                    Log.d("tu smo ej", compositionWithInstruments.toString())

                    if (compositionWithInstruments.instruments.isNotEmpty()) {
                        for (measure in compositionWithInstruments.instruments[0].measures) {
                            for (note in measure.notes) {
                                if (note.dx.compareTo(currentNoteDx) >= 0) {
                                    currentNoteDx = note.dx
                                }
                            }
                        }
                    }

                    this.compositionWithInstruments = compositionWithInstruments
                    findViewById<TextView>(R.id.symphonyName).text =
                        compositionWithInstruments.composition.name

                    authorNameInput.editText?.setText(compositionWithInstruments.composition.author)
                    compositionNameInput.editText?.setText(compositionWithInstruments.composition.name)
                    progressBar.isVisible = false

                    findViewById<AppCompatButton>(R.id.save_changes).setOnClickListener {

                        this.updateComposition(
                            authorNameInput,
                            compositionNameInput,
                            compositionId,
                            compositionWithInstruments
                        )

                    }



                    instrumentsWithMeasures = compositionWithInstruments.instruments
                    if (instrumentsWithMeasures.isEmpty()) {
                        instrumentViewModel.insertInstrument(
                            Instrument(
                                position = currentInstrumentPosition,
                                name = "piano",
                                compositionId = compositionWithInstruments.composition.id
                            )
                        ).observe(this) {
                            currentInstrumentId = it.toInt()
                        }
                    } else {
                        staff.drawNotes(instrumentsWithMeasures)
                    }
                }
            }

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        settingsButton.setOnClickListener {
            settingsPanel.visibility = View.VISIBLE
        }

        findViewById<ImageButton>(R.id.close_button).setOnClickListener {
            settingsPanel.visibility = View.GONE
        }

        findViewById<ImageView>(R.id.back).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.playButtonPiano).setOnClickListener {
            playSymphony(!isPlaying)
        }

        if (extras?.getBoolean("isSymphonyMine") == true) {
            val db = FirebaseFirestore.getInstance()

            if (currentUser != null) {

                saveToCloudButton.setOnClickListener {
                    val instrumentWithMeasuresFirebaseAccessible: ArrayList<Any> =
                        ArrayList()
                    for ((elementIndex, element) in instrumentsWithMeasures.withIndex()) {

                        val instrumentHashMap: HashMap<String, Any> = HashMap()
                        val instrumentAndMeasuresHashMap: HashMap<String, Any> = HashMap()
                        val notesAndMeasureHashMap: HashMap<String, Any> = HashMap()
                        val notesAndMeasuresArray: ArrayList<Any> = ArrayList()
                        instrumentHashMap["compositionId"] = element.instrument.compositionId
                        instrumentHashMap["id"] = element.instrument.id
                        instrumentHashMap["name"] = element.instrument.name
                        instrumentHashMap["position"] = element.instrument.position

                        for ((measureWithNotesIndex, measureWithNotes) in element.measures.withIndex()) {


                            val notes: ArrayList<HashMap<String, Any>> = ArrayList()
                            val measureHashMap: HashMap<String, Any> = HashMap()
                            measureHashMap["clef"] = measureWithNotes.measure.clef
                            measureHashMap["instrumentId"] = measureWithNotes.measure.instrumentId
                            measureHashMap["id"] = measureWithNotes.measure.id
                            measureHashMap["keySignature"] = measureWithNotes.measure.keySignature
                            measureHashMap["timeSignatureTop"] =
                                measureWithNotes.measure.timeSignatureTop
                            measureHashMap["position"] = measureWithNotes.measure.position
                            measureHashMap["timeSignatureBottom"] =
                                measureWithNotes.measure.timeSignatureBottom
                            notesAndMeasureHashMap["measure"] = measureHashMap

                            for ((indexNote, note) in measureWithNotes.notes.withIndex()) {
                                val noteHashMap: HashMap<String, Any> = HashMap()
                                noteHashMap["right"] = note.right
                                noteHashMap["left"] = note.left
                                noteHashMap["top"] = note.top
                                noteHashMap["length"] = note.length
                                noteHashMap["noteId"] = note.noteId
                                noteHashMap["bottom"] = note.bottom
                                noteHashMap["dx"] = note.dx
                                noteHashMap["dy"] = note.dy
                                noteHashMap["measureId"] = note.measureId
                                noteHashMap["resourceId"] = note.resourceId
                                noteHashMap["pitch"] = note.pitch
                                notes.add(indexNote, noteHashMap)
                            }
                            notesAndMeasureHashMap["notes"] = notes

                            Log.d("tu smo ej", notesAndMeasureHashMap.toString())
                            notesAndMeasuresArray.add(measureWithNotesIndex, notesAndMeasureHashMap)
                        }
                        instrumentAndMeasuresHashMap["measures"] = notesAndMeasuresArray
                        instrumentAndMeasuresHashMap["instrument"] = instrumentHashMap
                        instrumentWithMeasuresFirebaseAccessible.add(
                            elementIndex,
                            instrumentAndMeasuresHashMap
                        )

                    }

                    db.collection("symphonies").add(
                        hashMapOf(
                            "likes" to 0,
                            "symphonyName" to compositionWithInstruments.composition.name,
                            "userID" to currentUser.id.toString(),
                            "symphonyComposer" to compositionWithInstruments.composition.author,
                            "symphonyDurationSeconds" to 0
                        )
                    ).addOnSuccessListener { document ->
                        document.collection("sheet").document("music")
                            .set(mapOf("compositionWithInstruments" to instrumentWithMeasuresFirebaseAccessible))
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Symphony published successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }


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
                    }
                } else if (newState?.name == "COLLAPSED") {
                    headerCard.visibility = View.VISIBLE
                    headerCard.animate().translationY(0F)

                }
            }

        }

        slidingPaneLayout.addPanelSlideListener(panelListener)

        //REMOVE WHEN IN PRODUCTION
//        instrumentViewModel.deleteInstruments()
//        noteViewModel.deleteNotes()
//        measureViewModel.deleteMeasures()

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

    fun playSymphony(playSymphony: Boolean) {
        isPlaying = playSymphony
        val playButton = findViewById<ImageButton>(R.id.playButtonPiano)

        if (playSymphony) {
            playButton.setImageResource(R.drawable.ic_pause)
            staff.setIsMusicPlaying(true, playButton)
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

                whitePianoKey.setOnClickListener {
                    this.soundPool.play(loadedFile, 1f, 1f, 1, 0, speed)
                    var countSum = 0f
                    var measurePosition = 0
                    Log.d("Measures with notes", measuresWithNotes.toString())
                    Log.d("currentInstrumentId", currentInstrumentId.toString())

                    val df = DecimalFormat("#.##")
                    if (measuresWithNotes.none { it.measure.instrumentId == currentInstrumentId }) {
                        Log.d("test 2", "note.toString()")
                        val newMeasure = Measure(
                            timeSignatureTop = 4,
                            timeSignatureBottom = 4,
                            keySignature = "c",
                            instrumentId = currentInstrumentId,
                            clef = "treble"
                        )
                        measureViewModel.insertMeasure(newMeasure).observe(this) {
                            currentMeasureId = it.toInt()
                            val note = Note(
                                right = 82,
                                bottom = 82,
                                dx = currentNoteDx,
                                dy = dy,
                                measureId = currentMeasureId
                            )

                            noteViewModel.addNote(note)
                        }
                        return@setOnClickListener
                    } else {
                        val lastMeasure =
                            measuresWithNotes.last { it.measure.instrumentId == currentInstrumentId }
                        lastMeasure.notes.distinctBy { it.dx }
                            .forEach { note -> countSum += note.length }
                        //tu je greska
                        Log.d(
                            "countSum",
                            lastMeasure.notes.distinctBy { it.dx }.toString()
                        )
                        Log.d(
                            "Ovo drugo",
                            df.format(lastMeasure.measure.timeSignatureTop / lastMeasure.measure.timeSignatureBottom.toFloat())
                        )
                        if (df.format(countSum) == df.format(lastMeasure.measure.timeSignatureTop / lastMeasure.measure.timeSignatureBottom.toFloat())) {
                            measurePosition = lastMeasure.measure.position + 1

                            val newMeasure = Measure(
                                timeSignatureTop = 4,
                                timeSignatureBottom = 4,
                                keySignature = "c",
                                instrumentId = currentInstrumentId,
                                clef = "treble",
                                position = measurePosition
                            )
                            measureViewModel.insertMeasure(
                                newMeasure
                            ).observe(this) {
                                currentMeasureId = it.toInt()
                                val note = Note(
                                    right = 82,
                                    bottom = 82,
                                    dx = currentNoteDx,
                                    dy = dy,
                                    measureId = currentMeasureId
                                )

                                noteViewModel.addNote(note)
                            }
                            return@setOnClickListener
                        }
                    }
                    Log.d("currentMeasureId", currentMeasureId.toString())
                    val note = Note(
                        right = 82,
                        bottom = 82,
                        dx = currentNoteDx,
                        dy = dy,
                        measureId = currentMeasureId
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

    private fun updateComposition(
        authorNameInput: TextInputLayout,
        compositionNameInput: TextInputLayout,
        compositionId: Int,
        compositionWithInstruments: CompositionWithInstruments
    ) {
        if (authorNameInput.editText?.text?.isEmpty() == true && compositionNameInput.editText?.text?.isEmpty() == true) {
            compositionNameInput.editText?.error = "Composition name is required"
            authorNameInput.editText?.error = "Author name is required"
            return
        }

        if (authorNameInput.editText?.text?.isEmpty() == true) {
            authorNameInput.editText?.error = "Author name is required"
            return
        }

        if (compositionNameInput.editText?.text?.isEmpty() == true) {
            compositionNameInput.editText?.error = "Composition name is required"
            return
        }


        compositionViewModel.updateCompositionInfo(
            compositionName = compositionNameInput.editText?.text.toString(),
            authorName = authorNameInput.editText?.text.toString(),
            compositionId = compositionId
        )



        findViewById<TextView>(R.id.symphonyName).text =
            compositionWithInstruments.composition.name


    }
}





