package com.github.charleslzq.hwr

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.LinearLayout

open class CandidateButton(
        context: Context,
        private val candidate: Candidate,
        private val onSelect: (String) -> Unit
) : Button(context) {
    init {
        text = candidate.content
        setOnClickListener {
            onSelect(candidate.select())
        }
    }
}

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
        handWritingView.onResult {
            removeAllViewsInLayout()
            it.forEach {
                CandidateButton(context, it) {
                    onSelect(it)
                }.apply {
                    customButton(this)
                }.also {
                    addView(it)
                }
            }
        }
    }

    fun onCandidateSelected(handler: (String) -> Unit) {
        onSelect = handler
    }

    fun onCandidateSelected(candidateHandler: CandidateHandler) {
        onSelect = {
            candidateHandler.selected(it)
        }
    }

    fun custom(customer: (CandidateButton) -> Unit) {
        customButton = customer
    }

    fun custom(candidateButtonCustomer: CandidateButtonCustomer) {
        customButton = {
            candidateButtonCustomer.custom(it)
        }
    }

    interface CandidateHandler {
        fun selected(content: String)
    }

    interface CandidateButtonCustomer {
        fun custom(candidateButton: CandidateButton)
    }
}