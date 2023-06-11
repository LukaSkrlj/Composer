package com.example.composer.activities

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import com.example.composer.R
import com.example.composer.models.InstrumentWithMeasures
import com.example.composer.models.Note


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

    private val notesDrawable: Array<String> = arrayOf(
        "accidental_doubleflat",
        "accidental_doublesharp",
        "accidental_flat",
        "accidental_natural",
        "accidental_sharp",
        "dynamic_crescendo",
        "dynamic_diminuendo",
        "dynamic_forte",
        "dynamic_fortepiano",
        "dynamic_fortissimo",
        "dynamic_mezzo_piano",
        "dynamic_pianissimo",
        "dynamic_pianississimo",
        "dynamic_piano",
        "dynamic_sforzando",
        "left_repeat",
        "note_beam",
        "note_cclef",
        "note_clef",
        "note_dot",
        "note_doublewholenote",
        "note_fclef",
        "note_gclef",
        "note_halfnote",
        "note_hundredtwentyeighthnote",
        "note_octwholenote",
        "note_quadwholenote",
        "note_quarternote",
        "note_semibreve",
        "note_semigarrapatea",
        "note_sixteenthnote",
        "note_sixtyfourth",
        "note_th",
        "note_thirtysecondnote",
        "quarter_note",
        "rest_crochet",
        "rest_doublewholerest",
        "rest_eighthrest",
        "rest_halfrest",
        "rest_hundredtwentyeighthrest",
        "rest_octwholerest",
        "rest_quadwholerest",
        "rest_sixteenthrest",
        "rest_sixtyfourthrest",
        "rest_thirtysecondrest",
        "rest_twohundredfiftysix",
        "right_repeat",
        "time_0",
        "time_1",
        "time_2",
        "time_3",
        "time_4",
        "time_5",
        "time_6",
        "time_7",
        "time_8",
        "time_9",
    )
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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var instrumentSpacing = 0f
        for (instrument in instrumentsWithMeasures) {

            var previousMeasureEnd = 0f
            for (measure in instrument.measures) {

                //Measure start position
                val currentMeasureEnd = measure.notes.last().dx + lastNoteMeasureSpacing
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

                previousMeasureEnd = measure.notes.last().dx + lastNoteMeasureSpacing
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
}