package com.zdevlops.chystalin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zdevlops.chystalin.data.Track
import com.zdevlops.chystalin.ui.theme.Accent
import com.zdevlops.chystalin.ui.theme.Accent2
import com.zdevlops.chystalin.ui.theme.Bg
import com.zdevlops.chystalin.ui.theme.SurfaceHigh
import com.zdevlops.chystalin.ui.theme.TextDim
import com.zdevlops.chystalin.ui.theme.TextMain

private val colorScheme = darkColorScheme(
    primary = Accent,
    secondary = Accent2,
    background = Bg,
    surface = com.zdevlops.chystalin.ui.theme.Surface,
    onPrimary = Color.White,
    onBackground = TextMain,
    onSurface = TextMain
)

private enum class Tab { Library, Playlists }

@Composable
fun ChystalinRoot() {
    val vm: ChystalinViewModel = viewModel()
    var tab by remember { mutableStateOf(Tab.Library) }
    var showPlayer by remember { mutableStateOf(false) }
    var showDownload by remember { mutableStateOf(false) }
    var showCreatePlaylist by remember { mutableStateOf(false) }
    var addToPlaylistTrack by remember { mutableStateOf<Track?>(null) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(vm.message) {
        val msg = vm.message
        if (msg != null) {
            snackbar.showSnackbar(msg)
            vm.clearMessage()
        }
    }

    MaterialTheme(colorScheme = colorScheme) {
        Surface(modifier = Modifier.fillMaxSize(), color = Bg) {
            Box(modifier = Modifier.fillMaxSize()) {
                Scaffold(
                    containerColor = Bg,
                    snackbarHost = { SnackbarHost(snackbar) },
                    bottomBar = {
                        Column {
                            val track = vm.currentTrack
                            if (track != null) {
                                MiniPlayer(
                                    track = track,
                                    isPlaying = vm.isPlaying,
                                    onToggle = vm::togglePlayPause,
                                    onNext = vm::next,
                                    onClick = { showPlayer = true }
                                )
                            }
                            NavigationBar(containerColor = SurfaceHigh) {
                                NavigationBarItem(
                                    selected = tab == Tab.Library,
                                    onClick = { tab = Tab.Library },
                                    icon = { Icon(Icons.Rounded.LibraryMusic, contentDescription = "Pustaka") },
                                    label = { Text("Pustaka") },
                                    colors = navColors()
                                )
                                NavigationBarItem(
                                    selected = tab == Tab.Playlists,
                                    onClick = { tab = Tab.Playlists },
                                    icon = { Icon(Icons.Rounded.PlaylistPlay, contentDescription = "Playlist") },
                                    label = { Text("Playlist") },
                                    colors = navColors()
                                )
                            }
                        }
                    }
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        when (tab) {
                            Tab.Library -> LibraryScreen(
                                vm = vm,
                                onOpenDownload = { showDownload = true },
                                onAddToPlaylist = { addToPlaylistTrack = it },
                                onOpenPlayer = { showPlayer = true }
                            )
                            Tab.Playlists -> PlaylistsScreen(
                                vm = vm,
                                onCreate = { showCreatePlaylist = true },
                                onOpenPlayer = { showPlayer = true }
                            )
                        }
                    }
                }

                if (showPlayer) {
                    PlayerScreen(vm = vm, onClose = { showPlayer = false })
                }
            }

            if (showDownload) {
                DownloadDialog(
                    progress = vm.downloadProgress,
                    onDismiss = { showDownload = false },
                    onDownload = { url ->
                        vm.downloadFromUrl(url)
                        showDownload = false
                    }
                )
            }

            if (showCreatePlaylist) {
                TextInputDialog(
                    title = "Playlist Baru",
                    label = "Nama playlist",
                    confirmLabel = "Buat",
                    onDismiss = { showCreatePlaylist = false },
                    onConfirm = { name ->
                        vm.createPlaylist(name)
                        showCreatePlaylist = false
                    }
                )
            }

            val addTrack = addToPlaylistTrack
            if (addTrack != null) {
                AddToPlaylistDialog(
                    vm = vm,
                    track = addTrack,
                    onDismiss = { addToPlaylistTrack = null }
                )
            }
        }
    }
}

@Composable
private fun navColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = Accent,
    selectedTextColor = Accent,
    indicatorColor = Accent.copy(alpha = 0.15f),
    unselectedIconColor = TextDim,
    unselectedTextColor = TextDim
)

@Composable
private fun MiniPlayer(
    track: Track,
    isPlaying: Boolean,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        color = SurfaceHigh,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp)),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.LibraryMusic, contentDescription = null, tint = Accent2)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    track.title,
                    color = TextMain,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    track.artist,
                    color = TextDim,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onToggle) {
                Icon(
                    if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = "Putar",
                    tint = TextMain
                )
            }
            IconButton(onClick = onNext) {
                Icon(Icons.Rounded.SkipNext, contentDescription = "Berikutnya", tint = TextMain)
            }
        }
    }
}
