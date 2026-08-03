package com.example.animation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

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

        // Smooth natural hand posture entering from bottom-right (45 degrees down-right)
        // Keep angle steady at 0 degrees so hand glides smoothly across canvas without twisting
        val handAngle = 0f

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
        angleDegrees: Float,
        handMarkerBitmap: ImageBitmap? = null,
        handPencilBitmap: ImageBitmap? = null
    ) {
        if (handStyle == HandStyle.NO_HAND) return

        when (handStyle) {
            HandStyle.BLACK_MARKER, HandStyle.WHITE_MARKER, HandStyle.CUSTOM_PNG -> {
                if (handMarkerBitmap != null) {
                    drawRealPNGHand(
                        bitmap = handMarkerBitmap,
                        tipX = tipX,
                        tipY = tipY,
                        angleDegrees = angleDegrees,
                        tipXRatio = 0.2757f,
                        tipYRatio = 0.0f
                    )
                    return
                }
            }
            HandStyle.MALE_PENCIL, HandStyle.FEMALE_PENCIL -> {
                if (handPencilBitmap != null) {
                    drawRealPNGHand(
                        bitmap = handPencilBitmap,
                        tipX = tipX,
                        tipY = tipY,
                        angleDegrees = angleDegrees,
                        tipXRatio = 0.0f,
                        tipYRatio = 0.0f
                    )
                    return
                }
            }
            else -> {}
        }

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

    private fun DrawScope.drawRealPNGHand(
        bitmap: ImageBitmap,
        tipX: Float,
        tipY: Float,
        angleDegrees: Float,
        tipXRatio: Float = 0f,
        tipYRatio: Float = 0f
    ) {
        val targetWidthPx = (size.width * 0.65f).coerceIn(360f, 900f)
        val scale = targetWidthPx / bitmap.width
        val targetHeightPx = bitmap.height * scale
        val tipOffsetX = targetWidthPx * tipXRatio
        val tipOffsetY = targetHeightPx * tipYRatio

        rotate(degrees = angleDegrees, pivot = Offset(tipX, tipY)) {
            drawImage(
                image = bitmap,
                dstOffset = IntOffset(
                    x = (tipX - tipOffsetX).toInt(),
                    y = (tipY - tipOffsetY).toInt()
                ),
                dstSize = IntSize(targetWidthPx.toInt(), targetHeightPx.toInt())
            )
        }
    }

    private fun DrawScope.drawMalePencilHand() {
        // 1. Realistic Soft Drop Shadow (Semi 3D depth)
        val shadowPath = Path().apply {
            moveTo(25f, 35f)
            lineTo(220f, 230f)
            cubicTo(260f, 280f, 320f, 340f, 480f, 520f)
            lineTo(420f, 560f)
            lineTo(150f, 250f)
            close()
        }
        drawPath(shadowPath, color = Color.Black.copy(alpha = 0.22f))

        // 2. Graphite Pencil Lead Tip at (0, 0) pointing UP-LEFT
        val lead = Path().apply {
            moveTo(0f, 0f)
            lineTo(8.5f, 16.9f)
            lineTo(16.9f, 8.5f)
            close()
        }
        drawPath(lead, color = Color(0xFF0F172A))

        // 3. Wooden Cone (Sharpened Cedar Wood)
        val wood = Path().apply {
            moveTo(8.5f, 16.9f)
            lineTo(21.9f, 41.7f)
            lineTo(41.7f, 21.9f)
            lineTo(16.9f, 8.5f)
            close()
        }
        drawPath(wood, color = Color(0xFFEAB308))

        // 4. Yellow Hexagonal Pencil Barrel
        val barrel = Path().apply {
            moveTo(21.9f, 41.7f)
            lineTo(154.1f, 185.3f)
            lineTo(185.3f, 154.1f)
            lineTo(41.7f, 21.9f)
            close()
        }
        drawPath(barrel, color = Color(0xFFCA8A04))

        val barrelHighlight = Path().apply {
            moveTo(27f, 36f)
            lineTo(165f, 174f)
            lineTo(175f, 164f)
            lineTo(36f, 27f)
            close()
        }
        drawPath(barrelHighlight, color = Color(0xFFFDE047))

        // 5. Photorealistic Male Hand (Skin Base)
        val skinBase = Path().apply {
            moveTo(60f, 85f)
            cubicTo(95f, 110f, 140f, 170f, 190f, 230f)
            lineTo(320f, 360f)
            lineTo(480f, 520f)
            lineTo(380f, 580f)
            lineTo(210f, 420f)
            cubicTo(140f, 320f, 90f, 210f, 60f, 85f)
            close()
        }
        drawPath(skinBase, color = Color(0xFFD97706)) // Realistic warm skin tone

        // Skin Shading & Muscle Highlights
        val skinHighlight = Path().apply {
            moveTo(85f, 110f)
            cubicTo(120f, 140f, 170f, 200f, 230f, 270f)
            lineTo(340f, 380f)
            lineTo(290f, 430f)
            lineTo(160f, 290f)
            close()
        }
        drawPath(skinHighlight, color = Color(0xFFF59E0B).copy(alpha = 0.5f))

        // Index Finger wrapped firmly around Pencil Barrel
        val indexFinger = Path().apply {
            moveTo(45f, 70f)
            cubicTo(65f, 60f, 115f, 90f, 125f, 135f)
            cubicTo(110f, 150f, 65f, 120f, 45f, 70f)
            close()
        }
        drawPath(indexFinger, color = Color(0xFFB45309))

        // Thumb Grip Overlay
        val thumb = Path().apply {
            moveTo(75f, 115f)
            cubicTo(105f, 125f, 145f, 185f, 135f, 215f)
            cubicTo(105f, 225f, 65f, 165f, 75f, 115f)
            close()
        }
        drawPath(thumb, color = Color(0xFFD97706))

        // Fingernail Accent
        val nail = Path().apply {
            moveTo(110f, 125f)
            lineTo(122f, 138f)
            lineTo(112f, 145f)
            close()
        }
        drawPath(nail, color = Color(0xFFFDE68A).copy(alpha = 0.85f))
    }

    private fun DrawScope.drawFemalePencilHand() {
        // Drop Shadow
        val shadow = Path().apply {
            moveTo(20f, 30f)
            lineTo(200f, 210f)
            cubicTo(240f, 260f, 300f, 320f, 450f, 490f)
            lineTo(400f, 530f)
            lineTo(140f, 230f)
            close()
        }
        drawPath(shadow, color = Color.Black.copy(alpha = 0.18f))

        // Pencil Tip at (0, 0)
        val lead = Path().apply {
            moveTo(0f, 0f)
            lineTo(7f, 14f)
            lineTo(14f, 7f)
            close()
        }
        drawPath(lead, color = Color(0xFF0F172A))

        val wood = Path().apply {
            moveTo(7f, 14f)
            lineTo(18f, 36f)
            lineTo(36f, 18f)
            lineTo(14f, 7f)
            close()
        }
        drawPath(wood, color = Color(0xFFFED7AA))

        val pencilBody = Path().apply {
            moveTo(18f, 36f)
            lineTo(140f, 170f)
            lineTo(170f, 140f)
            lineTo(36f, 18f)
            close()
        }
        drawPath(pencilBody, color = Color(0xFFDB2777)) // Pink elegant pencil

        // Female Skin Base
        val skinBase = Path().apply {
            moveTo(55f, 75f)
            cubicTo(85f, 100f, 130f, 155f, 180f, 215f)
            lineTo(300f, 340f)
            lineTo(450f, 490f)
            lineTo(360f, 550f)
            lineTo(190f, 390f)
            cubicTo(130f, 290f, 80f, 190f, 55f, 75f)
            close()
        }
        drawPath(skinBase, color = Color(0xFFFDBA74))

        // French Manicure Nails
        val nail = Path().apply {
            moveTo(100f, 115f)
            lineTo(112f, 128f)
            lineTo(102f, 135f)
            close()
        }
        drawPath(nail, color = Color.White)
    }

    private fun DrawScope.drawMarkerHand(markerColor: Color) {
        // Drop Shadow (Semi-3D Depth)
        val shadow = Path().apply {
            moveTo(22f, 32f)
            lineTo(220f, 230f)
            cubicTo(260f, 280f, 330f, 350f, 500f, 540f)
            lineTo(430f, 590f)
            lineTo(160f, 260f)
            close()
        }
        drawPath(shadow, color = Color.Black.copy(alpha = 0.22f))

        // Marker Chisel Tip at (0, 0)
        val tip = Path().apply {
            moveTo(0f, 0f)
            lineTo(10f, 20f)
            lineTo(20f, 10f)
            close()
        }
        drawPath(tip, color = markerColor)

        // Marker Thick Matte Body
        val barrel = Path().apply {
            moveTo(10f, 20f)
            lineTo(150f, 170f)
            lineTo(170f, 150f)
            lineTo(20f, 10f)
            close()
        }
        drawPath(barrel, color = Color(0xFF0F172A))

        // Blue Accent Ring
        drawCircle(color = Color(0xFF3B82F6), radius = 18f, center = Offset(75f, 75f))

        // Realistic Hand Holding Marker
        val hand = Path().apply {
            moveTo(60f, 85f)
            cubicTo(95f, 110f, 140f, 170f, 190f, 230f)
            lineTo(320f, 360f)
            lineTo(490f, 530f)
            lineTo(390f, 590f)
            lineTo(210f, 420f)
            cubicTo(140f, 320f, 90f, 210f, 60f, 85f)
            close()
        }
        drawPath(hand, color = Color(0xFFE2E8F0))

        // Thumb Grip Overlay
        val thumb = Path().apply {
            moveTo(70f, 110f)
            cubicTo(100f, 120f, 140f, 180f, 130f, 210f)
            cubicTo(100f, 220f, 60f, 160f, 70f, 110f)
            close()
        }
        drawPath(thumb, color = Color(0xFFCBD5E1))
    }

    private fun DrawScope.drawStylusHand() {
        // Drop Shadow
        val shadow = Path().apply {
            moveTo(20f, 30f)
            lineTo(200f, 210f)
            cubicTo(240f, 260f, 300f, 320f, 450f, 490f)
            close()
        }
        drawPath(shadow, color = Color.Black.copy(alpha = 0.2f))

        // Stylus Fine Nib Tip at (0, 0)
        val nib = Path().apply {
            moveTo(0f, 0f)
            lineTo(6f, 12f)
            lineTo(12f, 6f)
            close()
        }
        drawPath(nib, color = Color(0xFF38BDF8))

        // Metallic Stylus Body
        val body = Path().apply {
            moveTo(6f, 12f)
            lineTo(140f, 160f)
            lineTo(160f, 140f)
            lineTo(12f, 6f)
            close()
        }
        drawPath(body, color = Color(0xFF64748B))

        // Hand
        val hand = Path().apply {
            moveTo(55f, 75f)
            cubicTo(85f, 100f, 130f, 155f, 180f, 215f)
            lineTo(300f, 340f)
            lineTo(450f, 490f)
            lineTo(360f, 550f)
            lineTo(190f, 390f)
            close()
        }
        drawPath(hand, color = Color(0xFFCBD5E1))
    }

    private fun DrawScope.drawCartoonHand() {
        // Drop Shadow
        val shadow = Path().apply {
            moveTo(20f, 30f)
            lineTo(190f, 200f)
            cubicTo(230f, 250f, 290f, 310f, 440f, 480f)
            close()
        }
        drawPath(shadow, color = Color.Black.copy(alpha = 0.2f))

        // Cartoon Pencil Tip at (0, 0)
        val tip = Path().apply {
            moveTo(0f, 0f)
            lineTo(8f, 16f)
            lineTo(16f, 8f)
            close()
        }
        drawPath(tip, color = Color.Black)

        val pencil = Path().apply {
            moveTo(8f, 16f)
            lineTo(130f, 150f)
            lineTo(150f, 130f)
            lineTo(16f, 8f)
            close()
        }
        drawPath(pencil, color = Color(0xFFFACC15))

        // White Cartoon Glove
        val glove = Path().apply {
            moveTo(50f, 70f)
            cubicTo(80f, 95f, 125f, 150f, 175f, 210f)
            lineTo(290f, 330f)
            lineTo(440f, 480f)
            lineTo(350f, 540f)
            lineTo(180f, 380f)
            close()
        }
        drawPath(glove, color = Color.White)
        drawPath(glove, color = Color.Black, style = Stroke(width = 3.0f))
    }
}
