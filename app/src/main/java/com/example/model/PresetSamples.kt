package com.example.model

import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin

object PresetSamples {

    val samples: List<PresetSample> by lazy {
        listOf(
            createRocketPreset(),
            createTechArchitecturePreset(),
            createMotivationalQuotePreset(),
            createPortraitPreset(),
            createCatPreset(),
            createSkylinePreset()
        )
    }

    private fun createRocketPreset(): PresetSample {
        val paths = mutableListOf<VectorPath>()

        // Rocket body
        paths.add(createBezierPath(
            listOf(Point2D(250f, 450f), Point2D(230f, 350f), Point2D(250f, 150f), Point2D(300f, 80f)),
            strokeWidth = 4f, color = Color(0xFF1E293B)
        ))
        paths.add(createBezierPath(
            listOf(Point2D(300f, 80f), Point2D(350f, 150f), Point2D(370f, 350f), Point2D(350f, 450f)),
            strokeWidth = 4f, color = Color(0xFF1E293B)
        ))
        // Rocket base
        paths.add(createLinePath(Point2D(250f, 450f), Point2D(350f, 450f), strokeWidth = 4f))

        // Window / Porthole
        paths.add(createCirclePath(center = Point2D(300f, 220f), radius = 35f, strokeWidth = 3f, color = Color(0xFF0284C7)))
        paths.add(createCirclePath(center = Point2D(300f, 220f), radius = 25f, strokeWidth = 2f, color = Color(0xFF38BDF8)))

        // Left fin
        paths.add(createPolylinePath(
            listOf(Point2D(240f, 380f), Point2D(180f, 470f), Point2D(245f, 450f)),
            strokeWidth = 4f, color = Color(0xFFDC2626)
        ))
        // Right fin
        paths.add(createPolylinePath(
            listOf(Point2D(360f, 380f), Point2D(420f, 470f), Point2D(355f, 450f)),
            strokeWidth = 4f, color = Color(0xFFDC2626)
        ))

        // Flame plumes
        paths.add(createPolylinePath(
            listOf(Point2D(270f, 450f), Point2D(260f, 520f), Point2D(285f, 480f), Point2D(300f, 550f), Point2D(315f, 480f), Point2D(340f, 520f), Point2D(330f, 450f)),
            strokeWidth = 3f, color = Color(0xFFF59E0B)
        ))

        // Smoke clouds
        paths.add(createCirclePath(Point2D(150f, 520f), 30f, strokeWidth = 2f, color = Color.Gray))
        paths.add(createCirclePath(Point2D(450f, 520f), 30f, strokeWidth = 2f, color = Color.Gray))

        // Stars & Text
        paths.add(createStarPath(Point2D(120f, 120f), 20f, strokeWidth = 2f, color = Color(0xFFEAB308)))
        paths.add(createStarPath(Point2D(480f, 180f), 24f, strokeWidth = 2f, color = Color(0xFFEAB308)))
        paths.add(createStarPath(Point2D(420f, 90f), 15f, strokeWidth = 2f, color = Color(0xFFEAB308)))

        // Text paths
        val textPaths = createTextPath("LAUNCH 2026", startX = 180f, startY = 580f, isText = true)
        paths.addAll(textPaths)

        return PresetSample(
            id = "rocket",
            title = "Space Rocket Launch",
            category = "Illustration",
            iconName = "rocket",
            description = "High energy whiteboard sketch of a rocket launching into space with stars and typography.",
            sampleSvgPaths = paths
        )
    }

