package com.alguojian.simple

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.alguojian.videoffmpeg.VideoFfmpeg
import com.alguojian.videoffmpeg.select.SelectVideoActivity
import io.microshow.rxffmpeg.RxFFmpegInvoke
import io.microshow.rxffmpeg.RxFFmpegSubscriber
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_SELECT_IMAGES_CODE = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        RxFFmpegInvoke.getInstance().setDebug(true)
    }

    fun selectVideo(view: View) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1
            )
        } else {
            VideoFfmpeg.openSelectVideo(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SELECT_IMAGES_CODE && resultCode == Activity.RESULT_OK && data != null) {

//            val commandCompress = getCommandCompress(imagePaths[0])
//            if (commandCompress.isNullOrEmpty())
//                return
//
//            startComPress(commandCompress)

        }
    }


    /**
     * 开始压缩视频
     */
    private fun startComPress(commandCompress: Array<String>) {
        RxFFmpegInvoke.getInstance().runCommandRxJava(commandCompress)
            .subscribe(object : RxFFmpegSubscriber() {
                override fun onFinish() {
                    compressTime.visibility = View.GONE
                    progressBar.visibility = View.GONE
                }

                @SuppressLint("SetTextI18n")
                override fun onProgress(progress: Int, progressTime: Long) {
                    compressTime.visibility = View.VISIBLE
                    progressBar.visibility = View.VISIBLE
                    progressBar.progress = progress
                    compressTime.text = "${progressTime.toInt() / 1000000}秒"
                }

                override fun onCancel() {
                    compressTime.visibility = View.GONE
                    progressBar.visibility = View.GONE
                }

                override fun onError(message: String) {
                    compressTime.visibility = View.GONE
                    progressBar.visibility = View.GONE
                }
            })
    }


    /**
     * 获得压缩命令
     */
    private fun getCommandCompress(originPath: String): Array<String>? {
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
        //码率低于400不进行压缩
        if (bitrate.toInt() < 400 * 1000)
            return null
        val newBitrate = if (bitrate.toInt() > 2000 * 1000) {
            2000
        } else {
            (bitrate.toInt() * 0.8f / 1000f).toInt()
        }

        println("----------------------码率是---$newBitrate")

        val width: Int
        val height: Int
        if (Integer.parseInt(videoRotation) == 90 || Integer.parseInt(videoRotation) == 270) {
            //角度不对需要宽高调换
            width = videoHeight.toInt()
            height = videoWidth.toInt()
        } else {
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
        list.add(this.externalCacheDir!!.absolutePath + File.separator + System.currentTimeMillis() + ".mp4")
        return list.toTypedArray()//采用list转换，防止文件名带空格造成分割错误
    }
}
