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
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import com.example.composer.R
import com.example.composer.models.*
import com.example.composer.viewmodel.CompositionViewModel
import com.example.composer.viewmodel.InstrumentViewModel
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
    private lateinit var instrumentViewModel: InstrumentViewModel
    private lateinit var compositionViewModel: CompositionViewModel
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
    private var instrumentsWithMeasures: List<InstrumentWithMeasures> = emptyList()
    private var currentInstrumentId = 0


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_piano)
        val slidingPaneLayout = findViewById<SlidingUpPanelLayout>(R.id.sliding_layout)

        compositionViewModel = ViewModelProvider(this)[CompositionViewModel::class.java]
        compositionViewModel.compositions.observe(this) { compositions ->
            if (compositions.isEmpty()) {
                compositionViewModel.upsertComposition(Composition(name = "test"))
            }
        }

        val extras = intent.extras
        var compositionId = 0

        if (extras != null) {
            compositionId = extras.getInt("compositionId")
        } else {
            compositionViewModel.upsertComposition(Composition(name = "test"))
            compositionViewModel.lastComposition.observe(this) { composition ->
                compositionId = composition.id
            }
        }

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        val headerCard = findViewById<CardView>(R.id.cardViewHeader)
        val favoritesIcon = findViewById<ImageButton>(R.id.imageHeart)
        val saveToCloudButton = findViewById<AppCompatButton>(R.id.store_to_cloud)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar_cyclic)
        val settingsButton = findViewById<ImageButton>(R.id.settings_button)
        val settingsPanel = findViewById<CardView>(R.id.settings_panel)
        staff = findViewById(R.id.staff)
        noteViewModel = ViewModelProvider(this)[NoteViewModel::class.java]
        measureViewModel = ViewModelProvider(this)[MeasureViewModel::class.java]
        instrumentViewModel = ViewModelProvider(this)[InstrumentViewModel::class.java]


        measureViewModel.measuresWithNotes.observe(this) { measuresWithNotes ->
            this.measuresWithNotes = measuresWithNotes
        }
        noteViewModel.notes.observe(this) { notes ->
            Log.d("piano notes", notes.toString())
        }
        instrumentViewModel.instrumentsWithMeasures.observe(this) { instrumentsWithMeasures ->
            staff.drawNotes(instrumentsWithMeasures)
            Log.d("Instruments with measures", instrumentsWithMeasures.toString())
            if (instrumentsWithMeasures.isEmpty()) {
                instrumentViewModel.upsertInstrument(
                    Instrument(
                        id = currentInstrumentId,
                        name = "piano",
                        compositionId = compositionId
                    )
                )
            }
        }

        findViewById<ImageButton>(R.id.addInstrument).setOnClickListener {
            instrumentViewModel.upsertInstrument(
                Instrument(
                    id = currentInstrumentId + 1,
                    name = "piano",
                    compositionId = compositionId
                )
            )
        }

        findViewById<ImageButton>(R.id.selectLowerInstrument).setOnClickListener {
            currentInstrumentId += 1
        }

        findViewById<ImageButton>(R.id.selectUpperInstrument).setOnClickListener {
            currentInstrumentId -= 1
        }

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


        val db = FirebaseFirestore.getInstance()
        val symphonyID = extras?.getString("symphonyID")

        val document = symphonyID?.let { db.collection("symphonies").document(it) }
        val currentUser = GoogleSignIn.getLastSignedInAccount(this)
        val measureWithNotesCopyMutable: MutableList<InstrumentWithMeasures> = mutableListOf()

        document?.collection("sheet")?.document("music")?.get()
            ?.addOnCompleteListener { music ->

                if (music.result.exists()) {
                    val measures =
                        music.result.data?.get("measures") as ArrayList<HashMap<Any, Any>>

                    for ((measureIndex, element) in measures.withIndex()) {

                        val measure = element["measure"] as HashMap<*, *>
                        val notes = element["notes"] as ArrayList<HashMap<Any, Any>>
                        var newMeasureWithNotes: MeasureWithNotes = MeasureWithNotes()
                        val newNotesList: MutableList<Note> = mutableListOf()
                        val newMeasure = Measure(
                            id = (measure["id"] as Long).toInt(),
                            timeSignatureTop = (measure["timeSignatureTop"] as Long).toInt(),
                            timeSignatureBottom = (measure["timeSignatureBottom"] as Long).toInt(),
                            keySignature = measure["keySignature"] as String,
                            compositionId = (measure["compositionId"] as Long).toInt(),
                            clef = measure["clef"] as String,
                            instrumentId = 0
                        )

                        newMeasureWithNotes.measure = newMeasure

                        for ((noteIndex, note) in notes.withIndex()) {
                            val newNote = Note(
                                right = (note["right"] as Long).toInt(),
                                bottom = (note["bottom"] as Long).toInt(),
                                dx = (note["dx"] as Double).toFloat(),
                                dy = (note["dy"] as Double).toFloat(),
                                measureId = (note["measureId"] as Long).toInt(),
                                key = note["key"] as String
                            )
                            newNotesList.add(noteIndex, newNote)
                        }
                        newMeasureWithNotes.notes = newNotesList.toList()
//                        measureWithNotesCopyMutable.add(measureIndex, newMeasureWithNotes)

                    }

                    staff.drawNotes(measureWithNotesCopyMutable)
                    progressBar.visibility = View.GONE
                } else {
                    progressBar.visibility = View.GONE
                }
            }
        document?.get()?.addOnSuccessListener { symphony ->
            findViewById<TextView>(R.id.symphonyName).text =
                symphony.get("symphonyName") as CharSequence?

            if (currentUser != null) {


                val newMeasuresWithNotesFirebaseAccessible: ArrayList<Any> =
                    ArrayList()
                saveToCloudButton.setOnClickListener {

                    for ((indexMeasure, measure) in measuresWithNotes.withIndex()) {
                        val notes: ArrayList<HashMap<String, Any>> = ArrayList()
                        val measureHashMap: HashMap<String, Any> = HashMap()
                        val notesAndMeasureHashMap: HashMap<String, Any> = HashMap()
                        measureHashMap["clef"] = measure.measure.clef
                        measureHashMap["compositionId"] = measure.measure.compositionId
                        measureHashMap["id"] = measure.measure.id
                        measureHashMap["keySignature"] = measure.measure.keySignature
                        measureHashMap["timeSignatureTop"] = measure.measure.timeSignatureTop
                        measureHashMap["timeSignatureBottom"] =
                            measure.measure.timeSignatureBottom

                        notesAndMeasureHashMap["measure"] = measureHashMap

                        for ((indexNote, note) in measure.notes.withIndex()) {
                            val noteHashMap: HashMap<String, Any> = HashMap()
                            noteHashMap["right"] = note.right
                            noteHashMap["bottom"] = note.bottom
                            noteHashMap["dx"] = note.dx
                            noteHashMap["dy"] = note.dy
                            noteHashMap["measureId"] = note.measureId
                            noteHashMap["key"] = note.key
                            notes.add(indexNote, noteHashMap)
                        }
                        notesAndMeasureHashMap["notes"] = notes
                        newMeasuresWithNotesFirebaseAccessible.add(
                            indexMeasure,
                            notesAndMeasureHashMap
                        )
                    }



                    db.collection("symphonies").document(symphonyID).collection("sheet")
                        .document("music")
                        .set(mapOf("measures" to newMeasuresWithNotesFirebaseAccessible))
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Symphony saved successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                }

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
                            .whereEqualTo("symphonyID", symphonyID).get()
                            .addOnSuccessListener {
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

        findViewById<ImageButton>(R.id.addNote).setOnClickListener {
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
                    var measureId = 0
                    val df = DecimalFormat("#.##")

                    if (measuresWithNotes.none { it.measure.instrumentId == currentInstrumentId }) {
                        val newMeasure = Measure(
                            id = 0,
                            timeSignatureTop = 4,
                            timeSignatureBottom = 4,
                            keySignature = "c",
                            instrumentId = currentInstrumentId,
                            clef = "treble"
                        )
                        measureViewModel.upsertMeasure(
                            newMeasure
                        )
                    } else {
                        val lastMeasure =
                            measuresWithNotes.last { it.measure.instrumentId == currentInstrumentId }
                        lastMeasure.notes.distinctBy { it.dx }
                            .map { note -> countSum += note.length }

                        if (measuresWithNotes.isEmpty()) {

                            addMeasure(0, 4, 4, "c", 0, "treble")
                        }

                        if (measuresWithNotes.isNotEmpty()) {
                            measuresWithNotes.last().notes.distinctBy { it.dx }
                                .map { note ->
                                    countSum += note.length

                                }

                            //tu je greska
                            Log.d(
                                "countSum",
                                measuresWithNotes.last().notes.distinctBy { it.dx }.toString()
                            )
                            Log.d(
                                "Ovo drugo",
                                df.format(lastMeasure.measure.timeSignatureTop / lastMeasure.measure.timeSignatureBottom.toFloat())
                            )

                            if (df.format(countSum) == df.format(lastMeasure.measure.timeSignatureTop / lastMeasure.measure.timeSignatureBottom.toFloat())) {
                                measureId = lastMeasure.measure.id + 1
                                Log.d("Measure id", measureId.toString())
                                val newMeasure = Measure(
                                    id = measureId,
                                    timeSignatureTop = 4,
                                    timeSignatureBottom = 4,
                                    keySignature = "c",
                                    instrumentId = currentInstrumentId,
                                    clef = "treble"
                                )
                                measureViewModel.upsertMeasure(
                                    newMeasure
                                )

                                if (df.format(countSum) == df.format(measuresWithNotes.last().measure.timeSignatureTop / measuresWithNotes.last().measure.timeSignatureBottom.toFloat())) {
                                    measureId = measuresWithNotes.last().measure.id + 1

                                    addMeasure(measureId, 4, 4, "c", 0, "treble")


                                }

                            }

                            addNote(82, 82, currentNoteDx, dy, measureId, newKey)

                        }

                    }
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
        whitePianoTile?.post {
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
                if (it.result.isEmpty) {
                    addLikeBackground(false)

                } else {
                    addLikeBackground(true)
                }
            }
    }

    private fun addNote(
        right: Int,
        bottom: Int,
        dx: Float,
        dy: Float,
        measureId: Int,
        key: String
    ) {
        val newNote = Note(
            right = right,
            bottom = bottom,
            dx = dx,
            dy = dy,
            measureId = measureId,
            key = key
        )

        noteViewModel.addNote(newNote)

    }

    private fun addMeasure(
        id: Int,
        timeSignatureTop: Int,
        timeSignatureBottom: Int,
        keySignature: String,
        compositionId: Int,
        clef: String
    ) {
        val newMeasure = Measure(
            id = id,
            timeSignatureTop = timeSignatureTop,
            timeSignatureBottom = timeSignatureBottom,
            keySignature = keySignature,
            compositionId = compositionId,
            clef = clef
        )
        measureViewModel.upsertMeasure(
            newMeasure
        )
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




