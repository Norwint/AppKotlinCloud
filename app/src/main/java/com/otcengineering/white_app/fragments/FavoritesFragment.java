package com.otcengineering.white_app.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import com.otc.alice.api.model.General;
import com.otc.alice.api.model.MyTrip;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.activities.FiltersFavouritesActivity;
import com.otcengineering.white_app.adapter.FavoriteRouteAdapter;
import com.otcengineering.white_app.interfaces.FragmentBackPresser;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.serialization.pojo.RouteItem;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.MapUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.PrefsManager;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class FavoritesFragment extends BaseFragment implements FragmentBackPresser {

    private Button btnEdit;
    private LinearLayout btnFilter;
    private ImageView imgFilter;
    private TextView txtFilter;
    private RecyclerView recycler;
    private FrameLayout btnScrollUp;
    private Button btnNewRoute;
    private LinearLayout layoutEdit;
    private View btnCancel, btnSelectAll, btnUnfavorite;

    private FavoriteRouteAdapter adapter;

    private boolean editMode;

    private int page = 1;
    private boolean retrievingNewPage = false;

    private MyRoutesFragment.RouteListener listener;

    private GetFavoritesRoutesTask getFavoritesRoutesTask;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_favorites, container, false);
        retrieveViews(v);

        if (!MySharedPreferences.createFilter(getContext()).contains("filters")) {
            MySharedPreferences.createFilter(getContext()).putInteger("filters", 0x3F);
        }

        setEvents();
        configureAdapter();
        return v;
    }

    @Override
    public boolean onBackPressed() {
        if (editMode) {
            manageEditMode();
            return false;
        } else {
            return true;
        }
    }

    private void retrieveViews(View v) {
        btnEdit = v.findViewById(R.id.favorites_btnEdit);
        btnFilter = v.findViewById(R.id.favorites_btnFilter);
        imgFilter = v.findViewById(R.id.favorites_imgFilter);
        txtFilter = v.findViewById(R.id.favorites_txtFilter);
        recycler = v.findViewById(R.id.favorites_recycler);
        btnScrollUp = v.findViewById(R.id.favorites_btnScrollUp);
        btnNewRoute = v.findViewById(R.id.favorites_btnNewRoute);
        layoutEdit = v.findViewById(R.id.favorites_layoutEdit);
        btnCancel = v.findViewById(R.id.favorites_btnCancel);
        btnSelectAll = v.findViewById(R.id.favorites_btnSelectAll);
        btnUnfavorite = v.findViewById(R.id.favorites_btnUnfavorite);
    }

    private void setEvents() {
        btnNewRoute.setOnClickListener(view -> {
            if (listener != null) {
                listener.onNewRoute();
            }
        });

        btnEdit.setOnClickListener(view -> manageEditMode());

        btnCancel.setOnClickListener(view -> manageEditMode());

        btnSelectAll.setOnClickListener(view -> {
            adapter.selectAll();
            adapter.notifyDataSetChanged();
        });

        btnUnfavorite.setOnClickListener(view -> {
            List<RouteItem> routesSelected = adapter.getItemsSelected();
            listener.onUnfavoriteRoutes(routesSelected);
            btnCancel.performClick();
        });

        btnFilter.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), FiltersFavouritesActivity.class);
            startActivity(intent);
        });

        btnScrollUp.setOnClickListener(view -> recycler.smoothScrollToPosition(0));
    }

    private void manageEditMode() {
        editMode = !editMode;
        manageButtonEditUI();
        manageLayoutEditUI();
        if (editMode) {
            adapter.openAll();
        } else {
            adapter.closeAll();
        }
        adapter.notifyDataSetChanged();
    }

    private void closeMenu() {
        editMode = false;
        manageButtonEditUI();
        manageLayoutEditUI();
        adapter.closeAll();
        adapter.notifyDataSetChanged();
    }

    private void manageButtonEditUI() {
        btnEdit.setBackgroundResource(editMode ? R.drawable.buttonshape_luis_blue : R.drawable.buttonshape_luis);
        btnEdit.setTextColor(getResources().getColor(editMode ? R.color.colorWhite : R.color.colorPrimary));
    }

    @Override
    public void onPause() {
        closeMenu();
        super.onPause();
    }

    private void manageLayoutEditUI() {
        btnNewRoute.setVisibility(editMode ? View.GONE : View.VISIBLE);
        layoutEdit.setVisibility(editMode ? View.VISIBLE : View.GONE);
    }

    private void configureAdapter() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recycler.setLayoutManager(layoutManager);
        adapter = new FavoriteRouteAdapter(getActivity(), new FavoriteRouteAdapter.RouteListener() {
            @Override
            public void onRouteItemSelected(int position) {
                if (listener != null) {
                    RouteItem routeItem = adapter.getData(position);
                    listener.onRouteSelected(routeItem);
                }
            }

            @Override
            public void onMenu(int position) {
                if (listener != null) {
                    RouteItem routeItem = adapter.getData(position);
                    listener.onShowMenu(true, routeItem);
                }
            }

            @Override
            public void onUnfavorite() {
                refreshList();
            }
        });
        recycler.setAdapter(adapter);
        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                btnScrollUp.setVisibility(dy > 0 ? View.VISIBLE : View.GONE);

                if (dy > 0) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        if (!retrievingNewPage) {
                            retrievingNewPage = true;
                            page++;
                            getFavoriteRoutes();
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        manageFilterUI();
        resetPage();
        showFavoritesRoutesFromDB();
        getFavoriteRoutes();
    }

    private void resetPage() {
        page = 1;
    }

    private boolean hasFilter() {
        MySharedPreferences msp = MySharedPreferences.createFilter(getActivity());
        return msp.getInteger("filters") != 0;
    }

    private void manageFilterUI() {
        boolean filter = hasFilter();
        imgFilter.setImageResource(filter ? R.drawable.my_routes_icons_9 : R.drawable.my_routes_icons_10);
        txtFilter.setVisibility(filter ? View.VISIBLE : View.GONE);
    }

    private void getFavoriteRoutes() {
        if (ConnectionUtils.isOnline(getContext())) {
            getFavoritesRoutesTask = new GetFavoritesRoutesTask();
            getFavoritesRoutesTask.execute();
        } else {
            ConnectionUtils.showOfflineToast();

        }
    }

    private boolean isFirstPage() {
        return page == 1;
    }

    @SuppressLint("StaticFieldLeak")
    private class GetFavoritesRoutesTask extends AsyncTask<String, Void, List<RouteItem>> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (isFirstPage()) {
                try {
                    progressDialog = new ProgressDialog(getContext());
                    progressDialog.setMessage(getString(R.string.loading));
                    progressDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected List<RouteItem> doInBackground(String... strings) {
            try {
                MySharedPreferences pref = MySharedPreferences.createLogin(getContext());

                MyTrip.RoutesFavourite.Builder routesBuilder = MyTrip.RoutesFavourite.newBuilder();
                MySharedPreferences msp = MySharedPreferences.createFilter(getActivity());

                int filter = msp.getInteger("filters");

                routesBuilder.setMyPlanned((filter & 0x01) == 0x01);
                routesBuilder.setMyDone((filter & 0x02) == 0x02);

                routesBuilder.setFriendsPlanned((filter & 0x04) == 0x04);
                routesBuilder.setFriendsDone((filter & 0x08) == 0x08);

                routesBuilder.setCommunityPlanned((filter & 0x10) == 0x10);
                routesBuilder.setCommunityDone((filter & 0x20) == 0x20);

                routesBuilder.setPage(page);
                MyTrip.RoutesFavouriteResponse response = ApiCaller.doCall(Endpoints.ROUTES_FAVOURITE, pref.getBytes("token"), routesBuilder.build(), MyTrip.RoutesFavouriteResponse.class);
                ArrayList<RouteItem> rts = new ArrayList<>();
                for (MyTrip.RouteFavourite rf : response.getRoutesList()) {
                    MyTrip.Route route = rf.getRoute();
                    RouteItem routeItem = new RouteItem(rf);
                    MySharedPreferences loc = MySharedPreferences.createLocationSecurity(getContext());
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
                    rts.add(routeItem);
                }
                return rts;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new ArrayList<>();
        }

        @Override
        protected void onPostExecute(List<RouteItem> routeFavourites) {
            super.onPostExecute(routeFavourites);
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            retrievingNewPage = false;
            showFavoriteRouteList(routeFavourites);
        }
    }

    private void showFavoriteRouteList(List<RouteItem> routeFavourites) {
        if (isFirstPage()) {
            adapter.clearItems();
            PrefsManager.getInstance().saveFavoritesRoutes(routeFavourites, getContext());
        }
        adapter.addItems(routeFavourites);
        adapter.notifyDataSetChanged();
    }

    public void setListener(MyRoutesFragment.RouteListener listener) {
        this.listener = listener;
    }

    void refreshList() {
        page = 1;
        getFavoriteRoutes();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (getFavoritesRoutesTask != null) {
            getFavoritesRoutesTask.cancel(true);
        }
    }

    private void showFavoritesRoutesFromDB(){
        List<RouteItem> doneRoutes = PrefsManager.getInstance().getFavoritesRoutes(getContext());
        if (doneRoutes != null){
            showFavoriteRouteList(doneRoutes);
        }
    }
}
