package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.animation.SpeedPaintRenderer.renderSpeedPaintFrame
import com.example.model.BackgroundStyle
import com.example.model.HandStyle
import com.example.model.SketchType
import com.example.model.VectorPath
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishOnPrimary
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishPrimaryContainer
import com.example.ui.theme.PolishSurface
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary

@Composable
fun CanvasPreviewPlayer(
    paths: List<VectorPath>,
    outlineProgress: Float,
    fillProgress: Float,
    isPlaying: Boolean,
    handStyle: HandStyle,
    backgroundStyle: BackgroundStyle,
    sketchType: SketchType,
    aspectRatioValue: Float,
    playbackSpeed: Float,
    totalDurationSec: Float,
    onTogglePlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
    onChangeSpeed: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("canvas_preview_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PolishSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Screen / Canvas Container bounded by selected aspect ratio
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatioValue.coerceAtLeast(0.5f))
                    .clip(RoundedCornerShape(18.dp))
                    .background(backgroundStyle.color)
                    .border(1.dp, PolishBorder, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    renderSpeedPaintFrame(
                        paths = paths,
                        progress = outlineProgress,
                        fillProgress = fillProgress,
                        handStyle = handStyle,
                        backgroundStyle = backgroundStyle,
                        sketchType = sketchType
                    )
                }

                // Play overlay pill if paused
                if (!isPlaying && paths.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(PolishPrimary)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = onTogglePlayPause) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = PolishOnPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Scrubber Bar
            val overallProgress = if (fillProgress > 0f) {
                0.8f + (fillProgress * 0.2f)
            } else {
                outlineProgress * 0.8f
            }

            val currentSec = overallProgress * totalDurationSec
            val formattedCurrent = String.format("%.1fs", currentSec)
            val formattedTotal = String.format("%.1fs", totalDurationSec)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedCurrent,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    color = PolishTextPrimary
                )

                Slider(
                    value = overallProgress.coerceIn(0f, 1f),
                    onValueChange = onSeek,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .testTag("scrubber_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = PolishPrimary,
                        activeTrackColor = PolishPrimary,
                        inactiveTrackColor = PolishBorder
                    )
                )

                Text(
                    text = formattedTotal,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = PolishTextSecondary
                )
            }

            // Controls toolbar (Play/Pause, Rewind, Speed toggle)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onSeek(0f) },
                        modifier = Modifier.testTag("rewind_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = "Restart",
                            tint = PolishTextPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PolishPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = onTogglePlayPause,
                            modifier = Modifier.testTag("play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = PolishOnPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Speed buttons (0.5x, 1x, 2x)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    listOf(0.5f, 1.0f, 2.0f).forEach { speed ->
                        val isSelected = playbackSpeed == speed
                        TextButton(
                            onClick = { onChangeSpeed(speed) },
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .testTag("speed_${speed}x")
                        ) {
                            Text(
                                text = "${speed}x",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) PolishPrimary else PolishTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

