package com.alguojian.videoffmpeg.trim;


import android.support.annotation.Nullable;

public interface VfVideoListener {
    void onStart();

    void onFinish(@Nullable String videoUrl, @Nullable String thumbImage);

    void onProgress(int progress);

    void onError();
}
