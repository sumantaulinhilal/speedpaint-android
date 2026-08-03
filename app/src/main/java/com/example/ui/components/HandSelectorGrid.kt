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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HandStyle
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishPrimaryContainer
import com.example.ui.theme.PolishSurface
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary

@Composable
fun HandSelectorGrid(
    selectedHand: HandStyle,
    onSelectHand: (HandStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "HAND STYLE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PolishTextSecondary,
                letterSpacing = 1.sp
            )

            Text(
                text = selectedHand.displayName,
                fontSize = 11.sp,
                color = PolishPrimary,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(HandStyle.values()) { hand ->
                val isSelected = hand == selectedHand

                Card(
                    modifier = Modifier
                        .width(100.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) PolishPrimary else PolishBorder,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onSelectHand(hand) }
                        .testTag("hand_card_${hand.name}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) PolishPrimaryContainer else PolishSurface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PolishPrimary else PolishBorder.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            val icon = when (hand) {
                                HandStyle.NO_HAND -> Icons.Default.DoNotDisturb
                                HandStyle.MALE_PENCIL -> Icons.Default.Create
                                HandStyle.FEMALE_PENCIL -> Icons.Default.Create
                                HandStyle.BLACK_MARKER -> Icons.Default.Brush
                                HandStyle.WHITE_MARKER -> Icons.Default.Brush
                                HandStyle.STYLUS -> Icons.Default.Gesture
                                HandStyle.CARTOON_HAND -> Icons.Default.Palette
                                HandStyle.CUSTOM_PNG -> Icons.Default.Brush
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = hand.displayName,
                                tint = if (isSelected) PolishPrimaryContainer else PolishTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = hand.displayName,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) PolishPrimary else PolishTextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

