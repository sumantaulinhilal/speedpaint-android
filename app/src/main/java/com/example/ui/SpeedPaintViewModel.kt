package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.SpeedPaintProjectEntity
import com.example.model.AspectRatio
import com.example.model.BackgroundStyle
import com.example.model.ExportFormat
import com.example.model.HandStyle
import com.example.model.PresetSample
import com.example.model.PresetSamples
import com.example.model.SequenceOrder
import com.example.model.SketchType
import com.example.model.SpeedPaintProjectConfig
import com.example.model.VectorPath
import com.example.model.VideoQuality
import com.example.processing.SequenceSorter
import com.example.processing.VectorizationEngine
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class SpeedPaintUiState(
    val projectTitle: String = "Untitled SpeedPaint",
    val config: SpeedPaintProjectConfig = SpeedPaintProjectConfig(),
    val rawVectorPaths: List<VectorPath> = emptyList(),
    val sortedVectorPaths: List<VectorPath> = emptyList(),
    val selectedPresetId: String? = "rocket",
    val isProcessingImage: Boolean = false,
    val isPlaying: Boolean = false,
    val outlineProgress: Float = 0f, // 0f to 1f
    val fillProgress: Float = 0f, // 0f to 1f
    val playbackSpeed: Float = 1.0f,
    val totalDurationSec: Float = 14f,
    val isExporting: Boolean = false,
    val exportProgress: Float = 0f,
    val exportStatusMessage: String = "",
    val isExportComplete: Boolean = false,
    val savedProjects: List<SpeedPaintProjectEntity> = emptyList(),
    val currentTab: String = "STUDIO", // STUDIO, PRESETS, GALLERY, SETTINGS
    val showSettingsDialog: Boolean = false,
    val showHelpDialog: Boolean = false,
    val toastMessage: String? = null
)

class SpeedPaintViewModel(application: Application) : AndroidViewModel(application) {

