/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.otcengineering.white_app.network.utils;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.protobuf.Message;
import com.otc.alice.api.model.Shared.OTCResponse;
import com.otc.alice.api.model.Shared.OTCStatus;
import com.otcengineering.white_app.MyApp;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.components.CustomProgressDialog;
import com.otcengineering.white_app.utils.Logger;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.NetworkManager;
import com.otcengineering.white_app.utils.Utils;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;

import static com.otcengineering.white_app.network.Endpoints.URL_BASE;

/**
 * @author dset
 */
public class ApiCaller {
    private static final int timeout = 3;
    private static final TimeUnit timeoutUnit = TimeUnit.MINUTES;
    private static ConnectionPool[] s_connectionPools = new ConnectionPool[10];

    public static synchronized void setPools() {
        for (int i = 0; i < s_connectionPools.length; ++i) {
            s_connectionPools[i] = new ConnectionPool(10, timeout, timeoutUnit);
        }
    }

    private static OkHttpClient checkClient() {
        return new OkHttpClient.Builder().connectTimeout(timeout, timeoutUnit).readTimeout(timeout, timeoutUnit).writeTimeout(timeout, timeoutUnit).connectionPool(getConnectionPool()).build();
    }

    private static int s_poolIter = 0;
    private static synchronized ConnectionPool getConnectionPool() {
        ConnectionPool conn = s_connectionPools[s_poolIter % s_connectionPools.length];
        s_poolIter++;
        return conn;
    }

    private static synchronized OkHttpClient checkClientTimeout() {
        return new OkHttpClient.Builder().connectTimeout(3 * timeout, timeoutUnit).readTimeout(3 * timeout, timeoutUnit).writeTimeout(3 * timeout, timeoutUnit).build();
    }

    public static <T extends Message> T doCall(String uri, Message request, Class<T> response) throws OTCException {
        return doCall(uri, "public_access:0".getBytes(), request, response);
    }

    public static <T extends Message> T doCall(String uri, byte[] token, final Message request, Class<T> response) throws OTCException {
        return doCall(URL_BASE, uri, token, request != null ? "POST" : "GET", request, response);
    }

    public static <T extends Message> T doCall(String url, boolean auth, final Message request, Class<T> response) throws OTCException {
        return doCall(URL_BASE, url, auth ? MySharedPreferences.createLogin(MyApp.getContext()).getBytes("token") : "public_access:0".getBytes(), request != null ? "POST" : "GET", request, response);
    }

    public static <T extends Message> T doCall(String url, boolean auth, final Message request, String method, Class<T> response) throws OTCException {
        return doCall(URL_BASE, url, auth ? MySharedPreferences.createLogin(MyApp.getContext()).getBytes("token") : "public_access:0".getBytes(), method, request, response);
    }

    private static Semaphore s_loginSemaphore = new Semaphore(1, true);
    private static int s_loginTries = 0;

    public static <T extends Message> T doCall(String url, String uri, boolean auth, final Message request, Class<T> response) throws OTCException {
        return doCall(url, uri, auth ? MySharedPreferences.createLogin(MyApp.getContext()).getBytes("token") : "public_access:0".getBytes(), request != null ? "POST" : "GET", request, response);
    }

