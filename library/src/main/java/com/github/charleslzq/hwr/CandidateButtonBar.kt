package com.github.charleslzq.hwr

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

class CandidateButtonBar
@JvmOverloads
constructor(
        context: Context,
        attributeSet: AttributeSet? = null,
        defStyle: Int = 0
) : LinearLayout(context, attributeSet, defStyle) {
    private var customButton: (CandidateButton) -> Unit = {}
    private var onSelect: (String) -> Unit = {}

    init {
        orientation = HORIZONTAL
    }

    fun link(handWritingView: HandWritingView) {
        handWritingView.subscribe {
            removeAllViewsInLayout()
            it.forEach {
                CandidateButton(context, it, onSelect).apply {
                    customButton(this)
                }.also {
                    addView(it)
                }
            }
        }
    }

    fun subscribe(handler: (String) -> Unit) {
        onSelect = handler
    }
}