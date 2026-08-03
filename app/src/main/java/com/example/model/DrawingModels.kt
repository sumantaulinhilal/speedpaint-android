package com.example.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Offset
import java.util.UUID

data class Point2D(
    val x: Float,
    val y: Float
) {
    fun toOffset() = Offset(x, y)
}

data class VectorPath(
    val id: String = UUID.randomUUID().toString(),
    val points: List<Point2D>,
    val color: Color = Color.Black,
    val strokeWidth: Float = 3f,
    val isText: Boolean = false,
    val totalLength: Float = 0f,
    val boundingBox: PathBoundingBox = PathBoundingBox.calculate(points)
) {
    val startPoint: Point2D get() = points.firstOrNull() ?: Point2D(0f, 0f)
    val endPoint: Point2D get() = points.lastOrNull() ?: Point2D(0f, 0f)
    val center: Point2D get() = boundingBox.center
}

data class PathBoundingBox(
    val minX: Float,
    val minY: Float,
    val maxX: Float,
    val maxY: Float
) {
    val width: Float get() = maxX - minX
    val height: Float get() = maxY - minY
    val center: Point2D get() = Point2D(minX + width / 2f, minY + height / 2f)

    companion object {
        fun calculate(points: List<Point2D>): PathBoundingBox {
            if (points.isEmpty()) return PathBoundingBox(0f, 0f, 0f, 0f)
            var minX = Float.MAX_VALUE
            var minY = Float.MAX_VALUE
            var maxX = -Float.MAX_VALUE
            var maxY = -Float.MAX_VALUE
            for (p in points) {
                if (p.x < minX) minX = p.x
                if (p.y < minY) minY = p.y
                if (p.x > maxX) maxX = p.x
                if (p.y > maxY) maxY = p.y
            }
            return PathBoundingBox(minX, minY, maxX, maxY)
        }
    }
}

enum class HandStyle(val displayName: String, val iconRes: Int? = null) {
    NO_HAND("No Hand"),
    MALE_PENCIL("Male Pencil"),
    FEMALE_PENCIL("Female Pencil"),
    BLACK_MARKER("Black Marker"),
    WHITE_MARKER("White Marker"),
    STYLUS("Stylus Pen"),
    CARTOON_HAND("Cartoon Hand"),
    CUSTOM_PNG("Custom Hand")
}

enum class SequenceOrder(val displayName: String, val description: String) {
    AUTO("Auto", "Smart path spatial & text ordering"),
    LEFT_TO_RIGHT("Left to Right", "Draws paths progressively from left edge to right"),
    RIGHT_TO_LEFT("Right to Left", "Draws paths progressively from right edge to left"),
    TOP_TO_BOTTOM("Top to Bottom", "Draws paths top down"),
    BOTTOM_TO_TOP("Bottom to Top", "Draws paths bottom up"),
    CENTER_OUT("Center Out", "Expands outwards from canvas center"),
    SPIRAL("Spiral", "Follows inward/outward spiral motion"),
    RANDOM("Random", "Dynamic artistic random ordering"),
    TEXT_FIRST("Text First", "Draws text annotations first, then illustrations"),
    TEXT_LAST("Text Last", "Draws illustrations first, then text annotations")
}

enum class SketchType(val displayName: String) {
    COLOR("Full Color"),
    BLACK_WHITE("Black & White"),
    GRAYSCALE("Grayscale")
}

enum class AspectRatio(val displayName: String, val ratio: Float) {
    RATIO_16_9("16:9 Landscape", 16f / 9f),
    RATIO_9_16("9:16 Story / Reel", 9f / 16f),
    RATIO_1_1("1:1 Square", 1f),
    RATIO_4_5("4:5 Instagram Portrait", 4f / 5f),
    RATIO_3_2("3:2 Classic Photo", 3f / 2f)
}

enum class VideoQuality(val displayName: String, val width: Int, val height: Int) {
    HD("HD (720p)", 1280, 720),
    FULL_HD("Full HD (1080p)", 1920, 1080),
    QUAD_HD("2K (1440p)", 2560, 1440),
    ULTRA_HD("4K (2160p)", 3840, 2160)
}

enum class BackgroundStyle(val displayName: String, val color: Color, val isDark: Boolean = false) {
    WHITE("Whiteboard", Color(0xFFFAFAFA), isDark = false),
    BLACK("Blackboard", Color(0xFF121212), isDark = true),
    CHALKBOARD("Chalkboard Green", Color(0xFF1E3A2B), isDark = true),
    BLUEPRINT("Blueprint Blue", Color(0xFF1E3A8A), isDark = true),
    PARCHMENT("Parchment Paper", Color(0xFFF7F1E3), isDark = false),
    TRANSPARENT("Transparent", Color.Transparent, isDark = false),
    CUSTOM("Custom Accent", Color(0xFF1E1B4B), isDark = true)
}

enum class ExportFormat(val displayName: String, val extension: String) {
    MP4("MP4 Video", "mp4"),
    GIF("Animated GIF", "gif"),
    PNG_SEQUENCE("PNG Frame Sequence", "zip")
}

data class SpeedPaintProjectConfig(
    val sketchDurationSec: Int = 10,
    val fillDurationSec: Int = 4,
    val handStyle: HandStyle = HandStyle.MALE_PENCIL,
    val sequenceOrder: SequenceOrder = SequenceOrder.AUTO,
    val sketchType: SketchType = SketchType.COLOR,
    val aspectRatio: AspectRatio = AspectRatio.RATIO_16_9,
    val fps: Int = 30,
    val quality: VideoQuality = VideoQuality.FULL_HD,
    val backgroundStyle: BackgroundStyle = BackgroundStyle.WHITE,
    val fadeIn: Boolean = true,
    val fadeOut: Boolean = true,
    val exportFormat: ExportFormat = ExportFormat.MP4
)

data class PresetSample(
    val id: String,
    val title: String,
    val category: String,
    val iconName: String,
    val description: String,
    val sampleSvgPaths: List<VectorPath>
)
