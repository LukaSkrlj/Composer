package com.example.composer.activities


import android.app.Dialog
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.example.composer.R
import com.example.composer.constants.*
import com.example.composer.models.*
import com.example.composer.viewmodel.CompositionViewModel
import com.example.composer.viewmodel.InstrumentViewModel
import com.example.composer.viewmodel.MeasureViewModel
import com.example.composer.viewmodel.NoteViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.math.roundToInt


class Piano : AppCompatActivity() {
    private val soundPool: SoundPool = SoundPool.Builder().setMaxStreams(10).build()
    private val whiteKeys = setOf("a", "b", "c", "d", "e", "f", "g")
    private val blackKeys = setOf("db", "eb", "gb", "ab", "bb")
    private var isStartingDxFound = false
    private val octaveColor = arrayOf(
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
    private var currentNoteLength = 0.25f
    private var currentInstrumentType = "piano"
    private var compositionSpeed = 0

    private class Callback : WebViewClient() {
        @Override
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            return false
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_piano)
        val help: WebView = findViewById<WebView>(R.id.help)

        help.settings.builtInZoomControls = true
        help.settings.javaScriptEnabled = true
        help.webViewClient = Callback()
        help.loadUrl("file:///android_asset/index.html")

        var isOptionsVisible = false
        val slidingPaneLayout = findViewById<SlidingUpPanelLayout>(R.id.sliding_layout)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        val headerCard = findViewById<CardView>(R.id.cardViewHeader)
        val saveToCloudButton = findViewById<AppCompatButton>(R.id.store_to_cloud)
        val saveToCloudTitle = findViewById<TextView>(R.id.save_to_cloud_title)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar_cyclic)
        val settingsButton = findViewById<ImageButton>(R.id.settings_button)
        val settingsPanel = findViewById<CardView>(R.id.settings_panel)
        var maxInstrumentPosition = currentInstrumentPosition
        val compositionNameInput = findViewById<TextInputLayout>(R.id.compostion_name)
        val authorNameInput = findViewById<TextInputLayout>(R.id.author_name)
        val compositionSpeedInput = findViewById<TextInputLayout>(R.id.composition_speed_input)
        val scrollContainer = findViewById<HorizontalScrollView>(R.id.scrollContainer)
        val currentNoteLengthImage = findViewById<ImageView>(R.id.current_note)
        val optionsContainer = findViewById<ConstraintLayout>(R.id.optionsContainer)
        val slider = findViewById<Slider>(R.id.slider)
        val doubleArrow = findViewById<ImageButton>(R.id.new_panel)
        val currentUser = GoogleSignIn.getLastSignedInAccount(this)
        val loadingSoundProgressBar = findViewById<ProgressBar>(R.id.loading_sound_progress_bar)
        staff = findViewById(R.id.staff)
        noteViewModel = ViewModelProvider(this)[NoteViewModel::class.java]
        measureViewModel = ViewModelProvider(this)[MeasureViewModel::class.java]
        instrumentViewModel = ViewModelProvider(this)[InstrumentViewModel::class.java]

