package com.otcengineering.white_app.tasks;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.annimon.stream.Stream;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.fragments.DoneFragment;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.serialization.pojo.PoiWrapper;
import com.otcengineering.white_app.serialization.pojo.RouteItem;
import com.otcengineering.white_app.utils.MapUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import com.otc.alice.api.model.General;
import com.otc.alice.api.model.MyTrip;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.apible.Utils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by cenci7
 */

@SuppressLint("StaticFieldLeak")
public class GetRoutesTask extends AsyncTask<Object, Void, List<RouteItem>> {

    private General.RouteType routeType;
    private ProgressDialog m_progressDialog;

    protected GetRoutesTask(General.RouteType routeType, final Context ctx) {
        this.routeType = routeType;
        m_progressDialog = new ProgressDialog(ctx);
        m_progressDialog.setMessage(ctx.getString(R.string.loading));
    }

    @Override
    protected List<RouteItem> doInBackground(Object... params) {
        List<RouteItem> routeItems = new ArrayList<>();
        try {
            Context context = (Context) params[1];
            Utils.runOnUiThread(() -> {
                try {
                    m_progressDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            MySharedPreferences msp = MySharedPreferences.createLogin(context);

            int page = (int) params[0];
            MyTrip.Routes.Builder routesBuilder = MyTrip.Routes.newBuilder();
            routesBuilder.setRouteType(routeType);
            routesBuilder.setPage(page);
            MyTrip.RoutesResponse response = ApiCaller.doCall(Endpoints.ROUTES, msp.getBytes("token"), routesBuilder.build(), MyTrip.RoutesResponse.class);
            List<MyTrip.Route> routesList = response.getRoutesList();
            DoneFragment.empty = this.routeType == General.RouteType.DONE && routesList.size() == 0;
            for (MyTrip.Route route : routesList) {
                RouteItem routeItem = new RouteItem(route);
                routeItems.add(routeItem);

                MySharedPreferences loc = MySharedPreferences.createLocationSecurity(context);
                if (loc.contains(String.format(Locale.US, "routeGpx_%d", route.getId()))) {
                    String str = loc.getString(String.format(Locale.US, "routeGpx_%d", route.getId()));
                    try {
                        routeItem.setPolyLine(str);
                        List<LatLng> decodedPath = PolyUtil.decode(str);
                        routeItem.setLatLngList(decodedPath);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    String url = Endpoints.FILE_GET + routeItem.getGpxFileId();
                    byte[] bytes = ApiCaller.getImage(url, msp.getString("token"));

                    if (routeItem.getRouteType() == General.RouteType.PLANNED) {
                        try {
                            String str = new String(bytes, StandardCharsets.UTF_8);
                            routeItem.setPolyLine(str);
                            List<LatLng> decodedPath = PolyUtil.decode(str);
                            routeItem.setLatLngList(decodedPath);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        List<LatLng> latLngList = null;
                        if (bytes != null) {
                            latLngList = MapUtils.getGpxInfo(bytes);
                            routeItem.setLatLngList(latLngList);
                        }

                        if (latLngList != null) {
                            routeItem.setPolyLine(PolyUtil.encode(latLngList));
                        }
                    }
                    if (routeItem.getPolyLine() != null) {
                        loc.putString(String.format(Locale.US, "routeGpx_%d", route.getId()), routeItem.getPolyLine());
                    }
                }
                MyTrip.RouteId.Builder poisBuilder = MyTrip.RouteId.newBuilder();
                poisBuilder.setRouteId(routeItem.getId());

                MyTrip.RoutePoisResponse rsp = ApiCaller.doCall(Endpoints.ROUTE_POIS, msp.getBytes("token"), poisBuilder.build(), MyTrip.RoutePoisResponse.class);

                List<PoiWrapper> wrapList = new ArrayList<>();
                Stream.of(rsp.getPoisList()).forEach(p -> {
                    PoiWrapper wrap = new PoiWrapper(p);
                    wrap.setStatus(PoiWrapper.PoiStatus.Modified);
                    wrapList.add(wrap);
                });
                routeItem.setPoiList(wrapList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Utils.runOnUiThread(() -> {
                try {
                    m_progressDialog.dismiss();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            });
        }
        return routeItems;
    }

    public void interrupt() {
        Utils.runOnUiThread(() -> m_progressDialog.dismiss());
    }
}
