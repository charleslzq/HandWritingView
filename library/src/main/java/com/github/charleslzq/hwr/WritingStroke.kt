package com.github.charleslzq.hwr

import android.graphics.Point

class WritingStroke {
    val points = mutableListOf<Point>()

    fun addPoint(x: Int, y: Int) {
        points.add(Point(x, y))
    }

    fun endStroke() {
        addPoint(-1, 0)
        addPoint(-1, -1)
    }

    companion object {
        val EndStroke = WritingStroke().apply {
            endStroke()
        }
    }
}