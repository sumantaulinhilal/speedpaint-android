package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishOnPrimary
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishPrimaryContainer
import com.example.ui.theme.PolishSurface
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary

@Composable
fun ImageUploadArea(
    isProcessing: Boolean,
    onImageSelected: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PolishSurface)
            .border(
                width = 1.dp,
                color = PolishBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { launcher.launch("image/*") }
            .padding(16.dp)
            .testTag("upload_image_area"),
        contentAlignment = Alignment.Center
    ) {
        if (isProcessing) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = PolishPrimary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Detecting Edges & Generating Vector Paths...",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = PolishTextPrimary
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(PolishPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "Upload",
                            tint = PolishPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Drop image or tap to upload",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = PolishTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "PNG, JPG, WEBP • MAX 30MB",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishTextSecondary,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Button(
                    onClick = { launcher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                    shape = CircleShape,
                    modifier = Modifier.testTag("choose_file_button")
                ) {
                    Text(
                        text = "Choose File",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishOnPrimary
                    )
                }
            }
        }
    }
}

