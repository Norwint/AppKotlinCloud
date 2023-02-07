package com.otcengineering.white_app.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.adapter.RouteAdapter;
import com.otcengineering.white_app.components.CustomTabLayout;
import com.otcengineering.white_app.components.TitleBar;
import com.otcengineering.white_app.serialization.pojo.RouteItem;
import com.otcengineering.white_app.tasks.GetRoutesTask;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otc.alice.api.model.General;

import java.util.List;


public class RouteSelectionActivity extends BaseActivity {
    public static final int TAB_PLANNED = 1;
    public static final int TAB_DONE = 2;

    private TitleBar titleBar;
    private CustomTabLayout customTabLayout;
    private RecyclerView recycler;

    private RouteAdapter adapter;

    private int page = 1;
    private boolean retrievingNewPage = false;

    private GetRoutesTask getRoutesTask;
    General.RouteType routeType;

    public RouteSelectionActivity() {
        super("RouteSelectionActivity");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_selection);
        retrieveViews();
        setEvents();
        configureAdapter();
    }

    private void retrieveViews() {
        titleBar = findViewById(R.id.route_selection_titleBar);
        customTabLayout = findViewById(R.id.route_selection_customTabLayout);
        recycler = findViewById(R.id.route_selection_recycler);
    }

    private void setEvents() {
        titleBar.setListener(new TitleBar.TitleBarListener() {
            @Override
            public void onLeftClick() {
                onBackPressed();
            }

            @Override
            public void onRight1Click() {

            }

            @Override
            public void onRight2Click() {

            }
        });

        customTabLayout.configure(TAB_PLANNED, this::manageTabChanged, TAB_PLANNED, TAB_DONE);
    }

    private void manageTabChanged(int tabSelected) {
        switch (tabSelected) {
            case TAB_PLANNED:
                routeType = General.RouteType.PLANNED;
                break;
            case TAB_DONE:
                routeType = General.RouteType.DONE;
                break;
        }
        resetPage();
        getRoutes();
    }

    private void configureAdapter() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(layoutManager);
        adapter = new RouteAdapter(this, new RouteAdapter.RouteListener() {
            @Override
            public void onRouteItemSelected(int position) {
                RouteItem routeItem = adapter.getData(position);
                Intent data = new Intent();
                if (routeItem != null) {
                    data.putExtra(Constants.Extras.ROUTE, routeItem.toString());
                }
                setResult(RESULT_OK, data);
                finish();
            }

            @Override
            public void onMenu(int position) {
            }
        });
        recycler.setAdapter(adapter);
        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        if (!retrievingNewPage) {
                            retrievingNewPage = true;
                            page++;
                            getRoutes();
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
        getRoutes();
    }

    private void resetPage() {
        page = 1;
    }

    @SuppressLint("StaticFieldLeak")
    private void getRoutes() {
        getRoutesTask = new GetRoutesTask(routeType, RouteSelectionActivity.this) {

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
        if (ConnectionUtils.isOnline(getApplicationContext())) {
            getRoutesTask.execute(page, this);
        } else {
            ConnectionUtils.showOfflineToast();
        }
    }

    private boolean isFirstPage() {
        return page == 1;
    }

    private void showRouteList(List<RouteItem> routeItems) {
        if (isFirstPage()) {
            adapter.clearItems();
        }
        adapter.addItems(routeItems);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (getRoutesTask != null) {
            getRoutesTask.cancel(true);
            getRoutesTask.interrupt();
        }
    }
}