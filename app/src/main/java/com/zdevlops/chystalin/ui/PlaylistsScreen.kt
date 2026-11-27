package com.zdevlops.chystalin.ui

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
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zdevlops.chystalin.data.Playlist
import com.zdevlops.chystalin.data.RotationMode
import com.zdevlops.chystalin.ui.theme.Accent
import com.zdevlops.chystalin.ui.theme.Accent2
import com.zdevlops.chystalin.ui.theme.Bg
import com.zdevlops.chystalin.ui.theme.SurfaceHigh
import com.zdevlops.chystalin.ui.theme.TextDim
import com.zdevlops.chystalin.ui.theme.TextMain

@Composable
fun PlaylistsScreen(
    vm: ChystalinViewModel,
    onCreate: () -> Unit,
    onOpenPlayer: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Header(
            title = "Playlist",
            subtitle = "${vm.playlists.size} playlist · atur urutan putar"
        )

        Button(
            onClick = onCreate,
            colors = ButtonDefaults.buttonColors(containerColor = Accent),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("Playlist Baru")
        }

        Spacer(Modifier.height(14.dp))

        if (vm.playlists.isEmpty()) {
            EmptyState(
                title = "Belum ada playlist",
                body = "Buat playlist lalu tambahkan lagu dari pustaka. Urutan putar bisa diatur."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 12.dp, end = 12.dp, bottom = 24.dp
                )
            ) {
                items(vm.playlists, key = { it.id }) { playlist ->
                    PlaylistCard(
                        playlist = playlist,
                        trackCount = playlist.trackIds.size,
                        onPlay = {
                            vm.playPlaylist(playlist)
                            onOpenPlayer()
                        },
                        onDelete = { vm.deletePlaylist(playlist.id) },
                        onRotation = { mode -> vm.setPlaylistRotation(playlist.id, mode) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaylistCard(
    playlist: Playlist,
    trackCount: Int,
    onPlay: () -> Unit,
    onDelete: () -> Unit,
    onRotation: (RotationMode) -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    var rotationMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceHigh)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Accent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.PlaylistPlay, contentDescription = null, tint = Accent2)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    playlist.name,
                    color = TextMain,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text("$trackCount lagu", color = TextDim, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onPlay) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = "Putar playlist", tint = Accent2)
            }
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "Menu", tint = TextDim)
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("Hapus playlist") },
                        leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null) },
                        onClick = {
                            menuOpen = false
                            onDelete()
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Rotasi:", color = TextDim, style = MaterialTheme.typography.bodySmall)
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Bg)
                        .clickable { rotationMenu = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        when (playlist.rotation) {
                            RotationMode.REPEAT_ONE -> Icons.Rounded.RepeatOne
                            RotationMode.SHUFFLE -> Icons.Rounded.Shuffle
                            else -> Icons.Rounded.Repeat
                        },
                        contentDescription = null,
                        tint = Accent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(rotationLabel(playlist.rotation), color = TextMain, style = MaterialTheme.typography.bodySmall)
                }
                DropdownMenu(expanded = rotationMenu, onDismissRequest = { rotationMenu = false }) {
                    RotationMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(rotationLabel(mode)) },
                            onClick = {
                                rotationMenu = false
                                onRotation(mode)
                            }
                        )
                    }
                }
            }
        }
    }
}

fun rotationLabel(mode: RotationMode): String = when (mode) {
    RotationMode.OFF -> "Sekali putar"
    RotationMode.REPEAT_ALL -> "Ulang semua"
    RotationMode.REPEAT_ONE -> "Ulang satu"
    RotationMode.SHUFFLE -> "Acak"
}
