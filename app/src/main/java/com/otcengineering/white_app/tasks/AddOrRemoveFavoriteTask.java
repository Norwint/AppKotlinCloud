package com.otcengineering.white_app.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otc.alice.api.model.MyTrip;
import com.otc.alice.api.model.Shared;

import java.util.concurrent.Semaphore;

/**
 * Created by cenci7
 */

public class AddOrRemoveFavoriteTask extends AsyncTask<Context, Void, Boolean> {

    private long routeId;
    private boolean isFavorite;
    protected static Semaphore s_sem = new Semaphore(1, true);

    protected AddOrRemoveFavoriteTask(long routeId, boolean isFavorite) {
        this.routeId = routeId;
        this.isFavorite = isFavorite;
    }

    @Override
    protected Boolean doInBackground(Context... params) {
        try {
            s_sem.acquire();

            Context context = params[0];
            MySharedPreferences msp = MySharedPreferences.createLogin(context);

            MyTrip.RouteId.Builder route = MyTrip.RouteId.newBuilder();
            route.setRouteId(routeId);

            String url = isFavorite ?
                    Endpoints.ROUTE_UNFAV : Endpoints.ROUTE_FAV;

            Shared.OTCResponse response = ApiCaller.doCall(url, msp.getBytes("token"), route.build(), Shared.OTCResponse.class);
            return response.getStatus() == Shared.OTCStatus.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