    private fun createTechArchitecturePreset(): PresetSample {
        val paths = mutableListOf<VectorPath>()

        // Cloud box (API Gateway)
        paths.add(createCloudPath(Point2D(300f, 100f), width = 160f, height = 70f))
        paths.addAll(createTextPath("API GATEWAY", 240f, 105f, isText = true))

        // 3 Microservice Boxes
        paths.add(createRectPath(Point2D(100f, 250f), 120f, 80f, color = Color(0xFF2563EB)))
        paths.addAll(createTextPath("AUTH SERVICE", 110f, 290f, isText = true))

        paths.add(createRectPath(Point2D(250f, 250f), 120f, 80f, color = Color(0xFF059669)))
        paths.addAll(createTextPath("SPEED ENGINE", 255f, 290f, isText = true))

        paths.add(createRectPath(Point2D(400f, 250f), 120f, 80f, color = Color(0xFFD97706)))
        paths.addAll(createTextPath("VIDEO EXPORT", 405f, 290f, isText = true))

        // Connectors (Arrows)
        paths.add(createArrowPath(Point2D(270f, 135f), Point2D(160f, 250f)))
        paths.add(createArrowPath(Point2D(300f, 135f), Point2D(310f, 250f)))
        paths.add(createArrowPath(Point2D(330f, 135f), Point2D(460f, 250f)))

        // Database Cylinder at bottom
        paths.add(createCylinderPath(Point2D(310f, 420f), radiusX = 60f, radiusY = 20f, height = 70f))
        paths.addAll(createTextPath("DATABASE", 270f, 450f, isText = true))

        // Arrows from services to DB
        paths.add(createArrowPath(Point2D(160f, 330f), Point2D(270f, 420f)))
        paths.add(createArrowPath(Point2D(310f, 330f), Point2D(310f, 420f)))
        paths.add(createArrowPath(Point2D(460f, 330f), Point2D(350f, 420f)))

        return PresetSample(
            id = "architecture",
            title = "Cloud System Diagram",
            category = "Diagram",
            iconName = "schema",
            description = "Clean professional system architecture diagram with microservices and database.",
            sampleSvgPaths = paths
        )
    }

    private fun createMotivationalQuotePreset(): PresetSample {
        val paths = mutableListOf<VectorPath>()

        // Decorative Banner
        paths.add(createPolylinePath(
            listOf(Point2D(100f, 120f), Point2D(500f, 120f), Point2D(480f, 170f), Point2D(500f, 220f), Point2D(100f, 220f), Point2D(120f, 170f), Point2D(100f, 120f)),
            strokeWidth = 4f, color = Color(0xFF6366F1)
        ))

        // Quote Text
        paths.addAll(createTextPath("DREAM BIG", 180f, 170f, isText = true))

        // Subtitle text
        paths.addAll(createTextPath("CREATE ANYTHING", 150f, 280f, isText = true))
        paths.addAll(createTextPath("SPEEDPAINT TIER 1", 160f, 350f, isText = true))

        // Decorative flourishes & lightbulb
        paths.add(createLightbulbPath(Point2D(300f, 460f)))

        return PresetSample(
            id = "quote",
            title = "Typography Banner",
            category = "Typography",
            iconName = "format_quote",
            description = "Hand-lettered motivational typography speedpaint sequence with decorative banners.",
            sampleSvgPaths = paths
        )
    }

