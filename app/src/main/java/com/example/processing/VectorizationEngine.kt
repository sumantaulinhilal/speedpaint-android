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

object VectorizationEngine {

    /**
     * Converts a raw input Bitmap into clean 1-line centerline vector paths for speedpaint drawing animation.
     * Uses Zhang-Suen Medial Axis Thinning to convert line art & sketches into crisp, non-overlapping strokes.
     */
    fun processBitmapToVectorPaths(
        bitmap: Bitmap,
        targetWidth: Float = 600f,
        targetHeight: Float = 600f,
        threshold: Int = 40,
        minPathLength: Float = 15f
    ): List<VectorPath> {
        val maxDim = 450
        val scale = minOf(maxDim.toFloat() / bitmap.width, maxDim.toFloat() / bitmap.height, 1.0f)
        val scaledW = (bitmap.width * scale).toInt().coerceAtLeast(10)
        val scaledH = (bitmap.height * scale).toInt().coerceAtLeast(10)

        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledW, scaledH, true)
        val pixels = IntArray(scaledW * scaledH)
        scaledBitmap.getPixels(pixels, 0, scaledW, 0, 0, scaledW, scaledH)

        val binaryGrid = Array(scaledH) { IntArray(scaledW) }
        val colorGrid = Array(scaledH) { arrayOfNulls<Color>(scaledW) }

        // Step 1: Detect dark drawing strokes on light background
        for (y in 0 until scaledH) {
            for (x in 0 until scaledW) {
                val pixel = pixels[y * scaledW + x]
                val a = (pixel shr 24) and 0xFF
                var r = (pixel shr 16) and 0xFF
                var g = (pixel shr 8) and 0xFF
                var b = pixel and 0xFF

                // Handle transparency against solid white paper canvas
                if (a < 255) {
                    val alphaFactor = a / 255f
                    r = (r * alphaFactor + 255 * (1f - alphaFactor)).toInt().coerceIn(0, 255)
                    g = (g * alphaFactor + 255 * (1f - alphaFactor)).toInt().coerceIn(0, 255)
                    b = (b * alphaFactor + 255 * (1f - alphaFactor)).toInt().coerceIn(0, 255)
                }

                val luma = 0.299f * r + 0.587f * g + 0.114f * b
                val isSaturated = (maxOf(r, g, b) - minOf(r, g, b)) > 45

                // Stroke pixel if dark enough or transparent cutout border
                if (luma < 170f || (a in 10..240)) {
                    binaryGrid[y][x] = 1
                    colorGrid[y][x] = if (isSaturated) Color(r, g, b) else Color.Black
                } else {
                    binaryGrid[y][x] = 0
                }
            }
        }

        // Step 2: Zhang-Suen Skeletonization / Thinning
        val skeleton = zhangSuenThinning(binaryGrid, scaledW, scaledH)

        // Step 3: Trace 1-pixel centerline skeleton into paths
        val visited = Array(scaledH) { BooleanArray(scaledW) }
        val rawPaths = mutableListOf<List<Point2D>>()
        val pathColors = mutableListOf<Color>()

        val imgRatio = bitmap.width.toFloat() / bitmap.height.toFloat().coerceAtLeast(1f)
        val drawW: Float
        val drawH: Float
        val offsetX: Float
        val offsetY: Float

        if (imgRatio >= 1f) {
            drawW = targetWidth
            drawH = targetWidth / imgRatio
            offsetX = 0f
            offsetY = (targetHeight - drawH) / 2f
        } else {
            drawH = targetHeight
            drawW = targetHeight * imgRatio
            offsetX = (targetWidth - drawW) / 2f
            offsetY = 0f
        }

        val scaleX = drawW / scaledW
        val scaleY = drawH / scaledH

        val dxDirs = intArrayOf(1, 1, 0, -1, -1, -1, 0, 1)
        val dyDirs = intArrayOf(0, 1, 1, 1, 0, -1, -1, -1)

        for (y in 1 until scaledH - 1) {
            for (x in 1 until scaledW - 1) {
                if (skeleton[y][x] == 1 && !visited[y][x]) {
                    val points = mutableListOf<Point2D>()
                    var currX = x
                    var currY = y

                    val strokeColor = colorGrid[y][x] ?: Color.Black

                    while (currX in 1 until scaledW - 1 && currY in 1 until scaledH - 1) {
                        if (skeleton[currY][currX] != 1 || visited[currY][currX]) break

                        visited[currY][currX] = true
                        points.add(
                            Point2D(
                                x = offsetX + (currX * scaleX),
                                y = offsetY + (currY * scaleY)
                            )
                        )

                        var found = false
                        for (d in 0 until 8) {
                            val nx = currX + dxDirs[d]
                            val ny = currY + dyDirs[d]
                            if (nx in 1 until scaledW - 1 && ny in 1 until scaledH - 1) {
                                if (skeleton[ny][nx] == 1 && !visited[ny][nx]) {
                                    currX = nx
                                    currY = ny
                                    found = true
                                    break
                                }
                            }
                        }
                        if (!found) break
                    }

                    if (points.size >= 3) {
                        rawPaths.add(points)
                        pathColors.add(strokeColor)
                    }
                }
            }
        }

