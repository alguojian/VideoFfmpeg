package com.alguojian.videoffmpeg.select;

import android.content.Context;

import com.alguojian.videoffmpeg.VfSimpleCallback;

public class VfVideoLoadManager {

    private VfILoader mLoader;

    public void setLoader(VfILoader loader) {
        this.mLoader = loader;
    }

    public void load(final Context context, final VfSimpleCallback listener) {
        mLoader.load(context, listener);
    }
}
