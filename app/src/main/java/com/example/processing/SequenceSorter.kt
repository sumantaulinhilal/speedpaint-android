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

        val rawSorted = when (order) {
            SequenceOrder.AUTO -> {
                val (textPaths, nonTextPaths) = paths.partition { it.isText }
                val sortedText = optimizePathOrderGreedy(textPaths)
                val sortedNonText = optimizePathOrderGreedy(nonTextPaths)
                sortedText + sortedNonText
            }

            SequenceOrder.LEFT_TO_RIGHT -> {
                optimizePathOrderGreedy(paths.sortedBy { it.boundingBox.minX })
            }

            SequenceOrder.RIGHT_TO_LEFT -> {
                optimizePathOrderGreedy(paths.sortedByDescending { it.boundingBox.maxX })
            }

            SequenceOrder.TOP_TO_BOTTOM -> {
                optimizePathOrderGreedy(paths.sortedBy { it.boundingBox.minY })
            }

            SequenceOrder.BOTTOM_TO_TOP -> {
                optimizePathOrderGreedy(paths.sortedByDescending { it.boundingBox.maxY })
            }

            SequenceOrder.CENTER_OUT -> {
                val sortedByCenter = paths.sortedBy { path ->
                    hypot((path.center.x - center.x).toDouble(), (path.center.y - center.y).toDouble()).toFloat()
                }
                optimizePathOrderGreedy(sortedByCenter)
            }

            SequenceOrder.SPIRAL -> {
                val sortedSpiral = paths.sortedWith(compareBy<VectorPath> { path ->
                    hypot((path.center.x - center.x).toDouble(), (path.center.y - center.y).toDouble()).toFloat()
                }.thenBy { path ->
                    val angle = atan2((path.center.y - center.y).toDouble(), (path.center.x - center.x).toDouble())
                    (angle + Math.PI).toFloat()
                })
                optimizePathOrderGreedy(sortedSpiral)
            }

            SequenceOrder.RANDOM -> {
                paths.shuffled()
            }

            SequenceOrder.TEXT_FIRST -> {
                val (textPaths, nonTextPaths) = paths.partition { it.isText }
                optimizePathOrderGreedy(textPaths) + optimizePathOrderGreedy(nonTextPaths)
            }

            SequenceOrder.TEXT_LAST -> {
                val (textPaths, nonTextPaths) = paths.partition { it.isText }
                optimizePathOrderGreedy(nonTextPaths) + optimizePathOrderGreedy(textPaths)
            }
        }

        return rawSorted
    }

    /**
     * Greedy Travelling Salesperson Optimization for paths:
     * - Finds nearest next path endpoint.
     * - Reverses path points if ending at the endPoint is closer than starting at startPoint.
     * - Minimizes hand jumping across the canvas for 100% natural, continuous drawing motion.
     */
    private fun optimizePathOrderGreedy(inputPaths: List<VectorPath>): List<VectorPath> {
        if (inputPaths.size <= 1) return inputPaths

        val unvisited = inputPaths.toMutableList()
        val result = mutableListOf<VectorPath>()

        // Start with the top-left-most path or first path
        var current = unvisited.minByOrNull { it.boundingBox.minY * 2 + it.boundingBox.minX } ?: unvisited.first()
        unvisited.remove(current)
        result.add(current)

        var currentPoint = current.endPoint

        while (unvisited.isNotEmpty()) {
            var bestIndex = -1
            var bestDistance = Float.MAX_VALUE
            var shouldReverse = false

            for (i in unvisited.indices) {
                val candidate = unvisited[i]
                val distStart = distanceSq(currentPoint, candidate.startPoint)
                val distEnd = distanceSq(currentPoint, candidate.endPoint)

                if (distStart < bestDistance) {
                    bestDistance = distStart
                    bestIndex = i
                    shouldReverse = false
                }
                if (distEnd < bestDistance) {
                    bestDistance = distEnd
                    bestIndex = i
                    shouldReverse = true
                }
            }

            if (bestIndex != -1) {
                val nextPath = unvisited.removeAt(bestIndex)
                val finalPath = if (shouldReverse) {
                    nextPath.copy(points = nextPath.points.reversed())
                } else {
                    nextPath
                }
                result.add(finalPath)
                currentPoint = finalPath.endPoint
            } else {
                val nextPath = unvisited.removeAt(0)
                result.add(nextPath)
                currentPoint = nextPath.endPoint
            }
        }

        return result
    }

    private fun distanceSq(p1: Point2D, p2: Point2D): Float {
        val dx = p1.x - p2.x
        val dy = p1.y - p2.y
        return dx * dx + dy * dy
    }
}
