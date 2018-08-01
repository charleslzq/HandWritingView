package com.github.charleslzq.hwr

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import com.github.charleslzq.hwview.support.UndoSupport
import com.sinovoice.hcicloudsdk.common.hwr.HwrRecogResult
import io.reactivex.subjects.PublishSubject


class HandWritingView
@JvmOverloads
constructor(
        context: Context,
        attributeSet: AttributeSet? = null,
        defStyle: Int = 0
) : ImageView(context, attributeSet, defStyle) {
    private val publisher = PublishSubject.create<HwrRecogResult>()
    private val strokes = UndoSupport<Stroke>()
    private var currentStroke: Stroke? = null
    val paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 3f
        isAntiAlias = true
        strokeJoin = Paint.Join.ROUND
        style = Paint.Style.STROKE
    }

    init {
        setOnTouchListener(StrokeListener {
            currentStroke = it
            if (it.finished) {
                strokes.done(it)
                currentStroke = null
                recognize()
            }
            invalidate()
        })
    }

    fun canUndo() = strokes.canUndo()

    fun canRedo() = strokes.canRedo()

    fun undo() {
        if (canUndo()) {
            strokes.undo()
            recognize()
            invalidate()
        }
    }

    fun redo() {
        if (canRedo()) {
            strokes.redo()
            recognize()
            invalidate()
        }
    }

    fun reset() {
        strokes.reset()
        currentStroke = null
        invalidate()
    }

    fun subscribe(handler: (HwrRecogResult) -> Unit) {
        publisher.subscribe(handler)
    }

    override fun onDraw(canvas: Canvas) {
        strokes.doneList().forEach {
            canvas.drawPath(it.toPath(), paint)
        }
        currentStroke?.let {
            canvas.drawPath(it.toPath(), paint)
        }
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
        var finished = false
            private set

        @JvmOverloads
        fun addPoint(x: Int, y: Int, last: Boolean = false) {
            if (!finished) {
                points.add(Point(x, y))
                if (last) {
                    finished = true
                }
            }
        }

        fun toPath() = Path().apply {
            moveTo(points[0].x.toFloat(), points[0].y.toFloat())
            repeat(points.size - 1) {
                lineTo(points[it + 1].x.toFloat(), points[it + 1].y.toFloat())
            }
        }

        companion object {
            val EndStroke = Stroke().apply {
                addPoint(-1, 0)
                addPoint(-1, -1)
            }
        }
    }

    class StrokeListener(
            val onStroke: (Stroke) -> Unit
    ) : View.OnTouchListener {
        private var currentStroke = Stroke()

        override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    currentStroke = Stroke()
                    currentStroke.addPoint(motionEvent.x.toInt(), motionEvent.y.toInt())
                    onStroke(currentStroke)
                }
                MotionEvent.ACTION_MOVE -> {
                    currentStroke.addPoint(motionEvent.x.toInt(), motionEvent.y.toInt())
                    onStroke(currentStroke)
                }
                MotionEvent.ACTION_UP -> {
                    currentStroke.addPoint(motionEvent.x.toInt(), motionEvent.y.toInt(), true)
                    onStroke(currentStroke)
                }
            }
            return true
        }
    }

    companion object {
        const val TAG = "HandWritingView"
    }
}
