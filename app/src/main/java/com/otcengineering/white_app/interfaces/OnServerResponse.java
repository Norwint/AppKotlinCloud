package com.otcengineering.white_app.interfaces;

import androidx.annotation.Nullable;

import com.otc.alice.api.model.Shared;

public interface OnServerResponse {
    void onResponse(boolean success, @Nullable Shared.OTCResponse data);
}
