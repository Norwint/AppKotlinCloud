package com.otcengineering.white_app.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.otc.alice.api.model.MyTrip;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.MyApp;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.activities.SaveRouteActivity;
import com.otcengineering.white_app.activities.SendPostActivity;
import com.otcengineering.white_app.components.DialogMultiple;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.serialization.pojo.RouteItem;
import com.otcengineering.white_app.tasks.AddOrRemoveFavoriteTask;
import com.otcengineering.white_app.tasks.DeleteRouteTask;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;

/**
 * Created by Luis on 22/01/2018.
 */

public class RoutesMenuFragment extends BaseFragment {

    public interface RefreshRoutesListener {
        void refresh();
    }
   // private View v;
    private View viewEmpty;
    private TextView btnEdit;
    private View btnEditSeparator;
    private TextView btnAddRemoveFavorites;
    private View btnAddRemoveFavoritesSeparator;
    private TextView btnShare;
    private View btnShareSeparator;
    private TextView btnDelete;
    private TextView btnCancel;
    private TextView btnSave;
    private View btnSaveSeperator;

    private boolean showEdit;

    private RouteItem routeSelected;

    private RefreshRoutesListener listener;

    public void configure(boolean showEdit, RouteItem routeSelected, RefreshRoutesListener listener) {
        this.showEdit = showEdit;
        this.routeSelected = routeSelected;
        this.listener = listener;
        if (isVisible()) {
            configureUI();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_menu_routes, container, false);
        retrieveViews(v);
        setEvents();
        configureUI();
        return v;
    }

    private void retrieveViews(View v) {
        viewEmpty = v.findViewById(R.id.menu_routes_viewEmpty);
        btnEdit = v.findViewById(R.id.menu_routes_btnEdit);
        btnEditSeparator = v.findViewById(R.id.menu_routes_btnEditSeparator);
        btnAddRemoveFavorites = v.findViewById(R.id.menu_routes_btnAddRemoveFavorites);
        btnAddRemoveFavoritesSeparator = v.findViewById(R.id.menu_routes_btnAddRemoveFavoritesSeparator);
        btnShare = v.findViewById(R.id.menu_routes_btnShare);
        btnShareSeparator = v.findViewById(R.id.menu_routes_btnShareSeparator);
        btnDelete = v.findViewById(R.id.menu_routes_btnDelete);
        btnCancel = v.findViewById(R.id.menu_routes_btnCancel);

        btnSave =  v.findViewById(R.id.menu_routes_btnSave);
        btnSaveSeperator =  v.findViewById(R.id.menu_routes_btnSaveSeparator);
    }

    private void setEvents() {
        viewEmpty.setOnClickListener(v -> {
            //do nothing. This is a hack to avoid scroll the list when layoutRankingType is shown
        });

        btnEdit.setOnClickListener(view -> {
            if (routeSelected.hasGpx()) {
                closeMenu();
                openEditRoute();
            }
        });

        btnAddRemoveFavorites.setOnClickListener(view -> {
            if (routeSelected.hasGpx()) {
                addOrRemoveFavorites();
            }
        });

        btnShare.setOnClickListener(view -> {
            if (routeSelected.hasGpx()) {
                closeMenu();
                openShareRoute();
            }
        });

        btnDelete.setOnClickListener(view -> {
            if (routeSelected.hasGpx()) {
                DialogMultiple dm = new DialogMultiple(getContext());
                dm.addButton(getString(R.string.cancel), () -> {});
                dm.addButton(getString(R.string.delete), this::deleteRoute);
                dm.setTitle("Are you sure you want to delete the route?");
                dm.show();
            }
        });

        btnCancel.setOnClickListener(view -> closeMenu());

        btnSave.setOnClickListener(v -> {
            closeMenu();
            refreshRouteList();
            new SaveRouteToAutoSave(routeSelected.getId()).execute();
        });
    }

