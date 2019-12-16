package com.alguojian.videoffmpeg.trim;


public interface VfVideoListener {
    void onStart();

    void onFinish(String url);

    void onPrgress(int progress, long time);
}
