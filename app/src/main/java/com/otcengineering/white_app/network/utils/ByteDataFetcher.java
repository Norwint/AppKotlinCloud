package com.otcengineering.white_app.network.utils;

import android.accounts.NetworkErrorException;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.otcengineering.white_app.MyApp;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.utils.MySharedPreferences;

import java.io.IOException;
import java.nio.ByteBuffer;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;

public class ByteDataFetcher implements DataFetcher<ByteBuffer> {
    private final Long model;

    ByteDataFetcher(Long model) {
        this.model = model;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super ByteBuffer> callback) {
        if (model > 0) {
            byte[] token = MySharedPreferences.createLogin(MyApp.getContext()).getBytes("token");
            StringBuilder authorization = new StringBuilder("A1 ");
            for (byte b : token) authorization.append((char) b);

            Log.d("ApiCaller", Endpoints.FILE_GET + model);

            Request.Builder httpRequest = new Request.Builder();
            httpRequest.url(Endpoints.URL_BASE + "v1" + Endpoints.FILE_GET + model);
            httpRequest.addHeader("Authorization", authorization.toString());

            OkHttpClient client = new OkHttpClient.Builder().build();

            Response httpResponse;
            try {
                httpResponse = client.newCall(httpRequest.build()).execute();
                if (httpResponse.code() == 200) {
                    Buffer buffer = new Buffer();

                    while (!httpResponse.body().source().exhausted()) {
                        httpResponse.body().source().read(buffer, 1024);
                    }

                    callback.onDataReady(ByteBuffer.wrap(buffer.readByteArray()));
                } else {
                    callback.onLoadFailed(new NetworkErrorException("Code " + httpResponse.code()));
                }
            } catch (IOException e) {
                e.printStackTrace();
                callback.onLoadFailed(e);
            }
        } else {
            callback.onLoadFailed(new Exception());
        }
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void cancel() {

    }

    @NonNull
    @Override
    public Class<ByteBuffer> getDataClass() {
        return ByteBuffer.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.LOCAL;
    }
}