        slider.setLabelFormatter { "1/4" }
        slider.addOnChangeListener { _, value, _ ->

            when (value) {
                0f -> {
                    slider.setLabelFormatter { "1" }
                    currentNoteLength = WHOLE_NOTE
                    currentNoteLengthImage.setImageResource(R.drawable.note_wholenote)
                }

                16.0f -> {
                    slider.setLabelFormatter { "1/2" }
                    currentNoteLength = HALF_NOTE
                    currentNoteLengthImage.setImageResource(R.drawable.note_halfnote)
                }

                32.0f -> {
                    slider.setLabelFormatter { "1/4" }
                    currentNoteLength = QUARTER_NOTE
                    currentNoteLengthImage.setImageResource(R.drawable.quarter_note)
                }

                48.0f -> {
                    slider.setLabelFormatter { "1/8" }
                    currentNoteLength = EIGHT_NOTE
                    currentNoteLengthImage.setImageResource(R.drawable.note_th)
                }

                64.0f -> {
                    slider.setLabelFormatter { "1/16" }
                    currentNoteLength = SIXTEEN_NOTE
                    currentNoteLengthImage.setImageResource(R.drawable.note_sixteenthnote)
                }

                80.0f -> {
                    slider.setLabelFormatter { "1/32" }
                    currentNoteLength = THIRTYTWO_NOTE
                    currentNoteLengthImage.setImageResource(R.drawable.note_thirtysecondnote)
                }

                96.0f -> {
                    slider.setLabelFormatter { "1/64" }
                    currentNoteLength = SIXTYFOUR_NOTE
                    currentNoteLengthImage.setImageResource(R.drawable.note_sixtyfourth)
                }

                112.0f -> {
                    slider.setLabelFormatter { "1/128" }
                    currentNoteLength = HUNDREDTWENTYEIGHT_NOTE
                    currentNoteLengthImage.setImageResource(R.drawable.note_hundredtwentyeighthnote)
                }
            }

        }
        //Extras
        var compositionId = 0
        val extras = intent.extras
        if (extras != null) {
            if (intent.hasExtra("compositionId")) {
                compositionId = extras.getInt("compositionId")
            }
            if (intent.hasExtra("instrumentType")) {
                currentInstrumentType =
                    extras.getString("instrumentType").toString()
            }
            if (intent.hasExtra("compositionSpeed")) {
                compositionSpeed = extras.getInt("compositionSpeed")
            }
        }
        // Views
        findViewById<ImageButton>(R.id.new_panel).setOnClickListener {
            isOptionsVisible = !isOptionsVisible
            if (isOptionsVisible) {
                scrollContainer.visibility = View.INVISIBLE
                optionsContainer.visibility = View.VISIBLE
                doubleArrow.setImageResource(R.drawable.ic_double_arrow_left)

            } else {
                scrollContainer.visibility = View.VISIBLE
                optionsContainer.visibility = View.INVISIBLE
                doubleArrow.setImageResource(R.drawable.ic_double_arrow_right)
            }
        }

        staff = findViewById(R.id.staff)
        findViewById<ImageButton>(R.id.addInstrument).setOnClickListener {
            val dialog = layoutInflater.inflate(R.layout.edit_note_dialog, null)
            val editNoteDialog = Dialog(this)

            editNoteDialog.setContentView(dialog)

            val adapter = ArrayAdapter(dialog.context, R.layout.list_item, INSTRUMENTS)
            dialog.findViewById<AppCompatAutoCompleteTextView>(R.id.dropdown).setAdapter(adapter)
            val instrumentType = dialog.findViewById<TextInputLayout>(R.id.instrumentType)

            editNoteDialog.setCancelable(true)
            editNoteDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            editNoteDialog.show()

            var instrumentTypeText: String = ""

            instrumentType.editText?.doOnTextChanged { text, _, _, _ ->
                if (text != null) {
                    instrumentType.error = null
                }
                instrumentTypeText = text.toString().lowercase()
            }

            dialog.findViewById<Button>(R.id.submit).setOnClickListener {
                if (!INSTRUMENTS.any { instrument -> instrument.lowercase() == instrumentTypeText }) {
                    instrumentType.error = "Instrument type is required"
                    return@setOnClickListener
                }
                currentInstrumentType = instrumentTypeText
                currentInstrumentPosition = maxInstrumentPosition + 1
                instrumentViewModel.insertInstrument(
                    Instrument(
                        position = currentInstrumentPosition,
                        name = instrumentTypeText,
                        compositionId = compositionId
                    )
                ).observe(this) {
                    currentNoteDx = 0f
                    currentInstrumentId = it.toInt()
                    maxInstrumentPosition += 1
                    staff.drawPointer(currentNoteDx, currentInstrumentPosition)
                }
                editNoteDialog.dismiss()
            }

        }

        findViewById<AppCompatButton>(R.id.delete_composition_button).setOnClickListener {
            compositionViewModel.deleteComposition(compositionId)
            finish()
        }

