package com.github.charleslzq.hwr

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        hwrView.subscribe {
            println(it.resultItemList.joinToString(";") { it.result })
            updateButtonState()
        }
        resetButton.setOnClickListener {
            hwrView.reset()
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
    }

    private fun updateButtonState() {
        undoButton.isEnabled = hwrView.canUndo()
        redoButton.isEnabled = hwrView.canRedo()
    }
}
