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
import com.otcengineering.white_app.utils.interfaces.Callback;

public class GenericTask extends AsyncTask<Object, Object, Shared.OTCResponse> {
    private static long s_loginTimeout = 0;
    private final Message m_msg;
    private final String m_url;
    private final boolean m_auth;
    private String m_method;
    private Callback<Shared.OTCResponse> m_callback;

    public GenericTask(String url, Message msg, boolean auth, Callback<Shared.OTCResponse> callback) {
        m_url = url;
        m_msg = msg;
        m_auth = auth;
        m_method = msg != null ? "POST" : "GET";
        m_callback = callback;
    }

    public GenericTask(String url, Message msg, boolean auth, String method, Callback<Shared.OTCResponse> callback) {
        m_url = url;
        m_msg = msg;
        m_auth = auth;
        m_method = method;
        m_callback = callback;
    }

    @Override
    protected Shared.OTCResponse doInBackground(Object... objects) {
        try {
            Shared.OTCResponse response = ApiCaller.doCall(m_url, m_auth, m_msg, m_method, Shared.OTCResponse.class);
            if (response != null && response.getStatus() == Shared.OTCStatus.INVALID_AUTHORIZATION && m_auth &&
                    (s_loginTimeout == 0 && s_loginTimeout >= System.currentTimeMillis() + 30000)) {
                s_loginTimeout = System.currentTimeMillis();
                if (login()) {
                    response = ApiCaller.doCall(m_url, true, m_msg, m_method, Shared.OTCResponse.class);
                    s_loginTimeout = 0;
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
            m_callback.run(otcResponse);
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
}
