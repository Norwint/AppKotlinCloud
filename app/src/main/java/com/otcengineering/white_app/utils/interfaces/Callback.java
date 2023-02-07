package com.otcengineering.white_app.utils.interfaces;

public interface Callback<T> {
    void run(T t) throws Exception;
}
