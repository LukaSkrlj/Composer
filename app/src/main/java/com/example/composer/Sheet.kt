package com.example.composer

import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
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

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return true
    }

    inner class PinchZoomListener : SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            var scale = detector.scaleFactor
            if (scale > 1) {
                textView.text = "Zoom out"
            } else {
                textView.text = "Zoom in"
            }
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            return true
        }
    }
}