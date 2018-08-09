package com.github.charleslzq.hwr

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import com.github.charleslzq.hwr.support.callOnCompute
import com.github.charleslzq.hwr.support.runOnCompute
import com.github.charleslzq.hwr.support.runOnUI
import com.github.charleslzq.hwv.R
import com.github.charleslzq.hwview.support.UndoSupport
import com.sinovoice.hcicloudsdk.common.hwr.HwrRecogResult
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject


class HandWritingView
@JvmOverloads
constructor(
        context: Context,
        attributeSet: AttributeSet? = null,
        defStyle: Int = 0
) : ImageView(context, attributeSet, defStyle) {
    private val publisher = PublishSubject.create<List<Candidate>>()
    private val strokes = UndoSupport<Stroke>()
    private var currentStroke: Stroke? = null
    private var onSelect: (String) -> Unit = {}
    var preTextLength = 3
    val paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 3f
        isAntiAlias = true
        strokeJoin = Paint.Join.ROUND
        style = Paint.Style.STROKE
    }

    init {
        attributeSet?.let {
            context.obtainStyledAttributes(it, R.styleable.HandWritingView, defStyle, 0).apply {
                preTextLength = getInt(R.styleable.HandWritingView_preTextLength, 3)
                recycle()
            }
        }
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

    fun onCandidatesAvailable(handler: (List<Candidate>) -> Unit) {
        publisher.observeOn(AndroidSchedulers.mainThread()).subscribe(handler)
    }

    fun onCandidatesAvailable(resultHandler: ResultHandler) = onCandidatesAvailable {
        resultHandler.receive(it)
    }

    fun onCandidateSelected(handler: (String) -> Unit) {
        onSelect = {
            runOnUI {
                handler(it)
            }
        }
    }

    fun onCandidateSelected(candidateHandler: CandidateHandler) {
        onSelect = {
            runOnUI {
                candidateHandler.selected(it)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        strokes.doneList().forEach {
            canvas.drawPath(it.path, paint)
        }
        currentStroke?.let {
            canvas.drawPath(it.path, paint)
        }
    }

    private fun recognize() = publish {
        try {
            HciHwrEngine.recognize(strokes.toShort()).toCandidates()
        } catch (exception: HciException) {
            Log.e(TAG, "error code: ${exception.errorCode}", exception)
            emptyList<RecognizeCandidate>()
        }
    }

    private fun UndoSupport<Stroke>.toShort() = doneList().toMutableList().apply {
        add(Stroke.EndStroke)
    }.flatMap { it.points }.flatMap { listOf(it.x.toShort(), it.y.toShort()) }.toShortArray()

    private fun HwrRecogResult.toCandidates() = resultItemList.map {
        RecognizeCandidate(it.result) {
            generateAssociates(it)
            reset()
        }
    }

    private fun generateAssociates(context: Pair<String, String>) {
        onSelect(context.second)
        val preText = (context.first + context.second).takeLast(preTextLength)
        publish {
            try {
                HciHwrEngine.associate(preText).resultList.map {
                    AssociateCandidate(preText, it, this::generateAssociates)
                }
            } catch (exception: HciException) {
                Log.e(TAG, "error code: ${exception.errorCode}", exception)
                emptyList<AssociateCandidate>()
            }
        }
    }

    private fun publish(generateData: () -> List<Candidate>) = runOnCompute {
        publisher.onNext(generateData())
    }

    class Stroke {
        val points = mutableListOf<Point>()
        var finished = false
            private set
        val path = Path()

        @JvmOverloads
        fun addPoint(x: Int, y: Int, last: Boolean = false) {
            if (path.isEmpty) {
                path.moveTo(x.toFloat(), y.toFloat())
            } else {
                path.lineTo(x.toFloat(), y.toFloat())
            }
            if (!finished) {
                points.add(Point(x, y))
                if (last) {
                    finished = true
                }
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
            repeat(motionEvent.historySize) {
                currentStroke.addPoint(
                        motionEvent.getHistoricalX(it).toInt(),
                        motionEvent.getHistoricalY(it).toInt()
                )
            }
            currentStroke.addPoint(motionEvent.x.toInt(), motionEvent.y.toInt(), motionEvent.action == MotionEvent.ACTION_UP)
            onStroke(currentStroke)
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                currentStroke = Stroke()
            }
            return true
        }
    }

    interface ResultHandler {
        fun receive(candidates: List<Candidate>)
    }

    interface CandidateHandler {
        fun selected(content: String)
    }

    companion object {
        const val TAG = "HandWritingView"
    }
}
