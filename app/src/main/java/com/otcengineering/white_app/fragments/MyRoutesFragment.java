package com.otcengineering.white_app.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.otc.alice.api.model.MyTrip;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.activities.NewRouteActivity;
import com.otcengineering.white_app.activities.RouteDetailsActivity;
import com.otcengineering.white_app.components.CustomTabLayout;
import com.otcengineering.white_app.components.DialogMultiple;
import com.otcengineering.white_app.interfaces.FragmentBackPresser;
import com.otcengineering.white_app.interfaces.ShowMenuRoutesListener;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.serialization.pojo.RouteItem;
import com.otcengineering.white_app.tasks.GenericTask;
import com.otcengineering.white_app.utils.CloudErrorHandler;
import com.otcengineering.white_app.utils.Utils;
import com.otcengineering.white_app.utils.images.ImageUtils;

import java.util.List;


public class MyRoutesFragment extends EventFragment implements FragmentBackPresser {
    public MyRoutesFragment() {
        super("MyRoutesActivity");
    }

    public interface RouteListener {
        void onShowMenu(boolean showEdit, RouteItem routeItem);

        void onRouteSelected(RouteItem routeItem);

        void onNewRoute();

        void onDeleteRoutes(List<RouteItem> routesSelected);

        void onUnfavoriteRoutes(List<RouteItem> routesSelected);

    }

    private static final int TAB_AUTOSAVE = 1;
    private static final int TAB_PLANNED = 2;
    private static final int TAB_DONE = 3;
    private static final int TAB_FAVORITES = 4;

    private AutosaveFragment autosaveFragment;
    private PlannedFragment plannedFragment;
    private DoneFragment doneFragment;
    private FavoritesFragment favoritesFragment;
    private Fragment currentFragment;

    private CustomTabLayout customTabLayout;

    private RouteItem routeSelected;

    private ProgressDialog progressDialog;

    private ShowMenuRoutesListener listener;

