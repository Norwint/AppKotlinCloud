package com.otcengineering.white_app.tasks;

import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.InvalidProtocolBufferException;
import com.otc.alice.api.model.Shared;
import com.otc.alice.api.model.Welcome;
import com.otcengineering.white_app.MyApp;
import com.otcengineering.white_app.interfaces.NetworkCallback;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.utils.Logger;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.Utils;
import com.otcengineering.white_app.utils.interfaces.Callback;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;

public class NetTask extends AsyncTask<Object, Object, NetTask.JsonResponse> {
    private static long sLoginTimeout = 0;
    private final JsonRequest mMsg;
    private final String mUrl, mUri;
    private final boolean mAuth;
    private String mMethod;
    private NetworkCallback<JsonResponse> mCallback;

    // Per fer GETS de manera senzilla
    public NetTask(String uri, boolean auth, NetworkCallback<JsonResponse> callback) {
        mUrl = Endpoints.URL_BASE;
        mUri = uri;
        mMsg = null;
        mAuth = auth;
        mMethod = "GET";
        mCallback = callback;
    }

    public static String urlify(String base, String... params) {
        StringBuilder baseBuilder = new StringBuilder(base);
        for (int i = 0; i < params.length; i++) {
            String param = params[i];
            baseBuilder.append(param);
            if (i < params.length - 1) {
                baseBuilder.append("/");
            }
        }
        return baseBuilder.toString();
    }

    // Per fer peticions JSON contra el servidor comú
    public NetTask(String uri, JsonRequest msg, boolean auth, NetworkCallback<JsonResponse> callback) {
        mUrl = Endpoints.URL_BASE;
        mUri = uri;
        mMsg = msg;
        mAuth = auth;
        mMethod = msg != null ? "POST" : "GET";
        mCallback = callback;
    }

    // Per fer peticions JSON contra el servidor comú
    public NetTask(String uri, JsonRequest msg, boolean auth, String method, NetworkCallback<JsonResponse> callback) {
        mUrl = Endpoints.URL_BASE;
        mUri = uri;
        mMsg = msg;
        mAuth = auth;
        mMethod = method;
        mCallback = callback;
    }

    // Si és obvi, no cal especificar el mètode
    public NetTask(String url, String uri, JsonRequest msg, boolean auth, NetworkCallback<JsonResponse> callback) {
        mUrl = url;
        mUri = uri;
        mMsg = msg;
        mAuth = auth;
        mMethod = msg != null ? "POST" : "GET";
        mCallback = callback;
    }

    // Personalització completa
    public NetTask(String url, String uri, JsonRequest msg, boolean auth, String method, NetworkCallback<JsonResponse> callback) {
        mUrl = url;
        mUri = uri;
        mMsg = msg;
        mAuth = auth;
        mMethod = method;
        mCallback = callback;
    }

    public static class JsonResponse {
        private int mCode;
        private String mResponse;
        private String mErrorMsg;

        public int getCode() {
            return mCode;
        }

        public String getErrorMsg() {
            return mErrorMsg;
        }

        public <T> T getResponse(Class<T> toConvert) {
            return Utils.getGson().fromJson(mResponse, toConvert);
        }

        public <T> T getResponse(Type type) {
            return Utils.getGson().fromJson(mResponse, type);
        }

        public String getRaw() {
            return mResponse;
        }

        public static JsonResponse fromString(int code, @Nullable String body) {
            JsonResponse resp = new JsonResponse();
            resp.mCode = code;
            resp.mErrorMsg = null;
            resp.mResponse = body;
            return resp;
        }

        public static JsonResponse fromCode(int code, @Nullable String errorMsg, @Nullable String msg) {
            JsonResponse resp = new JsonResponse();
            resp.mCode = code;
            resp.mErrorMsg = errorMsg;
            resp.mResponse = msg;
            return resp;
        }
    }

    public static class JsonRequest {
        private String mRequest;
        public static <T> JsonRequest create(T object) {
            JsonRequest req = new JsonRequest();
            req.mRequest = Utils.getGson().toJson(object);
            return req;
        }

        public static JsonRequest create(JSONObject object) {
            JsonRequest req = new JsonRequest();
            req.mRequest = object.toString();
            return req;
        }
    }

    @Override
    protected JsonResponse doInBackground(Object... objects) {
        OkHttpClient client = checkClient();

        Request.Builder httpRequest = new Request.Builder();

        byte[] token = mAuth ? MySharedPreferences.createLogin(MyApp.getContext()).getBytes("token") : "public_access:0".getBytes();
        StringBuilder authorization = new StringBuilder("A1 ");
        for (byte b : token) authorization.append((char) b);

        try {
            httpRequest.header("Authorization", authorization.toString());
            authorization.setLength(0);
        } catch (IllegalArgumentException ignored) {
            // Boo...
            return null;
        } finally {
            Arrays.fill(token, (byte) 0);
        }

        if (mMsg == null) {
            httpRequest.method(mMethod, null); // If method does not receive any parameter should be annotated with @GET (in api SERVER) in order to match calling method!
        } else {
            httpRequest.method(mMethod, new RequestBody() { // If method does not receive any parameter should be annotated with @POST (in api server) in order to match calling method!
                @Override
                public MediaType contentType() {
                    return MediaType.parse("application/json");
                }

                @Override
                public void writeTo(@NonNull BufferedSink bs) throws IOException {
                    bs.outputStream().write(mMsg.mRequest.getBytes());
                }
            });
        }
        httpRequest.url(mUrl + mUri);
        Logger.d("NetTask", mUrl + mUri);
        try {
            Response httpResponse = client.newCall(httpRequest.build()).execute();
            if (httpResponse.isSuccessful() && httpResponse.code() == 200) {
                ResponseBody body = httpResponse.body();
                assert body != null;
                return JsonResponse.fromString(200, body.string());
            } else {
                return JsonResponse.fromCode(httpResponse.code(), httpResponse.message(), httpResponse.body().string());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(JsonResponse otcResponse) {
        super.onPostExecute(otcResponse);
        try {
            if (otcResponse != null && otcResponse.getCode() == 200) {
                mCallback.onSuccess(otcResponse);
            } else {
                if (otcResponse != null) {
                    mCallback.onFailure(otcResponse.mCode, otcResponse.mErrorMsg);
                } else {
                    mCallback.onFailure(-1, "Server was disconnected by timeout");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean login() throws ApiCaller.OTCException {
        MySharedPreferences msp = MySharedPreferences.createLogin(MyApp.getContext());
        Welcome.Login log = Welcome.Login.newBuilder().setUsername(msp.getString("Nick")).setPassword(msp.getString("Pass")).build();
        Shared.OTCResponse resp = ApiCaller.doCall(Endpoints.LOGIN, log, Shared.OTCResponse.class);
        if (resp != null && resp.getStatus() == Shared.OTCStatus.SUCCESS) {
            try {
                Welcome.LoginResponse response = Welcome.LoginResponse.parseFrom(resp.getData().getValue());
                msp.putString("token", response.getApiToken());
                return true;
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private static OkHttpClient checkClient() {
        return new OkHttpClient.Builder().build();
    }
}
