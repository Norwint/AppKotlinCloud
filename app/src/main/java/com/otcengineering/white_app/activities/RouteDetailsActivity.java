package com.otcengineering.white_app.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.annimon.stream.Stream;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import com.google.protobuf.ByteString;
import com.otc.alice.api.model.General;
import com.otc.alice.api.model.MyTrip;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.MyApp;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.adapter.PoiRouteDetailsAdapter;
import com.otcengineering.white_app.components.CustomDialog;
import com.otcengineering.white_app.components.DialogYesNo;
import com.otcengineering.white_app.components.TitleBar;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.serialization.pojo.PoiImageWrapper;
import com.otcengineering.white_app.serialization.pojo.PoiWrapper;
import com.otcengineering.white_app.serialization.pojo.RouteItem;
import com.otcengineering.white_app.tasks.AddOrRemoveFavoriteTask;
import com.otcengineering.white_app.tasks.CreatePendingTask;
import com.otcengineering.white_app.tasks.DeleteRouteTask;
import com.otcengineering.white_app.tasks.GetGpxInfoTask;
import com.otcengineering.white_app.tasks.GetPoiInfoTask;
import com.otcengineering.white_app.utils.CloudErrorHandler;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.DateUtils;
import com.otcengineering.white_app.utils.MapUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.PrefsManager;
import com.otcengineering.white_app.utils.Utils;
import com.otcengineering.white_app.utils.images.ImageUtils;

