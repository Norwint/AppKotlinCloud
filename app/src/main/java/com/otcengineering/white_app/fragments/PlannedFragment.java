package com.otcengineering.white_app.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.otc.alice.api.model.General;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.adapter.RouteAdapter;
import com.otcengineering.white_app.interfaces.FragmentBackPresser;
import com.otcengineering.white_app.serialization.pojo.RouteItem;
import com.otcengineering.white_app.tasks.GetRoutesTask;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.PrefsManager;

import java.util.List;


public class PlannedFragment extends BaseFragment implements FragmentBackPresser {

    private Button btnEdit;
    private RecyclerView recycler;
    private FrameLayout btnScrollUp;
    private Button btnNewRoute;
    private LinearLayout layoutEdit;
    private View btnCancel, btnSelectAll, btnDelete;

    private RouteAdapter adapter;

    private boolean editMode;

    private int page = 1;
    private boolean retrievingNewPage = false;

    private MyRoutesFragment.RouteListener listener;

    private GetRoutesTask getPlannedRoutesTask;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_planned, container, false);
        retrieveViews(v);
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
        btnEdit = v.findViewById(R.id.planned_btnEdit);
        recycler = v.findViewById(R.id.planned_recycler);
        btnScrollUp = v.findViewById(R.id.planned_btnScrollUp);
        btnNewRoute = v.findViewById(R.id.planned_btnNewRoute);
        layoutEdit = v.findViewById(R.id.planned_layoutEdit);
        btnCancel = v.findViewById(R.id.planned_btnCancel);
        btnSelectAll = v.findViewById(R.id.planned_btnSelectAll);
        btnDelete = v.findViewById(R.id.planned_btnDelete);
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

        btnDelete.setOnClickListener(view -> {
            List<RouteItem> routesSelected = adapter.getItemsSelected();
            listener.onDeleteRoutes(routesSelected);
            btnCancel.performClick();
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

    @Override
    public void onPause() {
        closeMenu();
        super.onPause();
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

    private void manageLayoutEditUI() {
        btnNewRoute.setVisibility(editMode ? View.GONE : View.VISIBLE);
        layoutEdit.setVisibility(editMode ? View.VISIBLE : View.GONE);
    }

    private void configureAdapter() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recycler.setLayoutManager(layoutManager);
        adapter = new RouteAdapter(getActivity(), new RouteAdapter.RouteListener() {
            @Override
            public void onRouteItemSelected(int position) {
                if (listener != null) {
                    RouteItem routeItem = adapter.getData(position);
                    if (routeItem != null) {
                        listener.onRouteSelected(routeItem);
                    }
                }
            }

            @Override
            public void onMenu(int position) {
                if (listener != null) {
                    RouteItem routeItem = adapter.getData(position);
                    if (routeItem != null) {
                        listener.onShowMenu(true, routeItem);
                    }
                }
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
                        if (!retrievingNewPage && !editMode) {
                            retrievingNewPage = true;
                            page++;
                            getPlannedRoutes();
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        resetPage();
        showPlannedRoutesFromDB();
        getPlannedRoutes();
    }

    private void showPlannedRoutesFromDB() {
        List<RouteItem> plannedRoutes = PrefsManager.getInstance().getPlannedRoutes(getContext());
        showRouteList(plannedRoutes);
    }

    private void resetPage() {
        page = 1;
    }

    @SuppressLint("StaticFieldLeak")
    private void getPlannedRoutes() {
        getPlannedRoutesTask = new GetRoutesTask(General.RouteType.PLANNED, getContext()) {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(List<RouteItem> routeItems) {
                super.onPostExecute(routeItems);
                retrievingNewPage = false;
                showRouteList(routeItems);
            }
        };
        if (ConnectionUtils.isOnline(getContext())) {
            getPlannedRoutesTask.execute(page, getActivity());
        } else {
            ConnectionUtils.showOfflineToast();
        }
    }

    private boolean isFirstPage() {
        return page == 1;
    }

    private void showRouteList(List<RouteItem> routeItems) {
        if (routeItems == null) return;

        if (isFirstPage()) {
            adapter.clearItems();
            PrefsManager.getInstance().savePlannedRoutes(routeItems, getContext());
        }
        adapter.addItems(routeItems);
        adapter.notifyDataSetChanged();
    }

    public void setListener(MyRoutesFragment.RouteListener listener) {
        this.listener = listener;
    }

    void refreshList() {
        page = 1;
        getPlannedRoutes();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (getPlannedRoutesTask != null) {
            getPlannedRoutesTask.cancel(true);
            getPlannedRoutesTask.interrupt();
        }
    }
}