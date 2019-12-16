package com.alguojian.videoffmpeg

import android.content.Context
import com.alguojian.videoffmpeg.select.SelectVideoActivity
import com.alguojian.videoffmpeg.trim.VfVideoListener
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
     * 视频相关进度
     */
    private var vfVideoListener: VfVideoListener? = null

    /**
     * 是否正在进行视频的操作，包括裁剪，压缩上传等一系列，防止当前没操作完，禁止操作下一个视频
     */
    var isOperating = false

    /**
     * 是否显示日志
     */
    var showLog: Boolean = false

    /**
     * 必须初始化，需要拿到上下文来得到视频存放目录，是否显示log日志，包括视频裁剪，以及压缩等
     */
    @JvmStatic
    fun init(context: Context, openLog: Boolean = false) {
        this@VideoFfmpeg.mContext = context
        showLog = openLog
        RxFFmpegInvoke.getInstance().setDebug(openLog)
    }


    @JvmStatic
    fun setListener(vfVideoListener: VfVideoListener) {
        this@VideoFfmpeg.vfVideoListener = vfVideoListener
    }


    /**
     * 压缩视频
     */
    @JvmStatic
    fun startCompress(inPath: String, outPath: String) {
        vfVideoListener?.onStart()
        RxFFmpegInvoke.getInstance()
            .runCommandRxJava(VideoUtils.getCompressCommand(inPath, outPath))
            .subscribe(object : RxFFmpegSubscriber() {
                override fun onFinish() {
                    LogUtils.log("--------------压缩完成了------------$outPath")
                    vfVideoListener?.onFinish(outPath)
                }

                override fun onCancel() {
                    LogUtils.log("--------------压缩取消了")
                }

                override fun onProgress(progress: Int, progressTime: Long) {
                    LogUtils.log("--------------压缩进度是---$progress")
                    vfVideoListener?.onPrgress(progress, progressTime)
                }

                override fun onError(message: String?) {
                    LogUtils.log("--------------压缩失败了---$message")
                }
            })
    }


    /**
     * 截取第一帧
     */
    @JvmStatic
    fun startInterceptCover(inPath: String, outPath: String) {
        vfVideoListener?.onStart()
        RxFFmpegInvoke.getInstance()
            .runCommandRxJava(VideoUtils.getInterceptImage(inPath, outPath))
            .subscribe(object : RxFFmpegSubscriber() {
                override fun onFinish() {
                    LogUtils.log("--------------截取完成了------------$outPath")
                    vfVideoListener?.onFinish(outPath)
                }

                override fun onCancel() {
                    LogUtils.log("--------------截取取消了")
                }

                override fun onProgress(progress: Int, progressTime: Long) {
                    LogUtils.log("--------------截取进度是---$progress")
                    vfVideoListener?.onPrgress(progress, progressTime)
                }

                override fun onError(message: String?) {
                    LogUtils.log("--------------截取失败了---$message")
                }
            })
    }


    /**
     * 裁剪视频
     */
    @JvmStatic
    fun startCrop(
        inPath: String,
        outPath: String,
        startMs: Long,
        endMs: Long
    ) {
        vfVideoListener?.onStart()
        RxFFmpegInvoke.getInstance()
            .runCommandRxJava(VideoUtils.getCropCommand(inPath, outPath, startMs, endMs))
            .subscribe(object : RxFFmpegSubscriber() {
                override fun onFinish() {
                    LogUtils.log("--------------裁剪完成了------------$outPath")
                    vfVideoListener?.onFinish(outPath)
                }

                override fun onCancel() {
                    LogUtils.log("--------------裁剪取消了")
                }

                override fun onProgress(progress: Int, progressTime: Long) {
                    LogUtils.log("--------------裁剪进度是---$progress")
                    vfVideoListener?.onPrgress(progress, progressTime)
                }

                override fun onError(message: String?) {
                    LogUtils.log("--------------裁剪失败了---$message")
                }
            })
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