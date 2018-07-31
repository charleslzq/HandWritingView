package com.github.charleslzq.hwr

import android.view.MotionEvent
import android.view.View

class WritingStrokeListener(
        val onStroke: (WritingStroke) -> Unit
): View.OnTouchListener {
    private var currentStroke = WritingStroke()

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> currentStroke = WritingStroke()
            MotionEvent.ACTION_MOVE -> currentStroke.addPoint(motionEvent.x.toInt(), motionEvent.y.toInt())
            MotionEvent.ACTION_UP -> {
                currentStroke.addPoint(motionEvent.x.toInt(), motionEvent.y.toInt())
                onStroke(currentStroke)
            }
        }
        return true
    }
}