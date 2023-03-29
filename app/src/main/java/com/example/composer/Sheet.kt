package com.example.composer

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class Sheet : AppCompatActivity() {
    private lateinit var textView: TextView
    private lateinit var gestureDetector: ScaleGestureDetector
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sheet)
        textView = findViewById<TextView>(R.id.textView)
        gestureDetector = ScaleGestureDetector(this, PinchZoomListener())

        findViewById<ImageView>(R.id.imageView).setOnClickListener {
            val dialog = layoutInflater.inflate(R.layout.edit_note_dialog, null)
            val editNoteDialog = Dialog(this)
            editNoteDialog.setContentView(dialog)
            editNoteDialog.setCancelable(true)
            editNoteDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            editNoteDialog.show()

            dialog.findViewById<Button>(R.id.button2).setOnClickListener {
                editNoteDialog.dismiss()
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return true
    }

    inner class PinchZoomListener : SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            var scale = detector.scaleFactor
            if (scale > 1) {
                textView.text = "Zoom in"
            } else {
                textView.text = "Zoom out"
            }
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            return true
        }
    }
}