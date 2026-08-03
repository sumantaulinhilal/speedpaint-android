package com.example.export

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import androidx.compose.ui.graphics.asImageBitmap
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas as ComposeCanvas
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.example.animation.SpeedPaintRenderer.renderSpeedPaintFrame
import com.example.model.BackgroundStyle
import com.example.model.HandStyle
import com.example.model.SketchType
import com.example.model.VectorPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

object ExportManager {

    suspend fun exportSpeedPaintVideo(
        context: Context,
        paths: List<VectorPath>,
        handStyle: HandStyle,
        backgroundStyle: BackgroundStyle,
        sketchType: SketchType,
        sketchDurationSec: Int,
        fillDurationSec: Int,
        fps: Int = 30,
        aspectRatio: Float = 16f / 9f,
        onProgress: (Float, String) -> Unit
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            onProgress(0.05f, "Preparing frame dimensions and encoder...")

            val videoWidth = 720
            val videoHeight = ((videoWidth / aspectRatio.coerceAtLeast(0.5f)).toInt() / 2) * 2
            val totalSec = (sketchDurationSec + fillDurationSec).coerceAtLeast(1)
            val totalFrames = totalSec * fps
            val sketchFrames = sketchDurationSec * fps

            val tempFile = File(context.cacheDir, "speedpaint_${System.currentTimeMillis()}.mp4")
            if (tempFile.exists()) tempFile.delete()

            var muxer: MediaMuxer? = null
            var encoder: MediaCodec? = null
            var trackIndex = -1
            var muxerStarted = false

            val mimeType = MediaFormat.MIMETYPE_VIDEO_AVC
            val format = MediaFormat.createVideoFormat(mimeType, videoWidth, videoHeight).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, 3_500_000) // 3.5 Mbps
                setInteger(MediaFormat.KEY_FRAME_RATE, fps)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }

            encoder = MediaCodec.createEncoderByType(mimeType)
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            val inputSurface = encoder.createInputSurface()
            encoder.start()

            muxer = MediaMuxer(tempFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            val canvasDrawScope = CanvasDrawScope()
            val frameBitmap = Bitmap.createBitmap(videoWidth, videoHeight, Bitmap.Config.ARGB_8888)
            val androidCanvas = Canvas(frameBitmap)

            val decodeOptions = BitmapFactory.Options().apply { inScaled = false }
            val handMarkerBitmap = try {
                BitmapFactory.decodeResource(context.resources, com.example.R.drawable.real_hand_marker, decodeOptions)?.asImageBitmap()
            } catch (e: Throwable) { null }

            val handPencilBitmap = try {
                BitmapFactory.decodeResource(context.resources, com.example.R.drawable.real_hand_pencil, decodeOptions)?.asImageBitmap()
            } catch (e: Throwable) { null }

            val bufferInfo = MediaCodec.BufferInfo()

            for (frame in 0 until totalFrames) {
                val progressRatio = frame.toFloat() / totalFrames.toFloat()
                onProgress(
                    0.1f + (progressRatio * 0.75f),
                    "Encoding MP4 frame ${frame + 1}/$totalFrames (${(progressRatio * 100).toInt()}%)..."
                )

                val outlineFrac: Float
                val fillFrac: Float

                if (frame <= sketchFrames) {
                    outlineFrac = (frame.toFloat() / sketchFrames.coerceAtLeast(1)).coerceIn(0f, 1f)
                    fillFrac = 0f
                } else {
                    outlineFrac = 1.0f
                    val fillF = (frame - sketchFrames).toFloat() / ((totalFrames - sketchFrames).coerceAtLeast(1))
                    fillFrac = fillF.coerceIn(0f, 1f)
                }

                // Render frame onto bitmap
                androidCanvas.drawColor(android.graphics.Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

                canvasDrawScope.draw(
                    density = Density(1f),
                    layoutDirection = LayoutDirection.Ltr,
                    canvas = ComposeCanvas(androidCanvas),
                    size = Size(videoWidth.toFloat(), videoHeight.toFloat())
                ) {
                    renderSpeedPaintFrame(
                        paths = paths,
                        progress = outlineFrac,
                        fillProgress = fillFrac,
                        handStyle = handStyle,
                        backgroundStyle = backgroundStyle,
                        sketchType = sketchType,
                        handMarkerBitmap = handMarkerBitmap,
                        handPencilBitmap = handPencilBitmap
                    )
                }

                // Render bitmap onto Encoder Surface
                val surfaceCanvas = inputSurface.lockCanvas(null)
                surfaceCanvas.drawBitmap(frameBitmap, 0f, 0f, null)
                inputSurface.unlockCanvasAndPost(surfaceCanvas)

                // Drain encoder outputs
                while (true) {
                    val outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, 10_000L)
                    if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        break
                    } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        if (muxerStarted) throw RuntimeException("Format changed twice")
                        val newFormat = encoder.outputFormat
                        trackIndex = muxer.addTrack(newFormat)
                        muxer.start()
                        muxerStarted = true
                    } else if (outputBufferIndex >= 0) {
                        val encodedData = encoder.getOutputBuffer(outputBufferIndex) ?: continue
                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                            bufferInfo.size = 0
                        }

                        if (bufferInfo.size != 0 && muxerStarted) {
                            encodedData.position(bufferInfo.offset)
                            encodedData.limit(bufferInfo.offset + bufferInfo.size)
                            val presentationTimeUs = (frame * 1_000_000L) / fps
                            bufferInfo.presentationTimeUs = presentationTimeUs
                            muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                        }

                        encoder.releaseOutputBuffer(outputBufferIndex, false)
                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            break
                        }
                    }
                }
            }

            // Signal End of Stream
            encoder.signalEndOfInputStream()

            // Drain remaining frames
            var draining = true
            var drainCount = 0
            while (draining && drainCount < 30) {
                drainCount++
                val outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, 20_000L)
                if (outputBufferIndex >= 0) {
                    val encodedData = encoder.getOutputBuffer(outputBufferIndex)
                    if (encodedData != null && bufferInfo.size != 0 && muxerStarted) {
                        encodedData.position(bufferInfo.offset)
                        encodedData.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                    }
                    encoder.releaseOutputBuffer(outputBufferIndex, false)
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        draining = false
                    }
                } else if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    draining = false
                }
            }

            encoder.stop()
            encoder.release()

            if (muxerStarted) {
                muxer.stop()
            }
            muxer.release()

            onProgress(0.92f, "Saving MP4 video to device Gallery / Downloads...")

            // Copy generated MP4 file to public MediaStore / Downloads
            val savedUri = saveVideoToPublicStorage(context, tempFile)
            onProgress(1.0f, "Export complete!")
            return@withContext savedUri
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: Save single high-res preview image / final frame if MP4 hardware encoder failed
            onProgress(0.90f, "Saving SpeedPaint drawing to device Downloads...")
            val fallbackUri = saveFallbackDrawingImage(context, paths, backgroundStyle, sketchType, aspectRatio)
            return@withContext fallbackUri
        }
    }

    private fun saveVideoToPublicStorage(context: Context, videoFile: File): Uri? {
        val filename = "SpeedPaint_${System.currentTimeMillis()}.mp4"

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, filename)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/SpeedPaint")
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)

            if (uri != null) {
                resolver.openOutputStream(uri)?.use { out ->
                    videoFile.inputStream().use { input -> input.copyTo(out) }
                }
            }
            uri
        } else {
            val targetDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            val speedpaintDir = File(targetDir, "SpeedPaint").apply { mkdirs() }
            val destFile = File(speedpaintDir, filename)
            videoFile.copyTo(destFile, overwrite = true)

            MediaScannerConnection.scanFile(
                context,
                arrayOf(destFile.absolutePath),
                arrayOf("video/mp4"),
                null
            )
            Uri.fromFile(destFile)
        }
    }

    private fun saveFallbackDrawingImage(
        context: Context,
        paths: List<VectorPath>,
        backgroundStyle: BackgroundStyle,
        sketchType: SketchType,
        aspectRatio: Float
    ): Uri? {
        val width = 1080
        val height = (width / aspectRatio.coerceAtLeast(0.5f)).toInt()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val canvasDrawScope = CanvasDrawScope()

        canvasDrawScope.draw(
            density = Density(1f),
            layoutDirection = LayoutDirection.Ltr,
            canvas = ComposeCanvas(canvas),
            size = Size(width.toFloat(), height.toFloat())
        ) {
            renderSpeedPaintFrame(
                paths = paths,
                progress = 1.0f,
                fillProgress = 1.0f,
                handStyle = HandStyle.NO_HAND,
                backgroundStyle = backgroundStyle,
                sketchType = sketchType
            )
        }

        val filename = "SpeedPaint_Artwork_${System.currentTimeMillis()}.png"
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/SpeedPaint")
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
            }
            uri
        } else {
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val speedpaintDir = File(picturesDir, "SpeedPaint").apply { mkdirs() }
            val file = File(speedpaintDir, filename)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("image/png"), null)
            Uri.fromFile(file)
        }
    }
}