    public void setListener(ShowMenuRoutesListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_routes, container, false);
        retrieveViews(v);
        setEvents();
        return v;
    }

    private void retrieveViews(View v) {
        customTabLayout = v.findViewById(R.id.my_routes_customTabLayout);
    }

    private void setEvents() {
        customTabLayout.configure(this::manageTabChanged, TAB_AUTOSAVE, TAB_PLANNED, TAB_DONE, TAB_FAVORITES);
    }

    private void manageTabChanged(int tabSelected) {
        switch (tabSelected) {
            case TAB_AUTOSAVE:
                autosaveFragment = new AutosaveFragment();
                autosaveFragment.setListener(createRouteListener());
                changeFragment(autosaveFragment);
                break;
            case TAB_PLANNED:
                plannedFragment = new PlannedFragment();
                plannedFragment.setListener(createRouteListener());
                changeFragment(plannedFragment);
                break;
            case TAB_DONE:
                doneFragment = new DoneFragment();
                doneFragment.setListener(createRouteListener());
                changeFragment(doneFragment);
                break;
            case TAB_FAVORITES:
                favoritesFragment = new FavoritesFragment();
                favoritesFragment.setListener(new RouteListener() {
                    @Override
                    public void onShowMenu(boolean showEdit, RouteItem routeItem) {

                    }

                    @Override
                    public void onRouteSelected(RouteItem routeItem) {
                        routeSelected = routeItem;
                        openRouteDetails();
                    }

                    @Override
                    public void onNewRoute() {
                        openNewRoute();
                    }

                    @Override
                    public void onDeleteRoutes(List<RouteItem> routesSelected) {

                    }

                    @Override
                    public void onUnfavoriteRoutes(List<RouteItem> routesSelected) {
                        unfavoriteRoutesSelected(routesSelected);
                    }
                });
                changeFragment(favoritesFragment);
                break;
        }
    }

    public void refreshRouteList() {
        if (autosaveFragment != null && autosaveFragment.isVisible()) {
            autosaveFragment.refreshList();
        } else if (plannedFragment != null && plannedFragment.isVisible()) {
            plannedFragment.refreshList();
        } else if (doneFragment != null && doneFragment.isVisible()) {
            doneFragment.refreshList();
        } else if (favoritesFragment != null && favoritesFragment.isVisible()) {
            favoritesFragment.refreshList();
        }
    }

    private void changeFragment(Fragment fragment) {
        changeFragment(fragment, R.id.my_routes_layoutContainer);
        currentFragment = fragment;
    }

    private RouteListener createRouteListener() {
        return new RouteListener() {
            @Override
            public void onShowMenu(boolean showEdit, RouteItem routeItem) {
                routeSelected = routeItem;
                if (listener != null) {
                    listener.showMenu(showEdit, routeItem);
                }
            }

            @Override
            public void onRouteSelected(RouteItem routeItem) {
                routeSelected = routeItem;
                openRouteDetails();
            }

            @Override
            public void onNewRoute() {
                openNewRoute();
            }

            @Override
            public void onDeleteRoutes(List<RouteItem> routesSelected) {
                DialogMultiple dm = new DialogMultiple(getContext());
                dm.addButton(getString(R.string.cancel), () -> {});
                dm.addButton(getString(R.string.delete), () -> deleteRoutesSelected(routesSelected));
                dm.setTitle("Are you sure you want to delete the route?");
                dm.show();
            }

            @Override
            public void onUnfavoriteRoutes(List<RouteItem> routesSelected) {
                unfavoriteRoutesSelected(routesSelected);
            }
        };
    }

    private int totalTasks;
    private int totalTasksEnded;
    private int totalTasksEndedSuccessful;

    private void unfavoriteRoutesSelected(List<RouteItem> routesSelected) {
        totalTasks = routesSelected.size();
        totalTasksEnded = 0;
        totalTasksEndedSuccessful = 0;
        if (totalTasks > 0) {
            showProgressDialog();
            for (RouteItem routeItem : routesSelected) {
                unfavoriteRoute(routeItem);
            }
        }
    }

    private void deleteRoutesSelected(List<RouteItem> routesSelected) {
        totalTasks = routesSelected.size();
        totalTasksEnded = 0;
        totalTasksEndedSuccessful = 0;
        if (totalTasks > 0) {
            showProgressDialog();
            for (RouteItem routeItem : routesSelected) {
                removeRoute(routeItem);
            }
        }
    }

    private void removeRoute(final RouteItem rou) {
        MyTrip.Status status = MyTrip.Status.newBuilder().setRouteId(rou.getId()).setStatus(MyTrip.RouteStatus.DELETED).build();
        GenericTask gt = new GenericTask(Endpoints.ROUTE_STATUS, status, true, otcResponse -> {
            if (otcResponse.getStatus() != Shared.OTCStatus.SUCCESS) {
                Utils.runOnMainThread(() -> showCustomDialogError(CloudErrorHandler.handleError(otcResponse.getStatus())));
            } else {
                Utils.runOnMainThread(this::refreshRouteList);
            }
            --totalTasks;
            if (totalTasks == 0) {
                Utils.runOnMainThread(this::dismissProgressDialog);
            }
        });
        gt.execute();
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getString(R.string.loading));
        }
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void unfavoriteRoute(RouteItem routeItem) {
        MyTrip.RouteId.Builder route = MyTrip.RouteId.newBuilder();
        route.setRouteId(routeItem.getId());
        GenericTask gt = new GenericTask(Endpoints.ROUTE_UNFAV, route.build(), true, otcResponse -> {
            totalTasksEnded++;
            if (otcResponse.getStatus() == Shared.OTCStatus.SUCCESS) {
                totalTasksEndedSuccessful++;
            }
            closeIfAllTasksHaveEnded();
        });
        gt.execute();
    }

    private void closeIfAllTasksHaveEnded() {
        if (totalTasksEnded == totalTasks) {
            dismissProgressDialog();
            if (totalTasksEnded == totalTasksEndedSuccessful) {
                refreshRouteList();
            } else {
                showCustomDialogError();
            }
        }
    }

    private void openNewRoute() {
        Intent intent = new Intent(getActivity(), NewRouteActivity.class);
        startActivity(intent);
    }

    private void openRouteDetails() {
        Intent intent = new Intent(getActivity(), RouteDetailsActivity.class);
        ImageUtils.putPost(0, Constants.Extras.ROUTE, routeSelected.toString());
        // intent.putExtra(Constants.Extras.ROUTE, routeSelected.toString());
        startActivity(intent);
    }

    @Override
    public boolean onBackPressed() {
        Fragment frag = currentFragment;
        return (!(frag instanceof FragmentBackPresser)) || ((FragmentBackPresser) frag).onBackPressed();
    }

}
