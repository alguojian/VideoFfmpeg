package com.alguojian.videoffmpeg.trim;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import com.alguojian.videoffmpeg.LogUtils;
import com.alguojian.videoffmpeg.VfApp;
import com.alguojian.videoffmpeg.VfUtils;
import com.alguojian.videoffmpeg.VideoFfmpeg;
import com.alguojian.videoffmpeg.VideoUtils;

import java.io.File;

import io.microshow.rxffmpeg.RxFFmpegInvoke;
import io.microshow.rxffmpeg.RxFFmpegSubscriber;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class VfVideoTrimmerUtil {

    public static final long MIN_SHOOT_DURATION = 3000L;// 最小剪辑时间3s
    public static final int VIDEO_MAX_TIME = 15;// 15秒
    public static final long MAX_SHOOT_DURATION = VIDEO_MAX_TIME * 1000L;//视频最多剪切多长时间10s

    public static final int MAX_COUNT_RANGE = 10;  //seekBar的区域内一共有多少张图片
    private static final int SCREEN_WIDTH_FULL = VfUtils.getDeviceWidth();
    public static final int RECYCLER_VIEW_PADDING = VfUtils.dpToPx(35);
    public static final int VIDEO_FRAMES_WIDTH = SCREEN_WIDTH_FULL - RECYCLER_VIEW_PADDING * 2;
    private static final int THUMB_WIDTH = (SCREEN_WIDTH_FULL - RECYCLER_VIEW_PADDING * 2) / VIDEO_MAX_TIME;
    private static final int THUMB_HEIGHT = VfUtils.dpToPx(50);

    public static void trim(Context context, String inputFile, String outPath, long startMs, long endMs) {
        RxFFmpegInvoke.getInstance()
                .runCommandRxJava(VideoUtils.getCropCommand(inputFile, outPath, startMs, endMs))
                .subscribe(new RxFFmpegSubscriber() {
                    @Override
                    public void onFinish() {
                        VideoFfmpeg.startInterceptCover(outPath, VfApp.getMContext().getExternalCacheDir().getAbsolutePath()
                                + File.separator + System.currentTimeMillis() + ".jpg");
                        LogUtils.log("-------------裁剪完成了");
                    }

                    @Override
                    public void onProgress(int progress, long progressTime) {
                        LogUtils.log("----------------裁剪进度---" + progress + "----" + progressTime);
                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(String message) {
                        LogUtils.log("裁剪失败了---------------" + message);

                    }
                });


    }

    @SuppressLint("CheckResult")
    public static Flowable<Bitmap> shootVideoThumbInBackground(final Context context, final Uri videoUri, final int totalThumbsCount, final long startPosition,
                                                               final long endPosition) {

        return Flowable.create((FlowableOnSubscribe<Bitmap>) emitter -> {
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(context, videoUri);
            long interval = (endPosition - startPosition) / (totalThumbsCount - 1);
            for (long i = 0; i < totalThumbsCount; ++i) {
                long frameTime = startPosition + interval * i;
                Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime(frameTime * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                if (bitmap == null) continue;
                try {
                    bitmap = Bitmap.createScaledBitmap(bitmap, THUMB_WIDTH, THUMB_HEIGHT, false);
                } catch (final Throwable t) {
                    t.printStackTrace();
                }
                if (bitmap != null) {
                    emitter.onNext(bitmap);
                }
            }
            mediaMetadataRetriever.release();

        }, BackpressureStrategy.BUFFER)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
