package com.otcengineering.white_app.interfaces;

import androidx.annotation.MainThread;

public interface AsyncResponse<T> {
    @MainThread
    void onResponse(T val);

    @MainThread
    void onFailure();
}
