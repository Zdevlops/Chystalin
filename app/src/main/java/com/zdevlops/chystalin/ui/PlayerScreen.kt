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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zdevlops.chystalin.data.RotationMode
import com.zdevlops.chystalin.ui.theme.Accent
import com.zdevlops.chystalin.ui.theme.Accent2
import com.zdevlops.chystalin.ui.theme.Bg
import com.zdevlops.chystalin.ui.theme.TextDim
import com.zdevlops.chystalin.ui.theme.TextMain

@Composable
fun PlayerScreen(vm: ChystalinViewModel, onClose: () -> Unit) {
    val track = vm.currentTrack
    var sliderValue by remember(track?.id) { mutableStateOf<Float?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Accent.copy(alpha = 0.22f), Bg, Bg)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Rounded.Close, contentDescription = "Tutup", tint = TextMain)
                }
                Spacer(Modifier.weight(1f))
                Text("Sedang Diputar", color = TextDim, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(48.dp))
            }

            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Accent.copy(alpha = 0.55f), Accent2.copy(alpha = 0.35f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = TextMain.copy(alpha = 0.9f),
                    modifier = Modifier.size(110.dp)
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = track?.title ?: "Tidak ada lagu",
                color = TextMain,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = track?.artist ?: "",
                color = TextDim,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(22.dp))

            val max = vm.durationMs.coerceAtLeast(1L).toFloat()
            Slider(
                value = sliderValue ?: vm.positionMs.coerceIn(0L, max.toLong()).toFloat(),
                onValueChange = { sliderValue = it },
                onValueChangeFinished = {
                    sliderValue?.let { vm.seekTo(it.toLong()) }
                    sliderValue = null
                },
                valueRange = 0f..max,
                colors = SliderDefaults.colors(
                    thumbColor = Accent2,
                    activeTrackColor = Accent,
                    inactiveTrackColor = TextDim.copy(alpha = 0.3f)
                )
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatDuration(sliderValue?.toLong() ?: vm.positionMs), color = TextDim, style = MaterialTheme.typography.bodySmall)
                Text(formatDuration(vm.durationMs), color = TextDim, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = vm::cycleRotation) {
                    Icon(
                        when (vm.rotation) {
                            RotationMode.REPEAT_ONE -> Icons.Rounded.RepeatOne
                            RotationMode.SHUFFLE -> Icons.Rounded.Shuffle
                            RotationMode.OFF -> Icons.Rounded.Repeat
                            else -> Icons.Rounded.Repeat
                        },
                        contentDescription = "Rotasi: ${rotationLabel(vm.rotation)}",
                        tint = if (vm.rotation == RotationMode.OFF) TextDim else Accent
                    )
                }
                IconButton(onClick = vm::previous, modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Rounded.SkipPrevious, contentDescription = "Sebelumnya", tint = TextMain, modifier = Modifier.size(38.dp))
                }
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .clip(CircleShape)
                        .background(Accent),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = vm::togglePlayPause) {
                        Icon(
                            if (vm.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = "Putar/Jeda",
                            tint = TextMain,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                IconButton(onClick = vm::next, modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Rounded.SkipNext, contentDescription = "Berikutnya", tint = TextMain, modifier = Modifier.size(38.dp))
                }
                Spacer(Modifier.size(48.dp))
            }

            Spacer(Modifier.weight(1f))

            Text(
                text = "Rotasi: ${rotationLabel(vm.rotation)}",
                color = TextDim,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }
    }
}