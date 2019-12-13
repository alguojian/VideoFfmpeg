package com.alguojian.videoffmpeg

import android.content.Context
import com.alguojian.videoffmpeg.select.SelectVideoActivity
import io.microshow.rxffmpeg.RxFFmpegInvoke
import io.microshow.rxffmpeg.RxFFmpegSubscriber

/**
 * 视频裁剪以及压缩，使用ffmpeg
 *
 * @author alguojian
 * @date 2019/12/13
 */
object VideoFfmpeg {

    private lateinit var mContext: Context

    /**
     * 是否显示日志
     */
    var showLog: Boolean = false

    /**
     * 初始化，是否显示log日志，包括视频裁剪，以及压缩等
     */
    @JvmStatic
    fun init(context: Context, openLog: Boolean = false) {
        this@VideoFfmpeg.mContext = context
        showLog = openLog
        RxFFmpegInvoke.getInstance().setDebug(openLog)
    }


    /**
     * 压缩视频
     */
    fun startCompress(inPath: String, outPath: String, rxFfmpegSubscriber: RxFFmpegSubscriber) {
        RxFFmpegInvoke.getInstance()
            .runCommandRxJava(VideoUtils.getCompressCommand(mContext, inPath, outPath))
            .subscribe(rxFfmpegSubscriber)
    }


    /**
     * 裁剪视频
     */
    fun startCrop(
        inPath: String,
        outPath: String,
        startMs: Long,
        endMs: Long,
        rxFfmpegSubscriber: RxFFmpegSubscriber
    ) {
        RxFFmpegInvoke.getInstance()
            .runCommandRxJava(VideoUtils.getCropCommand(mContext, inPath, outPath, startMs, endMs))
            .subscribe(rxFfmpegSubscriber)
    }


    /**
     * 打开视频选择页面，进行裁剪压缩等一系列操作
     */
    fun openSelectVideo(context: Context) {
        SelectVideoActivity.starter(context)
    }


    /**
     * 结束裁剪或者结束压缩
     */
    fun onDestory() {
        RxFFmpegInvoke.getInstance().exit()
    }
}