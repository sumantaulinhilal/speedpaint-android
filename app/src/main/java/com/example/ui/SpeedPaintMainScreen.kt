package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CanvasPreviewPlayer
import com.example.ui.components.ExportProgressDialog
import com.example.ui.components.HandSelectorGrid
import com.example.ui.components.HelpDialog
import com.example.ui.components.ImageUploadArea
import com.example.ui.components.PresetGallery
import com.example.ui.components.SettingsDialog
import com.example.ui.components.SettingsPanel
import com.example.ui.components.SpeedPaintHeader
import com.example.ui.theme.PolishBackground
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishOnPrimary
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishPrimaryContainer
import com.example.ui.theme.PolishSurface
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary

@Composable
fun SpeedPaintMainScreen(
    viewModel: SpeedPaintViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    Scaffold(
        topBar = {
            SpeedPaintHeader(
                onOpenSettings = { viewModel.toggleSettingsDialog(true) },
                onOpenHelp = { viewModel.toggleHelpDialog(true) }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = PolishSurface,
                modifier = Modifier.border(width = 1.dp, color = PolishBorder),
                tonalElevation = 6.dp
            ) {
                listOf(
                    Triple("STUDIO", "Studio", Icons.Default.Videocam),
                    Triple("PRESETS", "Presets", Icons.Default.AutoAwesome),
                    Triple("GALLERY", "Library", Icons.Default.Bookmark),
                    Triple("SETTINGS", "Controls", Icons.Default.Settings)
                ).forEach { (key, label, icon) ->
                    val isSelected = uiState.currentTab == key
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setTab(key) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) PolishPrimary else PolishTextSecondary
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) PolishPrimary else PolishTextSecondary
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = PolishPrimaryContainer
                        ),
                        modifier = Modifier.testTag("tab_$key")
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = PolishBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Always visible: Realtime Canvas Preview Player
                CanvasPreviewPlayer(
                    paths = uiState.sortedVectorPaths,
                    outlineProgress = uiState.outlineProgress,
                    fillProgress = uiState.fillProgress,
                    isPlaying = uiState.isPlaying,
                    handStyle = uiState.config.handStyle,
                    backgroundStyle = uiState.config.backgroundStyle,
                    sketchType = uiState.config.sketchType,
                    aspectRatioValue = uiState.config.aspectRatio.ratio,
                    playbackSpeed = uiState.playbackSpeed,
                    totalDurationSec = uiState.totalDurationSec,
                    onTogglePlayPause = { viewModel.togglePlayPause() },
                    onSeek = { ratio -> viewModel.seekTo(ratio) },
                    onChangeSpeed = { speed -> viewModel.setPlaybackSpeed(speed) }
                )

                // Export & Save Action Row (Matching Professional Polish design buttons)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.startExport() },
                        colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                        shape = CircleShape,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("export_mp4_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Export",
                            tint = PolishOnPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Export ${uiState.config.exportFormat.displayName}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishOnPrimary
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.saveCurrentProject() },
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PolishTextPrimary),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("save_project_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save",
                            tint = PolishPrimary
                        )
                    }
                }

                // 2. Tab-dependent Content Panels
                when (uiState.currentTab) {
                    "STUDIO" -> {
                        // Image Upload Area
                        ImageUploadArea(
                            isProcessing = uiState.isProcessingImage,
                            onImageSelected = { uri -> viewModel.processCustomImageUri(uri) }
                        )

                        // Hand Selection Row
                        HandSelectorGrid(
                            selectedHand = uiState.config.handStyle,
                            onSelectHand = { hand -> viewModel.updateConfig { it.copy(handStyle = hand) } }
                        )

                        // Preset Quick Picker
                        PresetGallery(
                            selectedPresetId = uiState.selectedPresetId,
                            onSelectPreset = { preset -> viewModel.selectPreset(preset) }
                        )
                    }

                    "PRESETS" -> {
                        PresetGallery(
                            selectedPresetId = uiState.selectedPresetId,
                            onSelectPreset = { preset -> viewModel.selectPreset(preset) }
                        )
                    }

                    "GALLERY" -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "SAVED LIBRARY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PolishTextSecondary,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(start = 2.dp)
                            )

                            if (uiState.savedProjects.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .background(PolishSurface, RoundedCornerShape(16.dp))
                                        .border(1.dp, PolishBorder, RoundedCornerShape(16.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No saved projects yet. Tap 'Save' above to add your creation!",
                                        fontSize = 12.sp,
                                        color = PolishTextSecondary
                                    )
                                }
                            } else {
                                uiState.savedProjects.forEach { proj ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(PolishSurface, RoundedCornerShape(16.dp))
                                            .border(1.dp, PolishBorder, RoundedCornerShape(16.dp))
                                            .padding(14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = proj.title,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PolishTextPrimary
                                                )
                                                Text(
                                                    text = "${proj.sketchDurationSec}s sketch | ${proj.handStyleName} | ${proj.fps} FPS",
                                                    fontSize = 11.sp,
                                                    color = PolishTextSecondary
                                                )
                                            }

                                            Text(
                                                text = proj.qualityName,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PolishPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "SETTINGS" -> {
                        SettingsPanel(
                            config = uiState.config,
                            onConfigChange = { update -> viewModel.updateConfig(update) }
                        )
                    }
                }
            }

            // Export Progress Dialog
            ExportProgressDialog(
                isExporting = uiState.isExporting,
                progress = uiState.exportProgress,
                statusMessage = uiState.exportStatusMessage,
                exportFormat = uiState.config.exportFormat,
                onDismiss = {}
            )

            // Modals
            if (uiState.showHelpDialog) {
                HelpDialog(onDismiss = { viewModel.toggleHelpDialog(false) })
            }

            if (uiState.showSettingsDialog) {
                SettingsDialog(onDismiss = { viewModel.toggleSettingsDialog(false) })
            }
        }
    }
}