    public static <T extends Message> T doCall(String url, String uri, byte[] token, String method, final Message request, Class<T> response) throws OTCException {
        T returnValue = null;

        OkHttpClient client = checkClient();

        Logger.d("ApiCaller", "Start: " + uri);

        if (token == null) {
            if (NetworkManager.login(MyApp.getContext(), null, null)) {
                token = MySharedPreferences.createLogin(MyApp.getContext()).getBytes("token");
                if (token == null) {
                    return null;
                }
            } else {
                return null;
            }
        }

        try {
            StringBuilder authorization = new StringBuilder("A1 ");
            for (byte b : token) authorization.append((char) b);
            Request.Builder httpRequest = new Request.Builder();
            try {
                httpRequest.header("Authorization", authorization.toString());
                authorization.setLength(0);
            } catch (IllegalArgumentException ignored) {
                // Boo...
                Arrays.fill(token, (byte)0);
                return null;
            }
            if (request == null) {
                httpRequest.method(method, null); // If method does not receive any parameter should be annotated with @GET (in api SERVER) in order to match calling method!
            } else {
                httpRequest.method(method, new RequestBody() { // If method does not receive any parameter should be annotated with @POST (in api server) in order to match calling method!
                    @Override
                    public MediaType contentType() {
                        return MediaType.parse("application/x-protobuf");
                    }

                    @Override
                    public void writeTo(@NonNull BufferedSink bs) throws IOException {
                        request.writeTo(bs.outputStream());
                    }
                });
            }
            httpRequest.url(url + "v1" + uri);

            Response httpResponse = client.newCall(httpRequest.build()).execute();

            if (httpResponse.isSuccessful()) {
                ResponseBody body = httpResponse.body();
                assert body != null;
                OTCResponse otcResponse = OTCResponse.parseFrom(body.byteStream());

                if (response.isAssignableFrom(OTCResponse.class)) {
                    returnValue = (T) otcResponse;
                } else if (otcResponse.getStatus() == OTCStatus.SUCCESS) {
                    if (otcResponse.hasData()) {
                        returnValue = otcResponse.getData().unpack(response);
                    }
                } else if (otcResponse.getStatus() == OTCStatus.INVALID_AUTHORIZATION && !Arrays.equals(token, "public_access:0".getBytes())) {
                    s_loginSemaphore.acquire();
                    if (!MyApp.loginLock) {
                        MyApp.loginLock = true;
                        new Timer("ApiCallerTimer").schedule(new TimerTask() {
                            @Override
                            public void run() {
                                MyApp.loginLock = false;
                            }
                        }, 2000);
                        MySharedPreferences.createLogin(MyApp.getContext()).remove("token");
                        if (NetworkManager.login(MyApp.getContext(), null, null)) {
                            s_loginTries = 0;
                        } else {
                            ++s_loginTries;
                        }
                    }
                    s_loginSemaphore.release();
                    if (s_loginTries < 3) {
                        return ApiCaller.doCall(url, uri, MySharedPreferences.createLogin(MyApp.getContext()).getBytes("token"), method, request, response);
                    } else {
                        if (MySharedPreferences.createLogin(MyApp.getContext()).contains("token")) {
                            Utils.showPopup("NoLogin", "",
                                    "Cannot process network request. Please, try to login again. App will logout.", () -> Utils.logout(MyApp.getContext()));
                        }
                        throw new OTCException(OTCStatus.SERVER_ERROR, "Timeout");
                    }
                } else {
                    if (MySharedPreferences.createLogin(MyApp.getContext()).contains("loggin 2.0") && otcResponse.getStatus() == OTCStatus.USER_NOT_ENABLED) {
                        Utils.showPopup("Banned", "", MyApp.getContext().getString(R.string.user_blocked), () -> Utils.logout(MyApp.getContext()));
                    }
                    Logger.e("ApiCaller", String.format("Status: %s", otcResponse.getStatus().name()));
                    throw new OTCException(otcResponse.getStatus(), "test" + otcResponse.getMessage());
                }
            } else {
                Logger.e("ApiCaller", String.format(Locale.US, "%d: %s", httpResponse.code(), httpResponse.message()));
            }
        } catch (IOException e) {
            if (e.getClass() == SocketTimeoutException.class) {
                Utils.runOnMainThread(() -> Toast.makeText(MyApp.getContext(), "Network timeout", Toast.LENGTH_LONG).show());
            }
            throw new OTCException(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            Arrays.fill(token, (byte) 0);
            System.gc();
            Logger.d("ApiCaller", "End: " + uri);
        }

        return returnValue;
    }

    public static <T extends Message> T doCallWithProgress(String uri, String token, final Message request, Class<T> response, @NonNull final Context ctx) throws OTCException {
        T returnValue = null;

        AtomicBoolean shouldExit = new AtomicBoolean(false);

        AtomicReference<CustomProgressDialog> cpd = new AtomicReference<>();
        Utils.runOnMainThread(() -> {
            Context cont = ctx;
            if (Utils.isActivityFinish(ctx)) {
                cont = MyApp.getContext();
            }
            cpd.set(new CustomProgressDialog(cont));
            cpd.get().show();
            cpd.get().setTitle(R.string.uploading);
            cpd.get().setOnCancelCallback(() -> shouldExit.set(true));
            cpd.get().update(0);
        });
        
        OkHttpClient clientExtendedTimeout = checkClientTimeout();

        try {
            String authorization = "A1 " + token;
            Request.Builder httpRequest = new Request.Builder();
            httpRequest.header("Authorization", authorization);
            if (request == null) {
                httpRequest.method("GET", null); // If method does not receive any parameter should be annotated with @GET (in api SERVER) in order to match calling method!
            } else {
                httpRequest.method("POST", new RequestBody() { // If method does not receive any parameter should be annotated with @POST (in api server) in order to match calling method!
                    @Override
                    public MediaType contentType() {
                        return MediaType.parse("application/x-protobuf");
                    }

                    @Override
                    public void writeTo(@NonNull BufferedSink bs) throws IOException {
                        byte[] bytes = request.toByteArray();
                        int i = 0;
                        while (i < bytes.length) {
                            int step = 1 << 16;
                            if (i + step > bytes.length) {
                                step = bytes.length - i;
                            }
                            if (shouldExit.get()) {
                                break;
                            }
                            bs.write(bytes, i, step);
                            i += step;
                            //Log.d("Progress", String.format("Task: %s - Progress: %d of %d", uri, i, bytes.length));
                            int finalI = i;
                            Utils.runOnMainThread(() -> cpd.get().update((float) finalI /bytes.length));
                        }
                    }
                });
            }
            httpRequest.url(URL_BASE + "v1" + uri);

            Response httpResponse = clientExtendedTimeout.newCall(httpRequest.build()).execute();

            if (shouldExit.get()) {
                return null;
            }
            if (cpd.get() != null) {
                cpd.get().dismiss();
            }

            if (httpResponse.isSuccessful()) {
                ResponseBody body = httpResponse.body();
                assert body != null;
                OTCResponse otcResponse = OTCResponse.parseFrom(body.byteStream());

                if (response.isAssignableFrom(OTCResponse.class)) {
                    returnValue = (T) otcResponse;
                } else if (otcResponse.getStatus() == OTCStatus.SUCCESS) {
                    if (otcResponse.hasData()) {
                        returnValue = otcResponse.getData().unpack(response);
                    }
                } else {
                    throw new OTCException(otcResponse.getStatus(), "test" + otcResponse.getMessage());
                }
            }
        } catch (IOException e) {
            throw new OTCException(e);
        }

        return returnValue;
    }

    public static byte[] getImage(String uri, String token) throws OTCException {
        byte[] returnValue;
        try {
            String authorization = "A1 " + token;
            Request.Builder httpRequest = new Request.Builder();
            httpRequest.header("Authorization", authorization);
            httpRequest.method("GET", null); // If method does not receive any parameter should be annotated with @GET (in api SERVER) in order to match calling method!

            httpRequest.url(URL_BASE + "v1" + uri);

            OkHttpClient client = checkClient();
            Response httpResponse = client.newCall(httpRequest.build()).execute();
            Buffer buffer = new Buffer();

            while (!httpResponse.body().source().exhausted()) {
                httpResponse.body().source().read(buffer, 1024);
            }

            returnValue = buffer.readByteArray();
        } catch (IOException e) {
            throw new OTCException(e);
        }

        return returnValue;
    }

    public static class OTCException extends Exception {

        OTCStatus status;

        public OTCException(OTCStatus status, String message) {
            super(message);
            this.status = status;
        }

        public OTCException(Throwable cause) {
            super(cause);
        }

        public OTCStatus getStatus() {
            return status;
        }

        @NonNull
        @Override
        public String toString() {
            return "" + getMessage();
        }

        @Override
        public String getMessage() {
            String message = (status != null ? "status=" + status.name() + "\n" : "");
            if (super.getMessage() != null && !super.getMessage().isEmpty()) {
                message += "message=" + super.getMessage();
            }

            return message;
        }

    }

}
