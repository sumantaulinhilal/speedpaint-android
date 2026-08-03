package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AspectRatio
import com.example.model.BackgroundStyle
import com.example.model.ExportFormat
import com.example.model.SequenceOrder
import com.example.model.SketchType
import com.example.model.SpeedPaintProjectConfig
import com.example.model.VideoQuality
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishSurface
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary

@Composable
fun SettingsPanel(
    config: SpeedPaintProjectConfig,
    onConfigChange: ((SpeedPaintProjectConfig) -> SpeedPaintProjectConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PolishSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "ANIMATION CONTROLS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PolishTextSecondary,
                letterSpacing = 1.sp
            )

            // Slider 1: Sketch Duration (1s to 30s)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Sketch Duration",
                        fontSize = 13.sp,
                        color = PolishTextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${config.sketchDurationSec} sec",
                        fontSize = 13.sp,
                        color = PolishPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = config.sketchDurationSec.toFloat(),
                    onValueChange = { newValue ->
                        onConfigChange { it.copy(sketchDurationSec = newValue.toInt()) }
                    },
                    valueRange = 1f..30f,
                    steps = 28,
                    modifier = Modifier.testTag("sketch_duration_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = PolishPrimary,
                        activeTrackColor = PolishPrimary,
                        inactiveTrackColor = PolishBorder
                    )
                )
            }

            // Slider 2: Color Fill Duration (0s to 30s)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Color Fill Duration",
                        fontSize = 13.sp,
                        color = PolishTextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (config.fillDurationSec == 0) "Disabled (0s)" else "${config.fillDurationSec} sec",
                        fontSize = 13.sp,
                        color = PolishPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = config.fillDurationSec.toFloat(),
                    onValueChange = { newValue ->
                        onConfigChange { it.copy(fillDurationSec = newValue.toInt()) }
                    },
                    valueRange = 0f..30f,
                    steps = 29,
                    modifier = Modifier.testTag("fill_duration_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = PolishPrimary,
                        activeTrackColor = PolishPrimary,
                        inactiveTrackColor = PolishBorder
                    )
                )
            }

            // Sequence Order Dropdown
            DropdownSettingRow(
                label = "SEQUENCE ORDER",
                selectedName = config.sequenceOrder.displayName,
                options = SequenceOrder.values().map { it.displayName },
                onOptionSelected = { index ->
                    onConfigChange { it.copy(sequenceOrder = SequenceOrder.values()[index]) }
                }
            )

            // Sketch Type Dropdown
            DropdownSettingRow(
                label = "SKETCH TYPE",
                selectedName = config.sketchType.displayName,
                options = SketchType.values().map { it.displayName },
                onOptionSelected = { index ->
                    onConfigChange { it.copy(sketchType = SketchType.values()[index]) }
                }
            )

            // Aspect Ratio Dropdown
            DropdownSettingRow(
                label = "ASPECT RATIO",
                selectedName = config.aspectRatio.displayName,
                options = AspectRatio.values().map { it.displayName },
                onOptionSelected = { index ->
                    onConfigChange { it.copy(aspectRatio = AspectRatio.values()[index]) }
                }
            )

            // FPS & Video Quality
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    DropdownSettingRow(
                        label = "FPS",
                        selectedName = "${config.fps} FPS",
                        options = listOf("24 FPS", "30 FPS", "60 FPS"),
                        onOptionSelected = { index ->
                            val fpsVal = when (index) {
                                0 -> 24
                                1 -> 30
                                else -> 60
                            }
                            onConfigChange { it.copy(fps = fpsVal) }
                        }
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    DropdownSettingRow(
                        label = "QUALITY",
                        selectedName = config.quality.displayName,
                        options = VideoQuality.values().map { it.displayName },
                        onOptionSelected = { index ->
                            onConfigChange { it.copy(quality = VideoQuality.values()[index]) }
                        }
                    )
                }
            }

            // Canvas Background Style
            DropdownSettingRow(
                label = "BACKGROUND CANVAS",
                selectedName = config.backgroundStyle.displayName,
                options = BackgroundStyle.values().map { it.displayName },
                onOptionSelected = { index ->
                    onConfigChange { it.copy(backgroundStyle = BackgroundStyle.values()[index]) }
                }
            )

            // Fade In / Fade Out Toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Fade In", fontSize = 13.sp, color = PolishTextPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = config.fadeIn,
                        onCheckedChange = { chk -> onConfigChange { it.copy(fadeIn = chk) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = PolishPrimary)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Fade Out", fontSize = 13.sp, color = PolishTextPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = config.fadeOut,
                        onCheckedChange = { chk -> onConfigChange { it.copy(fadeOut = chk) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = PolishPrimary)
                    )
                }
            }

            // Export Format Selector
            DropdownSettingRow(
                label = "OUTPUT FORMAT",
                selectedName = config.exportFormat.displayName,
                options = ExportFormat.values().map { it.displayName },
                onOptionSelected = { index ->
                    onConfigChange { it.copy(exportFormat = ExportFormat.values()[index]) }
                }
            )
        }
    }
}

@Composable
fun DropdownSettingRow(
    label: String,
    selectedName: String,
    options: List<String>,
    onOptionSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = label,
            fontSize = 11.sp,
            color = PolishTextSecondary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PolishSurface)
                .border(1.dp, PolishBorder, RoundedCornerShape(12.dp))
                .clickable { expanded = true }
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedName,
                    fontSize = 13.sp,
                    color = PolishTextPrimary,
                    fontWeight = FontWeight.Medium
                )

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Expand",
                    tint = PolishTextSecondary
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(PolishSurface)
            ) {
                options.forEachIndexed { idx, opt ->
                    DropdownMenuItem(
                        text = { Text(text = opt, color = PolishTextPrimary, fontSize = 13.sp) },
                        onClick = {
                            onOptionSelected(idx)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