    private fun createPortraitPreset(): PresetSample {
        val paths = mutableListOf<VectorPath>()

        // Head outline
        paths.add(createBezierPath(
            listOf(Point2D(220f, 160f), Point2D(160f, 220f), Point2D(180f, 380f), Point2D(300f, 440f), Point2D(420f, 380f), Point2D(440f, 220f), Point2D(380f, 160f)),
            strokeWidth = 3.5f
        ))
        // Hairline
        paths.add(createBezierPath(
            listOf(Point2D(220f, 170f), Point2D(300f, 120f), Point2D(380f, 170f), Point2D(340f, 220f), Point2D(260f, 220f), Point2D(220f, 170f)),
            strokeWidth = 3f, color = Color(0xFF1E1B4B)
        ))
        // Eyes
        paths.add(createCirclePath(Point2D(250f, 270f), 15f, strokeWidth = 2.5f))
        paths.add(createCirclePath(Point2D(350f, 270f), 15f, strokeWidth = 2.5f))
        paths.add(createCirclePath(Point2D(250f, 270f), 6f, strokeWidth = 4f, color = Color.Black))
        paths.add(createCirclePath(Point2D(350f, 270f), 6f, strokeWidth = 4f, color = Color.Black))

        // Eyebrows
        paths.add(createPolylinePath(listOf(Point2D(230f, 240f), Point2D(270f, 245f)), strokeWidth = 3f))
        paths.add(createPolylinePath(listOf(Point2D(330f, 245f), Point2D(370f, 240f)), strokeWidth = 3f))

        // Nose
        paths.add(createPolylinePath(listOf(Point2D(300f, 260f), Point2D(305f, 320f), Point2D(290f, 330f)), strokeWidth = 2.5f))

        // Smile
        paths.add(createBezierPath(
            listOf(Point2D(260f, 370f), Point2D(300f, 400f), Point2D(340f, 370f)),
            strokeWidth = 3f, color = Color(0xFFDC2626)
        ))

        // Shoulders
        paths.add(createBezierPath(
            listOf(Point2D(230f, 430f), Point2D(140f, 500f), Point2D(100f, 580f)), strokeWidth = 3f
        ))
        paths.add(createBezierPath(
            listOf(Point2D(370f, 430f), Point2D(460f, 500f), Point2D(500f, 580f)), strokeWidth = 3f
        ))

        return PresetSample(
            id = "portrait",
            title = "Executive Portrait Sketch",
            category = "Portrait",
            iconName = "person",
            description = "Detailed human face & shoulders portrait sketching line work.",
            sampleSvgPaths = paths
        )
    }

    private fun createCatPreset(): PresetSample {
        val paths = mutableListOf<VectorPath>()

        // Cat Head
        paths.add(createCirclePath(Point2D(300f, 300f), 100f, strokeWidth = 4f, color = Color(0xFF1E293B)))

        // Ears
        paths.add(createPolylinePath(listOf(Point2D(220f, 240f), Point2D(190f, 150f), Point2D(260f, 210f)), strokeWidth = 4f))
        paths.add(createPolylinePath(listOf(Point2D(380f, 240f), Point2D(410f, 150f), Point2D(340f, 210f)), strokeWidth = 4f))

        // Eyes (Cute large cat eyes)
        paths.add(createCirclePath(Point2D(255f, 280f), 22f, strokeWidth = 3f, color = Color(0xFF10B981)))
        paths.add(createCirclePath(Point2D(345f, 280f), 22f, strokeWidth = 3f, color = Color(0xFF10B981)))
        paths.add(createCirclePath(Point2D(255f, 280f), 10f, strokeWidth = 5f, color = Color.Black))
        paths.add(createCirclePath(Point2D(345f, 280f), 10f, strokeWidth = 5f, color = Color.Black))

        // Cute Nose & Mouth
        paths.add(createPolylinePath(listOf(Point2D(290f, 320f), Point2D(310f, 320f), Point2D(300f, 332f), Point2D(290f, 320f)), strokeWidth = 3f, color = Color(0xFFEC4899)))
        paths.add(createBezierPath(listOf(Point2D(300f, 332f), Point2D(285f, 350f), Point2D(270f, 340f)), strokeWidth = 3f))
        paths.add(createBezierPath(listOf(Point2D(300f, 332f), Point2D(315f, 350f), Point2D(330f, 340f)), strokeWidth = 3f))

        // Whiskers
        paths.add(createLinePath(Point2D(240f, 320f), Point2D(150f, 300f), strokeWidth = 2.5f))
        paths.add(createLinePath(Point2D(240f, 330f), Point2D(140f, 330f), strokeWidth = 2.5f))
        paths.add(createLinePath(Point2D(240f, 340f), Point2D(150f, 360f), strokeWidth = 2.5f))

        paths.add(createLinePath(Point2D(360f, 320f), Point2D(450f, 300f), strokeWidth = 2.5f))
        paths.add(createLinePath(Point2D(360f, 330f), Point2D(460f, 330f), strokeWidth = 2.5f))
        paths.add(createLinePath(Point2D(360f, 340f), Point2D(450f, 360f), strokeWidth = 2.5f))

        paths.addAll(createTextPath("MEOW", 260f, 450f, isText = true))

        return PresetSample(
            id = "cat",
            title = "Cartoon Playful Cat",
            category = "Cartoon",
            iconName = "pets",
            description = "Playful cartoon cat illustration with whiskers and big glowing eyes.",
            sampleSvgPaths = paths
        )
    }

