package com.otcengineering.white_app.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.otc.alice.api.model.General;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.utils.MySharedPreferences;

/**
 * Created by cenci7
 */

public class DeletePoiTask extends AsyncTask<Context, Void, Boolean> {

    private long poiId;

    protected DeletePoiTask(long poiId) {
        this.poiId = poiId;
    }

    @Override
    protected Boolean doInBackground(Context... params) {
        try {
            Context context = params[0];
            MySharedPreferences msp = MySharedPreferences.createLogin(context);

            General.POI.Builder deleteBuilder = General.POI.newBuilder();
            deleteBuilder.setPoiId(poiId);

            Shared.OTCResponse response = ApiCaller.doCall(Endpoints.ROUTE_DELETE_POI, msp.getBytes("token"), deleteBuilder.build(), Shared.OTCResponse.class);
            return response.getStatus() == Shared.OTCStatus.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