        // Step 4: Merge close endpoints into continuous smooth strokes
        val stitchedPaths = mutableListOf<MutableList<Point2D>>()
        val stitchedColors = mutableListOf<Color>()

        for (i in rawPaths.indices) {
            val pts = rawPaths[i]
            val col = pathColors[i]

            if (stitchedPaths.isNotEmpty()) {
                val lastPath = stitchedPaths.last()
                val lastPt = lastPath.last()
                val firstPt = pts.first()
                val dist = hypot((firstPt.x - lastPt.x).toDouble(), (firstPt.y - lastPt.y).toDouble()).toFloat()

                if (dist < 20f && col == stitchedColors.last()) {
                    lastPath.addAll(pts)
                    continue
                }
            }
            stitchedPaths.add(pts.toMutableList())
            stitchedColors.add(col)
        }

        // Step 5: Smooth and simplify centerline paths
        val finalPaths = mutableListOf<VectorPath>()

        for (i in stitchedPaths.indices) {
            val pts = stitchedPaths[i]
            val simplified = ramerDouglasPeucker(pts, epsilon = 1.0f)
            val totalLen = calculatePathLength(simplified)

            if (totalLen >= 10f) {
                val bbox = PathBoundingBox.calculate(simplified)
                val isText = bbox.height in 8f..35f && bbox.width > bbox.height * 1.8f

                finalPaths.add(
                    VectorPath(
                        points = simplified,
                        color = stitchedColors[i],
                        strokeWidth = if (isText) 2.2f else 2.8f,
                        isText = isText,
                        totalLength = totalLen,
                        boundingBox = bbox
                    )
                )
            }
        }

        return if (finalPaths.isEmpty()) {
            PresetSamples.samples.first().sampleSvgPaths
        } else {
            finalPaths
        }
    }

    /**
     * Zhang-Suen Thinning Algorithm for 8-connected binary image skeletons.
     */
    private fun zhangSuenThinning(grid: Array<IntArray>, width: Int, height: Int): Array<IntArray> {
        val skel = Array(height) { grid[it].clone() }
        var hasChanged: Boolean

        val p2y = intArrayOf(-1, -1, 0, 1, 1, 1, 0, -1)
        val p2x = intArrayOf(0, 1, 1, 1, 0, -1, -1, -1)

        do {
            hasChanged = false
            val toDeleteStep1 = mutableListOf<Pair<Int, Int>>()

            for (y in 1 until height - 1) {
                for (x in 1 until width - 1) {
                    if (skel[y][x] != 1) continue

                    val p = IntArray(8)
                    for (i in 0 until 8) {
                        p[i] = skel[y + p2y[i]][x + p2x[i]]
                    }

                    val b = p.sum()
                    if (b !in 2..6) continue

                    var a = 0
                    for (i in 0 until 8) {
                        if (p[i] == 0 && p[(i + 1) % 8] == 1) a++
                    }
                    if (a != 1) continue

                    if (p[0] * p[2] * p[4] != 0) continue
                    if (p[2] * p[4] * p[6] != 0) continue

                    toDeleteStep1.add(Pair(y, x))
                }
            }

            for (pt in toDeleteStep1) {
                skel[pt.first][pt.second] = 0
                hasChanged = true
            }

            val toDeleteStep2 = mutableListOf<Pair<Int, Int>>()

            for (y in 1 until height - 1) {
                for (x in 1 until width - 1) {
                    if (skel[y][x] != 1) continue

                    val p = IntArray(8)
                    for (i in 0 until 8) {
                        p[i] = skel[y + p2y[i]][x + p2x[i]]
                    }

                    val b = p.sum()
                    if (b !in 2..6) continue

                    var a = 0
                    for (i in 0 until 8) {
                        if (p[i] == 0 && p[(i + 1) % 8] == 1) a++
                    }
                    if (a != 1) continue

                    if (p[0] * p[2] * p[6] != 0) continue
                    if (p[0] * p[4] * p[6] != 0) continue

                    toDeleteStep2.add(Pair(y, x))
                }
            }

            for (pt in toDeleteStep2) {
                skel[pt.first][pt.second] = 0
                hasChanged = true
            }

        } while (hasChanged)

        return skel
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
            val recResults1 = ramerDouglasPeucker(points.subList(0, index + 1), epsilon)
            val recResults2 = ramerDouglasPeucker(points.subList(index, end + 1), epsilon)
            recResults1.dropLast(1) + recResults2
        } else {
            listOf(points[0], points[end])
        }
    }

    private fun perpendicularDistance(p: Point2D, lineStart: Point2D, lineEnd: Point2D): Float {
        val dx = lineEnd.x - lineStart.x
        val dy = lineEnd.y - lineStart.y

        val mag = hypot(dx.toDouble(), dy.toDouble()).toFloat()
        if (mag == 0f) return hypot((p.x - lineStart.x).toDouble(), (p.y - lineStart.y).toDouble()).toFloat()

        val u = ((p.x - lineStart.x) * dx + (p.y - lineStart.y) * dy) / (mag * mag)
        val clampedU = u.coerceIn(0f, 1f)

        val ix = lineStart.x + clampedU * dx
        val iy = lineStart.y + clampedU * dy

        return hypot((p.x - ix).toDouble(), (p.y - iy).toDouble()).toFloat()
    }
}