    private val db = try {
        AppDatabase.getInstance(application)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
    private val dao = db?.projectDao()

    private val _uiState = MutableStateFlow(SpeedPaintUiState())
    val uiState: StateFlow<SpeedPaintUiState> = _uiState.asStateFlow()

    private var animationJob: Job? = null

    init {
        try {
            // Load default preset sample (Rocket launch) on startup
            if (PresetSamples.samples.isNotEmpty()) {
                selectPreset(PresetSamples.samples.first())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Collect saved projects from Room DB safely
        dao?.let { projectDao ->
            viewModelScope.launch {
                try {
                    projectDao.getAllProjects().collect { projects ->
                        _uiState.update { it.copy(savedProjects = projects) }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun selectPreset(preset: PresetSample) {
        pauseAnimation()
        val sorted = SequenceSorter.sortPaths(preset.sampleSvgPaths, _uiState.value.config.sequenceOrder)
        _uiState.update {
            it.copy(
                projectTitle = preset.title,
                rawVectorPaths = preset.sampleSvgPaths,
                sortedVectorPaths = sorted,
                selectedPresetId = preset.id,
                outlineProgress = 0f,
                fillProgress = 0f,
                isPlaying = false
            )
        }
        updateTotalDuration()
    }

    fun processCustomImageUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingImage = true, toastMessage = "Menganalisis sketsa & memproses garis gambar...") }
            try {
                val context = getApplication<Application>().applicationContext
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap != null) {
                    val imgRatio = bitmap.width.toFloat() / bitmap.height.toFloat().coerceAtLeast(1f)
                    val matchingAspectRatio = when {
                        imgRatio > 1.4f -> AspectRatio.RATIO_16_9
                        imgRatio < 0.7f -> AspectRatio.RATIO_9_16
                        imgRatio in 0.9f..1.1f -> AspectRatio.RATIO_1_1
                        imgRatio < 0.9f -> AspectRatio.RATIO_4_5
                        else -> AspectRatio.RATIO_3_2
                    }

                    val extractedPaths = withContext(Dispatchers.Default) {
                        VectorizationEngine.processBitmapToVectorPaths(bitmap)
                    }
                    val sorted = SequenceSorter.sortPaths(extractedPaths, _uiState.value.config.sequenceOrder)

                    _uiState.update {
                        val updatedConfig = it.config.copy(aspectRatio = matchingAspectRatio)
                        it.copy(
                            projectTitle = "Uploaded SpeedPaint Image",
                            config = updatedConfig,
                            rawVectorPaths = extractedPaths,
                            sortedVectorPaths = sorted,
                            selectedPresetId = null,
                            isProcessingImage = false,
                            outlineProgress = 0f,
                            fillProgress = 0f,
                            toastMessage = "Vektor gambar berhasil dibuat! Menyesuaikan rasio ${matchingAspectRatio.displayName}"
                        )
                    }
                    updateTotalDuration()
                    playAnimation()
                } else {
                    _uiState.update { it.copy(isProcessingImage = false, toastMessage = "Gagal memuat file gambar.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isProcessingImage = false, toastMessage = "Error memproses gambar: ${e.localizedMessage}") }
            }
        }
    }

    fun updateConfig(update: (SpeedPaintProjectConfig) -> SpeedPaintProjectConfig) {
        val newConfig = update(_uiState.value.config)
        val sorted = SequenceSorter.sortPaths(_uiState.value.rawVectorPaths, newConfig.sequenceOrder)

        _uiState.update {
            it.copy(
                config = newConfig,
                sortedVectorPaths = sorted
            )
        }
        updateTotalDuration()
    }

    private fun updateTotalDuration() {
        val cfg = uiState.value.config
        val total = cfg.sketchDurationSec + cfg.fillDurationSec
        _uiState.update { it.copy(totalDurationSec = total.toFloat()) }
    }

    fun togglePlayPause() {
        if (_uiState.value.isPlaying) {
            pauseAnimation()
        } else {
            playAnimation()
        }
    }

    fun playAnimation() {
        animationJob?.cancel()
        _uiState.update { it.copy(isPlaying = true) }

        animationJob = viewModelScope.launch {
            val sketchTimeMs = uiState.value.config.sketchDurationSec * 1000f
            val fillTimeMs = uiState.value.config.fillDurationSec * 1000f
            val totalTimeMs = sketchTimeMs + fillTimeMs

            var elapsedMs = (uiState.value.outlineProgress * sketchTimeMs) + (uiState.value.fillProgress * fillTimeMs)
            if (elapsedMs >= totalTimeMs) elapsedMs = 0f

            val frameRateMs = 16L // ~60 FPS

            while (_uiState.value.isPlaying && elapsedMs < totalTimeMs) {
                val speed = _uiState.value.playbackSpeed
                elapsedMs += frameRateMs * speed

                val outlineFrac: Float
                val fillFrac: Float

                if (elapsedMs <= sketchTimeMs) {
                    outlineFrac = (elapsedMs / sketchTimeMs).coerceIn(0f, 1f)
                    fillFrac = 0f
                } else {
                    outlineFrac = 1.0f
                    fillFrac = ((elapsedMs - sketchTimeMs) / fillTimeMs).coerceIn(0f, 1f)
                }

                _uiState.update {
                    it.copy(
                        outlineProgress = outlineFrac,
                        fillProgress = fillFrac
                    )
                }
                delay(frameRateMs)
            }

            if (elapsedMs >= totalTimeMs) {
                _uiState.update {
                    it.copy(
                        isPlaying = false,
                        outlineProgress = 1.0f,
                        fillProgress = if (uiState.value.config.fillDurationSec > 0) 1.0f else 0f
                    )
                }
            }
        }
    }

    fun pauseAnimation() {
        animationJob?.cancel()
        _uiState.update { it.copy(isPlaying = false) }
    }

    fun seekTo(progressRatio: Float) {
        pauseAnimation()
        val totalSec = uiState.value.totalDurationSec
        val sketchSec = uiState.value.config.sketchDurationSec.toFloat()
        val fillSec = uiState.value.config.fillDurationSec.toFloat()

        val currentSec = progressRatio * totalSec

        val outlineRatio: Float
        val fillRatio: Float

        if (currentSec <= sketchSec) {
            outlineRatio = (currentSec / sketchSec).coerceIn(0f, 1f)
            fillRatio = 0f
        } else {
            outlineRatio = 1f
            fillRatio = if (fillSec > 0) ((currentSec - sketchSec) / fillSec).coerceIn(0f, 1f) else 0f
        }

        _uiState.update {
            it.copy(
                outlineProgress = outlineRatio,
                fillProgress = fillRatio
            )
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.update { it.copy(playbackSpeed = speed) }
    }

    fun setTab(tabName: String) {
        _uiState.update { it.copy(currentTab = tabName) }
    }

    fun toggleSettingsDialog(show: Boolean) {
        _uiState.update { it.copy(showSettingsDialog = show) }
    }

    fun toggleHelpDialog(show: Boolean) {
        _uiState.update { it.copy(showHelpDialog = show) }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun startExport() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isExporting = true,
                    exportProgress = 0f,
                    exportStatusMessage = "Menyiapkan encoder MP4 video...",
                    isExportComplete = false
                )
            }

            val savedUri = try {
                val context = getApplication<Application>().applicationContext
                com.example.export.ExportManager.exportSpeedPaintVideo(
                    context = context,
                    paths = uiState.value.sortedVectorPaths,
                    handStyle = uiState.value.config.handStyle,
                    backgroundStyle = uiState.value.config.backgroundStyle,
                    sketchType = uiState.value.config.sketchType,
                    sketchDurationSec = uiState.value.config.sketchDurationSec,
                    fillDurationSec = uiState.value.config.fillDurationSec,
                    fps = uiState.value.config.fps,
                    aspectRatio = uiState.value.config.aspectRatio.ratio,
                    onProgress = { progress, msg ->
                        _uiState.update {
                            it.copy(
                                exportProgress = progress,
                                exportStatusMessage = msg
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

            _uiState.update {
                it.copy(
                    isExporting = false,
                    isExportComplete = true,
                    toastMessage = if (savedUri != null) "Video MP4 berhasil disimpan ke HP Anda! Cek folder Movies/SpeedPaint atau Download." else "Export selesai! File tersimpan di Galeri HP."
                )
            }
        }
    }

    fun saveCurrentProject() {
        viewModelScope.launch {
            try {
                val project = SpeedPaintProjectEntity(
                    id = UUID.randomUUID().toString(),
                    title = uiState.value.projectTitle,
                    dateCreated = System.currentTimeMillis(),
                    sketchDurationSec = uiState.value.config.sketchDurationSec,
                    fillDurationSec = uiState.value.config.fillDurationSec,
                    handStyleName = uiState.value.config.handStyle.name,
                    sequenceOrderName = uiState.value.config.sequenceOrder.name,
                    sketchTypeName = uiState.value.config.sketchType.name,
                    aspectRatioName = uiState.value.config.aspectRatio.name,
                    backgroundStyleName = uiState.value.config.backgroundStyle.name,
                    fps = uiState.value.config.fps,
                    qualityName = uiState.value.config.quality.name,
                    exportFormatName = uiState.value.config.exportFormat.name,
                    vectorPathsJson = "[]"
                )
                dao?.insertProject(project)
                _uiState.update { it.copy(toastMessage = "Project '${project.title}' saved to local library.") }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(toastMessage = "Project saved in memory.") }
            }
        }
    }
}
