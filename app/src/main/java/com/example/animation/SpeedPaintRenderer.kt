package com.example.animation

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.animation.VirtualHandEngine.drawVirtualHand
import com.example.model.BackgroundStyle
import com.example.model.HandStyle
import com.example.model.Point2D
import com.example.model.SketchType
import com.example.model.VectorPath
import kotlin.math.hypot

data class CanvasFrameTransform(
    val scale: Float,
    val translateX: Float,
    val translateY: Float
)

object SpeedPaintRenderer {

    fun calculateTransform(paths: List<VectorPath>, canvasWidth: Float, canvasHeight: Float): CanvasFrameTransform {
        if (paths.isEmpty() || canvasWidth <= 0f || canvasHeight <= 0f) {
            return CanvasFrameTransform(1f, 0f, 0f)
        }

        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE
        var maxY = -Float.MAX_VALUE

        for (path in paths) {
            for (p in path.points) {
                if (p.x < minX) minX = p.x
                if (p.y < minY) minY = p.y
                if (p.x > maxX) maxX = p.x
                if (p.y > maxY) maxY = p.y
            }
        }

        val artW = (maxX - minX).coerceAtLeast(10f)
        val artH = (maxY - minY).coerceAtLeast(10f)

        val targetW = canvasWidth * 0.82f
        val targetH = canvasHeight * 0.82f

        val scale = minOf(targetW / artW, targetH / artH).coerceIn(0.05f, 15.0f)

        val centerXArt = minX + (artW / 2f)
        val centerYArt = minY + (artH / 2f)

        val centerXCanvas = canvasWidth / 2f
        val centerYCanvas = canvasHeight / 2f

        val translateX = centerXCanvas - (centerXArt * scale)
        val translateY = centerYCanvas - (centerYArt * scale)

        return CanvasFrameTransform(scale, translateX, translateY)
    }

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
        showGridLines: Boolean = true,
        handMarkerBitmap: ImageBitmap? = null,
        handPencilBitmap: ImageBitmap? = null
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Step 1: Background rendering
        drawRect(color = backgroundStyle.color)

        if (showGridLines && backgroundStyle != BackgroundStyle.TRANSPARENT) {
            val gridSpacing = 42f
            val gridColor = when (backgroundStyle) {
                BackgroundStyle.BLACK, BackgroundStyle.CHALKBOARD, BackgroundStyle.BLUEPRINT -> Color(0x1AFFFFFF)
                BackgroundStyle.PARCHMENT -> Color(0x188C7A5B)
                else -> Color(0x18000000)
            }

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

        // Step 2: Perfect Centering Transformation
        val transform = calculateTransform(paths, canvasWidth, canvasHeight)

        val centeredPaths = paths.map { path ->
            path.copy(
                points = path.points.map { p ->
                    Point2D(
                        x = p.x * transform.scale + transform.translateX,
                        y = p.y * transform.scale + transform.translateY
                    )
                },
                strokeWidth = (path.strokeWidth * transform.scale).coerceIn(2.5f, 12f)
            )
        }

        val pathLengths = centeredPaths.map { calculateTotalPathLength(it) }
        val grandTotalLength = pathLengths.sum().coerceAtLeast(0.001f)
        val currentDrawnDistance = progress * grandTotalLength

        // Step 3: Draw Outline Paths
        var accumulatedLength = 0f

        for (i in centeredPaths.indices) {
            val vectorPath = centeredPaths[i]
            val pathLen = pathLengths[i]

            // Determine stroke color (ensure solid black is preserved on light backgrounds)
            val strokeColor = when (sketchType) {
                SketchType.COLOR -> {
                    if (backgroundStyle.isDark && (vectorPath.color == Color.Black || vectorPath.color == Color(0xFF0F172A))) {
                        Color.White
                    } else if (!backgroundStyle.isDark && (vectorPath.color == Color.Black || vectorPath.color == Color.White)) {
                        Color(0xFF0F172A)
                    } else {
                        vectorPath.color
                    }
                }
                SketchType.BLACK_WHITE -> {
                    if (backgroundStyle.isDark) Color.White else Color(0xFF0F172A)
                }
                SketchType.GRAYSCALE -> {
                    if (backgroundStyle.isDark) Color(0xFFE2E8F0) else Color(0xFF334155)
                }
            }

            if (accumulatedLength + pathLen <= currentDrawnDistance) {
                drawFullVectorPath(vectorPath, strokeColor)
            } else if (accumulatedLength < currentDrawnDistance) {
                val localDrawn = currentDrawnDistance - accumulatedLength
                drawPartialVectorPath(vectorPath, localDrawn, pathLen, strokeColor)
                break
            } else {
                break
            }

            accumulatedLength += pathLen
        }

        // Step 4: Color Fill Phase
        if (fillProgress > 0f) {
            for (vectorPath in centeredPaths) {
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

        // Step 5: Virtual Hand Overlay
        if (progress > 0f && progress < 1f && handStyle != HandStyle.NO_HAND) {
            val handState = VirtualHandEngine.calculateHandState(centeredPaths, progress)
            if (handState.isContactingCanvas) {
                drawVirtualHand(
                    handStyle = handStyle,
                    tipX = handState.tipPosition.x,
                    tipY = handState.tipPosition.y,
                    angleDegrees = handState.angleDegrees,
                    handMarkerBitmap = handMarkerBitmap,
                    handPencilBitmap = handPencilBitmap
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
            style = Stroke(
                width = path.strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }

    private fun DrawScope.drawPartialVectorPath(
        path: VectorPath,
        localDrawnLen: Float,
        totalPathLen: Float,
        strokeColor: Color
    ) {
        if (path.points.size < 2 || totalPathLen <= 0f) return

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
            style = Stroke(
                width = path.strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
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