    private void configureUI() {
        btnEdit.setVisibility(showEdit ? View.VISIBLE : View.GONE);
        btnEditSeparator.setVisibility(showEdit ? View.VISIBLE : View.GONE);
        btnAddRemoveFavorites.setVisibility(View.GONE);
        btnAddRemoveFavoritesSeparator.setVisibility(View.GONE);
        btnShare.setVisibility(View.GONE);
        btnShareSeparator.setVisibility(View.GONE);
        boolean routeIsFav = routeSelected.isFavorite();
        btnAddRemoveFavorites.setText(routeIsFav ? R.string.remove_from_favorite : R.string.add_to_favorite);

        btnSave.setVisibility(showEdit ? View.GONE : View.VISIBLE);
        btnSaveSeperator.setVisibility(showEdit ? View.GONE : View.VISIBLE);

    }

    private void openEditRoute() {
        Intent intent = new Intent(getActivity(), SaveRouteActivity.class);
        RouteItem newRoute = routeSelected.copy();
        newRoute.setConsumptionAvg(routeSelected.getConsumptionAvg() / 100);
        intent.putExtra(Constants.Extras.ROUTE, newRoute.toString());
        intent.putExtra("Edit", true);
        startActivity(intent);
    }

    private void openShareRoute() {
        Intent intent = new Intent(getActivity(), SendPostActivity.class);
        intent.putExtra(Constants.Extras.ROUTE, routeSelected.toString());
        startActivity(intent);
    }

    private void addOrRemoveFavorites() {
        @SuppressLint("StaticFieldLeak")
        AddOrRemoveFavoriteTask addOrRemoveFavoriteTask = new AddOrRemoveFavoriteTask(routeSelected.getId(), routeSelected.isFavorite()) {
            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                if (result) {
                    int messageRes = routeSelected.isFavorite() ?
                            R.string.remove_route_to_favorites_correctly :
                            R.string.add_route_to_favorites_correctly;
                    showCustomDialog(messageRes);
                    refreshRouteList();
                } else {
                    showCustomDialogError();
                }
                closeMenu();
            }
        };
        if (ConnectionUtils.isOnline(getContext())) {
            addOrRemoveFavoriteTask.execute(getActivity());
        } else {
            ConnectionUtils.showOfflineToast();
        }
    }

    private void deleteRoute() {
        @SuppressLint("StaticFieldLeak")
        DeleteRouteTask deleteRouteTask = new DeleteRouteTask(routeSelected.getId()) {

            private ProgressDialog progressDialog = new ProgressDialog(getActivity());

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog.show();
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                progressDialog.dismiss();
                if (result) {
                    refreshRouteList();
                } else {
                    Context ctx;
                    if (getActivity() == null) {
                        ctx = MyApp.getContext();
                    } else {
                        ctx = getActivity();
                    }
                    Toast.makeText(ctx, R.string.error_default, Toast.LENGTH_SHORT).show();
                }
                closeMenu();
            }
        };
        if (ConnectionUtils.isOnline(getContext())) {
            deleteRouteTask.execute(getActivity());
        } else {
            ConnectionUtils.showOfflineToast();
        }
    }

    private void refreshRouteList() {
        if (listener != null) {
            listener.refresh();
        }
    }

    private void closeMenu() {
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

    @SuppressLint("StaticFieldLeak")
    private class SaveRouteToAutoSave extends AsyncTask<Void, Void, Integer> {
        private long routeId;

        SaveRouteToAutoSave(long routeId) {
            this.routeId = routeId;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                MySharedPreferences msp = MySharedPreferences.createLogin(getActivity());

                MyTrip.RouteId.Builder route = MyTrip.RouteId.newBuilder();
                route.setRouteId(routeId);

                Shared.OTCResponse response = ApiCaller.doCall(Endpoints.ROUTE_DONE, msp.getBytes("token"), route.build(), Shared.OTCResponse.class);

                return response.getStatusValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            refreshRouteList();
        }
    }
}
