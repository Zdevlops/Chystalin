package com.zdevlops.chystalin.data

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.util.UUID

object ImportHelper {

    fun importUri(context: Context, uri: Uri): Track? {
        val name = displayName(context, uri) ?: "audio_${System.currentTimeMillis()}"
        val dir = File(context.filesDir, "music").apply { mkdirs() }
        val safeName = name.replace(Regex("[^A-Za-z0-9._ -]"), "_")
        val target = File(dir, "${UUID.randomUUID()}_$safeName")

        val copied = runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
            target.exists() && target.length() > 0
        }.getOrDefault(false)

        if (!copied) {
            target.delete()
            return null
        }

        return buildTrack(target, name)
    }

    fun buildTrack(file: File, fallbackTitle: String): Track {
        val retriever = MediaMetadataRetriever()
        var title = fallbackTitle.substringBeforeLast('.')
        var artist = "Artis tidak diketahui"
        var duration = 0L
        runCatching {
            retriever.setDataSource(file.absolutePath)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)?.let { title = it }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)?.let { artist = it }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.let { duration = it.toLongOrNull() ?: 0L }
        }
        runCatching { retriever.release() }
        return Track(
            id = UUID.randomUUID().toString(),
            title = title,
            artist = artist,
            filePath = file.absolutePath,
            durationMs = duration,
            addedAt = System.currentTimeMillis()
        )
    }

    fun displayName(context: Context, uri: Uri): String? {
        runCatching {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0 && cursor.moveToFirst()) return cursor.getString(idx)
            }
        }
        return uri.lastPathSegment
    }
}
