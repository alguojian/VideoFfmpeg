package com.alguojian.videoffmpeg

import android.media.MediaMetadataRetriever

/**
 * context.externalCacheDir!!.absolutePath + File.separator + System.currentTimeMillis() + ".mp4"
 */
object VideoUtils {
    //点击完成开始视频操作时，关闭截取页面，以及视频选择页面
    const val start_video_operating_finish_activity = "START_VIDEO_OPERATING_FINISH_ACTIVITY"


    val videoCropOutPath: String
        get() {
            return VideoFfmpeg.mContext.externalCacheDir.absolutePath + java.io.File.separator + System.currentTimeMillis() + ".mp4"
        }

    val videoInterceptImageOutPath: String
        get() {
            return VideoFfmpeg.mContext.externalCacheDir.absolutePath + java.io.File.separator + System.currentTimeMillis() + ".jpg"
        }

    val videoCompressOutPath: String
        get() {
            return VideoFfmpeg.mContext.externalCacheDir.absolutePath + java.io.File.separator + System.currentTimeMillis() + ".mp4"
        }


    /**
     * 获得视频裁剪命令
     * [context] 上下文
     * [originPath] 视频路径
     * [outPath] 视频输出路径
     * [startMs] 开始裁剪的时间
     * [endMs] 结束裁剪的时间
     *
     * 裁剪视频ffmpeg指令说明：
     * ffmpeg -ss START -t DURATION -i INPUT -codec copy -avoid_negative_ts 1 OUTPUT
     * -ss 开始时间，如： 00:00:20，表示从20秒开始；
     * -t 时长，如： 00:00:15，表示截取15秒长的视频；
     * -i 输入，后面是空格，紧跟着就是输入视频文件；
     * -codec copy -avoid_negative_ts 1 表示所要使用的视频和音频的编码格式，这里指定为copy表示原样拷贝；
     * originPath，输入视频文件；
     * outPath，输出视频文件
     */
    @JvmStatic
    fun getCropCommand(
        originPath: String, outPath: String,
        startMs: Long, endMs: Long
    ): Array<String>? {
        val start: String = VfUtils.convertSecondsToTime(startMs / 1000)
        val duration: String = VfUtils.convertSecondsToTime((endMs - startMs) / 1000)

        LogUtils.log("--------------开始裁剪时间----------$start-----------裁剪时长为-----$duration")

        val list = mutableListOf<String>()
        list.add("ffmpeg")
        list.add("-ss")
        list.add(start)
        list.add("-t")
        list.add(duration)
        list.add("-accurate_seek")
        list.add("-i")
        list.add(originPath)
        list.add("-codec")
        list.add("copy")
        list.add("-avoid_negative_ts")
        list.add("1")
        list.add(outPath)
        return list.toTypedArray()//采用list转换，防止文件名带空格造成分割错误
    }


    /**
     * 获得视频第一帧作为视频封面
     */
    @JvmStatic
    fun getInterceptImage(
        originPath: String, outPath: String
    ): Array<String> {
        val list = mutableListOf<String>()
        list.add("ffmpeg")
        list.add("-y")
        list.add("-i")
        list.add(originPath)
        list.add("-f")
        list.add("image2")
        list.add("-ss")
        list.add("00:00:01")
        list.add("-vframes")
        list.add("1")
        list.add("-preset")
        list.add("superfast")
        list.add(outPath)
        return list.toTypedArray()
    }


    /**
     * 获得压缩命令
     * [originPath] 视频路径
     * [outPath] 视频输出路径
     */
    @JvmStatic
    fun getCompressCommand(originPath: String, outPath: String): Array<String>? {
        //https://blog.csdn.net/qq_31332467/article/details/79166945
        //4K视频可能会闪退，所以需要添加尺寸压缩
        val mMetadataRetriever = MediaMetadataRetriever()
        mMetadataRetriever.setDataSource(originPath)
        val videoRotation =
            mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
        val videoHeight =
            mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
        val videoWidth =
            mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
        val bitrate =
            mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
        mMetadataRetriever.release()
        //码率低于600不进行压缩
        if (bitrate.toInt() < 600 * 1000)
            return null
        val newBitrate = if (bitrate.toInt() > 2000 * 1000) {
            2000
        } else {
            (bitrate.toInt() * 0.8f / 1000f).toInt()
        }
        val width: Int
        val height: Int
        if (Integer.parseInt(videoRotation) == 90 || Integer.parseInt(videoRotation) == 270) {
            width = videoHeight.toInt()
            height = videoWidth.toInt()
        } else {
            //角度不对需要宽高调换
            width = videoWidth.toInt()
            height = videoHeight.toInt()
        }
        //需要根据视频大小和视频时长计算得到需要压缩的码率，不然会导致高清视频压缩后变模糊，非高清视频压缩后文件变大
        //https://blog.csdn.net/zhezhebie/article/details/79263492
        val list = mutableListOf<String>()
        list.add("ffmpeg")
        list.add("-y")
        list.add("-i")
        list.add(originPath)
        list.add("-b")
        list.add("${newBitrate}k")
        list.add("-r")
        list.add("30")
        list.add("-vcodec")
        list.add("libx264")
        if (kotlin.math.min(width, height) > 1080) {//大于1080p
            list.add("-vf")
            list.add("scale=${if (width > height) "1080:-1" else "-1:1080"}")
        }
        list.add("-preset")
        list.add("superfast")
        list.add(outPath)
        return list.toTypedArray()//采用list转换，防止文件名带空格造成分割错误
    }


}