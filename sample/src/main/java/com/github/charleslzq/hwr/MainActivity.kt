package com.github.charleslzq.hwr

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        hwrView.onCandidateSelected {
            text.append(it)
            updateButtonState()
        }
        hwrView.onCandidatesAvailable {
            updateButtonState()
            candidates.removeAllViewsInLayout()
            it.forEach {
                Button(candidates.context).apply {
                    it.bind(this)
                }.also {
                    candidates.addView(it)
                }
            }
        }
        undoButton.setOnClickListener {
            hwrView.undo()
            updateButtonState()
        }
        redoButton.setOnClickListener {
            hwrView.redo()
            updateButtonState()
        }
        clearButton.setOnClickListener {
            hwrView.reset()
            candidates.removeAllViewsInLayout()
            updateButtonState()
        }
        resetButton.setOnClickListener {
            text.text = ""
            hwrView.reset()
            candidates.removeAllViewsInLayout()
            updateButtonState()
        }
    }

    private fun updateButtonState() {
        undoButton.isEnabled = hwrView.canUndo()
        redoButton.isEnabled = hwrView.canRedo()
    }
}
