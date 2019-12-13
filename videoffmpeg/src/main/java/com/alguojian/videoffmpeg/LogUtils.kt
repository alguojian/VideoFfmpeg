package com.alguojian.videoffmpeg

import android.util.Log

object LogUtils {

    @JvmStatic
    fun log(message: String?) {
        if (VideoFfmpeg.showLog)
            Log.d("---videoFfpeg---", message)
    }
}