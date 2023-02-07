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

public class DeleteRouteTask extends AsyncTask<Context, Void, Boolean> {

    private long routeId;

    public DeleteRouteTask(long routeId) {
        this.routeId = routeId;
    }

    @Override
    protected Boolean doInBackground(Context... params) {
        try {
            Context context = params[0];
            MySharedPreferences msp = MySharedPreferences.createLogin(context);

            MyTrip.Status.Builder deleteBuilder = MyTrip.Status.newBuilder();
            deleteBuilder.setRouteId(routeId);
            deleteBuilder.setStatus(MyTrip.RouteStatus.DELETED);

            Shared.OTCResponse response = ApiCaller.doCall(Endpoints.ROUTE_STATUS, msp.getBytes("token"), deleteBuilder.build(), Shared.OTCResponse.class);

            return response.getStatus() == Shared.OTCStatus.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
