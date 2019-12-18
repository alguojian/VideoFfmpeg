package com.alguojian.videoffmpeg

import android.net.Uri

object VfVideoStatusBean {

    const val VIDEO_CROP = 0
    const val VIDEO_INTERCEPT_IMAGE = 1
    const val VIDEO_COMPRESS = 2

    @JvmStatic
    var leftTime: Long = 0L

    @JvmStatic
    var rightTime: Long = 0L

    @JvmStatic
    var videoUri: Uri? = null

    @JvmStatic
    var status: Int = VIDEO_CROP

}