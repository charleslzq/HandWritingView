package com.github.charleslzq.hwr

sealed class Candidate(val preText: String, val content: String, val onSelect: (Pair<String, String>) -> Unit) {
    fun select(): String {
        onSelect(preText to content)
        return content
    }
}

class RecognizeCandidate(content: String, onSelect: (Pair<String, String>) -> Unit) : Candidate("", content, onSelect)

class AssociateCandidate(preText: String, content: String, onSelect: (Pair<String, String>) -> Unit) : Candidate(preText, content, onSelect)
