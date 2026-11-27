package com.zdevlops.chystalin

import android.app.Application
import com.zdevlops.chystalin.playback.PlayerHolder

class ChystalinApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        PlayerHolder.init(this)
    }

    companion object {
        lateinit var instance: ChystalinApp
            private set
    }
}
