package com.alguojian.videoffmpeg

import android.app.Application
import android.content.Context
import kotlin.properties.Delegates

class VfApp : Application() {

    companion object {
        @JvmStatic
        var mContext: Context by Delegates.notNull()
    }

    override fun onCreate() {
        super.onCreate()
        mContext = applicationContext
    }
}