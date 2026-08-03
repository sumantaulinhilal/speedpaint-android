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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Schema
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PresetSample
import com.example.model.PresetSamples
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishOnPrimary
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishPrimaryContainer
import com.example.ui.theme.PolishSurface
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary

@Composable
fun PresetGallery(
    selectedPresetId: String?,
    onSelectPreset: (PresetSample) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp, start = 2.dp, end = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Presets",
                    tint = PolishPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SAMPLE PRESETS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PolishTextSecondary,
                    letterSpacing = 1.sp
                )
            }

            Text(
                text = "${PresetSamples.samples.size} Presets",
                fontSize = 11.sp,
                color = PolishPrimary,
                fontWeight = FontWeight.Medium
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            items(PresetSamples.samples) { preset ->
                val isSelected = preset.id == selectedPresetId

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) PolishPrimary else PolishBorder,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onSelectPreset(preset) }
                        .testTag("preset_card_${preset.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) PolishPrimaryContainer else PolishSurface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PolishPrimary else PolishBorder.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                val icon = when (preset.iconName) {
                                    "rocket" -> Icons.Default.RocketLaunch
                                    "schema" -> Icons.Default.Schema
                                    "format_quote" -> Icons.Default.FormatQuote
                                    "person" -> Icons.Default.Person
                                    "pets" -> Icons.Default.Pets
                                    "location_city" -> Icons.Default.LocationCity
                                    else -> Icons.Default.AutoAwesome
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = preset.title,
                                    tint = if (isSelected) PolishOnPrimary else PolishPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column {
                                Text(
                                    text = preset.category.uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PolishPrimary
                                )
                                Text(
                                    text = preset.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = PolishTextPrimary,
                                    maxLines = 1
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = preset.description,
                            fontSize = 10.sp,
                            color = PolishTextSecondary,
                            maxLines = 2,
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }
    }
}

