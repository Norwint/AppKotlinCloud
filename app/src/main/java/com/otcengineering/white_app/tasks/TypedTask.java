package com.otcengineering.white_app.tasks;

import android.os.AsyncTask;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.otc.alice.api.model.Shared;
import com.otc.alice.api.model.Welcome;
import com.otcengineering.white_app.MyApp;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.interfaces.TypedCallback;

public class TypedTask<T extends Message> extends AsyncTask<Object, Object, Shared.OTCResponse> {
    private static boolean s_loginDone = false;
    private final Message m_msg;
    private final String m_url;
    private final boolean m_auth;
    private String m_method;
    private TypedCallback<T> m_callback;
    private Class<T> m_class;

    public TypedTask(String url, Message msg, boolean auth, Class<T> clazz, TypedCallback<T> callback) {
        m_url = url;
        m_msg = msg;
        m_auth = auth;
        m_method = msg != null ? "POST" : "GET";
        m_callback = callback;
        m_class = clazz;
    }

    public TypedTask(String url, Message msg, boolean auth, String method, Class<T> clazz, TypedCallback<T> callback) {
        m_url = url;
        m_msg = msg;
        m_auth = auth;
        m_method = method;
        m_callback = callback;
        m_class = clazz;
    }

    @Override
    protected final Shared.OTCResponse doInBackground(Object... objects) {
        try {
            Shared.OTCResponse response = ApiCaller.doCall(m_url, m_auth, m_msg, m_method, Shared.OTCResponse.class);
            if (response != null && response.getStatus() == Shared.OTCStatus.INVALID_AUTHORIZATION && m_auth && !s_loginDone) {
                s_loginDone = true;
                if (login()) {
                    response = ApiCaller.doCall(m_url, true, m_msg, m_method, Shared.OTCResponse.class);
                }
            }
            return response;
        } catch (ApiCaller.OTCException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Shared.OTCResponse otcResponse) {
        super.onPostExecute(otcResponse);
        try {
            if (otcResponse.getStatus() == Shared.OTCStatus.SUCCESS) {
                if (m_class == Shared.OTCResponse.class) {
                    m_callback.onSuccess((T) otcResponse);
                } else {
                    m_callback.onSuccess(otcResponse.getData().unpack(m_class));
                }
            } else {
                m_callback.onError(otcResponse.getStatus(), otcResponse.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute() {
        super.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private boolean login() throws ApiCaller.OTCException {
        MySharedPreferences msp = MySharedPreferences.createLogin(MyApp.getContext());
        Welcome.Login log = Welcome.Login.newBuilder().setUsername(msp.getString("Nick")).setPassword(msp.getString("Pass")).build();
        Shared.OTCResponse resp = ApiCaller.doCall(Endpoints.LOGIN, log, Shared.OTCResponse.class);
        if (resp != null && resp.getStatus() == Shared.OTCStatus.SUCCESS) {
            try {
                Welcome.LoginResponse response = Welcome.LoginResponse.parseFrom(resp.getData().getValue());
                msp.putString("token", response.getApiToken());
                s_loginDone = false;
                return true;
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
