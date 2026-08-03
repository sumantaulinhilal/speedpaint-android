package com.example.animation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import com.example.model.HandStyle
import com.example.model.Point2D
import com.example.model.VectorPath
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

import androidx.compose.ui.graphics.drawscope.Stroke

data class HandState(
    val tipPosition: Point2D,
    val angleDegrees: Float,
    val currentSpeedRatio: Float,
    val isContactingCanvas: Boolean = true
)

object VirtualHandEngine {

    /**
     * Calculates precise hand state at exact time progression for realistic drawing physics.
     */
    fun calculateHandState(
        paths: List<VectorPath>,
        progress: Float // 0f to 1f
    ): HandState {
        if (paths.isEmpty() || progress <= 0f) {
            return HandState(Point2D(0f, 0f), angleDegrees = -25f, currentSpeedRatio = 1.0f, isContactingCanvas = false)
        }
        if (progress >= 1f) {
            val lastPath = paths.last()
            val lastPt = lastPath.endPoint
            return HandState(lastPt, angleDegrees = -25f, currentSpeedRatio = 1.0f, isContactingCanvas = false)
        }

        // Calculate total distance across all vector paths
        val pathLengths = paths.map { calculateTotalPathLength(it) }
        val grandTotalLength = pathLengths.sum().coerceAtLeast(0.001f)

        var targetDistance = progress * grandTotalLength

        // Find active path index
        var activeIndex = 0
        var accumulatedDist = 0f

        for (i in paths.indices) {
            val len = pathLengths[i]
            if (accumulatedDist + len >= targetDistance) {
                activeIndex = i
                break
            }
            accumulatedDist += len
            if (i == paths.size - 1) activeIndex = i
        }

        val activePath = paths[activeIndex]
        val pathLocalDistance = targetDistance - accumulatedDist

        val (pt, tangentAngle, localCurvature) = interpolatePathPointAndTangent(activePath, pathLocalDistance)

        // Dynamic speed calculation: fast on straight, slow on tight curves
        val speedRatio = (1.0f - (localCurvature * 0.6f)).coerceIn(0.3f, 1.2f)

        // Smooth hand rotation angle based on tangent
        val handAngle = (tangentAngle * (180f / Math.PI.toFloat()) - 45f).coerceIn(-75f, 15f)

        return HandState(
            tipPosition = pt,
            angleDegrees = handAngle,
            currentSpeedRatio = speedRatio,
            isContactingCanvas = true
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

    private fun interpolatePathPointAndTangent(path: VectorPath, targetDistance: Float): Triple<Point2D, Float, Float> {
        if (path.points.isEmpty()) return Triple(Point2D(0f, 0f), 0f, 0f)
        if (path.points.size == 1) return Triple(path.points[0], 0f, 0f)

        var currDist = 0f
        for (i in 0 until path.points.size - 1) {
            val p1 = path.points[i]
            val p2 = path.points[i + 1]
            val segLen = hypot((p2.x - p1.x).toDouble(), (p2.y - p1.y).toDouble()).toFloat()

            if (currDist + segLen >= targetDistance || i == path.points.size - 2) {
                val frac = if (segLen > 0.001f) ((targetDistance - currDist) / segLen).coerceIn(0f, 1f) else 0f
                val interpolatedX = p1.x + frac * (p2.x - p1.x)
                val interpolatedY = p1.y + frac * (p2.y - p1.y)

                val dx = p2.x - p1.x
                val dy = p2.y - p1.y
                val angle = atan2(dy.toDouble(), dx.toDouble()).toFloat()

                // Curvature estimation from next segment
                var curvature = 0f
                if (i < path.points.size - 2) {
                    val p3 = path.points[i + 2]
                    val dx2 = p3.x - p2.x
                    val dy2 = p3.y - p2.y
                    val angle2 = atan2(dy2.toDouble(), dx2.toDouble()).toFloat()
                    curvature = (Math.abs(angle2 - angle) / Math.PI.toFloat()).coerceIn(0f, 1f)
                }

                return Triple(Point2D(interpolatedX, interpolatedY), angle, curvature)
            }
            currDist += segLen
        }
        val last = path.points.last()
        return Triple(last, 0f, 0f)
    }

    /**
     * Renders virtual hand on Compose Canvas with exact tip alignment $(x, y)$.
     */
    fun DrawScope.drawVirtualHand(
        handStyle: HandStyle,
        tipX: Float,
        tipY: Float,
        angleDegrees: Float
    ) {
        if (handStyle == HandStyle.NO_HAND) return

        rotate(degrees = angleDegrees, pivot = Offset(tipX, tipY)) {
            translate(left = tipX, top = tipY) {
                when (handStyle) {
                    HandStyle.MALE_PENCIL -> drawMalePencilHand()
                    HandStyle.FEMALE_PENCIL -> drawFemalePencilHand()
                    HandStyle.BLACK_MARKER -> drawMarkerHand(markerColor = Color(0xFF0F172A))
                    HandStyle.WHITE_MARKER -> drawMarkerHand(markerColor = Color(0xFFF1F5F9))
                    HandStyle.STYLUS -> drawStylusHand()
                    HandStyle.CARTOON_HAND -> drawCartoonHand()
                    HandStyle.CUSTOM_PNG -> drawMalePencilHand()
                    HandStyle.NO_HAND -> {}
                }
            }
        }
    }

    private fun DrawScope.drawMalePencilHand() {
        // Pencil Tip is exactly at (0, 0) relative origin!
        val leadPath = Path().apply {
            moveTo(0f, 0f)
            lineTo(8f, -20f)
            lineTo(-8f, -20f)
            close()
        }
        drawPath(leadPath, color = Color(0xFF0F172A))

        val woodPath = Path().apply {
            moveTo(8f, -20f)
            lineTo(18f, -45f)
            lineTo(-18f, -45f)
            lineTo(-8f, -20f)
            close()
        }
        drawPath(woodPath, color = Color(0xFFFDE68A))

        val pencilBody = Path().apply {
            moveTo(18f, -45f)
            lineTo(22f, -180f)
            lineTo(-22f, -180f)
            lineTo(-18f, -45f)
            close()
        }
        drawPath(pencilBody, color = Color(0xFFF59E0B))

        // Hand & Fingers gripping pencil
        val handSkin = Path().apply {
            moveTo(12f, -90f)
            cubicTo(50f, -100f, 80f, -140f, 90f, -200f)
            lineTo(140f, -240f)
            lineTo(120f, -300f)
            lineTo(-10f, -220f)
            cubicTo(-15f, -160f, -20f, -120f, 12f, -90f)
            close()
        }
        drawPath(handSkin, color = Color(0xFFD97706).copy(alpha = 0.9f))

        // Thumb overlay
        val thumb = Path().apply {
            moveTo(0f, -100f)
            cubicTo(20f, -110f, 30f, -140f, 25f, -160f)
            cubicTo(10f, -160f, -10f, -130f, 0f, -100f)
            close()
        }
        drawPath(thumb, color = Color(0xFFF59E0B).copy(alpha = 0.95f))
    }

    private fun DrawScope.drawFemalePencilHand() {
        val leadPath = Path().apply {
            moveTo(0f, 0f)
            lineTo(6f, -18f)
            lineTo(-6f, -18f)
            close()
        }
        drawPath(leadPath, color = Color(0xFF0F172A))

        val woodPath = Path().apply {
            moveTo(6f, -18f)
            lineTo(15f, -40f)
            lineTo(-15f, -40f)
            lineTo(-6f, -18f)
            close()
        }
        drawPath(woodPath, color = Color(0xFFFED7AA))

        val pencilBody = Path().apply {
            moveTo(15f, -40f)
            lineTo(18f, -170f)
            lineTo(-18f, -170f)
            lineTo(-15f, -40f)
            close()
        }
        drawPath(pencilBody, color = Color(0xFFEC4899))

        val handSkin = Path().apply {
            moveTo(10f, -80f)
            cubicTo(45f, -90f, 75f, -130f, 85f, -190f)
            lineTo(130f, -230f)
            lineTo(110f, -290f)
            lineTo(-15f, -210f)
            cubicTo(-20f, -150f, -15f, -110f, 10f, -80f)
            close()
        }
        drawPath(handSkin, color = Color(0xFFFED7AA))

        // French manicure fingernail accent
        val nail = Path().apply {
            moveTo(22f, -100f)
            lineTo(28f, -115f)
            lineTo(18f, -120f)
            close()
        }
        drawPath(nail, color = Color.White)
    }

    private fun DrawScope.drawMarkerHand(markerColor: Color) {
        // Marker Chisel Tip
        val tip = Path().apply {
            moveTo(0f, 0f)
            lineTo(12f, -15f)
            lineTo(-12f, -15f)
            close()
        }
        drawPath(tip, color = markerColor)

        // Marker Barrel
        val barrel = Path().apply {
            moveTo(14f, -15f)
            lineTo(20f, -160f)
            lineTo(-20f, -160f)
            lineTo(-14f, -15f)
            close()
        }
        drawPath(barrel, color = Color(0xFF1E293B))

        // Marker Cap Accent Ring
        drawCircle(color = Color(0xFF2563EB), radius = 18f, center = Offset(0f, -80f))

        // Hand
        val hand = Path().apply {
            moveTo(10f, -70f)
            cubicTo(50f, -80f, 80f, -130f, 95f, -190f)
            lineTo(135f, -220f)
            lineTo(110f, -280f)
            lineTo(-20f, -200f)
            close()
        }
        drawPath(hand, color = Color(0xFFE2E8F0).copy(alpha = 0.95f))
    }

    private fun DrawScope.drawStylusHand() {
        // Stylus Fine Nib Tip
        val nib = Path().apply {
            moveTo(0f, 0f)
            lineTo(4f, -22f)
            lineTo(-4f, -22f)
            close()
        }
        drawPath(nib, color = Color(0xFF38BDF8))

        // Metallic Stylus Body
        val body = Path().apply {
            moveTo(10f, -22f)
            lineTo(14f, -190f)
            lineTo(-14f, -190f)
            lineTo(-10f, -22f)
            close()
        }
        drawPath(body, color = Color(0xFF64748B))

        // Hand
        val hand = Path().apply {
            moveTo(12f, -90f)
            cubicTo(45f, -100f, 75f, -140f, 85f, -200f)
            lineTo(130f, -240f)
            lineTo(110f, -290f)
            lineTo(-10f, -220f)
            close()
        }
        drawPath(hand, color = Color(0xFFCBD5E1))
    }

    private fun DrawScope.drawCartoonHand() {
        // Cartoon Pencil Tip
        val tip = Path().apply {
            moveTo(0f, 0f)
            lineTo(10f, -20f)
            lineTo(-10f, -20f)
            close()
        }
        drawPath(tip, color = Color.Black)

        val pencil = Path().apply {
            moveTo(15f, -20f)
            lineTo(22f, -150f)
            lineTo(-22f, -150f)
            lineTo(-15f, -20f)
            close()
        }
        drawPath(pencil, color = Color(0xFFFACC15))

        // White Cartoon Glove with 4 fingers
        val glove = Path().apply {
            moveTo(15f, -70f)
            cubicTo(60f, -80f, 90f, -120f, 100f, -180f)
            lineTo(130f, -220f)
            lineTo(90f, -260f)
            lineTo(-25f, -180f)
            cubicTo(-30f, -130f, -20f, -90f, 15f, -70f)
            close()
        }
        drawPath(glove, color = Color.White)

        // Black outline for cartoon glove
        val outline = Path().apply {
            moveTo(15f, -70f)
            cubicTo(60f, -80f, 90f, -120f, 100f, -180f)
            lineTo(130f, -220f)
            lineTo(90f, -260f)
            lineTo(-25f, -180f)
            close()
        }
        drawPath(outline, color = Color.Black, style = Stroke(width = 2.5f))
    }
}
