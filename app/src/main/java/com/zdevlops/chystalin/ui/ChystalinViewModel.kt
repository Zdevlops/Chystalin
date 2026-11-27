package com.zdevlops.chystalin.ui

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.zdevlops.chystalin.data.ImportHelper
import com.zdevlops.chystalin.data.LibraryRepository
import com.zdevlops.chystalin.data.Playlist
import com.zdevlops.chystalin.data.RotationMode
import com.zdevlops.chystalin.data.Track
import com.zdevlops.chystalin.download.Downloader
import com.zdevlops.chystalin.playback.PlayerHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class ChystalinViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = LibraryRepository(app)
    private val player = PlayerHolder.player
        ?: throw IllegalStateException("Pemutar belum siap")

    var tracks by mutableStateOf<List<Track>>(emptyList())
        private set
    var playlists by mutableStateOf<List<Playlist>>(emptyList())
        private set
    var currentTrackId by mutableStateOf<String?>(null)
        private set
    var isPlaying by mutableStateOf(false)
        private set
    var positionMs by mutableStateOf(0L)
        private set
    var durationMs by mutableStateOf(0L)
        private set
    var rotation by mutableStateOf(RotationMode.REPEAT_ALL)
        private set
    var downloadProgress by mutableStateOf<Int?>(null)
        private set
    var message by mutableStateOf<String?>(null)
        private set

    val currentTrack: Track?
        get() = tracks.firstOrNull { it.id == currentTrackId }

    init {
        tracks = repo.loadTracks()
        playlists = repo.loadPlaylists()
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                currentTrackId = mediaItem?.mediaId
                durationMs = player.duration.coerceAtLeast(0L)
                positionMs = 0L
            }

            override fun onPlaybackStateChanged(state: Int) {
                durationMs = player.duration.coerceAtLeast(0L)
            }
        })
        viewModelScope.launch {
            while (true) {
                if (player.isPlaying) {
                    positionMs = player.currentPosition.coerceAtLeast(0L)
                    val d = player.duration
                    if (d > 0) durationMs = d
                }
                delay(400)
            }
        }
    }

    fun clearMessage() {
        message = null
    }

    fun playTracks(list: List<Track>, startIndex: Int = 0, playlistId: String? = null) {
        if (list.isEmpty()) return
        val items = list.map { t ->
            MediaItem.Builder()
                .setMediaId(t.id)
                .setUri(Uri.fromFile(java.io.File(t.filePath)))
                .build()
        }
        player.setMediaItems(items, startIndex.coerceIn(0, items.lastIndex), 0L)
        player.prepare()
        player.play()
        currentTrackId = list[startIndex.coerceIn(0, list.lastIndex)].id
        if (playlistId != null) {
            playlists.firstOrNull { it.id == playlistId }?.let { setRotation(it.rotation) }
        }
    }

    fun playPlaylist(playlist: Playlist) {
        val list = playlist.trackIds.mapNotNull { id -> tracks.firstOrNull { it.id == id } }
        if (list.isEmpty()) {
            message = "Playlist masih kosong"
            return
        }
        playTracks(list, 0, playlist.id)
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun next() {
        player.seekToNextMediaItem()
    }

    fun previous() {
        player.seekToPreviousMediaItem()
    }

    fun seekTo(ms: Long) {
        player.seekTo(ms)
        positionMs = ms
    }

    fun cycleRotation() {
        val next = when (rotation) {
            RotationMode.OFF -> RotationMode.REPEAT_ALL
            RotationMode.REPEAT_ALL -> RotationMode.REPEAT_ONE
            RotationMode.REPEAT_ONE -> RotationMode.SHUFFLE
            RotationMode.SHUFFLE -> RotationMode.OFF
        }
        setRotation(next)
    }

    private fun setRotation(mode: RotationMode) {
        rotation = mode
        player.repeatMode = when (mode) {
            RotationMode.REPEAT_ONE -> Player.REPEAT_MODE_ONE
            RotationMode.OFF -> Player.REPEAT_MODE_OFF
            else -> Player.REPEAT_MODE_ALL
        }
        player.shuffleModeEnabled = mode == RotationMode.SHUFFLE
    }

    fun setPlaylistRotation(playlistId: String, mode: RotationMode) {
        playlists = playlists.map { if (it.id == playlistId) it.copy(rotation = mode) else it }
        repo.savePlaylists(playlists)
        setRotation(mode)
    }

    fun importUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            val imported = withContext(Dispatchers.IO) {
                uris.mapNotNull { uri -> ImportHelper.importUri(getApplication(), uri) }
            }
            if (imported.isEmpty()) {
                message = "Tidak ada berkas yang berhasil diimpor"
                return@launch
            }
            tracks = tracks + imported
            repo.saveTracks(tracks)
            message = "${imported.size} lagu ditambahkan"
        }
    }

    fun downloadFromUrl(url: String) {
        if (url.isBlank()) {
            message = "Tautan kosong"
            return
        }
        if (downloadProgress != null) return
        downloadProgress = 0
        viewModelScope.launch {
            val file = Downloader.download(getApplication(), url) { p -> downloadProgress = p }
            downloadProgress = null
            if (file == null) {
                message = "Gagal mengunduh. Cek tautan langsung ke berkas audio."
                return@launch
            }
            val track = withContext(Dispatchers.IO) { ImportHelper.buildTrack(file, file.name) }
            tracks = tracks + track
            repo.saveTracks(tracks)
            message = "Selesai diunduh: ${track.title}"
        }
    }

    fun createPlaylist(name: String) {
        val playlist = Playlist(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "Playlist Baru" },
            trackIds = emptyList(),
            rotation = RotationMode.REPEAT_ALL
        )
        playlists = playlists + playlist
        repo.savePlaylists(playlists)
        message = "Playlist dibuat"
    }

    fun deletePlaylist(id: String) {
        playlists = playlists.filterNot { it.id == id }
        repo.savePlaylists(playlists)
    }

    fun addToPlaylist(playlistId: String, trackId: String) {
        playlists = playlists.map { p ->
            if (p.id == playlistId && !p.trackIds.contains(trackId)) {
                p.copy(trackIds = p.trackIds + trackId)
            } else p
        }
        repo.savePlaylists(playlists)
        message = "Ditambahkan ke playlist"
    }

    fun removeFromPlaylist(playlistId: String, trackId: String) {
        playlists = playlists.map { p ->
            if (p.id == playlistId) p.copy(trackIds = p.trackIds - trackId) else p
        }
        repo.savePlaylists(playlists)
    }

    fun deleteTrack(trackId: String) {
        val track = tracks.firstOrNull { it.id == trackId } ?: return
        runCatching { java.io.File(track.filePath).delete() }
        tracks = tracks.filterNot { it.id == trackId }
        playlists = playlists.map { p -> p.copy(trackIds = p.trackIds - trackId) }
        repo.saveTracks(tracks)
        repo.savePlaylists(playlists)
        message = "Lagu dihapus"
    }

    fun playlist(id: String?): Playlist? = playlists.firstOrNull { it.id == id }
}
