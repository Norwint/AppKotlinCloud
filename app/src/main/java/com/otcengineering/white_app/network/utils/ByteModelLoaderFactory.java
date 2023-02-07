package com.otcengineering.white_app.network.utils;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

import java.nio.ByteBuffer;

public class ByteModelLoaderFactory implements ModelLoaderFactory<Long, ByteBuffer> {
    @NonNull
    @Override
    public ModelLoader<Long, ByteBuffer> build(@NonNull MultiModelLoaderFactory multiFactory) {
        return new ByteModelLoader();
    }

    @Override
    public void teardown() {

    }
}
