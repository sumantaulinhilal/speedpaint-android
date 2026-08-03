package com.example.processing

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import com.example.model.PathBoundingBox
import com.example.model.Point2D
import com.example.model.PresetSamples
import com.example.model.VectorPath
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.max

object VectorizationEngine {

    /**
     * Converts a raw input Bitmap into vector paths for speedpaint whiteboard drawing.
     */
    fun processBitmapToVectorPaths(
        bitmap: Bitmap,
        targetWidth: Float = 600f,
        targetHeight: Float = 600f,
        threshold: Int = 40,
        minPathLength: Float = 15f
    ): List<VectorPath> {
        // Step 1: Scale down bitmap for fast local image processing
        val maxDim = 400
        val scale = minOf(maxDim.toFloat() / bitmap.width, maxDim.toFloat() / bitmap.height, 1.0f)
        val scaledW = (bitmap.width * scale).toInt().coerceAtLeast(10)
        val scaledH = (bitmap.height * scale).toInt().coerceAtLeast(10)

        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledW, scaledH, true)

        // Step 2: Convert to Grayscale & Luminance matrix
        val pixels = IntArray(scaledW * scaledH)
        scaledBitmap.getPixels(pixels, 0, scaledW, 0, 0, scaledW, scaledH)

        val gray = FloatArray(scaledW * scaledH)
        val colors = Array(scaledH) { IntArray(scaledW) }

        for (y in 0 until scaledH) {
            for (x in 0 until scaledW) {
                val pixel = pixels[y * scaledW + x]
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                val luma = 0.299f * r + 0.587f * g + 0.114f * b
                gray[y * scaledW + x] = luma
                colors[y][x] = pixel
            }
        }

        // Step 3: Sobel Filter for Edge Magnitude & Direction Detection
        val edges = BooleanArray(scaledW * scaledH)
        val edgeColors = Array(scaledH) { arrayOfNulls<Color>(scaledW) }

        val gxKernel = arrayOf(
            intArrayOf(-1, 0, 1),
            intArrayOf(-2, 0, 2),
            intArrayOf(-1, 0, 1)
        )
        val gyKernel = arrayOf(
            intArrayOf(-1, -2, -1),
            intArrayOf(0, 0, 0),
            intArrayOf(1, 2, 1)
        )

        for (y in 1 until scaledH - 1) {
            for (x in 1 until scaledW - 1) {
                var gx = 0f
                var gy = 0f

                for (ky in -1..1) {
                    for (kx in -1..1) {
                        val pixelLuma = gray[(y + ky) * scaledW + (x + kx)]
                        gx += pixelLuma * gxKernel[ky + 1][kx + 1]
                        gy += pixelLuma * gyKernel[ky + 1][kx + 1]
                    }
                }

                val mag = hypot(gx.toDouble(), gy.toDouble()).toFloat()
                if (mag > threshold) {
                    edges[y * scaledW + x] = true
                    val originalIntColor = colors[y][x]
                    edgeColors[y][x] = Color(originalIntColor)
                }
            }
        }

        // Step 4: Contour / Path Tracing using 8-neighbor connectivity
        val visited = BooleanArray(scaledW * scaledH)
        val rawPaths = mutableListOf<List<Point2D>>()
        val rawPathColors = mutableListOf<Color>()

        val dxDirs = intArrayOf(1, 1, 0, -1, -1, -1, 0, 1)
        val dyDirs = intArrayOf(0, 1, 1, 1, 0, -1, -1, -1)

        val scaleX = targetWidth / scaledW
        val scaleY = targetHeight / scaledH

