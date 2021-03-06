package com.alguojian.videoffmpeg.select;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;


import com.alguojian.videoffmpeg.VfSimpleCallback;

public class VfVideoCursorLoader implements LoaderManager.LoaderCallbacks<Cursor>, VfILoader {

    private Context mContext;
    private VfSimpleCallback mSimpleCallback;

    @Override
    public void load(final Context context, final VfSimpleCallback listener) {
        mContext = context;
        mSimpleCallback = listener;
        LoaderManager.getInstance((FragmentActivity) context).initLoader(1, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new CursorLoader(
                mContext,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                PROJECTION,
                SELECTION,
                SELECTION_ARGS,
                ORDER_BY
        );
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (mSimpleCallback != null && cursor != null) {
            mSimpleCallback.success(cursor);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}
