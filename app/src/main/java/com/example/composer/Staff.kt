package com.example.composer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class Staff @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val linesCount = 5
    private val lineThickness = 2f
    private val staffWidth = 500f
    private val firstLine: Array<Float> =
        arrayOf(0f, lineThickness / 2, staffWidth, lineThickness / 2)
    private var lines: Array<Array<Float>> = arrayOf()
    private val spacing = 20f
    var staffCanvas: Canvas? = null

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        staffCanvas = canvas
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
        canvas?.drawLines(lines.flatten().toFloatArray(), paint)
        val d = resources.getDrawable(
            R.drawable.quarter_note,
            null
        )

        d.setBounds(0, 0, 82, 82)
        d.draw(canvas!!)
        drawNotes()
    }

    fun drawNotes() {
        val d = resources.getDrawable(
            R.drawable.quarter_note,
            null
        )
        d.setBounds(0, 0, 82, 82)
        staffCanvas?.translate(50f, 250f)
        d.draw(staffCanvas!!)
    }
}