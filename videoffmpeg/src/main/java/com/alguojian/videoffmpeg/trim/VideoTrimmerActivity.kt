package com.alguojian.videoffmpeg.trim

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import com.alguojian.videoffmpeg.R
import kotlinx.android.synthetic.main.vf_activity_video_trimmer.*

class VideoTrimmerActivity : AppCompatActivity() {

    private lateinit var videoPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            )
        }
        setContentView(R.layout.vf_activity_video_trimmer)
        videoPath = intent.getStringExtra("videoPath")
        trimmer_view.initVideoByURI(Uri.parse(videoPath))
    }

    companion object {
        @JvmStatic
        fun starter(context: Context, videoPath: String) {
            val starter = Intent(context, VideoTrimmerActivity::class.java)
            starter.putExtra("videoPath", videoPath)
            context.startActivity(starter)
        }
    }

    override fun onResume() {
        super.onResume()
        trimmer_view.onResume()
    }

    override fun onPause() {
        super.onPause()
        trimmer_view.onVideoPause()
        trimmer_view.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        trimmer_view.onDestroy()
    }
}
