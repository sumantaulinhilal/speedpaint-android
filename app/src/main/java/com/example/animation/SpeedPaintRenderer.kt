package com.example.animation

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import com.example.animation.VirtualHandEngine.drawVirtualHand
import com.example.model.BackgroundStyle
import com.example.model.HandStyle
import com.example.model.Point2D
import com.example.model.SketchType
import com.example.model.VectorPath
import kotlin.math.hypot

object SpeedPaintRenderer {

    /**
     * Renders full SpeedPaint canvas frame with background, animated vector paths,
     * progressive color fill phase, and virtual hand overlay.
     */
    fun DrawScope.renderSpeedPaintFrame(
        paths: List<VectorPath>,
        progress: Float, // 0f to 1f for outline drawing phase
        fillProgress: Float, // 0f to 1f for color fill phase
        handStyle: HandStyle,
        backgroundStyle: BackgroundStyle,
        sketchType: SketchType,
        showGridLines: Boolean = true
    ) {
        // Step 1: Render Canvas Background
        val canvasWidth = size.width
        val canvasHeight = size.height

        drawRect(color = backgroundStyle.color)

        // Draw subtle whiteboard / blackboard textures or grid lines
        if (showGridLines && backgroundStyle != BackgroundStyle.TRANSPARENT) {
            val gridSpacing = 40f
            val gridColor = if (backgroundStyle.isDark) Color(0x1AFFFFFF) else Color(0x1A000000)

            var x = gridSpacing
            while (x < canvasWidth) {
                drawLine(gridColor, start = Offset(x, 0f), end = Offset(x, canvasHeight), strokeWidth = 1f)
                x += gridSpacing
            }
            var y = gridSpacing
            while (y < canvasHeight) {
                drawLine(gridColor, start = Offset(0f, y), end = Offset(canvasWidth, y), strokeWidth = 1f)
                y += gridSpacing
            }
        }

        if (paths.isEmpty()) return

        // Calculate total distance of all paths combined
        val pathLengths = paths.map { calculateTotalPathLength(it) }
        val grandTotalLength = pathLengths.sum().coerceAtLeast(0.001f)

        val currentDrawnDistance = progress * grandTotalLength

        // Step 2: Render Outline Paths up to currentDrawnDistance
        var accumulatedLength = 0f

        for (i in paths.indices) {
            val vectorPath = paths[i]
            val pathLen = pathLengths[i]

            // Determine sketch color based on SketchType
            val strokeColor = when (sketchType) {
                SketchType.COLOR -> if (backgroundStyle.isDark && vectorPath.color == Color.Black) Color.White else vectorPath.color
                SketchType.BLACK_WHITE -> if (backgroundStyle.isDark) Color.White else Color.Black
                SketchType.GRAYSCALE -> if (backgroundStyle.isDark) Color(0xFFCCCCCC) else Color(0xFF444444)
            }

            if (accumulatedLength + pathLen <= currentDrawnDistance) {
                // Fully drawn path
                drawFullVectorPath(vectorPath, strokeColor)
            } else if (accumulatedLength < currentDrawnDistance) {
                // Partially drawn path
                val localDrawn = currentDrawnDistance - accumulatedLength
                drawPartialVectorPath(vectorPath, localDrawn, pathLen, strokeColor)
                break
            } else {
                // Not drawn yet
                break
            }

            accumulatedLength += pathLen
        }

        // Step 3: Progressive Color Fill Phase
        if (fillProgress > 0f) {
            for (vectorPath in paths) {
                if (vectorPath.points.size > 3 && vectorPath.color != Color.Black && vectorPath.color != Color.White) {
                    val fillAlpha = (fillProgress * 0.85f).coerceIn(0f, 0.85f)
                    val fillColor = vectorPath.color.copy(alpha = fillAlpha)

                    val filledPath = Path().apply {
                        val first = vectorPath.points.first()
                        moveTo(first.x, first.y)
                        for (p in vectorPath.points.drop(1)) {
                            lineTo(p.x, p.y)
                        }
                        close()
                    }
                    drawPath(filledPath, color = fillColor)
                }
            }
        }

        // Step 4: Virtual Hand Overlay tracking active drawing tip
        if (progress > 0f && progress < 1f && handStyle != HandStyle.NO_HAND) {
            val handState = VirtualHandEngine.calculateHandState(paths, progress)
            if (handState.isContactingCanvas) {
                drawVirtualHand(
                    handStyle = handStyle,
                    tipX = handState.tipPosition.x,
                    tipY = handState.tipPosition.y,
                    angleDegrees = handState.angleDegrees
                )
            }
        }
    }

    private fun DrawScope.drawFullVectorPath(path: VectorPath, strokeColor: Color) {
        if (path.points.size < 2) return
        val composePath = Path().apply {
            moveTo(path.points[0].x, path.points[0].y)
            for (i in 1 until path.points.size) {
                lineTo(path.points[i].x, path.points[i].y)
            }
        }
        drawPath(
            composePath,
            color = strokeColor,
            style = Stroke(width = path.strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
    }

    private fun DrawScope.drawPartialVectorPath(
        path: VectorPath,
        localDrawnLen: Float,
        totalPathLen: Float,
        strokeColor: Color
    ) {
        if (path.points.size < 2 || totalPathLen <= 0f) return

        val fraction = (localDrawnLen / totalPathLen).coerceIn(0f, 1f)

        val partialPath = Path()
        var currentLen = 0f

        partialPath.moveTo(path.points[0].x, path.points[0].y)

        for (i in 0 until path.points.size - 1) {
            val p1 = path.points[i]
            val p2 = path.points[i + 1]
            val segLen = hypot((p2.x - p1.x).toDouble(), (p2.y - p1.y).toDouble()).toFloat()

            if (currentLen + segLen <= localDrawnLen) {
                partialPath.lineTo(p2.x, p2.y)
            } else {
                val rem = localDrawnLen - currentLen
                val frac = if (segLen > 0.001f) (rem / segLen).coerceIn(0f, 1f) else 0f
                val endX = p1.x + frac * (p2.x - p1.x)
                val endY = p1.y + frac * (p2.y - p1.y)
                partialPath.lineTo(endX, endY)
                break
            }
            currentLen += segLen
        }

        drawPath(
            partialPath,
            color = strokeColor,
            style = Stroke(width = path.strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
    }

    private fun calculateTotalPathLength(path: VectorPath): Float {
        if (path.points.size < 2) return 0f
        var sum = 0f
        for (i in 0 until path.points.size - 1) {
            val p1 = path.points[i]
            val p2 = path.points[i + 1]
            sum += hypot((p2.x - p1.x).toDouble(), (p2.y - p1.y).toDouble()).toFloat()
        }
        return sum
    }
}
