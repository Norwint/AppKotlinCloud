package com.otcengineering.white_app.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otc.alice.api.model.General;
import com.otc.alice.api.model.MyTrip;
import com.otcengineering.white_app.network.utils.ApiCaller;

import java.util.List;

/**
 * Created by cenci7
 */

public class GetPoiInfoTask extends AsyncTask<Context, Void, List<General.POI>> {

    private long routeId;

    protected GetPoiInfoTask(long routeId) {
        this.routeId = routeId;
    }

    @Override
    protected List<General.POI> doInBackground(Context... params) {
        try {
            Context context = params[0];
            MySharedPreferences msp = MySharedPreferences.createLogin(context);

            MyTrip.RouteId.Builder poisBuilder = MyTrip.RouteId.newBuilder();
            poisBuilder.setRouteId(routeId);

            MyTrip.RoutePoisResponse response = ApiCaller.doCall(Endpoints.ROUTE_POIS, msp.getBytes("token"), poisBuilder.build(), MyTrip.RoutePoisResponse.class);

            return response.getPoisList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
