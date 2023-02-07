package com.otcengineering.white_app.interfaces;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.otc.alice.api.model.Shared;

public interface ExtendedCallback<T> {
    void onSuccess(@NonNull T success);
    void onError(@Nullable Shared.OTCStatus status, @Nullable String message);
}
