package com.github.charleslzq.hwr

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.github.charleslzq.hwview.support.UndoSupport
import com.sinovoice.hcicloudsdk.common.hwr.HwrRecogResult
import io.reactivex.subjects.PublishSubject


class HandWritingView
@JvmOverloads
constructor(
        context: Context,
        attributeSet: AttributeSet? = null,
        defStyle: Int = 0
) : FrameLayout(context, attributeSet, defStyle) {
    private val publisher = PublishSubject.create<HwrRecogResult>()
    private val strokes = UndoSupport<Stroke>()

    init {
        setOnTouchListener(StrokeListener { stroke, finished ->
            if (finished) {
                strokes.done(stroke)
                recognize()
            }
        })
    }

    fun canUndo() = strokes.canUndo()

    fun canRedo() = strokes.canRedo()

    fun undo() {
        if (canUndo()) {
            strokes.undo()
            recognize()
        }
    }

    fun redo() {
        if (canRedo()) {
            strokes.redo()
            recognize()
        }
    }

    fun reset() = strokes.reset()

    fun subscribe(handler: (HwrRecogResult) -> Unit) {
        publisher.subscribe(handler)
    }

    @Throws(HciRecogFailException::class, HciSessionException::class)
    private fun recognize() {
        publisher.onNext(HciHwrEngine.recognize(strokes.toShort()))
    }

    private fun UndoSupport<Stroke>.toShort() = doneList().toMutableList().apply {
        add(Stroke.EndStroke)
    }.flatMap { it.points }.flatMap { listOf(it.x.toShort(), it.y.toShort()) }.toShortArray()

    class Stroke {
        val points = mutableListOf<Point>()

        fun addPoint(x: Int, y: Int) {
            points.add(Point(x, y))
        }

        companion object {
            val EndStroke = Stroke().apply {
                addPoint(-1, 0)
                addPoint(-1, -1)
            }
        }
    }

    class StrokeListener(
            val onStroke: (Stroke, Boolean) -> Unit
    ) : View.OnTouchListener {
        private var currentStroke = Stroke()

        override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    currentStroke = Stroke()
                    currentStroke.addPoint(motionEvent.x.toInt(), motionEvent.y.toInt())
                    onStroke(currentStroke, false)
                }
                MotionEvent.ACTION_MOVE -> {
                    currentStroke.addPoint(motionEvent.x.toInt(), motionEvent.y.toInt())
                    onStroke(currentStroke, false)
                }
                MotionEvent.ACTION_UP -> {
                    currentStroke.addPoint(motionEvent.x.toInt(), motionEvent.y.toInt())
                    onStroke(currentStroke, true)
                }
            }
            return true
        }
    }

    companion object {
        const val TAG = "HandWritingView"
    }
}