import org.threeten.bp.Clock;
import org.threeten.bp.LocalDateTime;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RouteDetailsActivity extends MapBaseActivity {

    private static final int REQUEST_CODE_NEW_POI = 1;

    private TitleBar titleBar;
    private TextView btnHide;
    private ScrollView scrollView;
    private FrameLayout layoutMap;
    private View viewTransparent;
    private ImageView btnMenu;
    private TextView txtTitle, txtDescription, txtDuration, txtDistance, txtConsumption, txtConsumptionAvg,
            txtDrivingTechnique, txtLikes;
    private FrameLayout btnAddRemoveFavorite;
    private ImageView imgFavorite;
    private FrameLayout btnShare;
    private FrameLayout btnLocation;
    private LinearLayout layoutMenu;
    private View viewEmpty;
    private TextView txtEdit;
    private View txtEditSeparator;
    private TextView txtAddRemoveFavorites;
    private TextView txtShare;
    private TextView txtDelete;
    private TextView txtCancel;
    private LinearLayout btnInformation;
    private Button btnStart,btnSave;
    private LinearLayout layoutFinish;
    private TextView btnFinish;
    private FrameLayout btnNewPoi;
    private RecyclerView recycler;
    private ConstraintLayout buttonFav;

    private boolean fromPost = false;

    private PoiRouteDetailsAdapter adapter;

    private RouteItem routeItem;

    private RouteItem newRouteItem;

    private boolean showEditInMenu;

    private GetPoiInfoTask getPoiInfoTask;
    private GetGpxInfoTask getGpxInfoTask;

    private String polyline;

    public RouteDetailsActivity() {
        super("RouteDetailsActivity");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_details);
        retrieveExtras();
        retrieveViews();
        setEvents();
        configureAdapter();
        configureTitle();
        startGoogleMap(savedInstanceState);
        showRouteInfo();
        // getPoiInfo();
        pd = new ProgressDialog(this);
        pd.setMessage(getString(R.string.download_route));
        pd.show();
        showPoiList(routeItem.getRawPoiList());
        getGpxInfo();
        //routeItem.getRouteType().equals("AUTOSAVE");
        //
        if (routeItem.getRouteType() == General.RouteType.AUTOSAVED) {
            this.btnShare.setVisibility(View.GONE);
        }
        showSaveButton();
    }

    private void showSaveButton(){
       // General.RouteType s = routeItem.getRouteType();

        if (routeItem.getRouteType() == General.RouteType.AUTOSAVED){
            btnSave.setVisibility(View.VISIBLE);
            btnSave.setEnabled(true);
        }
        if (fromPost) {
            btnSave.setVisibility(View.GONE);
            txtEdit.setVisibility(View.GONE);
            txtEditSeparator.setVisibility(View.GONE);
            txtShare.setVisibility(View.GONE);
            txtDelete.setVisibility(View.GONE);
        }
    }

    private void retrieveExtras() {
        if (getIntent() != null && getIntent().getExtras() != null) {
            routeItem = new RouteItem(getIntent().getExtras().getString(Constants.Extras.ROUTE));
            if (getIntent().getExtras().containsKey("UserID")) {
                fromPost = true;
            }
            showEditInMenu = !routeItem.isAutosave();
        }
        String rou = ImageUtils.getPost(0, Constants.Extras.ROUTE);
        routeItem = new RouteItem(rou);
        showEditInMenu = !routeItem.isAutosave();
        ImageUtils.deletePost(0, Constants.Extras.ROUTE);
    }

    private void retrieveViews() {
        titleBar =  findViewById(R.id.route_details_titleBar);
        btnHide =  findViewById(R.id.route_details_btnHide);
        txtTitle =  findViewById(R.id.route_details_txtTitle);
        txtDescription =  findViewById(R.id.route_details_txtDescription);
        scrollView =  findViewById(R.id.route_details_scrollView);
        layoutMap =  findViewById(R.id.route_details_layoutMap);
        viewTransparent =  findViewById(R.id.route_details_viewTransparent);
        mapView =  findViewById(R.id.route_details_map);
        btnMenu =  findViewById(R.id.route_details_btnMenu);
        txtDuration =  findViewById(R.id.route_details_txtDuration);
        txtDistance =  findViewById(R.id.route_details_txtDistance);
        txtConsumption =  findViewById(R.id.route_details_txtConsumption);
        txtConsumptionAvg =  findViewById(R.id.route_details_txtConsumptionAvg);
        txtDrivingTechnique =  findViewById(R.id.route_details_txtDrivingTechnique);
        txtLikes =  findViewById(R.id.route_details_txtLikes);
        btnAddRemoveFavorite =  findViewById(R.id.route_details_btnAddRemoveFavorite);
        imgFavorite =  findViewById(R.id.route_details_imgFavorite);
        btnShare =  findViewById(R.id.route_details_btnShare);
        btnLocation =  findViewById(R.id.route_details_btnLocation);
        layoutMenu =  findViewById(R.id.route_details_layoutMenu);
        viewEmpty =  findViewById(R.id.route_details_viewEmpty);
        txtEdit =  findViewById(R.id.route_details_txtEdit);
        txtEditSeparator =  findViewById(R.id.route_details_txtEditSeparator);
        txtAddRemoveFavorites =  findViewById(R.id.route_details_txtAddRemoveFavorites);
        txtShare =  findViewById(R.id.route_details_txtShare);
        txtDelete =  findViewById(R.id.route_details_txtDelete);
        txtCancel =  findViewById(R.id.route_details_txtCancel);
        btnInformation =  findViewById(R.id.route_details_btnInformation);
        btnStart =  findViewById(R.id.route_details_btnStart);
        layoutFinish =  findViewById(R.id.route_details_layoutFinish);
        btnFinish =  findViewById(R.id.route_details_btnFinish);
        btnNewPoi =  findViewById(R.id.route_details_btnNewPoi);
        recycler =  findViewById(R.id.route_details_recyclerPoi);
        btnSave = findViewById(R.id.route_details_btnSave);
        buttonFav = findViewById(R.id.route_button_fav);
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

        btnHide.setOnClickListener(view -> hideRoute());

        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int scrollHeight = scrollView.getHeight();
                int btnInformationHeight = btnInformation.getHeight();
                if (scrollHeight != 0 && btnInformationHeight != 0) {
                    int layoutMapHeight = scrollHeight - btnInformationHeight;
                    ViewGroup.LayoutParams layoutParams = layoutMap.getLayoutParams();
                    layoutParams.height = layoutMapHeight;
                    layoutMap.setLayoutParams(layoutParams);
                    scrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });

        //this is a hack to allow move the map inside the scrollview
        viewTransparent.setOnTouchListener((v, event) -> {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    // Disallow ScrollView to intercept touch events.
                    scrollView.requestDisallowInterceptTouchEvent(true);
                    v.performClick();
                    // Disable touch on transparent view
                    return false;

                case MotionEvent.ACTION_UP:
                    // Allow ScrollView to intercept touch events.
                    scrollView.requestDisallowInterceptTouchEvent(false);
                    return true;

                default:
                    return true;
            }
        });

        btnStart.setOnClickListener(view -> startRoute());

        btnFinish.setOnClickListener(view -> finishRoute());

        btnNewPoi.setOnClickListener(view -> openNewPoi());

        btnMenu.setOnClickListener(view -> showMenu());

        btnLocation.setOnClickListener(view -> centerMapInMyLocationWithAnimation());

        buttonFav.setOnClickListener(view -> {
            txtCancel.performClick();
            addOrRemoveFavorites();
        });

        btnShare.setOnClickListener(view -> openShareRoute());
        if (fromPost) {
            btnShare.setVisibility(View.GONE);
        }

        btnInformation.setOnClickListener(view -> {
            if (scrollView.getScrollY() == 0) {
                scrollView.smoothScrollTo(0, scrollView.getHeight() / 2);
            }
        });

        viewEmpty.setOnClickListener(v -> {
            //do nothing. This is a hack to avoid scroll the list when layoutRankingType is shown
        });

        txtEdit.setOnClickListener(view -> {
            txtCancel.performClick();
            finish();
            openEditRoute();
        });
        if (fromPost) txtEdit.setVisibility(View.GONE);

        txtAddRemoveFavorites.setOnClickListener(view -> {
            txtCancel.performClick();
            addOrRemoveFavorites();
        });

        txtShare.setOnClickListener(view -> {
            txtCancel.performClick();
            openShareRoute();
        });
        if (fromPost) txtShare.setVisibility(View.GONE);

        txtDelete.setOnClickListener(view -> {
            txtCancel.performClick();
            deleteRoute();
        });
        if (fromPost) txtDelete.setVisibility(View.GONE);

        txtCancel.setOnClickListener(view -> hideMenu());

        btnSave.setOnClickListener(v -> new SaveRouteToAutoSave(routeItem.getId()).execute());
        if (fromPost) btnSave.setVisibility(View.GONE);
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
                MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());
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
            if (result == 1){
                onBackPressed();
            } else {
                if (!Utils.isActivityFinish(RouteDetailsActivity.this)) {
                    showError(getResources().getString(R.string.server_error), CloudErrorHandler.handleError(result));
                }
            }
        }
    }

    private void hideRoute() {
        newRouteItem.setPolyLine(polyline);
        newRouteItem.setRouteType(General.RouteType.PENDING);
        PrefsManager.getInstance().saveRouteInProgress(newRouteItem, this);
        openDashboard();
    }

    private void openDashboard() {
        MySharedPreferences.createLogin(this).putBoolean("GoToDashboard", true);
        finish();
        overridePendingTransition(0, R.anim.top_to_bottom);
    }

    private void addOrRemoveFavorites() {
        @SuppressLint("StaticFieldLeak")
        AddOrRemoveFavoriteTask addOrRemoveFavoriteTask = new AddOrRemoveFavoriteTask(routeItem.getId(), routeItem.isFavorite()) {
            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                if (result) {
                    int messageRes = routeItem.isFavorite() ?
                            R.string.remove_route_to_favorites_correctly :
                            R.string.add_route_to_favorites_correctly;
                    showCustomDialog(messageRes);
                    routeItem.setFavorite(!routeItem.isFavorite());
                    showRouteInfo();
                }
            }
        };
        if (ConnectionUtils.isOnline(getApplicationContext())) {
            addOrRemoveFavoriteTask.execute(this);
        } else {
            ConnectionUtils.showOfflineToast();
        }
    }

    ProgressDialog finishPD;
    private void finishRoute() {
        if (newRouteItem.getId() == 0) {
            finishPD = new ProgressDialog(MyApp.getContext());
            finishPD.setMessage(getString(R.string.loading));
            finishPD.setCancelable(false);
            finishPD.show();
            Thread thread = new Thread(() -> {
                while (newRouteItem.getId() == 0) {
                    synchronized (this) {
                        try {
                            wait(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                runOnUiThread(() -> {
                    finishPD.dismiss();
                    finishPD = null;
                    newRouteItem.setDateEnd(LocalDateTime.now(Clock.systemUTC()));
                    newRouteItem.setRouteType(General.RouteType.PENDING);
                    openSaveRoute();
                });
            });
            thread.start();
        } else {
            newRouteItem.setDateEnd(LocalDateTime.now(Clock.systemUTC()));
            newRouteItem.setRouteType(General.RouteType.PENDING);
            openSaveRoute();
        }
    }

    private void startRoute() {
        if (MySharedPreferences.createLogin(getApplicationContext()).contains("ROUTE_IN_PROGRESS")) {
            DialogYesNo dyn = new DialogYesNo(this, getString(R.string.you_have_active_route), () -> {},
                    () -> runOnUiThread(() -> {
                MySharedPreferences.createLogin(getApplicationContext()).remove("ROUTE_IN_PROGRESS");
                btnStart.setVisibility(View.GONE);
                layoutFinish.setVisibility(View.VISIBLE);
                btnHide.setVisibility(View.VISIBLE);
                btnSave.setVisibility(View.GONE);
                newRouteItem = new RouteItem(routeItem);
                newRouteItem.setId(0);
                newRouteItem.setDateStart(LocalDateTime.now(Clock.systemUTC()));
                newRouteItem.setDateEnd(null);
                newRouteItem.setDurationInMins(0);
                newRouteItem.getPoiList().clear();
                for (PoiWrapper wrap : routeItem.getPoiList()) {
                    General.POI poi = wrap.getPoi().toBuilder().setPoiId(0).build();
                    PoiWrapper wrapper = new PoiWrapper(poi);
                    wrapper.setStatus(PoiWrapper.PoiStatus.Added);
                    newRouteItem.getPoiList().add(wrapper);
                }
                centerMapInMyLocationWithAnimation();
                createPendingRoute();
            }));
            dyn.setYesButtonText(getString(R.string.no));
            dyn.setNoButtonText(getString(R.string.yes));
            dyn.setTitle(getString(R.string.in_route));
            dyn.setYesButtonColor(Color.argb(255, 0, 122, 255));
            dyn.setNoButtonColor(Color.argb(255, 0, 122, 255));

            runOnUiThread(() -> {
                try {
                    if (!Utils.isActivityFinish(RouteDetailsActivity.this)) {
                        dyn.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else {
            btnSave.setVisibility(View.GONE);
            btnStart.setVisibility(View.GONE);
            layoutFinish.setVisibility(View.VISIBLE);
            btnHide.setVisibility(View.VISIBLE);
            newRouteItem = new RouteItem(routeItem);
            newRouteItem.setId(0);
            newRouteItem.setDateStart(LocalDateTime.now(Clock.systemUTC()));
            newRouteItem.setDateEnd(null);
            newRouteItem.getPoiList().clear();
            if (newRouteItem.getTitle().isEmpty()) {
                newRouteItem.setTitle(DateUtils.utcToString(newRouteItem.getDateStart(), "dd/MM/yyyy HH:mm:ss"));
            }
            centerMapInMyLocationWithAnimation();
            createPendingRoute();
        }

        showRouteInGoogleMaps();
    }

    private void showRouteInGoogleMaps() {
        Runnable run = () -> {
            try {
                LatLng placeEnd = routeItem.getLatLngList().get(routeItem.getLatLngList().size() - 1);
                Utils.showGoogleMapsRoute(this, placeEnd);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        DialogYesNo dyn = new DialogYesNo(this);
        dyn.setTitle("Google Maps");
        dyn.setMessage("Do you want to open the route in Google Maps?");
        dyn.setYesButtonClickListener(run);
        dyn.show();
    }

    private void createPendingRoute() {
        @SuppressLint("StaticFieldLeak")
        CreatePendingTask cpt = new CreatePendingTask(newRouteItem) {
            @Override
            protected void onPostExecute(Long aLong) {
                if (aLong != null) {
                    newRouteItem.setId(aLong);

                    if (routeItem.getPoiList().size() > 0) {
                        new Thread(() -> {
                            for (PoiWrapper wrap : routeItem.getPoiList()) {
                                newRouteItem.getPoiList().add(new PoiWrapper(wrap));

                                General.POI.Builder builder = General.POI.newBuilder();

                                builder.setRouId(aLong);
                                builder.setTitle(wrap.getPoi().getTitle());
                                builder.setLatitude(wrap.getPoi().getLatitude());
                                builder.setLongitude(wrap.getPoi().getLongitude());
                                builder.setType(wrap.getPoi().getType());

                                try {
                                    MyTrip.PoiId poiId = ApiCaller.doCall(Endpoints.ROUTE_ADD_POI, true, builder.build(), MyTrip.PoiId.class);

                                    if (wrap.getPoi().getImagesCount() > 0) {
                                        if (wrap.getPoi().getImages(0) > 0) {
                                            byte[] poiImage =
                                                    ApiCaller.getImage(Endpoints.FILE_GET + wrap.getPoi().getImages(0), MySharedPreferences.createLogin(RouteDetailsActivity.this).getString("token"));
                                            MyTrip.PoiImage.Builder builderImg = MyTrip.PoiImage.newBuilder();

                                            builderImg.setData(ByteString.copyFrom(poiImage));
                                            builderImg.setName( wrap.getPoi().getImages(0) + ".jpg");
                                            builderImg.setPoiId(poiId.getPoiId());

                                            ApiCaller.doCall(Endpoints.ROUTE_POI_ADD_IMAGE, true, builderImg.build(), Shared.OTCResponse.class);
                                        }
                                    }
                                } catch (ApiCaller.OTCException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                }
            }
        };
        cpt.execute();
    }

    private void openSaveRoute() {
        Intent intent = new Intent(this, SaveRouteActivity.class);
        newRouteItem.setPolyLine(polyline);
        //intent.putExtra(Constants.Extras.ROUTE_POLYLINE, newRouteItem.getPolyLine());
        //intent.putExtra(Constants.Extras.ROUTE, newRouteItem.toString());
        ImageUtils.putPost(0, Constants.Extras.ROUTE, newRouteItem.toString());
        startActivity(intent);
        finish();
    }

    private void openNewPoi() {
        Intent intent = new Intent(this, NewPoiActivity.class);
        intent.putExtra(Constants.Extras.ROUTE, newRouteItem.toString());
        Location myLocation = getMyLocation();
        if (myLocation != null) {
            LatLng poiLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            intent.putExtra(Constants.Extras.POI_LOCATION, poiLocation);
            startActivityForResult(intent, REQUEST_CODE_NEW_POI);
        } else {
            FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(loc -> {
                if (loc != null) {
                    LatLng poiLocation = new LatLng(loc.getLatitude(), loc.getLongitude());
                    intent.putExtra(Constants.Extras.POI_LOCATION, poiLocation);
                    startActivityForResult(intent, REQUEST_CODE_NEW_POI);
                } else {
                    Utils.runOnMainThread(() -> {
                        CustomDialog cd = new CustomDialog(RouteDetailsActivity.this, getString(R.string.gps_not_found), true);
                        cd.show();
                    });
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_NEW_POI && resultCode == RESULT_OK && data != null && data.getExtras() != null) {
            PoiWrapper poi = (PoiWrapper) data.getExtras().get(Constants.Extras.POI);
            if (poi != null) {
                drawMarker(new LatLng(poi.getPoi().getLatitude(), poi.getPoi().getLongitude()), R.drawable.marker_poi);
                poi.setStatus(PoiWrapper.PoiStatus.Added);
                if (data.getExtras().containsKey("PoiImg")) {
                    String img = data.getExtras().getString("PoiImg");
                    PoiImageWrapper piw = Utils.getGson().fromJson(img, PoiImageWrapper.class);
                    poi.getImages().add(piw);
                }
                routeItem.getPoiList().add(poi);
                newRouteItem.getPoiList().add(poi);
            }  //Log.e("RouteDetailsActivity", "Poi is null");

        }
    }

    private void deleteRoute() {
        @SuppressLint("StaticFieldLeak")
        DeleteRouteTask deleteRouteTask = new DeleteRouteTask(routeItem.getId()) {
            private ProgressDialog progressDialog = new ProgressDialog(RouteDetailsActivity.this);

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                try {
                    if (!Utils.isActivityFinish(RouteDetailsActivity.this)) {
                        progressDialog.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                progressDialog.dismiss();
                if (result) {
                    finish();
                } else {
                    try {
                        if (!Utils.isActivityFinish(RouteDetailsActivity.this)) {
                            Toast.makeText(RouteDetailsActivity.this, R.string.error_default, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        if (ConnectionUtils.isOnline(getApplicationContext())) {
            deleteRouteTask.execute(RouteDetailsActivity.this);
        } else {
            ConnectionUtils.showOfflineToast();
        }
    }

    private void configureAdapter() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(layoutManager);
        adapter = new PoiRouteDetailsAdapter(this);
        recycler.setAdapter(adapter);
    }

    private void openEditRoute() {
        Intent intent = new Intent(this, SaveRouteActivity.class);
        RouteItem toEdit = routeItem.copy();
        toEdit.setConsumptionAvg(routeItem.getConsumptionAvg() / 100);
        intent.putExtra(Constants.Extras.ROUTE, toEdit.toString());
        intent.putExtra("Edit", true);
        startActivity(intent);
    }

    private void openShareRoute() {
        Intent intent = new Intent(this, SendPostActivity.class);
        intent.putExtra(Constants.Extras.ROUTE, routeItem.toString());
        startActivity(intent);
    }

    private void configureTitle() {
        General.RouteType routeType = routeItem.getRouteType();
        if (routeType == General.RouteType.AUTOSAVED) {
            titleBar.setTitle(R.string.autosaved_route);
        } else if (routeType == General.RouteType.PLANNED) {
            titleBar.setTitle(R.string.planned_route);
        } else if (routeType == General.RouteType.DONE) {
            titleBar.setTitle(R.string.done_route);
        }
    }

    private void showRouteInfo() {
        if (routeItem.isAutosave() || (routeItem.getTitle() != null && routeItem.getTitle().isEmpty())) {
            txtTitle.setText(String.format("%s", DateUtils.utcToString(routeItem.getDateStart(), "dd/MM/yyyy - HH:mm:ss")));
        } else {
            txtTitle.setText(routeItem.getTitle() == null ? "" : routeItem.getTitle());
        }
        String description = routeItem.getDescription();
        txtDescription.setText(description);
        if (description == null || description.isEmpty()) {
            txtDescription.setVisibility(View.GONE);
        }
        txtDuration.setText(routeItem.getDurationInMinsFormatted());
        txtDistance.setText(String.format(Locale.US, "%.1f km", routeItem.getDistanceInKms()));
        txtConsumption.setText(String.format(Locale.US, "%.2f l", routeItem.getConsumption()));
        txtConsumptionAvg.setText(String.format(Locale.US, "%.1f km/l", routeItem.getConsumptionAvg()));
        txtDrivingTechnique.setText(String.format(Locale.US, "%.1f", routeItem.getDrivingTechnique() / 10));
        txtLikes.setText(String.valueOf(routeItem.getLikes()));
        boolean isFavorite = routeItem.isFavorite();
        imgFavorite.setImageResource(isFavorite ? R.drawable.my_routes_icons_2 : R.drawable.my_routes_icons_3);
    }

    private void showMenu() {
        txtEdit.setVisibility(showEditInMenu ? View.VISIBLE : View.GONE);
        txtEditSeparator.setVisibility(showEditInMenu ? View.VISIBLE : View.GONE);
        txtShare.setVisibility(routeItem.getRouteType() == General.RouteType.AUTOSAVED ? View.GONE : View.VISIBLE);
        layoutMenu.setVisibility(View.VISIBLE);
        boolean routeIsFav = routeItem.isFavorite();
        if (fromPost) {
            btnSave.setVisibility(View.GONE);
            txtEdit.setVisibility(View.GONE);
            txtEditSeparator.setVisibility(View.GONE);
            txtShare.setVisibility(View.GONE);
            txtDelete.setVisibility(View.GONE);
        }
        txtAddRemoveFavorites.setText(routeIsFav ? R.string.remove_from_favorite : R.string.add_to_favorite);
    }

    private void hideMenu() {
        layoutMenu.setVisibility(View.GONE);
    }

    @SuppressLint("StaticFieldLeak")
    private void getPoiInfo() {
        try {
            if (!Utils.isActivityFinish(this)) {
                pd = new ProgressDialog(this);
                pd.setMessage(getString(R.string.download_route));
                pd.show();
                getPoiInfoTask = new GetPoiInfoTask(routeItem.getId()) {
                    @Override
                    protected void onPostExecute(List<General.POI> pois) {
                        super.onPostExecute(pois);
                        List<PoiWrapper> wrapList = new ArrayList<>();
                        Stream.of(pois).forEach(p -> {
                            PoiWrapper wrap = new PoiWrapper(p);
                            wrap.setStatus(PoiWrapper.PoiStatus.Modified);
                            wrapList.add(wrap);
                        });
                        routeItem.setPoiList(wrapList);
                        showPoiList(pois);
                        getGpxInfo();
                    }
                };

                if (ConnectionUtils.isOnline(getApplicationContext())) {
                    getPoiInfoTask.execute(this);
                } else {
                    ConnectionUtils.showOfflineToast();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showPoiList(List<General.POI> poiList) {
        adapter.clearItems();
        adapter.addItems(poiList);
        adapter.notifyDataSetChanged();

        Thread th = new Thread(() -> {
            try {
                while (googleMap == null) {
                    com.otcengineering.apible.Utils.wait(this, 100);
                }
                if (poiList != null) {
                    for (General.POI poi : poiList) {
                        Utils.runOnMainThread(() -> drawMarker(new LatLng(poi.getLatitude(), poi.getLongitude()), R.drawable.marker_poi, poi.getTitle()));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        th.start();

        scrollToTheTop();
    }

    private void scrollToTheTop() {
        scrollView.post(() -> scrollView.scrollTo(0, 0));
    }

    private ProgressDialog pd;

    @SuppressLint("StaticFieldLeak")
    private void getGpxInfo() {
        getGpxInfoTask = new GetGpxInfoTask(routeItem.getGpxFileId()) {
            @Override
            protected void onPostExecute(byte[] result) {
                super.onPostExecute(result);
                runOnUiThread(pd::dismiss);
                drawMapRoute(result);
            }
        };

        if (ConnectionUtils.isOnline(getApplicationContext())) {
            getGpxInfoTask.execute(this);
        } else {
            ConnectionUtils.showOfflineToast();
        }
    }

    private void drawMapRoute(byte[] gpxBytes) {
        if (routeItem.getRouteType() == General.RouteType.PLANNED) {
            try {
                String str = new String(gpxBytes, StandardCharsets.UTF_8);
                polyline = str;
                List<LatLng> decodedPath = PolyUtil.decode(str);
                routeItem.setLatLngList(decodedPath);
                drawRouteInfo(decodedPath, routeItem.getDistanceInKms());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            List<LatLng> latLngList = null;
            if (gpxBytes != null) {
                latLngList = MapUtils.getGpxInfo(gpxBytes);
            }

            if (latLngList != null) {
                polyline = PolyUtil.encode(latLngList);
            }
            drawRouteInfo(latLngList, routeItem.getDistanceInKms());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (getPoiInfoTask != null) {
            getPoiInfoTask.cancel(true);
        }
        if (getGpxInfoTask != null) {
            getGpxInfoTask.cancel(true);
        }
    }

    private void showError(String title, String message) {
        new AlertDialog.Builder(RouteDetailsActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.close), (dialog, which) -> {
                    // Whatever...
                }).show();

    }

    @Override
    public void onBackPressed() {
        if (layoutMenu.getVisibility() == View.VISIBLE) {
            hideMenu();
        }
        else if (newRouteItem != null) {
            DialogYesNo dyn = new DialogYesNo(RouteDetailsActivity.this, getString(R.string.dismiss_route), this::dismissRoute, () -> {}
            );
            dyn.setTitle(getString(R.string.unsaved_route));
            dyn.setYesButtonColor(Color.argb(255, 0, 122, 255));
            dyn.setNoButtonColor(Color.argb(255, 0, 122, 255));

            dyn.show();
        }
        else
        {
            finish();
        }
    }

    private void dismissRoute() {
        DeleteRouteTask drt = new DeleteRouteTask(newRouteItem.getId());
        drt.execute();
        finish();
    }
}
