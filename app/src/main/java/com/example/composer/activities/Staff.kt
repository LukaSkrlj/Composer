package com.example.composer.activities

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.example.composer.R
import com.example.composer.models.Note

class Staff @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val linesCount = 5
    private val lineThickness = 2f
    private val staffWidth = 500f
    private val firstLine: Array<Float> =
        arrayOf(0f, lineThickness / 2, staffWidth, lineThickness / 2)
    private var lines: Array<Array<Float>> = arrayOf()

    var notes: List<Note> = listOf()

    companion object {
        // Note is starting on first line, note F3, use spacing to move notes vertically
        private val spacing = 20f
        fun getSpacing(): Float {
            return spacing
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var lastLine: Array<Float> = firstLine
        for (i in 1..linesCount) {
            lines += lastLine
            lastLine =
                lastLine.mapIndexed { index, value -> if (index % 2 == 1) value + spacing else value }
                    .toTypedArray()
        }

        val paint = Paint()
        paint.strokeWidth = lineThickness
        paint.isAntiAlias = true
        paint.isFilterBitmap = true
        canvas.drawLines(lines.flatten().toFloatArray(), paint)

        for (note in notes) {
            val d = resources.getDrawable(
                R.drawable.quarter_note,
                null
            )
            d.setBounds(note.left, note.top, note.right, note.bottom)
            canvas.translate(note.dx, note.dy)
            d.draw(canvas)
            canvas.translate(-note.dx, -note.dy)
        }
    }


    fun drawNotes(notesList: List<Note>) {
        notes = notesList
        invalidate()
    }
}