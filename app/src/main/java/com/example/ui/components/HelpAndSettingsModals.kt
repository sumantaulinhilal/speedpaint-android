package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishOnPrimary
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishSurface
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary

@Composable
fun HelpDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = PolishSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("help_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "SpeedPaint Tier 1 Guide",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = PolishTextPrimary
                )

                Text(
                    text = "SpeedPaint converts images into realistic whiteboard drawing animations with virtual hands.",
                    fontSize = 12.sp,
                    color = PolishTextSecondary
                )

                HelpSection(
                    title = "1. Image Processing Pipeline",
                    body = "Uploaded images undergo Grayscale conversion -> Sobel Edge Detection -> 8-Neighbor Contour Tracing -> Ramer-Douglas-Peucker path simplification."
                )

                HelpSection(
                    title = "2. Realism & Physics",
                    body = "Virtual hand tip aligns precisely with path coordinates. Movement accelerates on straight lines and slows down smoothly on curves."
                )

                HelpSection(
                    title = "3. Sequence Order & Text",
                    body = "Choose spatial orders (Left-to-Right, Spiral, Center-Out) or Text-First/Text-Last to prioritize annotations."
                )

                HelpSection(
                    title = "4. Export Options",
                    body = "Export rendered speedpaints directly into MP4 (H.264), Animated GIF, or PNG Frame Sequence."
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(text = "Got It", color = PolishOnPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun HelpSection(title: String, body: String) {
    Column {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = PolishPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = body,
            fontSize = 11.sp,
            color = PolishTextSecondary,
            lineHeight = 15.sp
        )
    }
}

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = PolishSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("settings_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Application Preferences",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = PolishTextPrimary
                )

                Text(
                    text = "Preferences are automatically saved locally.",
                    fontSize = 12.sp,
                    color = PolishTextSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Engine Version", fontSize = 12.sp, color = PolishTextPrimary)
                    Text(text = "v1.0.4 (Compose 60FPS)", fontSize = 12.sp, color = PolishPrimary, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Hardware Acceleration", fontSize = 12.sp, color = PolishTextPrimary)
                    Text(text = "Enabled (Android Canvas)", fontSize = 12.sp, color = PolishPrimary, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(text = "Close", color = PolishOnPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

