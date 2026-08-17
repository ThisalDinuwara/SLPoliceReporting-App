package com.slpolice.reporting.util

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.slpolice.reporting.data.EvidenceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * A media file copied into the app's private evidence vault.
 *
 * [capturedAt] is the moment the camera actually recorded the file, read from the image's EXIF
 * header or the video's metadata. It is null when the file carries no such stamp — typically a
 * screenshot, a download, or an image whose metadata was stripped by a messaging app.
 */
data class StoredMedia(
    val file: File,
    val type: EvidenceType,
    val sizeBytes: Long,
    val integrityHash: String,
    val capturedAt: Long?,
    /** Coordinates the camera wrote into the file, when the phone had location switched on. */
    val capturedLatitude: Double? = null,
    val capturedLongitude: Double? = null
) {
    val hasCaptureLocation: Boolean get() = capturedLatitude != null && capturedLongitude != null

    val captureVerified: Boolean get() = capturedAt != null

    /** True when the file is provably older than the reporting window. */
    val tooOld: Boolean
        get() = capturedAt != null && !Validators.withinReportingWindow(capturedAt)
}

/**
 * Every attachment is copied into internal app storage before it is linked to a report. Files
 * never sit in shared storage, so no other app on the phone can read or alter evidence while it
 * waits to be handed to the Police Department.
 */
object MediaStorage {

    const val MAX_ATTACHMENTS = 4
    const val MAX_IMAGE_BYTES = 8L * 1024 * 1024      // 8 MB per photo
    const val MAX_VIDEO_BYTES = 40L * 1024 * 1024     // 40 MB per clip

    fun evidenceDir(context: Context): File =
        File(context.filesDir, "evidence").apply { if (!exists()) mkdirs() }

    fun newFile(context: Context, extension: String): File =
        File(evidenceDir(context), "EV_${System.currentTimeMillis()}.$extension")

    fun uriFor(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    /** Wraps a file the app's own camera just produced, so its capture time is beyond doubt. */
    fun describeCapture(file: File, type: EvidenceType): StoredMedia {
        val position = if (type == EvidenceType.IMAGE) readExifLocation(file) else null
        return StoredMedia(
            file = file,
            type = type,
            sizeBytes = file.length(),
            integrityHash = Security.fileDigest(file),
            capturedAt = System.currentTimeMillis(),
            capturedLatitude = position?.first,
            capturedLongitude = position?.second
        )
    }

    /** Copies a gallery or document selection into the vault. Returns null if it cannot be read. */
    suspend fun importFrom(context: Context, uri: Uri): StoredMedia? = withContext(Dispatchers.IO) {
        val mime = context.contentResolver.getType(uri).orEmpty()
        val type = if (mime.startsWith("video")) EvidenceType.VIDEO else EvidenceType.IMAGE
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)
            ?: if (type == EvidenceType.VIDEO) "mp4" else "jpg"
        val target = newFile(context, extension)
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            } ?: return@withContext null

            StoredMedia(
                file = target,
                type = type,
                sizeBytes = target.length(),
                integrityHash = Security.fileDigest(target),
                capturedAt = readCaptureTime(target, type),
                capturedLatitude = if (type == EvidenceType.IMAGE) readExifLocation(target)?.first else null,
                capturedLongitude = if (type == EvidenceType.IMAGE) readExifLocation(target)?.second else null
            )
        }.getOrElse {
            target.delete()
            null
        }
    }

    /**
     * Reads the original capture timestamp out of the file itself. This is what stops someone
     * dredging up footage from months ago and filing it as a fresh incident: the phone wrote the
     * date into the file when the shutter fired, and copying the file preserves it.
     */
    private fun readCaptureTime(file: File, type: EvidenceType): Long? = runCatching {
        if (type == EvidenceType.IMAGE) readExifTime(file) else readVideoTime(file)
    }.getOrNull()

    /**
     * Reads the GPS tags a camera writes alongside the image. Where present these are far harder
     * to fake than a typed address, so the officer can see where the shutter actually fired.
     */
    private fun readExifLocation(file: File): Pair<Double, Double>? = runCatching {
        val exif = ExifInterface(file.absolutePath)
        val coordinates = FloatArray(2)
        if (exif.getLatLong(coordinates)) {
            coordinates[0].toDouble() to coordinates[1].toDouble()
        } else {
            null
        }
    }.getOrNull()

    private fun readExifTime(file: File): Long? {
        val exif = ExifInterface(file.absolutePath)
        val stamp = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
            ?: exif.getAttribute(ExifInterface.TAG_DATETIME)
            ?: return null
        val parser = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.ENGLISH)
        return runCatching { parser.parse(stamp)?.time }.getOrNull()
    }

    private fun readVideoTime(file: File): Long? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            val stamp = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE)
                ?: return null
            val parser = SimpleDateFormat("yyyyMMdd'T'HHmmss.SSS'Z'", Locale.ENGLISH).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            runCatching { parser.parse(stamp)?.time }.getOrNull()
                ?: runCatching {
                    SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.ENGLISH).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }.parse(stamp)?.time
                }.getOrNull()
        } catch (error: Exception) {
            null
        } finally {
            runCatching { retriever.release() }
        }
    }

    fun sizeLimitFor(type: EvidenceType): Long =
        if (type == EvidenceType.VIDEO) MAX_VIDEO_BYTES else MAX_IMAGE_BYTES

    fun delete(file: File) {
        runCatching { if (file.exists()) file.delete() }
    }
}