    private fun createSkylinePreset(): PresetSample {
        val paths = mutableListOf<VectorPath>()

        // Horizon line
        paths.add(createLinePath(Point2D(50f, 450f), Point2D(550f, 450f), strokeWidth = 4f, color = Color(0xFF334155)))

        // Buildings silhouette polylines
        val skylinePoints = listOf(
            Point2D(70f, 450f), Point2D(70f, 300f), Point2D(120f, 300f), Point2D(120f, 450f),
            Point2D(130f, 450f), Point2D(130f, 200f), Point2D(160f, 150f), Point2D(190f, 200f), Point2D(190f, 450f),
            Point2D(200f, 450f), Point2D(200f, 340f), Point2D(270f, 340f), Point2D(270f, 450f),
            Point2D(280f, 450f), Point2D(280f, 180f), Point2D(350f, 180f), Point2D(350f, 450f),
            Point2D(360f, 450f), Point2D(360f, 250f), Point2D(430f, 250f), Point2D(430f, 450f),
            Point2D(440f, 450f), Point2D(440f, 320f), Point2D(500f, 320f), Point2D(500f, 450f)
        )
        paths.add(createPolylinePath(skylinePoints, strokeWidth = 3.5f, color = Color(0xFF0F172A)))

        // Windows in buildings
        for (x in 295..335 step 20) {
            for (y in 210..420 step 30) {
                paths.add(createRectPath(Point2D(x.toFloat(), y.toFloat()), 12f, 16f, strokeWidth = 1.5f, color = Color(0xFFF59E0B)))
            }
        }

        // Moon in sky
        paths.add(createCirclePath(Point2D(480f, 120f), 35f, strokeWidth = 3f, color = Color(0xFFFDE047)))

        return PresetSample(
            id = "skyline",
            title = "Metropolis City Skyline",
            category = "Architecture",
            iconName = "location_city",
            description = "Detailed urban city skyline with skyscraper spires and moonlit windows.",
            sampleSvgPaths = paths
        )
    }

    // --- Helper math path generators ---

    private fun createLinePath(start: Point2D, end: Point2D, strokeWidth: Float = 3f, color: Color = Color.Black): VectorPath {
        return VectorPath(points = listOf(start, end), strokeWidth = strokeWidth, color = color)
    }

    private fun createPolylinePath(points: List<Point2D>, strokeWidth: Float = 3f, color: Color = Color.Black): VectorPath {
        return VectorPath(points = points, strokeWidth = strokeWidth, color = color)
    }

    private fun createCirclePath(center: Point2D, radius: Float, strokeWidth: Float = 3f, color: Color = Color.Black): VectorPath {
        val points = mutableListOf<Point2D>()
        val segments = 32
        for (i in 0..segments) {
            val angle = (i.toDouble() / segments) * 2 * Math.PI
            val x = (center.x + radius * cos(angle)).toFloat()
            val y = (center.y + radius * sin(angle)).toFloat()
            points.add(Point2D(x, y))
        }
        return VectorPath(points = points, strokeWidth = strokeWidth, color = color)
    }

