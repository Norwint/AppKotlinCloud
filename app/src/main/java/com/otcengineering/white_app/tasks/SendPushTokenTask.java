package com.otcengineering.white_app.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otc.alice.api.model.Shared;
import com.otc.alice.api.model.Welcome;

/**
 * Created by cenci7
 */

public class SendPushTokenTask extends AsyncTask<Context, Object, Boolean>{

    public static final String TAG = "SendPushTokenTask";

    @Override
    protected Boolean doInBackground(Context... params) {
       try {
           Context context = params[0];
           MySharedPreferences msp = MySharedPreferences.createLogin(context);

           String myPushToken = MySharedPreferences.createDefault(context).getString("token");

           Welcome.PushTokenRegistration.Builder builder = Welcome.PushTokenRegistration.newBuilder();
           builder.setToken(myPushToken);
           builder.setPlatform(Welcome.PushTokenRegistration.Platform.GCM);

           String url = Endpoints.REGISTER_PUSH_TOKEN;
           Shared.OTCResponse response = ApiCaller.doCall(url, msp.getBytes("token"), builder.build(), Shared.OTCResponse.class);

           return response.getStatus() == Shared.OTCStatus.SUCCESS;
       } catch (Exception e) {
           //Log.e(TAG, e.getMessage(), e);
       }
       return null;
    }
}
