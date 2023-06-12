package com.example.composer.activities

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.media.SoundPool
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.content.res.ResourcesCompat
import com.example.composer.R
import com.example.composer.models.InstrumentWithMeasures
import com.example.composer.models.MeasureWithNotes
import com.example.composer.models.Note
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


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

    private val barLinePaint = Paint()
    private val playingLinePaint = Paint()
    private var xPositionPlayingLine = 0f
    private var globalScope: Job? = null
    private val soundPool: SoundPool = SoundPool.Builder().setMaxStreams(100).build()
    val notesHmap = LinkedHashMap<String, Note>()
    private val linesCount = 5
    private val lineThickness = 2f
    private val lastNoteMeasureSpacing = 150f
    private val defaultInstrumentSpacing = 300f

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
    private var notes: List<Note> = emptyList()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var instrumentSpacing = 0f
        for (instrument in instrumentsWithMeasures) {

            var previousMeasureEnd = 0f
            for (measure in instrument.measures) {

                //Measure start position
                var currentMeasureEnd = 0f
                if (measure.notes.isNotEmpty()) {
                    currentMeasureEnd = measure.notes.last().dx + lastNoteMeasureSpacing
                }

//            measure(currentMeasureEnd.toInt(), 500)
                this.layoutParams = ViewGroup.LayoutParams(currentMeasureEnd.toInt(), 500)
                //Bar line
                canvas.drawLine(
                    barLine[0] + currentMeasureEnd,
                    barLine[1],
                    barLine[2] + currentMeasureEnd,
                    barLine[3],
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

                Log.d("notes", measure.notes.toString())

                drawTimeSignature(
                    canvas,
                    measure.measure.timeSignatureTop,
                    measure.measure.timeSignatureBottom,
                    dy = instrumentSpacing
                )
                val startingOffset = 50f
                for (note in measure.notes) {
                    val d = resources.getDrawable(
                        R.drawable.quarter_note,
                        null
                    )
                    d.setBounds(note.left, note.top, note.right, note.bottom)
                    canvas.translate(note.dx + startingOffset, note.dy)
                    d.draw(canvas)
                    canvas.translate(-note.dx - startingOffset, -note.dy)
                }
                if (measure.notes.isNotEmpty()) {
                    previousMeasureEnd = measure.notes.last().dx + lastNoteMeasureSpacing
                }
            }
            this.drawEnd(canvas, previousMeasureEnd)
            instrumentSpacing += defaultInstrumentSpacing
        }

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)
    }


    fun drawNotes(instruments: List<InstrumentWithMeasures>) {
        instrumentsWithMeasures = instruments
        Log.d("AAAAAAAAAAAAAAAAAAAAAAAAA", instrumentsWithMeasures.joinToString(" "))
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
//        notesDrawable.forEach { note ->
//            val resourceId = resources.getIdentifier(
//                note, "drawable",
//                context.packageName
//            )
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
        val signatureArray: ArrayList<Drawable?> = ArrayList<Drawable?>(numberOfSignatures)
        val signatureHeight = (lineSpacing * 2).toInt()
        val signatureWidth = (notesHeight / 4)

        for (i in 0..numberOfSignatures - 1) {
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

    fun drawEnd(canvas: Canvas, dx: Float) {
        canvas.drawLine(
            dx + barLine[0] - lineSpacing,
            barLine[1],
            dx + barLine[2] - lineSpacing,
            barLine[3],
            barLinePaint
        )
        val thickLinePaint = Paint()
        val thickLineStrokeWidth = lineSpacing / 2
        thickLinePaint.strokeWidth = thickLineStrokeWidth
        thickLinePaint.isAntiAlias = true
        thickLinePaint.isFilterBitmap = true
        canvas.drawLine(
            dx - thickLineStrokeWidth / 2,
            barLine[1],
            dx - thickLineStrokeWidth / 2,
            barLine[3],
            thickLinePaint
        )
    }

    fun drawTimeSignature(
        canvas: Canvas,
        upperNumber: Int,
        lowerNumber: Int,
        dx: Float = 0f,
        dy: Float = 0f
    ) {
        val upperBound = (lines.last().last() / 2 + dy).toInt()
        var resourceId = resources.getIdentifier(
            "time_$upperNumber", "drawable",
            context.packageName
        )
        var d = resources.getDrawable(
            resourceId,
            null
        )
        d.setBounds(0, 0, upperBound, upperBound)
        canvas.translate(dx, 0f)
        d.draw(canvas)
        canvas.translate(-dx, 0f)

        val lowerBound = lines.first().first().toInt() / 2
        resourceId = resources.getIdentifier(
            "time_$lowerNumber", "drawable",
            context.packageName
        )
        d = resources.getDrawable(
            resourceId,
            null
        )
        d.setBounds(0, 0, upperBound, upperBound)
        canvas.translate(dx, upperBound.toFloat())
        d.draw(canvas)
        canvas.translate(-dx, -upperBound.toFloat())
    }


//    private fun drawPlayingLine(canvas: Canvas, x: Float) {
//        playingLinePaint.strokeWidth = 10F
//
//
//        val handler = Handler(Looper.getMainLooper());
//        val movePlayer0Runnable = object : Runnable {
//            override fun run() {
//                canvas.drawLine(
//                    x,
//                    0f,
//                    x,
//                    canvas.height.toFloat(),
//                    playingLinePaint
//                )
//                xPositionPlayingLine += 0.1f
//                invalidate(); //will trigger the onDraw
//                handler.postDelayed(this, 1); //in 5 sec player0 will move again
//            }
//        }
//
//        if (x.compareTo(canvas.width) == 0 || x.compareTo(canvas.width) == 1) {
//            isMusicPlaying = false
//            xPositionPlayingLine = 0f
//            return
//        }
//        movePlayer0Runnable.run()
//
//
//    }


    private fun playMusic(measuresList: List<MeasureWithNotes>, playButton: ImageButton?) {
        val measureListCopy = measuresList.toMutableList()
        globalScope = GlobalScope.launch(Dispatchers.IO) {
            for (measure in measureListCopy) {
                for ((noteIndex, note) in measure.notes.withIndex()) {
                    val noteLength =
                        (measure.measure.timeSignatureBottom.toFloat() * note.length * (60.0f / 100.0f)) * 1000.0f
                    val loadedMultipleSounds = ArrayList<Int>()
                    val currentNoteDx = note.dx


                    if (noteIndex + 1 < measure.notes.size) {
                        var notesIndexNext = noteIndex + 1
                        var nextNoteDx = measure.notes[notesIndexNext].dx

                        //while notes have same dx load them to array
                        while (nextNoteDx.compareTo(currentNoteDx) == 0) {
                            val newNoteSoundID =
                                resources.getIdentifier(
                                    "raw/${measure.notes[notesIndexNext].key}",
                                    null,
                                    context.packageName
                                )
                            val newNoteSound = async { loadSound(soundPool, newNoteSoundID) }
                            loadedMultipleSounds.add(newNoteSound.await())

                            if (notesIndexNext + 1 < measure.notes.size) {
                                notesIndexNext++
                                nextNoteDx = measure.notes[notesIndexNext].dx

                            } else {
                                break
                            }
                        }
                    }

                    if (loadedMultipleSounds.size > 0) {
                        for (loadedSound in loadedMultipleSounds) {
                            soundPool.play(loadedSound, 1f, 1f, 1, 0, 1f)
                        }
                    } else {
                        val fileName =
                            resources.getIdentifier(
                                "raw/${note.key}",
                                null,
                                context.packageName
                            )
                        val loadedSoundId = async { loadSound(soundPool, fileName) }
                        soundPool.play(loadedSoundId.await(), 1f, 1f, 1, 0, 1f)
                        delay(noteLength.toLong())
                    }

                }
            }
            playButton?.setImageResource(R.drawable.ic_play)
            isMusicPlaying = false
            return@launch
        }

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        globalScope?.cancel()
    }

    private suspend fun loadSound(soundPool: SoundPool, soundId: Int): Int {
        return suspendCoroutine { continuation ->
            val soundLoadedListener =
                SoundPool.OnLoadCompleteListener { pool, sampleId, status ->
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
