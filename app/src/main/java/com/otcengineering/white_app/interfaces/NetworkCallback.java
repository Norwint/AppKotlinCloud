package com.otcengineering.white_app.interfaces;

public interface NetworkCallback<T> {
    void onSuccess(T response) throws Exception;
    void onFailure(int code, String errorMsg) throws Exception;
}
