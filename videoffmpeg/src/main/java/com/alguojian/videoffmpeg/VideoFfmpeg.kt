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

    @JvmStatic
    private lateinit var mContext: Context

    /**
     * 视频相关进度
     */
    @JvmStatic
    var vfVideoListener: VfVideoListener? = null

    /**
     * 点击保存时的回调
     */
    var vfSaveClickListener: VfSaveClickListener? = null
    var vfSaveClickStatus = 0

    /**
     * 是否正在进行视频的操作，包括裁剪，压缩上传等一系列，防止当前没操作完，禁止操作下一个视频
     */
    @JvmStatic
    var isOperating = false

    /**
     * 是否显示日志
     */
    @JvmStatic
    var showLog: Boolean = false

    /**
     * 必须初始化，需要拿到上下文来得到视频存放目录，是否显示log日志，包括视频裁剪，以及压缩等
     */
    @JvmStatic
    @JvmOverloads
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
     * 设置完成点击时的回调，带有状态，区分多个业务逻辑
     */
    @JvmStatic
    fun setSaveClickListener(status: Int, vfSaveClickListener: VfSaveClickListener) {
        this@VideoFfmpeg.vfSaveClickListener = vfSaveClickListener
        this@VideoFfmpeg.vfSaveClickStatus = status
    }


    /**
     * 压缩视频
     */
    @JvmStatic
    @JvmOverloads
    fun startCompress(
        inPath: String,
        outPath: String = VideoUtils.videoCompressOutPath,
        thumbImage: String? = null,
        aloneTransfer: Boolean = true
    ) {
        if (aloneTransfer) {
            isOperating = true
            vfVideoListener?.onStart()
        } else {
            vfVideoListener?.onProgress(25)
        }
        val compressCommand = VideoUtils.getCompressCommand(inPath, outPath)
        if (compressCommand.isNullOrEmpty()) {
            LogUtils.log("--------------压缩完成了--------------低于400码率不需要压缩------------------------$outPath")
            vfVideoListener?.onFinish(inPath, thumbImage)
            if (aloneTransfer) {
                vfVideoListener?.onProgress(100)
            } else {
                vfVideoListener?.onProgress(85)
            }
            changeStatus(aloneTransfer)
            return
        }
        RxFFmpegInvoke.getInstance()
            .runCommandRxJava(compressCommand)
            .subscribe(object : RxFFmpegSubscriber() {
                override fun onFinish() {
                    LogUtils.log("--------------压缩完成了-----------------------------$outPath")
                    vfVideoListener?.onFinish(outPath, thumbImage)
                    if (aloneTransfer) {
                        vfVideoListener?.onProgress(100)
                    } else {
                        vfVideoListener?.onProgress(85)
                    }
                    changeStatus(aloneTransfer)
                }

                override fun onCancel() {
                    LogUtils.log("--------------压缩取消了")
                    changeStatus(aloneTransfer)
                    vfVideoListener?.onError()
                }

                override fun onProgress(progress: Int, progressTime: Long) {
                    LogUtils.log("--------------压缩进度是---$progress-------压缩时间-------$progressTime")
                    if (aloneTransfer) {
                        vfVideoListener?.onProgress(progress)
                    } else {
                        vfVideoListener?.onProgress(25 + progress * 60 / 100)
                    }
                }

                override fun onError(message: String?) {
                    LogUtils.log("--------------压缩失败了---$message")
                    changeStatus(aloneTransfer)
                    vfVideoListener?.onError()
                }
            })
    }


    /**
     * 截取第一帧
     */
    @JvmStatic
    @JvmOverloads
    fun startInterceptCover(
        inPath: String,
        outPath: String = VideoUtils.videoInterceptImageOutPath,
        aloneTransfer: Boolean = true
    ) {
        if (aloneTransfer) {
            isOperating = true
            vfVideoListener?.onStart()
        } else {
//            vfVideoListener!!.onProgress(20)
        }
        RxFFmpegInvoke.getInstance()
            .runCommandRxJava(VideoUtils.getInterceptImage(inPath, outPath))
            .subscribe(object : RxFFmpegSubscriber() {
                override fun onFinish() {
                    LogUtils.log("--------------截取完成了----------------------------$outPath")
                    if (aloneTransfer) {
                        vfVideoListener?.onFinish(null, outPath)
                    } else {
                        VfVideoStatusBean.videoPath = inPath
                        VfVideoStatusBean.thumbImagePath = outPath
                        vfSaveClickListener?.saveClickListener(vfSaveClickStatus)
//                        vfVideoListener?.onProgress(25)
                        //获取第一帧完成，开始压缩
//                        startCompress(inPath, VideoUtils.videoCompressOutPath, outPath, false)
                    }
                    changeStatus(aloneTransfer)
                }

                override fun onCancel() {
                    LogUtils.log("--------------截取取消了")
                    changeStatus(aloneTransfer)
                    vfVideoListener?.onError()
                }

                override fun onProgress(progress: Int, progressTime: Long) {
                    LogUtils.log("--------------截取进度是---$progress-------截取时间-------$progressTime")
                    if (aloneTransfer) {
                        vfVideoListener?.onProgress(progress)
                    } else {
//                        vfVideoListener?.onProgress(20 + progress * 5 / 100)
                    }
                }

                override fun onError(message: String?) {
                    LogUtils.log("--------------截取失败了---$message")
                    changeStatus(aloneTransfer)
                    vfVideoListener?.onError()
                }
            })
    }


    /**
     * 裁剪视频
     *
     * [inPath] 视频路径
     * [outPath] 视频输出路径，默认缓存目录
     * [startMs] 开始时间
     * [endMs] 结束时间
     * [aloneTransfer] 是否只是单独调用改方法，默认true
     */
    @JvmStatic
    @JvmOverloads
    fun startCrop(
        inPath: String,
        outPath: String = VideoUtils.videoCropOutPath,
        startMs: Long,
        endMs: Long,
        aloneTransfer: Boolean = true
    ) {
        if (aloneTransfer) {
            isOperating = true
            vfVideoListener?.onStart()
        }
        RxFFmpegInvoke.getInstance()
            .runCommandRxJava(VideoUtils.getCropCommand(inPath, outPath, startMs, endMs))
            .subscribe(object : RxFFmpegSubscriber() {
                override fun onFinish() {
                    LogUtils.log("--------------裁剪完成了------------------------------------$outPath")
                    if (aloneTransfer) {
                        vfVideoListener?.onFinish(outPath, null)
                    } else {
                        //裁剪完成，开始获取第一帧
//                        vfVideoListener?.onProgress(20)
                        startInterceptCover(outPath, VideoUtils.videoInterceptImageOutPath, false)
                    }
                    changeStatus(aloneTransfer)
                }

                override fun onCancel() {
                    LogUtils.log("--------------裁剪取消了")
                    vfVideoListener?.onError()
                    changeStatus(aloneTransfer)
                }

                override fun onProgress(progress: Int, progressTime: Long) {
                    LogUtils.log("--------------裁剪进度是---$progress-------裁剪时间-------$progressTime")
                    if (aloneTransfer) {
                        vfVideoListener?.onProgress(progress)
                    } else {
//                        vfVideoListener?.onProgress(progress * 20 / 100)
                    }
                }

                override fun onError(message: String?) {
                    vfVideoListener?.onError()
                    LogUtils.log("--------------裁剪失败了---$message")
                    changeStatus(aloneTransfer)
                }
            })
    }


    /**
     * 打开视频选择页面，进行裁剪压缩等一系列操作
     */
    @JvmStatic
    fun openSelectVideo(context: Context) {
        SelectVideoActivity.starter(context)
    }


    /**
     * 结束裁剪或者结束压缩
     */
    @JvmStatic
    fun onDestroy() {
        changeStatus()
        RxFFmpegInvoke.getInstance().exit()
    }

    /**
     * 修改视频操作状态,默认改为未操作状态
     */
    @JvmStatic
    @JvmOverloads
    fun changeStatus(aloneTransfer: Boolean = true) {
        if (aloneTransfer) {
            isOperating = false
        }
    }
}