package com.alguojian.videoffmpeg.select

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.alguojian.videoffmpeg.R
import com.alguojian.videoffmpeg.VfSimpleCallback
import kotlinx.android.synthetic.main.vf_activity_select_video.*
import kotlinx.android.synthetic.main.vf_common_action_bar.*

class SelectVideoActivity : AppCompatActivity() {

    private var mVideoSelectAdapter: SelectVideoAdapter? = null
    private lateinit var mVideoLoadManager: VfVideoLoadManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.vf_activity_select_video)

        tvTitle.text = "选择视频"
        back.setOnClickListener { finish() }

        mVideoLoadManager = VfVideoLoadManager()
        mVideoLoadManager.setLoader(VfVideoCursorLoader())

        mVideoLoadManager.load(this, object : VfSimpleCallback() {

            override fun success(o: Any?) {
                super.success(o)
                if (mVideoSelectAdapter == null) {
                    mVideoSelectAdapter = SelectVideoAdapter(this@SelectVideoActivity, o as Cursor)
                } else {
                    mVideoSelectAdapter!!.swapCursor(o as Cursor)
                }
                if (gridView.adapter == null) {
                    gridView.adapter = mVideoSelectAdapter
                }
                mVideoSelectAdapter!!.notifyDataSetChanged()
            }
        })
    }

    companion object {

        @JvmStatic
        fun starter(context: Context) {
            val starter = Intent(context, SelectVideoActivity::class.java)
            context.startActivity(starter)
        }
    }
}

