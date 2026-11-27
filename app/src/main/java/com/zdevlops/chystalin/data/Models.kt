package com.zdevlops.chystalin.data

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val filePath: String,
    val durationMs: Long,
    val addedAt: Long
)

enum class RotationMode {
    OFF,
    REPEAT_ALL,
    REPEAT_ONE,
    SHUFFLE
}

data class Playlist(
    val id: String,
    val name: String,
    val trackIds: List<String>,
    val rotation: RotationMode
)