    private fun createRectPath(topLeft: Point2D, width: Float, height: Float, strokeWidth: Float = 3f, color: Color = Color.Black): VectorPath {
        val points = listOf(
            topLeft,
            Point2D(topLeft.x + width, topLeft.y),
            Point2D(topLeft.x + width, topLeft.y + height),
            Point2D(topLeft.x, topLeft.y + height),
            topLeft
        )
        return VectorPath(points = points, strokeWidth = strokeWidth, color = color)
    }

    private fun createBezierPath(controlPoints: List<Point2D>, strokeWidth: Float = 3f, color: Color = Color.Black): VectorPath {
        val sampled = mutableListOf<Point2D>()
        val steps = 30
        for (step in 0..steps) {
            val t = step.toFloat() / steps
            val pt = evaluateBezier(controlPoints, t)
            sampled.add(pt)
        }
        return VectorPath(points = sampled, strokeWidth = strokeWidth, color = color)
    }

    private fun evaluateBezier(points: List<Point2D>, t: Float): Point2D {
        if (points.size == 1) return points[0]
        var current = points
        while (current.size > 1) {
            val next = mutableListOf<Point2D>()
            for (i in 0 until current.size - 1) {
                val x = (1 - t) * current[i].x + t * current[i + 1].x
                val y = (1 - t) * current[i].y + t * current[i + 1].y
                next.add(Point2D(x, y))
            }
            current = next
        }
        return current[0]
    }

    private fun createStarPath(center: Point2D, outerRadius: Float, strokeWidth: Float = 2f, color: Color = Color.Black): VectorPath {
        val points = mutableListOf<Point2D>()
        val innerRadius = outerRadius * 0.4f
        val pointsCount = 10
        for (i in 0..pointsCount) {
            val r = if (i % 2 == 0) outerRadius else innerRadius
            val angle = (i.toDouble() / pointsCount) * 2 * Math.PI - Math.PI / 2
            val x = (center.x + r * cos(angle)).toFloat()
            val y = (center.y + r * sin(angle)).toFloat()
            points.add(Point2D(x, y))
        }
        return VectorPath(points = points, strokeWidth = strokeWidth, color = color)
    }

    private fun createCloudPath(center: Point2D, width: Float, height: Float): VectorPath {
        val points = mutableListOf<Point2D>()
        val rx = width / 2
        val ry = height / 2
        val segments = 24
        for (i in 0..segments) {
            val angle = (i.toDouble() / segments) * 2 * Math.PI
            val x = (center.x + rx * cos(angle) + 8 * sin(3 * angle)).toFloat()
            val y = (center.y + ry * sin(angle) + 5 * cos(3 * angle)).toFloat()
            points.add(Point2D(x, y))
        }
        return VectorPath(points = points, strokeWidth = 3f, color = Color(0xFF0284C7))
    }

    private fun createArrowPath(from: Point2D, to: Point2D): VectorPath {
        val points = mutableListOf<Point2D>()
        points.add(from)
        points.add(to)
        // Arrowhead
        val dx = to.x - from.x
        val dy = to.y - from.y
        val angle = Math.atan2(dy.toDouble(), dx.toDouble())
        val headLen = 15.0
        val p1x = (to.x - headLen * cos(angle - Math.PI / 6)).toFloat()
        val p1y = (to.y - headLen * sin(angle - Math.PI / 6)).toFloat()
        val p2x = (to.x - headLen * cos(angle + Math.PI / 6)).toFloat()
        val p2y = (to.y - headLen * sin(angle + Math.PI / 6)).toFloat()

        points.add(Point2D(p1x, p1y))
        points.add(to)
        points.add(Point2D(p2x, p2y))

        return VectorPath(points = points, strokeWidth = 3f, color = Color(0xFF475569))
    }

