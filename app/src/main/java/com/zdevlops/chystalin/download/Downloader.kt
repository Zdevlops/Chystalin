package com.zdevlops.chystalin.download

import android.content.Context
import android.webkit.MimeTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

object Downloader {

    suspend fun download(context: Context, rawUrl: String, onProgress: (Int) -> Unit): File? =
        withContext(Dispatchers.IO) {
            val url = runCatching { URL(rawUrl) }.getOrNull() ?: return@withContext null
            var connection: HttpURLConnection? = null
            val dir = File(context.filesDir, "music").apply { mkdirs() }

            try {
                connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 20_000
                    readTimeout = 30_000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "Chystalin/1.0")
                }
                if (connection.responseCode !in 200..299) return@withContext null

                val type = connection.contentType?.substringBefore(';')
                val extension = extensionFor(type, url.path)
                val target = File(dir, "${UUID.randomUUID()}.$extension")
                val total = connection.contentLengthLong

                connection.inputStream.use { input ->
                    target.outputStream().use { output ->
                        val buffer = ByteArray(64 * 1024)
                        var written = 0L
                        var lastPercent = -1
                        while (true) {
                            val read = input.read(buffer)
                            if (read <= 0) break
                            output.write(buffer, 0, read)
                            written += read
                            if (total > 0) {
                                val percent = ((written * 100) / total).toInt()
                                if (percent != lastPercent) {
                                    lastPercent = percent
                                    onProgress(percent)
                                }
                            }
                        }
                    }
                }

                if (target.length() <= 0) {
                    target.delete()
                    null
                } else {
                    onProgress(100)
                    target
                }
            } catch (e: Exception) {
                null
            } finally {
                connection?.disconnect()
            }
        }

    private fun extensionFor(mime: String?, path: String): String {
        val fromMime = mime?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) }
        if (!fromMime.isNullOrBlank()) return fromMime
        val fromPath = path.substringAfterLast('.', "").lowercase()
        return if (fromPath in setOf("mp3", "m4a", "aac", "ogg", "opus", "wav", "flac")) fromPath else "mp3"
    }
}
