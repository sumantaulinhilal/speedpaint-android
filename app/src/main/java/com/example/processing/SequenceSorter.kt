package com.example.processing

import com.example.model.Point2D
import com.example.model.SequenceOrder
import com.example.model.VectorPath
import kotlin.math.atan2
import kotlin.math.hypot

object SequenceSorter {

    fun sortPaths(paths: List<VectorPath>, order: SequenceOrder, canvasWidth: Float = 600f, canvasHeight: Float = 600f): List<VectorPath> {
        if (paths.isEmpty()) return paths

        val center = Point2D(canvasWidth / 2f, canvasHeight / 2f)

        return when (order) {
            SequenceOrder.AUTO -> {
                val (textPaths, nonTextPaths) = paths.partition { it.isText }
                val sortedText = textPaths.sortedWith(compareBy({ it.boundingBox.minY }, { it.boundingBox.minX }))
                val sortedNonText = nonTextPaths.sortedWith(compareBy({ it.boundingBox.minY }, { it.boundingBox.minX }))
                sortedText + sortedNonText
            }

            SequenceOrder.LEFT_TO_RIGHT -> {
                paths.sortedBy { it.boundingBox.minX }
            }

            SequenceOrder.RIGHT_TO_LEFT -> {
                paths.sortedByDescending { it.boundingBox.maxX }
            }

            SequenceOrder.TOP_TO_BOTTOM -> {
                paths.sortedBy { it.boundingBox.minY }
            }

            SequenceOrder.BOTTOM_TO_TOP -> {
                paths.sortedByDescending { it.boundingBox.maxY }
            }

            SequenceOrder.CENTER_OUT -> {
                paths.sortedBy { path ->
                    hypot((path.center.x - center.x).toDouble(), (path.center.y - center.y).toDouble()).toFloat()
                }
            }

            SequenceOrder.SPIRAL -> {
                paths.sortedWith(compareBy<VectorPath> { path ->
                    hypot((path.center.x - center.x).toDouble(), (path.center.y - center.y).toDouble()).toFloat()
                }.thenBy { path ->
                    val angle = atan2((path.center.y - center.y).toDouble(), (path.center.x - center.x).toDouble())
                    (angle + Math.PI).toFloat()
                })
            }

            SequenceOrder.RANDOM -> {
                paths.shuffled()
            }

            SequenceOrder.TEXT_FIRST -> {
                val (textPaths, nonTextPaths) = paths.partition { it.isText }
                textPaths + nonTextPaths
            }

            SequenceOrder.TEXT_LAST -> {
                val (textPaths, nonTextPaths) = paths.partition { it.isText }
                nonTextPaths + textPaths
            }
        }
    }
}
