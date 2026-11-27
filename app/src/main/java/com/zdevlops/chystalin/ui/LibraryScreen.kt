package com.zdevlops.chystalin.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zdevlops.chystalin.data.Track
import com.zdevlops.chystalin.ui.theme.Accent
import com.zdevlops.chystalin.ui.theme.Accent2
import com.zdevlops.chystalin.ui.theme.Bg
import com.zdevlops.chystalin.ui.theme.Surface
import com.zdevlops.chystalin.ui.theme.SurfaceHigh
import com.zdevlops.chystalin.ui.theme.TextDim
import com.zdevlops.chystalin.ui.theme.TextMain

@Composable
fun LibraryScreen(
    vm: ChystalinViewModel,
    onOpenDownload: () -> Unit,
    onAddToPlaylist: (Track) -> Unit,
    onOpenPlayer: () -> Unit
) {
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris -> vm.importUris(uris) }

    Column(modifier = Modifier.fillMaxSize()) {
        Header(
            title = "Pustaka",
            subtitle = "${vm.tracks.size} lagu tersimpan offline"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { picker.launch(arrayOf("audio/*")) },
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Impor")
            }
            Button(
                onClick = onOpenDownload,
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceHigh),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Rounded.CloudDownload, contentDescription = null, tint = Accent2)
                Spacer(Modifier.width(6.dp))
                Text("Unduh", color = TextMain)
            }
        }

        Spacer(Modifier.height(14.dp))

        if (vm.tracks.isEmpty()) {
            EmptyState(
                title = "Belum ada musik",
                body = "Impor berkas audio dari penyimpanan atau unduh lagu lewat tautan langsung."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 12.dp, end = 12.dp, bottom = 24.dp
                )
            ) {
                items(vm.tracks, key = { it.id }) { track ->
                    TrackRow(
                        track = track,
                        isCurrent = vm.currentTrackId == track.id,
                        isPlaying = vm.isPlaying && vm.currentTrackId == track.id,
                        onClick = {
                            val index = vm.tracks.indexOf(track)
                            vm.playTracks(vm.tracks, index)
                            onOpenPlayer()
                        },
                        onPlay = {
                            val index = vm.tracks.indexOf(track)
                            vm.playTracks(vm.tracks, index)
                            onOpenPlayer()
                        },
                        onAddToPlaylist = { onAddToPlaylist(track) },
                        onDelete = { vm.deleteTrack(track.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun Header(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextMain
        )
        Text(subtitle, color = TextDim, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun EmptyState(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.linearGradient(listOf(Accent.copy(alpha = 0.35f), Accent2.copy(alpha = 0.25f)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.LibraryMusic,
                contentDescription = null,
                tint = TextMain,
                modifier = Modifier.size(44.dp)
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(title, color = TextMain, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(6.dp))
        Text(body, color = TextDim, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun TrackRow(
    track: Track,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onPlay: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onDelete: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isCurrent) Accent.copy(alpha = 0.12f) else Bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.PlayArrow,
                contentDescription = null,
                tint = if (isCurrent) Accent2 else TextDim
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                track.title,
                color = if (isCurrent) Accent2 else TextMain,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                track.artist,
                color = TextDim,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(formatDuration(track.durationMs), color = TextDim, style = MaterialTheme.typography.bodySmall)
        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(Icons.Rounded.MoreVert, contentDescription = "Menu", tint = TextDim)
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text("Tambah ke playlist") },
                    leadingIcon = { Icon(Icons.Rounded.PlaylistAdd, contentDescription = null) },
                    onClick = {
                        menuOpen = false
                        onAddToPlaylist()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Hapus lagu") },
                    leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null) },
                    onClick = {
                        menuOpen = false
                        onDelete()
                    }
                )
            }
        }
    }
}

fun formatDuration(ms: Long): String {
    if (ms <= 0) return "--:--"
    val total = ms / 1000
    val m = total / 60
    val s = total % 60
    return "%d:%02d".format(m, s)
}
