package com.github.charleslzq.hwr

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        candidates.link(hwrView)
        candidates.onCandidateSelected {
            text.append(it)
            updateButtonState()
        }
        hwrView.onResult {
            updateButtonState()
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
            updateButtonState()
        }
        resetButton.setOnClickListener {
            text.text = ""
            hwrView.reset()
            updateButtonState()
        }
    }

    private fun updateButtonState() {
        undoButton.isEnabled = hwrView.canUndo()
        redoButton.isEnabled = hwrView.canRedo()
    }
}
