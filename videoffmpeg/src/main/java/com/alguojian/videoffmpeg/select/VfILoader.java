package com.alguojian.videoffmpeg.select;

import android.content.Context;
import android.provider.MediaStore;

import com.alguojian.videoffmpeg.VfSimpleCallback;

public interface VfILoader {
    String SELECTION =
            MediaStore.Video.Media.MIME_TYPE + "=? or "
                    + MediaStore.Video.Media.MIME_TYPE + "=? or "
                    + MediaStore.Video.Media.MIME_TYPE + "=? or "
                    + MediaStore.Video.Media.MIME_TYPE + "=? or "
                    + MediaStore.Video.Media.MIME_TYPE + "=? or "
                    + MediaStore.Video.Media.MIME_TYPE + "=? or "
                    + MediaStore.Video.Media.MIME_TYPE + "=? or "
                    + MediaStore.Video.Media.MIME_TYPE + "=? or "
                    + MediaStore.Video.Media.MIME_TYPE + "=?";
    String[] SELECTION_ARGS = {
            "video/mp4",
            "video/3gp",
            "video/aiv",
            "video/rmvb",
            "video/vob",
            "video/flv",
            "video/mkv",
            "video/mov",
            "video/mpg"
    };
    String[] PROJECTION = {
            MediaStore.Video.VideoColumns._ID,
            MediaStore.Video.VideoColumns.DATE_ADDED,
            MediaStore.Video.VideoColumns.DATA,
            MediaStore.Video.VideoColumns.DISPLAY_NAME,
            MediaStore.Video.VideoColumns.DURATION
    };
    String ORDER_BY = MediaStore.Images.Media.DATE_MODIFIED + " DESC";

    void load(final Context mContext, final VfSimpleCallback listener);
}
