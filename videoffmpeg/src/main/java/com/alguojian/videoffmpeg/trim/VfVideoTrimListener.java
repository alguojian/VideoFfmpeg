package com.alguojian.videoffmpeg.trim;


public interface VfVideoTrimListener {
    void onStartTrim();

    void onFinishTrim(String url);

    void onCancel();
}
