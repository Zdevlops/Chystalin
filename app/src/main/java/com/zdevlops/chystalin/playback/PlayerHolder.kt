package com.zdevlops.chystalin.playback

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer

object PlayerHolder {
    @Volatile
    var player: ExoPlayer? = null
        private set

    fun init(context: Context) {
        if (player == null) {
            synchronized(this) {
                if (player == null) {
                    player = ExoPlayer.Builder(context.applicationContext).build()
                }
            }
        }
    }
}