    private fun createCylinderPath(center: Point2D, radiusX: Float, radiusY: Float, height: Float): VectorPath {
        val points = mutableListOf<Point2D>()
        // Top oval
        for (i in 0..20) {
            val angle = (i.toDouble() / 20) * 2 * Math.PI
            points.add(Point2D((center.x + radiusX * cos(angle)).toFloat(), (center.y + radiusY * sin(angle)).toFloat()))
        }
        // Side & Bottom
        points.add(Point2D(center.x + radiusX, center.y + height))
        for (i in 0..10) {
            val angle = (i.toDouble() / 10) * Math.PI
            points.add(Point2D((center.x + radiusX * cos(angle)).toFloat(), (center.y + height + radiusY * sin(angle)).toFloat()))
        }
        points.add(Point2D(center.x - radiusX, center.y))
        return VectorPath(points = points, strokeWidth = 3f, color = Color(0xFFD97706))
    }

    private fun createLightbulbPath(center: Point2D): VectorPath {
        val points = mutableListOf<Point2D>()
        // Bulb sphere curve
        for (i in 0..16) {
            val angle = -Math.PI / 4 + (i.toDouble() / 16) * (1.5 * Math.PI)
            points.add(Point2D((center.x + 40 * cos(angle)).toFloat(), (center.y + 40 * sin(angle)).toFloat()))
        }
        // Base threads
        points.add(Point2D(center.x - 20f, center.y + 50f))
        points.add(Point2D(center.x + 20f, center.y + 50f))
        points.add(Point2D(center.x - 15f, center.y + 65f))
        points.add(Point2D(center.x + 15f, center.y + 65f))
        return VectorPath(points = points, strokeWidth = 3.5f, color = Color(0xFFEAB308))
    }

    private fun createTextPath(text: String, startX: Float, startY: Float, isText: Boolean = true): List<VectorPath> {
        val paths = mutableListOf<VectorPath>()
        var curX = startX
        val charWidth = 22f
        val charHeight = 32f

        for (ch in text) {
            if (ch == ' ') {
                curX += charWidth * 0.8f
                continue
            }
            val letterPoints = getSimpleLetterPoints(ch, curX, startY, charWidth, charHeight)
            paths.add(VectorPath(
                points = letterPoints,
                strokeWidth = 3.5f,
                color = Color(0xFF1E1B4B),
                isText = isText
            ))
            curX += charWidth + 6f
        }
        return paths
    }

