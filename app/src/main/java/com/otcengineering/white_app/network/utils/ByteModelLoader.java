package com.otcengineering.white_app.network.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.signature.ObjectKey;

import java.nio.ByteBuffer;

public final class ByteModelLoader implements ModelLoader<Long, ByteBuffer> {
    @Nullable
    @Override
    public LoadData<ByteBuffer> buildLoadData(@NonNull Long aLong, int width, int height, @NonNull Options options) {
        return new LoadData<>(new ObjectKey(aLong), new ByteDataFetcher(aLong));
    }

    @Override
    public boolean handles(@NonNull Long aLong) {
        return true;
    }
}

