package com.example.composer.activities

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.view.Window
import android.widget.ImageButton
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.composer.R
import com.example.composer.adapters.EditNoteAdapter
import com.example.composer.constants.*
import com.example.composer.models.InstrumentWithMeasures
import com.example.composer.models.MeasureWithNotes
import com.example.composer.models.SoundLoader
import com.example.composer.models.Note
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.math.abs
import kotlin.math.roundToInt

class Staff @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    companion object {
        // Note is starting on first line, note F4, use spacing to move notes vertically
        private const val lineSpacing = 20f

        fun getSpacing(): Float {
            return lineSpacing
        }
    }

    private var notesAdapter: EditNoteAdapter? = null
    private var startX = 0f
    private var startY = 0f
    private var currentNoteDx = 0f
    private var currentInstrumentPosition = 0
    private val barLinePaint = Paint()
    private var isViewOnly = false
    private var globalScope: Job? = null
    private val soundPool: SoundPool = SoundPool.Builder().setMaxStreams(100).build()
    private val linesCount = 5
    private val lineThickness = 2f
    private val lastNoteMeasureSpacing = 180f
    private val defaultInstrumentSpacing = 300f
    private val clefSpacing = 50f
    private val pointerDrawable =
        ResourcesCompat.getDrawable(resources, R.drawable.ic_arrow_drop_down, null)
    private var barLine =
        arrayOf(
            lineThickness / 2,
            lineThickness / 2,
            lineThickness / 2,
            lineThickness / 2 + (linesCount - 1) * lineSpacing
        )

    private var lines: Array<Array<Float>> = arrayOf()

    private var instrumentsWithMeasures: List<InstrumentWithMeasures> = emptyList()

    private var isMusicPlaying: Boolean = false
    private var measuresWithNotes: List<MeasureWithNotes> = listOf()
    private var compositionSpeed = 0

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var instrumentSpacing = 100f
        for (instrument in instrumentsWithMeasures) {
            Log.d("tu smo ej", instrument.toString())
            var previousMeasureEnd = 0f
            for (measure in instrument.measures) {
                //Measure start position
                val startingOffset = 80f
                var currentMeasureEnd = lastNoteMeasureSpacing
                if (measure.notes.isNotEmpty()) {
                    currentMeasureEnd = measure.notes.last().dx + lastNoteMeasureSpacing
                }

//            measure(currentMeasureEnd.toInt(), 500)
//              this.layoutParams = ViewGroup.LayoutParams(currentMeasureEnd.toInt(), 500)
                //Bar line
                canvas.drawLine(
                    barLine[0] + currentMeasureEnd,
                    barLine[1] + instrumentSpacing,
                    barLine[2] + currentMeasureEnd,
                    barLine[3] + instrumentSpacing,
                    barLinePaint
                )

                //draw staff
                var lastLine: Array<Float> =
                    arrayOf(
                        previousMeasureEnd,
                        lineThickness / 2 + instrumentSpacing,
                        currentMeasureEnd,
                        lineThickness / 2 + instrumentSpacing
                    )

                for (i in 1..linesCount) {
                    lines += lastLine
                    lastLine =
                        lastLine.mapIndexed { index, value -> if (index % 2 == 1) value + lineSpacing else value }
                            .toTypedArray()
                }

                barLinePaint.strokeWidth = lineThickness
                barLinePaint.isAntiAlias = true
                barLinePaint.isFilterBitmap = true
                canvas.drawLines(lines.flatten().toFloatArray(), barLinePaint)
                drawKeySignature(canvas, dy = instrumentSpacing)
                drawTimeSignature(
                    canvas,
                    measure.measure.timeSignatureTop,
                    measure.measure.timeSignatureBottom,
                    dy = instrumentSpacing,
                    dx = clefSpacing
                )


                for (note in measure.notes) {
                    var noteAddedHeight = 0
                    var noteAddedWidth = 0
                    var noteAdjustDy = 0
                    var noteAdjustDx = 0

                    val d = ResourcesCompat.getDrawable(
                        resources,
                        when (note.length) {
                            WHOLE_NOTE -> {
                                noteAdjustDy = 60
                                noteAddedHeight = -60
                                noteAddedWidth = -50
                                noteAdjustDx = 30
                                R.drawable.note_wholenote
                            }

                            HALF_NOTE -> {
                                noteAddedWidth = -50
                                noteAdjustDx = 20
                                R.drawable.note_halfnote
                            }

                            QUARTER_NOTE -> {
                                R.drawable.quarter_note
                            }

                            EIGHT_NOTE -> {
                                R.drawable.note_th
                            }

                            SIXTEEN_NOTE -> {
                                noteAddedWidth = -30
                                noteAdjustDx = 20
                                R.drawable.note_sixteenthnote
                            }

                            THIRTYTWO_NOTE -> {
                                noteAddedWidth = -30
                                noteAdjustDx = 20
                                R.drawable.note_thirtysecondnote
                            }

                            SIXTYFOUR_NOTE -> {
                                noteAdjustDy = -15
                                noteAddedWidth = -30
                                noteAddedHeight = 15
                                noteAdjustDx = 20
                                R.drawable.note_sixtyfourth
                            }

                            HUNDREDTWENTYEIGHT_NOTE -> {
                                noteAdjustDy = -25
                                noteAddedWidth = -30
                                noteAddedHeight = 25
                                noteAdjustDx = 20
                                R.drawable.note_hundredtwentyeighthnote
                            }

                            else -> {
                                R.drawable.quarter_note
                            }
                        },
                        null
                    )

                    if (note.pitch.getOrNull(1) == 'b') {
                        val flat = ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.accidental_flat,
                            null
                        )
                        flat?.setBounds(
                            note.left - 5,
                            note.top + 40,
                            note.right - 65 + noteAddedWidth,
                            note.bottom + noteAddedHeight
                        )
                        canvas.translate(note.dx + startingOffset, note.dy + instrumentSpacing)
                        flat?.draw(canvas)
                        canvas.translate(-note.dx - startingOffset, -note.dy - instrumentSpacing)
                    }

                    d?.setBounds(
                        (note.left + noteAdjustDx),
                        note.top + noteAdjustDy,
                        (note.right + noteAddedWidth + noteAdjustDx),
                        note.bottom + noteAddedHeight + noteAdjustDy
                    )
                    canvas.translate(note.dx + startingOffset, note.dy + instrumentSpacing)
                    d?.draw(canvas)
                    canvas.translate(-note.dx - startingOffset, -note.dy - instrumentSpacing)
                    val distanceFromStaff =
                        note.dy + note.bottom + noteAddedHeight + noteAdjustDy - (note.top + noteAdjustDy)

                    if (distanceFromStaff > lineSpacing * 5 || distanceFromStaff <= -lineSpacing) {
                        note.dy + note.bottom + noteAddedHeight - note.top

                        if (distanceFromStaff >= lineSpacing * 5 || distanceFromStaff <= -lineSpacing) {
                            val smallLineWidth = 20f
                            val lineX =
                                note.dx + startingOffset + (note.right - note.left + noteAddedWidth) / 2
                            for (i in 0..((distanceFromStaff - lineSpacing * 5) / lineSpacing).toInt()) {
                                val lineY =
                                    lineSpacing * (5 + i) + lineThickness / 2 + instrumentSpacing
                                canvas.drawLine(
                                    lineX - smallLineWidth,
                                    lineY,
                                    lineX + smallLineWidth,
                                    lineY,
                                    barLinePaint
                                )
                            }
                        }
                    }

                    if (measure.notes.isNotEmpty()) {
                        previousMeasureEnd = measure.notes.last().dx + lastNoteMeasureSpacing
                    }
                }
            }
            this.drawEnd(canvas, previousMeasureEnd, instrumentSpacing)
            instrumentSpacing += defaultInstrumentSpacing

            if (!isViewOnly) {
                pointerDrawable?.setBounds(
                    currentNoteDx.toInt() + 2 * clefSpacing.toInt(),
                    50 + currentInstrumentPosition * defaultInstrumentSpacing.toInt(),
                    currentNoteDx.toInt() + 2 * clefSpacing.toInt() + 50,
                    100 + currentInstrumentPosition * defaultInstrumentSpacing.toInt()
                )
                pointerDrawable?.draw(canvas)
            }
        }
    }

    private fun getLifecycleOwner(): LifecycleOwner? {
        var context = context
        while (context is ContextWrapper) {
            if (context is Activity) {
                return context as LifecycleOwner
            }
            context = (context as ContextWrapper).baseContext
        }
        return null
    }

    private fun getViewModelStoreOwner(): ViewModelStoreOwner? {
        var context = context
        while (context is ContextWrapper) {
            if (context is ViewModelStoreOwner) {
                return context
            }
            context = (context as ContextWrapper).baseContext
        }
        return null
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x;
                startY = event.y;
                return true;
            }

            MotionEvent.ACTION_UP -> {
                val endX = event.x
                val endY = event.y
                // Check for a click gesture
                val CLICK_THRESHOLD = 10f
                if (abs(endX - startX) < CLICK_THRESHOLD && abs(endY - startY) < CLICK_THRESHOLD && !isViewOnly) {
                    // Handle click here

                    val instrumentCounter = (endY.roundToInt() / 300)

                    if (instrumentCounter < instrumentsWithMeasures.size) {
                        val notesThatMatchDx =
                            instrumentsWithMeasures[instrumentCounter].measures.map { it ->
                                it.notes.filter { note ->
                                    endX >= note.dx.plus(100) && endX <= note.dx.plus(150)
                                }
                            }
                        if (notesThatMatchDx.isNotEmpty()) {
                            for (notesList in notesThatMatchDx) {
                                if (notesList.isNotEmpty()) {
                                    showDialog(notesList)
                                    break
                                }
                            }
                        }
                    }

                    return true;
                }
            }

        }
        return super.onTouchEvent(event)
    }

    fun setIsViewOnly(isViewOnly: Boolean) {
        this.isViewOnly = isViewOnly
    }

    private fun showDialog(notes: List<Note>) {
        val dialog = Dialog(context)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_sheet_notes)

        val recyclerView = dialog.findViewById<RecyclerView>(R.id.notes_list_recycler)
        notesAdapter = EditNoteAdapter(
            context,
            notes.toMutableList(),
            getViewModelStoreOwner(),
            getLifecycleOwner()
        )

        recyclerView.adapter = notesAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        dialog.show()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimations
        dialog.window?.setGravity(Gravity.BOTTOM)
    }


    fun drawPointer(currentNoteDx: Float, currentInstrumentPosition: Int) {
        this.currentNoteDx = currentNoteDx
        this.currentInstrumentPosition = currentInstrumentPosition
        invalidate()
    }

    fun drawNotes(instruments: List<InstrumentWithMeasures>, compositionSpeed: Int) {
        this.compositionSpeed = compositionSpeed
        instrumentsWithMeasures = instruments
        measuresWithNotes = instrumentsWithMeasures.map { it.measures }.flatten()
        var largestNoteDx = 200f
        for (instrument in instrumentsWithMeasures) {
            if (instrument.measures.isNotEmpty() && instrument.measures.last().notes.isNotEmpty()) {
                val lastNoteInInstrumentDx = instrument.measures.last().notes.last().dx
                if (lastNoteInInstrumentDx.compareTo(largestNoteDx) >= 0) {
                    largestNoteDx = lastNoteInInstrumentDx
                }
            }
        }

        this.layoutParams =
            ViewGroup.LayoutParams(
                largestNoteDx.toInt() + 250,
                instrumentsWithMeasures.size * defaultInstrumentSpacing.toInt() + 200
            )
        invalidate()
    }

    fun setIsMusicPlaying(isMusicPlaying: Boolean, playButton: ImageButton? = null) {
        this.isMusicPlaying = isMusicPlaying
        if (isMusicPlaying) {
            playMusic(measuresWithNotes, playButton)

        } else {
            globalScope?.cancel()
        }
    }


