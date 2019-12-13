package com.alguojian.videoffmpeg;

public interface VfCallBack<T, V> {
    void success(T t);

    void failure(V v);
}

