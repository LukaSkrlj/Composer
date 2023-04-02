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
    private val lineThickness = 4f
    private val staffWidth = 500f
    private val firstLine: Array<Float> =
        arrayOf(0f, lineThickness / 2, staffWidth, lineThickness / 2)
    private var lines: Array<Array<Float>> = arrayOf()
    private val spacing = 25f

    override fun onDraw(canvas: Canvas?) {
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

        canvas?.drawLines(lines.flatten().toFloatArray(), paint)
    }
}