//    private fun setNotesSize() {
//        val notesHeight = lines.last().last().toInt()
//
//        instrumentsWithMeasures.forEach { instrument ->
//            instrument.measures.forEach { measure ->
//                measure.notes.forEach { note ->
//
//                    val resourceId = resources.getIdentifier(
//                        note., "drawable",
//                        context.packageName
//                    )
//                }
//            }
//
//            when (note) {
//                "accidental_doublesharp", "note_clef", "note_semibreve", "rest_doublewholerest" -> notesHmap[note] =
//                    Note(
//                        (notesHeight / linesCount + 3 * lineThickness).toInt(),
//                        (notesHeight / linesCount + 3 * lineThickness).toInt(),
//                        resourceId = resourceId
//                    )
//                "accidental_natural", "accidental_sharp" -> notesHmap[note] =
//                    Note(
//                        note,
//                        (notesHeight / (linesCount - 1)),
//                        (notesHeight / (linesCount - 3) + lineThickness).toInt(),
//                        resourceId
//                    )
//                "dynamic_crescendo", "dynamic_diminuendo" -> notesHmap[note] =
//                    Note(
//                        note,
//                        (notesHeight),
//                        (notesHeight / (linesCount - 3) + lineThickness).toInt(),
//                        resourceId
//                    )
//                "dynamic_forte",
//                "dynamic_fortepiano",
//                "dynamic_fortissimo",
//                "dynamic_mezzo_piano",
//                "dynamic_pianissimo",
//                "dynamic_pianississimo",
//                "dynamic_piano",
//                "dynamic_sforzando" -> notesHmap[note] =
//                    Note(
//                        note,
//                        (notesHeight / (linesCount - 3) + lineThickness).toInt(),
//                        (notesHeight / (linesCount - 3) + lineThickness).toInt(),
//                        resourceId
//                    )
//                "left_repeat", "note_cclef", "right_repeat" -> notesHmap[note] =
//                    Note(
//                        note,
//                        notesHeight / 2,
//                        notesHeight,
//                        resourceId
//                    )
//                "note_dot" -> notesHmap[note] =
//                    Note(
//                        note,
//                        (notesHeight / (linesCount + lineThickness)).toInt(),
//                        (notesHeight / (linesCount + lineThickness)).toInt(),
//                        resourceId
//                    )
//                "note_doublewholenote" -> notesHmap[note] =
//                    Note(
//                        note,
//                        notesHeight / 3,
//                        (notesHeight / (linesCount - 2) - 2 * lineThickness).toInt(),
//                        resourceId
//                    )
//                "note_fclef" -> notesHmap[note] =
//                    Note(
//                        note,
//                        notesHeight / 2,
//                        (notesHeight / 1.4).toInt(),
//                        resourceId
//                    )
//                "note_gclef",
//                "note_hundredtwentyeighthnote",
//                "note_semigarrapatea",
//                "note_sixtyfourth",
//                "rest_hundredtwentyeighthrest",
//                "rest_sixtyfourthrest",
//                "rest_twohundredfiftysix" -> notesHmap[note] =
//                    Note(
//                        note,
//                        notesHeight / 2,
//                        (notesHeight * 1.5).toInt(),
//                        resourceId
//                    )
//                "note_halfnote", "rest_crochet" -> notesHmap[note] =
//                    Note(
//                        note,
//                        notesHeight / 3,
//                        notesHeight,
//                        resourceId
//                    )
//                "note_sixteenthnote", "note_thirtysecondnote", "rest_thirtysecondrest" -> notesHmap[note] =
//                    Note(
//                        note,
//                        notesHeight / 2,
//                        notesHeight,
//                        resourceId
//                    )
//                "rest_eighthrest" -> notesHmap[note] =
//                    Note(
//                        note,
//                        notesHeight / 3,
//                        notesHeight / 2,
//                        resourceId
//                    )
//                "rest_halfrest" -> notesHmap[note] =
//                    Note(
//                        note,
//                        notesHeight / 3,
//                        (notesHeight / (linesCount + lineThickness)).toInt(),
//                        resourceId
//                    )
//                "rest_octwholerest",
//                "time_0",
//                "time_1",
//                "time_2",
//                "time_3",
//                "time_4",
//                "time_5",
//                "time_6",
//                "time_7",
//                "time_8",
//                "time_9" -> notesHmap[note] =
//                    Note(
//                        note,
//                        notesHeight / 2,
//                        (notesHeight / 2).toInt(),
//                        resourceId
//                    )
//                "rest_quadwholerest", "rest_sixteenthrest" -> notesHmap[note] =
//                    Note(
//                        note,
//                        notesHeight / 3,
//                        (notesHeight / 2).toInt(),
//                        resourceId
//                    )
//
//                else -> {
//                    notesHmap[note] = Note(note, notesHeight, notesHeight, resourceId)
//                }
//            }
//
//        }
//
//    }

    private fun drawKeySignatures(numberOfSignatures: Int) {
        val notesHeight = lines.last().last().toInt()
        val signatureArray: ArrayList<Drawable?> = ArrayList(numberOfSignatures)
        val signatureHeight = (lineSpacing * 2).toInt()
        val signatureWidth = (notesHeight / 4)

        for (i in 0 until numberOfSignatures) {
            val signature = ResourcesCompat.getDrawable(
                resources,
                R.drawable.accidental_flat,
                null
            )
            signatureArray.add(signature)

            when (i + 1) {
                1 -> signatureArray[i]?.setBounds(
                    10,
                    (lineSpacing / 2).toInt(),
                    10 + signatureWidth,
                    (lineSpacing / 2).toInt() + signatureHeight
                )

                2 -> signatureArray[i]?.setBounds(
                    30,
                    -(lineSpacing + 0.2 * lineThickness).toInt(),
                    30 + signatureWidth,
                    -(lineSpacing + 0.2 * lineThickness).toInt() + signatureHeight
                )

                3 -> signatureArray[i]?.setBounds(
                    55,
                    (lineSpacing / 1).toInt(),
                    55 + signatureWidth,
                    (lineSpacing / 1).toInt() + signatureHeight
                )

                4 -> signatureArray[i]?.setBounds(
                    75,
                    -(lineSpacing / 5 + 3 * lineThickness).toInt(),
                    75 + signatureWidth,
                    -(lineSpacing / 5 + 3 * lineThickness).toInt() + signatureHeight
                )

                5 -> signatureArray[i]?.setBounds(
                    100,
                    (1.6 * lineSpacing).toInt(),
                    100 + signatureWidth,
                    (1.6 * lineSpacing).toInt() + signatureHeight
                )

                6 -> signatureArray[i]?.setBounds(
                    125,
                    -(lineSpacing / 25).toInt(),
                    125 + signatureWidth,
                    -(lineSpacing / 25).toInt() + signatureHeight
                )

                7 -> signatureArray[i]?.setBounds(
                    150,
                    (2 * lineSpacing).toInt(),
                    150 + signatureWidth,
                    (2 * lineSpacing).toInt() + signatureHeight
                )
            }
        }
    }

    private fun drawEnd(canvas: Canvas, dx: Float, dy: Float) {
        canvas.drawLine(
            dx + barLine[0] - lineSpacing,
            barLine[1] + dy,
            dx + barLine[2] - lineSpacing,
            barLine[3] + dy,
            barLinePaint
        )
        val thickLinePaint = Paint()
        val thickLineStrokeWidth = lineSpacing / 2
        thickLinePaint.strokeWidth = thickLineStrokeWidth
        thickLinePaint.isAntiAlias = true
        thickLinePaint.isFilterBitmap = true
        canvas.drawLine(
            dx - thickLineStrokeWidth / 2,
            barLine[1] + dy,
            dx - thickLineStrokeWidth / 2,
            barLine[3] + dy,
            thickLinePaint
        )
    }

    private fun drawTimeSignature(
        canvas: Canvas,
        upperNumber: Int,
        lowerNumber: Int,
        dx: Float = 0f,
        dy: Float = 0f
    ) {
        val middle = 2 * lineSpacing.toInt()
        var resourceId = resources.getIdentifier(
            "time_$upperNumber", "drawable",
            context.packageName
        )
        var d = ResourcesCompat.getDrawable(
            resources,
            resourceId,
            null
        )
        d?.setBounds(0, 0, middle, middle)
        canvas.translate(dx, dy)
        d?.draw(canvas)
        canvas.translate(-dx, -dy)

        resourceId = resources.getIdentifier(
            "time_$lowerNumber", "drawable",
            context.packageName
        )
        d = ResourcesCompat.getDrawable(
            resources,
            resourceId,
            null
        )
        d?.setBounds(0, 0, middle, middle)

        val translationY = 2 * lineSpacing + dy
        canvas.translate(dx, translationY)
        d?.draw(canvas)
        canvas.translate(-dx, -translationY)
    }

    fun drawKeySignature(canvas: Canvas, dx: Float = 0f, dy: Float = 0f) {
        var resourceId = resources.getIdentifier(
            "note_gclef", "drawable",
            context.packageName
        )
        var d = resources.getDrawable(
            resourceId,
            null
        )
        d.setBounds(
            0,
            -lineSpacing.toInt() / 2,
            2 * lineSpacing.toInt(),
            (5.5f * lineSpacing).toInt()
        )
        canvas.translate(dx, dy)
        d.draw(canvas)
        canvas.translate(-dx, -dy)
    }

    private fun playMusic(measuresList: List<MeasureWithNotes>, playButton: ImageButton?) {
        val measureListCopy = measuresList.toMutableList()
        val sortedNotes = measureListCopy.map { it.notes }.flatten()
        globalScope = GlobalScope.launch(Dispatchers.Default) {
            var previousNoteLength = 0f
            var noteSounds = mutableListOf<SoundLoader>()
            for (unique in sortedNotes.sortedBy { it.length }.distinctBy { it.dx }
                .sortedBy { it.dx }) {
                for (note in sortedNotes.sortedBy { it.dx }) {
                    if (note.dx == unique.dx) {
                        val noteLength = 4 * note.length * (60f / compositionSpeed) * 1000.0f
                        val newNoteSoundID =
                            resources.getIdentifier(
                                "raw/${instrumentsWithMeasures.find { it.measures.any { measureWithNotes -> measureWithNotes.measure.id == note.measureId } }?.instrument?.name}${note.pitch}",
                                null,
                                context.packageName
                            )
                        noteSounds.add(
                            SoundLoader(
                                withContext(Dispatchers.Default) {
                                    loadSound(
                                        soundPool,
                                        newNoteSoundID
                                    )
                                },
                                noteLength.toLong(),
                                previousNoteLength.toLong()
                            )
                        )
                    }
                }
                previousNoteLength += 4 * unique.length * (60f / compositionSpeed) * 1000.0f
            }

            for (sound in noteSounds) {
                Handler(Looper.getMainLooper()).postDelayed({
                    val streamId = soundPool.play(sound.newNoteSoundID, 1f, 1f, 1, 0, 1f)

                    Handler(Looper.getMainLooper()).postDelayed({
                        soundPool.stop(streamId)
                    }, sound.noteLength)
                }, sound.previousNoteLength)
            }
            //wait to finish playing then change icon
            Handler(Looper.getMainLooper()).postDelayed({
                playButton?.setImageResource(R.drawable.ic_play)
                isMusicPlaying = false
            }, previousNoteLength.toLong())
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        soundPool.release()
        globalScope?.cancel()
    }

    private suspend fun loadSound(soundPool: SoundPool, soundId: Int): Int {
        return suspendCoroutine { continuation ->
            val soundLoadedListener =
                SoundPool.OnLoadCompleteListener { _, sampleId, status ->
                    if (status == 0) {
                        continuation.resume(sampleId)
                    } else {
                        continuation.resumeWithException(Exception("Sound loading failed"))
                    }
                }

            soundPool.setOnLoadCompleteListener(soundLoadedListener)
            soundPool.load(context, soundId, 1)
        }
    }
}
