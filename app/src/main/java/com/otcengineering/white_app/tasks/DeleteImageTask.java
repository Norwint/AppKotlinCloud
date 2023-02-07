package com.otcengineering.white_app.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otc.alice.api.model.MyTrip;
import com.otc.alice.api.model.Shared;

/**
 * Created by cenci7
 */

public class DeleteImageTask extends AsyncTask<Context, Void, Boolean> {

    private long imageId;

    protected DeleteImageTask(long imageId) {
        this.imageId = imageId;
    }

    @Override
    protected Boolean doInBackground(Context... params) {
        try {
            Context context = params[0];
            MySharedPreferences msp = MySharedPreferences.createLogin(context);

            MyTrip.PoiImage.Builder deleteBuilder = MyTrip.PoiImage.newBuilder();
            deleteBuilder.setFilId(imageId);
            Shared.OTCResponse response = ApiCaller.doCall(Endpoints.ROUTE_POI_DELETE_IMAGE, msp.getBytes("token"), deleteBuilder.build(), Shared.OTCResponse.class);

            return response.getStatus() == Shared.OTCStatus.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
