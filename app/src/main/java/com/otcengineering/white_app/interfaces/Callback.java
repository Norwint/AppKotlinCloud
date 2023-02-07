package com.otcengineering.white_app.interfaces;

import com.otc.alice.api.model.Shared;

public interface Callback<T> {
    void onSuccess(T success);
    void onError(Shared.OTCStatus status);
}
