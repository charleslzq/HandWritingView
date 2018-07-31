package com.github.charleslzq.hwr

import android.content.Context
import android.util.AttributeSet
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
    private val strokes = UndoSupport<WritingStroke>()

    init {
        setOnTouchListener(WritingStrokeListener {
            strokes.done(it)
            recognize()
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

    private fun UndoSupport<WritingStroke>.toShort() = doneList().toMutableList().apply {
        add(WritingStroke.EndStroke)
    }.flatMap { it.points }.flatMap { listOf(it.x.toShort(), it.y.toShort()) }.toShortArray()

    companion object {
        const val TAG = "HandWritingView"
    }
}
