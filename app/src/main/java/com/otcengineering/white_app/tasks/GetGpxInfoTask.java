package com.otcengineering.white_app.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.network.utils.ApiCaller;

/**
 * Created by cenci7
 */

public class GetGpxInfoTask extends AsyncTask<Context, Object, byte[]> {

    private long fileId;

    protected GetGpxInfoTask(long fileId) {
        this.fileId = fileId;
    }

    @Override
    protected byte[] doInBackground(Context... params) {
        try {
            Context context = params[0];
            MySharedPreferences msp = MySharedPreferences.createLogin(context);
            String url = Endpoints.FILE_GET + fileId;
            return ApiCaller.getImage(url, msp.getString("token"));
        } catch (Exception e) {
            return null;
        }
    }
}