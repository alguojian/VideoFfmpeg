package com.alguojian.simple

import android.Manifest
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.alguojian.videoffmpeg.LogUtils
import com.alguojian.videoffmpeg.VideoFfmpeg
import com.alguojian.videoffmpeg.trim.VfVideoListener


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        VideoFfmpeg.init(this, true)
        VideoFfmpeg.setListener(object : VfVideoListener {
            override fun onFinish(videoUrl: String?, thumbImage: String?) {
                VideoFfmpeg.changeStatus()
                LogUtils.log("----------------------------------================视频完成---$videoUrl----------------\n$thumbImage")
            }

            override fun onProgress(progress: Int) {
                LogUtils.log("------------------------------------================视频完成进度--------------------$progress")
            }

            override fun onError() {
            }

            override fun onStart() {
            }
        })
    }


    override fun onDestroy() {
        super.onDestroy()
        VideoFfmpeg.onDestroy()

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
            if (VideoFfmpeg.isOperating) {
                return
            }
            VideoFfmpeg.openSelectVideo(this)
        }

    }
}
