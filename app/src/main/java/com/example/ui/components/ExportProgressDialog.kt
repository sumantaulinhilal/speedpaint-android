package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.ExportFormat
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishSurface
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary

@Composable
fun ExportProgressDialog(
    isExporting: Boolean,
    progress: Float,
    statusMessage: String,
    exportFormat: ExportFormat,
    onDismiss: () -> Unit
) {
    if (isExporting) {
        Dialog(onDismissRequest = {}) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PolishSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("export_progress_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Rendering ${exportFormat.displayName}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishTextPrimary
                    )

                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = PolishPrimary,
                        trackColor = PolishBorder
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = statusMessage,
                            fontSize = 11.sp,
                            color = PolishTextSecondary,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = "${(progress * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishPrimary
                        )
                    }

                    Text(
                        text = "Rendering vector frames locally on Android GPU...",
                        fontSize = 10.sp,
                        color = PolishTextSecondary
                    )
                }
            }
        }
    }
}

