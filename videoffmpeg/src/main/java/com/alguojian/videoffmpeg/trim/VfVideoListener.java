package com.alguojian.videoffmpeg.trim;


import androidx.annotation.Nullable;

public interface VfVideoListener {
    void onStart();

    void onFinish(@Nullable String videoUrl,@Nullable String thumbImage);

    void onProgress(int progress);

    void onError();
}