    private fun getSimpleLetterPoints(ch: Char, x: Float, y: Float, w: Float, h: Float): List<Point2D> {
        return when (ch.uppercaseChar()) {
            'A' -> listOf(Point2D(x, y + h), Point2D(x + w / 2, y), Point2D(x + w, y + h), Point2D(x + w * 0.75f, y + h / 2), Point2D(x + w * 0.25f, y + h / 2))
            'B' -> listOf(Point2D(x, y + h), Point2D(x, y), Point2D(x + w * 0.8f, y + h * 0.25f), Point2D(x, y + h * 0.5f), Point2D(x + w, y + h * 0.75f), Point2D(x, y + h))
            'C' -> listOf(Point2D(x + w, y + h * 0.2f), Point2D(x + w * 0.3f, y), Point2D(x, y + h * 0.5f), Point2D(x + w * 0.3f, y + h), Point2D(x + w, y + h * 0.8f))
            'D' -> listOf(Point2D(x, y + h), Point2D(x, y), Point2D(x + w * 0.8f, y + h * 0.3f), Point2D(x + w * 0.8f, y + h * 0.7f), Point2D(x, y + h))
            'E' -> listOf(Point2D(x + w, y), Point2D(x, y), Point2D(x, y + h / 2), Point2D(x + w * 0.7f, y + h / 2), Point2D(x, y + h / 2), Point2D(x, y + h), Point2D(x + w, y + h))
            'F' -> listOf(Point2D(x + w, y), Point2D(x, y), Point2D(x, y + h / 2), Point2D(x + w * 0.7f, y + h / 2), Point2D(x, y + h / 2), Point2D(x, y + h))
            'G' -> listOf(Point2D(x + w, y + h * 0.2f), Point2D(x + w * 0.3f, y), Point2D(x, y + h * 0.5f), Point2D(x + w * 0.3f, y + h), Point2D(x + w, y + h), Point2D(x + w, y + h * 0.5f), Point2D(x + w * 0.5f, y + h * 0.5f))
            'H' -> listOf(Point2D(x, y), Point2D(x, y + h), Point2D(x, y + h / 2), Point2D(x + w, y + h / 2), Point2D(x + w, y), Point2D(x + w, y + h))
            'I' -> listOf(Point2D(x + w * 0.2f, y), Point2D(x + w * 0.8f, y), Point2D(x + w / 2, y), Point2D(x + w / 2, y + h), Point2D(x + w * 0.2f, y + h), Point2D(x + w * 0.8f, y + h))
            'L' -> listOf(Point2D(x, y), Point2D(x, y + h), Point2D(x + w, y + h))
            'M' -> listOf(Point2D(x, y + h), Point2D(x, y), Point2D(x + w / 2, y + h * 0.6f), Point2D(x + w, y), Point2D(x + w, y + h))
            'N' -> listOf(Point2D(x, y + h), Point2D(x, y), Point2D(x + w, y + h), Point2D(x + w, y))
            'O' -> listOf(Point2D(x + w / 2, y), Point2D(x + w, y + h / 2), Point2D(x + w / 2, y + h), Point2D(x, y + h / 2), Point2D(x + w / 2, y))
            'P' -> listOf(Point2D(x, y + h), Point2D(x, y), Point2D(x + w, y + h * 0.25f), Point2D(x, y + h * 0.5f))
            'R' -> listOf(Point2D(x, y + h), Point2D(x, y), Point2D(x + w, y + h * 0.25f), Point2D(x, y + h * 0.5f), Point2D(x + w, y + h))
            'S' -> listOf(Point2D(x + w, y + h * 0.2f), Point2D(x, y + h * 0.3f), Point2D(x + w, y + h * 0.7f), Point2D(x, y + h * 0.8f))
            'T' -> listOf(Point2D(x, y), Point2D(x + w, y), Point2D(x + w / 2, y), Point2D(x + w / 2, y + h))
            'U' -> listOf(Point2D(x, y), Point2D(x, y + h * 0.8f), Point2D(x + w / 2, y + h), Point2D(x + w, y + h * 0.8f), Point2D(x + w, y))
            'W' -> listOf(Point2D(x, y), Point2D(x + w * 0.25f, y + h), Point2D(x + w / 2, y + h * 0.4f), Point2D(x + w * 0.75f, y + h), Point2D(x + w, y))
            'Y' -> listOf(Point2D(x, y), Point2D(x + w / 2, y + h * 0.5f), Point2D(x + w, y), Point2D(x + w / 2, y + h * 0.5f), Point2D(x + w / 2, y + h))
            '1' -> listOf(Point2D(x + w * 0.2f, y + h * 0.3f), Point2D(x + w * 0.5f, y), Point2D(x + w * 0.5f, y + h))
            '0' -> listOf(Point2D(x + w / 2, y), Point2D(x + w, y + h / 2), Point2D(x + w / 2, y + h), Point2D(x, y + h / 2), Point2D(x + w / 2, y))
            '2' -> listOf(Point2D(x, y + h * 0.25f), Point2D(x + w / 2, y), Point2D(x + w, y + h * 0.3f), Point2D(x, y + h), Point2D(x + w, y + h))
            '6' -> listOf(Point2D(x + w, y + h * 0.2f), Point2D(x, y + h * 0.5f), Point2D(x + w, y + h * 0.75f), Point2D(x, y + h), Point2D(x, y + h * 0.5f))
            else -> listOf(Point2D(x, y + h), Point2D(x + w, y))
        }
    }
}
