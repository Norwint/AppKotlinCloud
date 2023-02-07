package com.otcengineering.white_app.interfaces;

import android.graphics.Bitmap;
import androidx.annotation.Nullable;

public interface OnBitmapReceived {
    void onReceive(@Nullable Bitmap bmp, @Nullable String error);
}