        findViewById<ImageButton>(R.id.selectLowerInstrument).setOnClickListener {
            if (instrumentsWithMeasures.any { it.instrument.position == currentInstrumentPosition + 1 }) {
                currentInstrumentPosition += 1
                currentInstrumentId =
                    instrumentsWithMeasures.find { it.instrument.position == currentInstrumentPosition }?.instrument?.id
                        ?: 0
                currentInstrumentType =
                    instrumentsWithMeasures[currentInstrumentPosition].instrument.name
                if (instrumentsWithMeasures[currentInstrumentPosition].measures.isNotEmpty() && instrumentsWithMeasures[currentInstrumentPosition].measures.last().notes.isNotEmpty()) {
                    currentNoteDx =
                        instrumentsWithMeasures[currentInstrumentPosition].measures.last().notes.last().dx
                    currentMeasureId =
                        instrumentsWithMeasures[currentInstrumentPosition].measures.last().measure.id
                    staff.drawPointer(currentNoteDx, currentInstrumentPosition)
                } else if (instrumentsWithMeasures[currentInstrumentPosition].measures.isNotEmpty() && instrumentsWithMeasures[currentInstrumentPosition].measures.last().notes.isEmpty()) {
                    currentNoteDx = 0f
                    currentMeasureId =
                        instrumentsWithMeasures[currentInstrumentPosition].measures.last().measure.id
                    staff.drawPointer(currentNoteDx, currentInstrumentPosition)
                } else {
                    staff.drawPointer(0f, currentInstrumentPosition)
                }
            }
        }

        findViewById<ImageButton>(R.id.selectUpperInstrument).setOnClickListener {

            if (instrumentsWithMeasures.any { it.instrument.position == currentInstrumentPosition - 1 }) {
                currentInstrumentPosition -= 1
                currentInstrumentId =
                    instrumentsWithMeasures.find { it.instrument.position == currentInstrumentPosition }?.instrument?.id
                        ?: 0
                currentInstrumentType =
                    instrumentsWithMeasures[currentInstrumentPosition].instrument.name
                if (instrumentsWithMeasures[currentInstrumentPosition].measures.isNotEmpty()) {
                    currentNoteDx =
                        instrumentsWithMeasures[currentInstrumentPosition].measures.last().notes.last().dx
                    currentMeasureId =
                        instrumentsWithMeasures[currentInstrumentPosition].measures.last().measure.id
                    staff.drawPointer(currentNoteDx, currentInstrumentPosition)
                } else {
                    staff.drawPointer(0f, currentInstrumentPosition)
                }
            }
        }

        findViewById<ImageButton>(R.id.addNote).setOnClickListener {
            currentNoteDx += horizontalNoteSpacing
            staff.drawPointer(currentNoteDx, currentInstrumentPosition)
        }


        if (currentUser == null) {
            saveToCloudButton.isVisible = false
            saveToCloudTitle.isVisible = false
        }


        // CompositionViewModel
        compositionViewModel = ViewModelProvider(this)[CompositionViewModel::class.java]

        instrumentViewModel.getMaxPosition(compositionId).observe(this) {
            if (it != null) {
                maxInstrumentPosition = it
            }
        }

