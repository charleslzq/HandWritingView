package com.github.charleslzq.hwr

import android.widget.Button

sealed class Candidate(
        val preText: String,
        val content: String,
        private val onSelect: (Pair<String, String>) -> Unit) {
    fun select() {
        onSelect(preText to content)
    }

    fun bind(button: Button) {
        button.text = content
        button.setOnClickListener {
            select()
        }
    }
}

class RecognizeCandidate(content: String, onSelect: (Pair<String, String>) -> Unit) : Candidate("", content, onSelect)

class AssociateCandidate(preText: String, content: String, onSelect: (Pair<String, String>) -> Unit) : Candidate(preText, content, onSelect)
