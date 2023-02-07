package com.otcengineering.white_app.tasks;

import android.os.AsyncTask;

import com.otc.alice.api.model.General;
import com.otc.alice.api.model.MyTrip;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.serialization.pojo.RouteItem;
import com.otcengineering.white_app.utils.DateUtils;

import org.threeten.bp.ZoneId;

public abstract class CreatePendingTask extends AsyncTask<Object, Void, Long> {
    private RouteItem rouItem;

    public CreatePendingTask(final RouteItem routeItem) {
        this.rouItem = routeItem;
    }

    @Override
    protected Long doInBackground(Object... objects) {
        MyTrip.RouteNew rnew = MyTrip.RouteNew.newBuilder()
                .setType(General.RouteType.PENDING)
                .setDateStart(DateUtils.dateTimeToString(rouItem.getDateStart(), "yyyy-MM-dd HH:mm:ss"))
                .setDateEnd(DateUtils.dateTimeToString(rouItem.getDateStart(), "yyyy-MM-dd HH:mm:ss"))
                .setLocalDateStart(DateUtils.dateTimeToString(rouItem.getDateStart(), "yyyy-MM-dd HH:mm:ss", ZoneId.systemDefault()))
                .setTitle(rouItem.getTitle())
                .setDescription(rouItem.getDescription())
                .build();

        try {
            MyTrip.RouteId rid = ApiCaller.doCall(Endpoints.ROUTE_NEW, true, rnew, MyTrip.RouteId.class);
            return rid.getRouteId();
        } catch (ApiCaller.OTCException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected abstract void onPostExecute(Long aLong);
}