        compositionViewModel.getCompositionWIthInstruments(compositionId)
            .observe(this) { compositionWithInstruments ->
                if (compositionWithInstruments != null) {
                    //fine largest dx
                    if (compositionWithInstruments.instruments.isNotEmpty() && !isStartingDxFound) {
                        for (instrument in compositionWithInstruments.instruments) {
                            if (instrument.measures.isNotEmpty() && instrument.measures.last().notes.isNotEmpty()) {
                                val lastMeasure = instrument.measures.last()
                                val lastNote = lastMeasure.notes.last()
                                if (lastNote.dx.compareTo(currentNoteDx) >= 0) {
                                    currentNoteDx = lastNote.dx
                                    currentInstrumentId = lastMeasure.measure.instrumentId
                                    currentMeasureId = lastMeasure.measure.id
                                    currentInstrumentPosition = instrument.instrument.position
                                    currentInstrumentType = instrument.instrument.name
                                }
                            } else {
                                currentInstrumentType = instrument.instrument.name
                                currentInstrumentId = instrument.instrument.id
                                currentInstrumentPosition = instrument.instrument.position
                            }
                        }
                        compositionSpeed = compositionWithInstruments.composition.compositionSpeed
                        staff.drawPointer(currentNoteDx, currentInstrumentPosition)
                        isStartingDxFound = true
                    }



                    this.compositionWithInstruments = compositionWithInstruments
                    findViewById<TextView>(R.id.symphonyName).text =
                        compositionWithInstruments.composition.name
                    compositionSpeedInput.editText?.setText(compositionWithInstruments.composition.compositionSpeed.toString())
                    compositionSpeedInput.editText?.inputType = InputType.TYPE_CLASS_NUMBER
                    authorNameInput.editText?.setText(compositionWithInstruments.composition.author)
                    compositionNameInput.editText?.setText(compositionWithInstruments.composition.name)
                    progressBar.isVisible = false

                    findViewById<AppCompatButton>(R.id.save_changes).setOnClickListener {

                        this.updateComposition(
                            authorNameInput,
                            compositionNameInput,
                            compositionSpeedInput,
                            compositionId,
                            compositionWithInstruments,
                        )
                    }

                    instrumentsWithMeasures = compositionWithInstruments.instruments
                    if (instrumentsWithMeasures.isEmpty()) {
                        instrumentViewModel.insertInstrument(
                            Instrument(
                                position = currentInstrumentPosition,
                                name = currentInstrumentType,
                                compositionId = compositionWithInstruments.composition.id
                            )
                        ).observe(this) {
                            currentInstrumentId = it.toInt()
                        }
                    } else {
                        measuresWithNotes = instrumentsWithMeasures.map { it.measures }.flatten()
                        staff.drawNotes(
                            instrumentsWithMeasures,
                            compositionSpeed,
                            loadingSoundProgressBar
                        )
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
                    fun convertToFirebaseMeasureWithNotes(measureWithNotes: MeasureWithNotes): FirebaseMeasureWithNotes {
                        val measure = measureWithNotes.measure
                        val notes = measureWithNotes.notes.map { note ->
                            FirebaseNote(
                                note.noteId,
                                note.measureId,
                                note.pitch,
                                note.left,
                                note.top,
                                note.right,
                                note.bottom,
                                note.dx,
                                note.dy,
                                note.resourceId,
                                note.length
                            )
                        }
                        val firebaseMeasure = FirebaseMeasure(
                            measure.id,
                            measure.timeSignatureTop,
                            measure.timeSignatureBottom,
                            measure.keySignature,
                            measure.instrumentId,
                            measure.clef,
                            measure.position,
                        )
                        return FirebaseMeasureWithNotes(firebaseMeasure, notes)
                    }

                    val firebaseAccessible = instrumentsWithMeasures.map { obj ->
                        val instrument = obj.instrument
                        val measures = obj.measures.map { convertToFirebaseMeasureWithNotes(it) }

                        FirebaseInstrumentWithMeasures(
                            FirebaseInstrument(
                                instrument.id,
                                instrument.name,
                                instrument.position,
                                instrument.compositionId
                            ),
                            measures
                        )
                    }

                    db.collection("symphonies").add(
                        hashMapOf(
                            "likes" to 0,
                            "symphonyName" to compositionWithInstruments.composition.name,
                            "userID" to currentUser.id.toString(),
                            "symphonyComposer" to compositionWithInstruments.composition.author,
                            "symphonyDurationSeconds" to 0,
                            "compositionSpeed" to compositionSpeed
                        )
                    ).addOnSuccessListener { document ->
                        document.collection("sheet").document("music")
                            .set(mapOf("compositionWithInstruments" to firebaseAccessible))
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
        supportActionBar?.hide()
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
        var lineCounter = 0
        var octave = 0
        outer@ while (octave < 9) {
            inner@ for (key in blackKeys) {
                if (key == "db" || key == "gb") {
                    lineCounter++
                }
                var newKey = key + octave
                if (key == "bb") ++octave
                if (newKey == "db8") break@outer

                if (octave == 0) {

                    newKey = "bb0"
                    octave++
                }

//                val fileName = resources.getIdentifier(
//                    "raw/$currentInstrumentType$newKey",
//                    null,
//                    this.packageName
//                )
//                var loadedFile: Int? = null
//                if (fileName != 0) {
//                    loadedFile = soundPool.load(this, fileName, 1)
//                }


                val blackPianoKey = Button(this)

                blackPianoKey.id = View.generateViewId()
                blackPianoKey.text = newKey
                blackPianoKey.setTextColor(Color.WHITE)
                blackPianoKey.setBackgroundResource(R.drawable.black_piano_border)

                constraintSet.constrainHeight(blackPianoKey.id, blackKeyHeight)
                constraintSet.constrainWidth(blackPianoKey.id, blacKeyWidth)

                val dy = initialNotePosition - (lineSpacing / 2) * lineCounter

                this.connectBlackKey(constraintSet, newKey, blackPianoKey.id)

                blackPianoKey.bringToFront()

                blackPianoKey.setOnClickListener {
//                    if (loadedFile != null) {
//                        this.soundPool.play(loadedFile, 1f, 1f, 1, 0, speed)
//                    }
                    playSound(newKey)
                    //Update this as white keys
                    //noteViewModel.addNote(Note(right = 82, bottom = 82, dx = 0f, dy = 0f))
                    var countSum = 0f
                    var measurePosition = 0

                    val df = DecimalFormat("#.##")
                    if (measuresWithNotes.none { it.measure.instrumentId == currentInstrumentId }) {
                        val newMeasure = Measure(
                            timeSignatureTop = 4,
                            timeSignatureBottom = 4,
                            keySignature = "c",
                            instrumentId = currentInstrumentId,
                            clef = "treble"
                        )
                        val insertObserver = measureViewModel.insertMeasure(
                            newMeasure
                        )

                        insertObserver.observe(this) {
                            currentMeasureId = it.toInt()
                            val note = Note(
                                right = 82,
                                bottom = 82,
                                dx = currentNoteDx,
                                dy = dy,
                                measureId = currentMeasureId,
                                pitch = newKey,
                                length = currentNoteLength
                            )

                            noteViewModel.addNote(note)
                            insertObserver.removeObservers(this)
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
                            val insertObserver = measureViewModel.insertMeasure(
                                newMeasure
                            )
                            insertObserver.observe(this) {
                                currentMeasureId = it.toInt()
                                val note = Note(
                                    right = 82,
                                    bottom = 82,
                                    dx = currentNoteDx,
                                    dy = dy,
                                    measureId = currentMeasureId,
                                    pitch = newKey,
                                    length = currentNoteLength
                                )

                                noteViewModel.addNote(note)
                                insertObserver.removeObservers(this)
                            }

                            return@setOnClickListener
                        }
                    }

                    val note = Note(
                        right = 82,
                        bottom = 82,
                        dx = currentNoteDx,
                        dy = dy,
                        measureId = currentMeasureId,
                        pitch = newKey,
                        length = currentNoteLength
                    )

                    noteViewModel.addNote(note)
                }

                constraintLayout.addView(blackPianoKey)
                lineCounter++
                if (newKey == "bb0") {
                    break@inner
                }

            }
        }
        constraintSet.applyTo(constraintLayout)
    }

    private fun playSound(newKey: String) {
        GlobalScope.launch(Dispatchers.Main) {
            val fileName = resources.getIdentifier(
                "raw/$currentInstrumentType$newKey",
                null,
                packageName
            )
            if (fileName != 0) {
                val loadedFile = loadSound(soundPool, fileName)
                soundPool.play(loadedFile, 1f, 1f, 1, 0, speed)
            }
        }
    }

    private suspend fun loadSound(soundPool: SoundPool, soundId: Int): Int {
        return suspendCoroutine { continuation ->
            val soundLoadedListener = SoundPool.OnLoadCompleteListener { _, sampleId, status ->
                if (status == 0) {
                    continuation.resume(sampleId)
                } else {
                    continuation.resumeWithException(Exception("Sound loading failed"))
                }
            }

            soundPool.setOnLoadCompleteListener(soundLoadedListener)
            soundPool.load(this, soundId, 1)
        }
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
//                val fileName = resources.getIdentifier(
//                    "raw/$currentInstrumentType$newKey",
//                    null,
//                    this.packageName
//                )
//
//                var loadedFile: Int? = null
//                if (fileName != 0) {
//                    loadedFile = soundPool.load(this, fileName, 1)
//                }

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
                val dy = initialNotePosition - (lineSpacing / 2) * lineCounter

                whitePianoKey.setOnClickListener {
//                    if (loadedFile != null) {
//                        this.soundPool.play(loadedFile, 1f, 1f, 1, 0, speed)
//                    }
                    playSound(newKey)
                    var countSum = 0f
                    var measurePosition = 0
                    val df = DecimalFormat("#.##")

                    if (measuresWithNotes.none { it.measure.instrumentId == currentInstrumentId }) {
                        val newMeasure = Measure(
                            timeSignatureTop = 4,
                            timeSignatureBottom = 4,
                            keySignature = "c",
                            instrumentId = currentInstrumentId,
                            clef = "treble"
                        )

                        val insertObserver = measureViewModel.insertMeasure(
                            newMeasure
                        )
                        insertObserver.observe(this) {
                            currentMeasureId = it.toInt()
                            val note = Note(
                                right = 82,
                                bottom = 82,
                                dx = currentNoteDx,
                                dy = dy,
                                measureId = currentMeasureId,
                                pitch = newKey,
                                length = currentNoteLength
                            )

                            noteViewModel.addNote(note)
                            insertObserver.removeObservers(this)
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
                            val insertObserver = measureViewModel.insertMeasure(
                                newMeasure
                            )
                            insertObserver.observe(this) {
                                currentMeasureId = it.toInt()
                                val note = Note(
                                    right = 82,
                                    bottom = 82,
                                    dx = currentNoteDx,
                                    dy = dy,
                                    measureId = currentMeasureId,
                                    pitch = newKey,
                                    length = currentNoteLength
                                )

                                noteViewModel.addNote(note)
                                insertObserver.removeObservers(this)
                            }

                            return@setOnClickListener
                        }
                    }

                    val note = Note(
                        right = 82,
                        bottom = 82,
                        dx = currentNoteDx,
                        dy = dy,
                        measureId = currentMeasureId,
                        pitch = newKey,
                        length = currentNoteLength
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
        compositionSpeedInput: TextInputLayout,
        compositionId: Int,
        compositionWithInstruments: CompositionWithInstruments,
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

        if (compositionSpeedInput.editText?.text?.isEmpty() == true) {
            compositionSpeedInput.editText?.error = "Speed cannot be be empty"
            return
        }


        if (compositionSpeedInput.editText?.text?.toString()
                ?.toInt() == 0 || compositionSpeedInput.editText?.text?.toString()?.toInt()!! > 260
        ) {
            compositionSpeedInput.editText?.error = "Speed must be between 0 and 260"
            return
        }

        compositionViewModel.updateCompositionInfo(
            compositionName = compositionNameInput.editText?.text.toString(),
            authorName = authorNameInput.editText?.text.toString(),
            compositionId = compositionId,
            compositionSpeed = compositionSpeedInput.editText?.text?.toString()?.toInt()!!
        )
        Toast.makeText(this, "Changes saved successfully", Toast.LENGTH_SHORT).show()
        compositionSpeed = compositionSpeedInput.editText?.text?.toString()?.toInt()!!
        findViewById<TextView>(R.id.symphonyName).text =
            compositionWithInstruments.composition.name
    }
}





