package com.github.charleslzq.hwr

import android.content.Context
import android.widget.Button

sealed class Candidate(val preText: String, val content: String, val onSelect: (Pair<String, String>) -> Unit) {
    fun select(): String {
        onSelect(preText to content)
        return content
    }
}

class RecognizeCandidate(content: String, onSelect: (Pair<String, String>) -> Unit) : Candidate("", content, onSelect)

class AssociateCandidate(preText: String, content: String, onSelect: (Pair<String, String>) -> Unit) : Candidate(preText, content, onSelect)

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
