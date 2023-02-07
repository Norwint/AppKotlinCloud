package com.otcengineering.white_app.utils.interfaces;

import androidx.annotation.Nullable;

import com.otc.alice.api.model.Shared;

import javax.annotation.Nonnull;

// Aquesta interfície executarà onSuccess quan OTCStatus doni SUCCESS, sinó executa onError
public interface TypedCallback<T> {
    void onSuccess(@Nonnull T value);
    void onError(@Nonnull Shared.OTCStatus status, @Nullable String message);
}