        for (y in 1 until scaledH - 1) {
            for (x in 1 until scaledW - 1) {
                val idx = y * scaledW + x
                if (edges[idx] && !visited[idx]) {
                    // Trace connected component
                    val currentPathPoints = mutableListOf<Point2D>()
                    var currX = x
                    var currY = y

                    val sampledColor = edgeColors[y][x] ?: Color.Black

                    while (currX in 0 until scaledW && currY in 0 until scaledH) {
                        val cIdx = currY * scaledW + currX
                        if (!edges[cIdx] || visited[cIdx]) break

                        visited[cIdx] = true
                        currentPathPoints.add(Point2D(currX * scaleX, currY * scaleY))

                        // Find next unvisited neighbor
                        var foundNeighbor = false
                        for (d in 0 until 8) {
                            val nx = currX + dxDirs[d]
                            val ny = currY + dyDirs[d]
                            if (nx in 0 until scaledW && ny in 0 until scaledH) {
                                val nIdx = ny * scaledW + nx
                                if (edges[nIdx] && !visited[nIdx]) {
                                    currX = nx
                                    currY = ny
                                    foundNeighbor = true
                                    break
                                }
                            }
                        }
                        if (!foundNeighbor) break
                    }

                    if (currentPathPoints.size >= 3) {
                        rawPaths.add(currentPathPoints)
                        rawPathColors.add(sampledColor)
                    }
                }
            }
        }

        // Step 5: Path Simplification (RDP) & Text Detection Classification
        val finalPaths = mutableListOf<VectorPath>()

        for (i in rawPaths.indices) {
            val pts = rawPaths[i]
            val simplified = ramerDouglasPeucker(pts, epsilon = 2.5f)

            val totalLen = calculatePathLength(simplified)
            if (totalLen >= minPathLength) {
                val bbox = PathBoundingBox.calculate(simplified)

                // Detect text characteristic (compact height, high horizontal aspect ratio)
                val isText = bbox.height in 8f..40f && bbox.width > bbox.height * 1.5f

                finalPaths.add(
                    VectorPath(
                        points = simplified,
                        color = rawPathColors[i],
                        strokeWidth = if (isText) 2.5f else 3.5f,
                        isText = isText,
                        totalLength = totalLen,
                        boundingBox = bbox
                    )
                )
            }
        }

        return if (finalPaths.isEmpty()) {
            // Fallback grid pattern if image had low edges
            PresetSamples.samples.first().sampleSvgPaths
        } else {
            finalPaths
        }
    }

    private fun calculatePathLength(points: List<Point2D>): Float {
        var len = 0f
        for (i in 0 until points.size - 1) {
            val p1 = points[i]
            val p2 = points[i + 1]
            len += hypot((p2.x - p1.x).toDouble(), (p2.y - p1.y).toDouble()).toFloat()
        }
        return len
    }

    private fun ramerDouglasPeucker(points: List<Point2D>, epsilon: Float): List<Point2D> {
        if (points.size <= 2) return points

        var dmax = 0f
        var index = 0
        val end = points.size - 1

        for (i in 1 until end) {
            val d = perpendicularDistance(points[i], points[0], points[end])
            if (d > dmax) {
                index = i
                dmax = d
            }
        }

        return if (dmax > epsilon) {
            val recursiveResult1 = ramerDouglasPeucker(points.subList(0, index + 1), epsilon)
            val recursiveResult2 = ramerDouglasPeucker(points.subList(index, points.size), epsilon)

            recursiveResult1.dropLast(1) + recursiveResult2
        } else {
            listOf(points.first(), points.last())
        }
    }

    private fun perpendicularDistance(point: Point2D, lineStart: Point2D, lineEnd: Point2D): Float {
        val dx = lineEnd.x - lineStart.x
        val dy = lineEnd.y - lineStart.y

        if (dx == 0f && dy == 0f) {
            return hypot((point.x - lineStart.x).toDouble(), (point.y - lineStart.y).toDouble()).toFloat()
        }

        val numerator = abs(dy * point.x - dx * point.y + lineEnd.x * lineStart.y - lineEnd.y * lineStart.x)
        val denominator = hypot(dx.toDouble(), dy.toDouble()).toFloat()

        return numerator / denominator
    }
}
