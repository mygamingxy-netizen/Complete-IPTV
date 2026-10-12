package com.uragontv.app

import android.app.Application
import com.uragontv.app.data.repository.StreamSourceRepository

class UragonTvApp : Application() {

    lateinit var repository: StreamSourceRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        repository = StreamSourceRepository(this)
    }

    companion object {
        lateinit var instance: UragonTvApp
            private set
    }
}
