package com.otcengineering.white_app.activities;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;
import com.google.maps.model.Unit;
import com.otc.alice.api.model.General;
import com.otc.alice.api.model.MyTrip;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.BuildConfig;
import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.MyApp;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.components.DialogMultiple;
import com.otcengineering.white_app.components.DialogYesNo;
import com.otcengineering.white_app.components.TitleBar;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.serialization.pojo.PoiImageWrapper;
import com.otcengineering.white_app.serialization.pojo.PoiWrapper;
import com.otcengineering.white_app.serialization.pojo.RouteItem;
import com.otcengineering.white_app.tasks.CreatePendingTask;
import com.otcengineering.white_app.tasks.DeleteRouteTask;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.DateUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.PrefsManager;
import com.otcengineering.white_app.utils.Utils;
import com.otcengineering.white_app.utils.images.ImageUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.threeten.bp.Clock;
import org.threeten.bp.LocalDateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.otcengineering.white_app.utils.Utils.runOnMainThread;

public class NewRouteActivity extends MapBaseActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_CODE_PLACE_AUTOCOMPLETE = 1;
    private static final int REQUEST_CODE_NEW_POI = 2;

    private TitleBar titleBar;
    private TextView btnHide;
    private LinearLayout layoutLocations, layoutRouteInfo;
    private FrameLayout btnLocation;
    private TextView btnCalculate;
    private LinearLayout btnAddLocation;
    private ImageView imgTwoDots, imgDestination, imgTwoDotsMiddle1, imgDestinationMiddle1, imgTwoDotsMiddle2, imgDestinationMiddle2;
    private EditText editStart, editLocationMiddle1, editLocationMiddle2, editEnd;
    private TextView txtDuration, txtDistance, txtConsumption, txtConsumptionAvg, txtDrivingTechnique;
    private LinearLayout layoutStartSave, layoutFinish;
    private FrameLayout btnNewPoi;
    private TextView btnStart, btnFinish, btnSave;

    private View viewSelected;

    private Place placeStart;
    private Place placeLocation1;
    private Place placeLocation2;
    private Place placeEnd;

    private CalculateRouteTask calculateRouteTask;
    private GetRouteInfoTask getRouteInfoTask;

    private MyTrip.RouteInfo.Builder routeInfoBuilder = MyTrip.RouteInfo.newBuilder();
    private RouteItem routeItem = new RouteItem();

    private ProgressDialog progressDialog;
    private boolean inProgressRoute = false;

    public NewRouteActivity() {
        super("NewRouteActivity");
    }

    private Marker m_endPoint, m_startPoint, m_middlePoint1, m_middlePoint2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_route);

        if (getIntent() != null && getIntent().getBooleanExtra("RouteActive", false)) {
            inProgressRoute = true;
        }

        retrieveViews();
        setEvents();
        TAG = "NewRouteActivity";
        startGoogleMap(savedInstanceState);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
        if (!retrieveRouteInProgress()) {
            new Handler().postDelayed(this::centerMapInMyLocation, 500);
        }

        googleMap.setOnMapClickListener(latLng -> {
            if (viewSelected != null) {
                if (viewSelected == editEnd) {
                    if (m_endPoint != null) {
                        m_endPoint.remove();
                    }
                    runOnMainThread(() -> editEnd.setText(""));
                    m_endPoint = googleMap.addMarker(new MarkerOptions().position(latLng));
                } else if (viewSelected == editStart) {
                    if (m_startPoint != null) {
                        m_startPoint.remove();
                    }
                    runOnMainThread(() -> editStart.setText(""));
                    m_startPoint = googleMap.addMarker(new MarkerOptions().position(latLng));
                } else if (viewSelected == editLocationMiddle1) {
                    if (m_middlePoint1 != null) {
                        m_middlePoint1.remove();
                    }
                    runOnMainThread(() -> editLocationMiddle1.setText(""));
                    m_middlePoint1 = googleMap.addMarker(new MarkerOptions().position(latLng));
                } else if (viewSelected == editLocationMiddle2) {
                    if (m_middlePoint2 != null) {
                        m_middlePoint2.remove();
                    }
                    runOnMainThread(() -> editLocationMiddle2.setText(""));
                    m_middlePoint2 = googleMap.addMarker(new MarkerOptions().position(latLng));
                }
                Utils.getAddress(latLng.latitude, latLng.longitude, this, s -> {
                    if (viewSelected == editEnd) {
                        placeEnd = Place.builder().setLatLng(latLng).setAddress(s).setName(s).build();
                        runOnMainThread(() -> editEnd.setText(s));
                    } else if (viewSelected == editStart) {
                        placeStart = Place.builder().setLatLng(latLng).setAddress(s).setName(s).build();
                        runOnMainThread(() -> editStart.setText(s));
                    } else if (viewSelected == editLocationMiddle1) {
                        placeLocation1 = Place.builder().setLatLng(latLng).setAddress(s).setName(s).build();
                        runOnMainThread(() -> editLocationMiddle1.setText(s));
                    } else if (viewSelected == editLocationMiddle2) {
                        placeLocation2 = Place.builder().setLatLng(latLng).setAddress(s).setName(s).build();
                        runOnMainThread(() -> editLocationMiddle2.setText(s));
                    }
                });
            }
        });
    }

    private boolean retrieveRouteInProgress() {
        RouteItem routeInProgress = PrefsManager.getInstance().getRouteInProgress(this);
        if (inProgressRoute && routeInProgress != null) {
            routeItem = routeInProgress;
            org.threeten.bp.Duration d = org.threeten.bp.Duration.between(routeItem.getDateStart(), LocalDateTime.now(Clock.systemUTC()));
            int duration = (int) d.toMinutes();
            routeItem.setDurationInMins(duration);
            routeItem.setConsumptionAvg(routeItem.getConsumptionAvg());

            List<LatLng> decodedPath = PolyUtil.decode(routeItem.getPolyLine());
            routeItem.setLatLngList(decodedPath);

            drawRouteInfo(routeItem.getLatLngList(), routeItem.getDistanceInKms());
            drawPois(routeItem.getRawPoiList());
            showRouteInfo();
            manageUI();
            manageStartedRouteUI();
            return true;
        }
        return false;
    }

    private void retrieveViews() {
        titleBar = findViewById(R.id.new_route_titleBar);
        btnHide = findViewById(R.id.new_route_btnHide);
        layoutLocations = findViewById(R.id.new_route_layoutLocations);
        layoutRouteInfo = findViewById(R.id.new_route_layoutRouteInfo);
        btnLocation = findViewById(R.id.new_route_btnLocation);
        btnCalculate = findViewById(R.id.new_route_btnCalculate);
        btnAddLocation = findViewById(R.id.new_route_btnAddLocation);
        imgTwoDots = findViewById(R.id.new_route_imgTwoDots);
        imgDestination = findViewById(R.id.new_route_imgDestination);
        imgTwoDotsMiddle1 = findViewById(R.id.new_route_imgTwoDotsMiddle1);
        imgDestinationMiddle1 = findViewById(R.id.new_route_imgDestinationMiddle1);
        imgTwoDotsMiddle2 = findViewById(R.id.new_route_imgTwoDotsMiddle2);
        imgDestinationMiddle2 = findViewById(R.id.new_route_imgDestinationMiddle2);
        editStart = findViewById(R.id.new_route_editStart);
        editLocationMiddle1 = findViewById(R.id.new_route_editLocationMiddle1);
        editLocationMiddle2 = findViewById(R.id.new_route_editLocationMiddle2);
        editEnd = findViewById(R.id.new_route_editEnd);
        txtDuration = findViewById(R.id.new_route_txtDuration);
        txtDistance = findViewById(R.id.new_route_txtDistance);
        txtConsumption = findViewById(R.id.new_route_txtConsumption);
        txtConsumptionAvg = findViewById(R.id.new_route_txtConsumptionAvg);
        txtDrivingTechnique = findViewById(R.id.new_route_txtDrivingTechnique);
        mapView = findViewById(R.id.new_route_map);
        layoutStartSave = findViewById(R.id.new_route_layoutStartSave);
        layoutFinish = findViewById(R.id.new_route_layoutFinish);
        btnStart = findViewById(R.id.new_route_btnStart);
        btnFinish = findViewById(R.id.new_route_btnFinish);
        btnSave = findViewById(R.id.new_route_btnSave);
        btnNewPoi = findViewById(R.id.new_route_btnNewPoi);
    }

    private void setEvents() {
        if (titleBar != null) {
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
        }

        btnHide.setOnClickListener(view -> hideRoute());

        btnCalculate.setOnClickListener(view -> calculateRoute());

        btnAddLocation.setOnClickListener(view -> addLocation());

        editStart.setOnClickListener(this::openAutocomplete);

        editLocationMiddle1.setOnClickListener(this::openAutocomplete);

        editLocationMiddle2.setOnClickListener(this::openAutocomplete);

        editEnd.setOnClickListener(this::openAutocomplete);

        btnLocation.setOnClickListener(view -> centerMapInMyLocationWithAnimation());

        btnStart.setOnClickListener(view -> startRoute());

        btnFinish.setOnClickListener(view -> finishRoute());

        btnNewPoi.setOnClickListener(view -> openNewPoi());

        btnSave.setOnClickListener(view -> {
            if (routeItem.getRouteType() != General.RouteType.DONE) {
                routeItem.setRouteType(General.RouteType.PLANNED);
            }
            openSaveRoute();
        });
    }

    private void addLocation() {
        if (editLocationMiddle1.getVisibility() != View.VISIBLE) {
            editLocationMiddle1.setVisibility(View.VISIBLE);
            animateView(editLocationMiddle1, editStart.getHeight());
            imgTwoDotsMiddle1.setVisibility(View.VISIBLE);
            animateView(imgTwoDotsMiddle1, imgTwoDots.getHeight());
            imgDestinationMiddle1.setVisibility(View.VISIBLE);
            animateView(imgDestinationMiddle1, imgDestination.getHeight());
        } else if (editLocationMiddle1.getVisibility() == View.VISIBLE) {
            editLocationMiddle2.setVisibility(View.VISIBLE);
            animateView(editLocationMiddle2, editStart.getHeight());
            imgTwoDotsMiddle2.setVisibility(View.VISIBLE);
            animateView(imgTwoDotsMiddle2, imgTwoDots.getHeight());
            imgDestinationMiddle2.setVisibility(View.VISIBLE);
            animateView(imgDestinationMiddle2, imgDestination.getHeight());
            btnAddLocation.setVisibility(View.GONE);
        }
    }

    private void animateView(View view, int height) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, height);
        valueAnimator.setDuration(500);
        valueAnimator.addListener(new ValueAnimator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }
        });

        valueAnimator.addUpdateListener(valueAnimator1 -> {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = (int) valueAnimator1.getAnimatedValue();
            view.setLayoutParams(layoutParams);
        });
        valueAnimator.start();
    }

    private void openAutocomplete(View view) {
        editStart.setError(null);
        editLocationMiddle1.setError(null);
        editLocationMiddle2.setError(null);
        editEnd.setError(null);
        viewSelected = view;
        ((EditText)view).setError("*");
        if (viewSelected == editEnd) {
            if (m_endPoint != null) {
                m_endPoint.remove();
            }
            runOnMainThread(() -> editEnd.setText(""));
        } else if (viewSelected == editStart) {
            if (m_startPoint != null) {
                m_startPoint.remove();
            }
            runOnMainThread(() -> editStart.setText(""));
        } else if (viewSelected == editLocationMiddle1) {
            if (m_middlePoint1 != null) {
                m_middlePoint1.remove();
            }
            runOnMainThread(() -> editLocationMiddle1.setText(""));
        } else if (viewSelected == editLocationMiddle2) {
            if (m_middlePoint2 != null) {
                m_middlePoint2.remove();
            }
            runOnMainThread(() -> editLocationMiddle2.setText(""));
        }
        PlacesClient placesClient = MyApp.getPlacesClient();
        if (placesClient != null) {
            List<Place.Field> fields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(this);
            startActivityForResult(intent, REQUEST_CODE_PLACE_AUTOCOMPLETE);
        }
        Utils.dismissKeyboard(view);
    }

    private void finishRoute() {
        routeItem.setDateEnd(LocalDateTime.now(Clock.systemUTC()));
        routeItem.setRouteType(General.RouteType.PENDING);
        openSaveRoute();
    }

    private void startRoute() {
        RouteItem routeInProgress = PrefsManager.getInstance().getRouteInProgress(this);
        if (routeInProgress == null) {
            routeItem.setDateStart(LocalDateTime.now(Clock.systemUTC()));
            routeItem.setTitle("");
            routeItem.setDescription("");
            routeItem.setRouteType(General.RouteType.PENDING);
            createPendingRoute();
            manageStartedRouteUI();
        } else {
            DialogMultiple dm = new DialogMultiple(this);
            dm.setTitle("Active route found.");
            dm.setDescription("Do you want to delete the previous route?");
            dm.addButton("Yes", () -> {
                PrefsManager.getInstance().deleteRouteInProgress(this);
                routeItem.setDateStart(LocalDateTime.now(Clock.systemUTC()));
                routeItem.setTitle("");
                routeItem.setDescription("");
                routeItem.setRouteType(General.RouteType.PENDING);
                createPendingRoute();
                manageStartedRouteUI();
            });
            dm.addButton("No", () -> {});
            dm.show();
        }
        if (BuildConfig.DEBUG) {
            //debugFunction01();
        }
    }

    private void debugFunction01() {
        if (BuildConfig.DEBUG) {
            Runnable run = () -> {
                try {
                    assert placeEnd.getLatLng() != null;
                    Uri intentUri = Uri.parse(String.format("google.navigation:q=%s,%s", placeEnd.getLatLng().latitude, placeEnd.getLatLng().longitude));
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, intentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");

                    startActivity(mapIntent);
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
    }

    private void createPendingRoute() {
        routeItem.setTitle(DateUtils.utcToString(routeItem.getDateStart(), "dd/MM/yyyy HH:mm:ss"));
        @SuppressLint("StaticFieldLeak")
        CreatePendingTask cpt = new CreatePendingTask(routeItem) {
            @Override
            protected void onPostExecute(Long aLong) {
                if (aLong != null) {
                    routeItem.setId(aLong);
                }
            }
        };
        cpt.execute();
    }

    private void manageStartedRouteUI() {
        layoutStartSave.setVisibility(View.GONE);
        layoutFinish.setVisibility(View.VISIBLE);
        btnHide.setVisibility(View.VISIBLE);
        centerMapInMyLocationWithAnimation();
    }

    private void hideRoute() {
        try {
            PrefsManager.getInstance().saveRouteInProgress(routeItem, this);
            openDashboard();
        } catch (Exception e) {
            //Log.e("NewRouteActivity", "Exception", e);
        }
    }

    private void openDashboard() {
        MySharedPreferences.createLogin(this).putBoolean("GoToDashboard", true);
        finish();
        overridePendingTransition(0, R.anim.top_to_bottom);
    }

    private void openNewPoi() {
        Intent intent = new Intent(this, NewPoiActivity.class);
        intent.putExtra(Constants.Extras.ROUTE, routeItem.toString());
        final Location[] myLocation = {getMyLocation()};
        new Thread(() -> {
            while(myLocation[0] == null) {
                myLocation[0] = getMyLocation();
            }
            LatLng poiLocation = new LatLng(myLocation[0].getLatitude(), myLocation[0].getLongitude());
            intent.putExtra(Constants.Extras.POI_LOCATION, poiLocation);
            runOnUiThread(() -> startActivityForResult(intent, REQUEST_CODE_NEW_POI));
        }, "NewPoiThread").start();
    }

    private void openSaveRoute() {
        String json = routeItem.toString();
        Intent intent = new Intent(this, SaveRouteActivity.class);
        ImageUtils.putPost(0, Constants.Extras.ROUTE, json);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PLACE_AUTOCOMPLETE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                managePlaceSelected(place);
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Toast.makeText(this, status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE_NEW_POI && resultCode == RESULT_OK && data.getExtras() != null) {
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
            }
        }
    }

    private void managePlaceSelected(Place place) {
        savePlaceSelected(place);
        showPlaceNames();
        LatLng latLng = place.getLatLng();
        if (latLng != null) {
            if (viewSelected == editEnd) {
                m_endPoint = googleMap.addMarker(new MarkerOptions().position(latLng));
            } else if (viewSelected == editStart) {
                m_startPoint = googleMap.addMarker(new MarkerOptions().position(latLng));
            } else if (viewSelected == editLocationMiddle1) {
                m_middlePoint1 = googleMap.addMarker(new MarkerOptions().position(latLng));
            } else if (viewSelected == editLocationMiddle2) {
                m_middlePoint2 = googleMap.addMarker(new MarkerOptions().position(latLng));
            }
        }
    }

    private void savePlaceSelected(Place place) {
        try {
            if (viewSelected.getId() == editStart.getId()) {
                placeStart = place;
            } else if (viewSelected.getId() == editLocationMiddle1.getId()) {
                placeLocation1 = place;
            } else if (viewSelected.getId() == editLocationMiddle2.getId()) {
                placeLocation2 = place;
            } else if (viewSelected.getId() == editEnd.getId()) {
                placeEnd = place;
            }
        } catch (NullPointerException ignored) {
            // NPE...
        }
    }

    private void showPlaceNames() {
        editStart.setText(placeStart != null ? placeStart.getName() : "");
        editLocationMiddle1.setText(placeLocation1 != null ? placeLocation1.getName() : "");
        editLocationMiddle2.setText(placeLocation2 != null ? placeLocation2.getName() : "");
        editEnd.setText(placeEnd != null ? placeEnd.getName() : "");
    }

    private void calculateRoute() {
        if ((placeEnd == null)
                || (placeLocation1 == null && editLocationMiddle1.getVisibility() == View.VISIBLE)
                || (placeLocation2 == null && editLocationMiddle2.getVisibility() == View.VISIBLE)) {
            Toast.makeText(this, R.string.please_select_locations, Toast.LENGTH_SHORT).show();
        } else if (Utils.isLocationDisabled(this) && placeStart == null) {
            DialogYesNo dyn = new DialogYesNo(this);
            dyn.setMessage(getString(R.string.gps_is_necessary));
            dyn.setYesButtonClickListener(() -> Utils.enableLocation(this));
            dyn.show();
        } else if (Utils.hasLocationPermissionGiven(this)) {
            if (ConnectionUtils.isOnline(getApplicationContext())) {
                if (m_endPoint != null) {
                    m_endPoint.remove();
                }
                if (m_startPoint != null) {
                    m_startPoint.remove();
                }
                if (m_middlePoint1 != null) {
                    m_middlePoint1.remove();
                }
                if (m_middlePoint2 != null) {
                    m_middlePoint2.remove();
                }
                viewSelected = null;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    calculateRouteTask = new CalculateRouteTask();
                    calculateRouteTask.execute();
                } else {
                    calculateRouteNoTask();
                }
            } else {
                ConnectionUtils.showOfflineToast();
            }
        }
    }

    private void calculateRouteNoTask() {
        progressDialog = new ProgressDialog(NewRouteActivity.this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();
        new Thread(() -> {
            try {
                DirectionsApiRequest req = DirectionsApi.newRequest(new GeoApiContext.Builder().apiKey(getString(R.string.google_maps_key)).build());
                req.mode(TravelMode.DRIVING);
                LatLng startLocation, endLocation;
                Location myLocation = getMyLocation();
                if (placeStart == null && myLocation == null) {
                    Task<Location> tsk = Utils.getLocation(NewRouteActivity.this);
                    while (!tsk.isComplete()) {
                        com.otcengineering.apible.Utils.wait(this, 250);
                    }
                    myLocation = tsk.getResult();
                }
                Runnable errorRetrievingInfoFromGoogleMapsPopup = () -> showCustomDialogError(getString(R.string.error_retrieving_info_from_google_maps));
                if (placeStart == null) {
                    if (myLocation == null) {
                        progressDialog.dismiss();
                        runOnMainThread(errorRetrievingInfoFromGoogleMapsPopup);
                        return;
                    }
                    startLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                } else {
                    startLocation = placeStart.getLatLng();
                }
                endLocation = placeEnd.getLatLng();
                if (startLocation == null || endLocation == null) {
                    progressDialog.dismiss();
                    runOnUiThread(() -> Toast.makeText(this, R.string.please_select_locations, Toast.LENGTH_SHORT).show());
                    return;
                }
                com.google.maps.model.LatLng ori = change(startLocation);
                com.google.maps.model.LatLng fin = change(endLocation);
                req.origin(ori);
                req.destination(fin);
                req.optimizeWaypoints(true);
                if (placeLocation1 != null && placeLocation2 != null) {
                    req.waypoints(change(placeLocation1.getLatLng()), change(placeLocation2.getLatLng()));
                } else if (placeLocation1 != null) {
                    req.waypoints(change(placeLocation1.getLatLng()));
                } else if (placeLocation2 != null) {
                    req.waypoints(change(placeLocation2.getLatLng()));
                }
                req.units(Unit.METRIC);
                try {
                    DirectionsResult res = req.await();
                    long distance = 0;
                    int routeSeconds = 0;
                    for (DirectionsLeg leg : res.routes[0].legs) {
                        distance += leg.distance.inMeters;
                        routeSeconds += leg.duration.inSeconds;
                    }
                    routeInfoBuilder.setDistance(distance);
                    routeItem.setDistanceInMeters(distance);
                    routeInfoBuilder.setDuration(routeSeconds / 60);
                    routeItem.setDurationInMins(routeSeconds / 60);
                    routeItem.setPolyLine(res.routes[0].overviewPolyline.getEncodedPath());
                    if (placeStart != null) {
                        routeInfoBuilder.setPointStart(placeStart.getName());
                    }
                    routeInfoBuilder.setPointEnd(placeEnd.getName());
                    routeItem.setLatLngList(change(res.routes[0].overviewPolyline.decodePath()));
                } catch (ArrayIndexOutOfBoundsException | ApiException | InterruptedException | IOException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                    runOnMainThread(errorRetrievingInfoFromGoogleMapsPopup);
                    return;
                }

                Utils.runOnMainThread(() -> {
                    progressDialog.dismiss();
                    drawRouteInfo(routeItem.getLatLngList(), routeItem.getDistanceInKms());
                    getRouteInfo();

                    if (placeLocation1 != null) {
                        drawMarkerCircle(placeLocation1.getLatLng());
                        drawMarker(placeLocation1.getLatLng(), R.drawable.marker_pin);
                    }

                    if (placeLocation2 != null) {
                        drawMarkerCircle(placeLocation2.getLatLng());
                        drawMarker(placeLocation2.getLatLng(), R.drawable.marker_pin);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Utils.runOnMainThread(() -> showCustomDialogError(getString(R.string.error_try_again)));
                progressDialog.dismiss();
            }
        }, "CalculateRouteThread").start();
    }

    private com.google.maps.model.LatLng change(com.google.android.gms.maps.model.LatLng location) {
        return new com.google.maps.model.LatLng(location.latitude, location.longitude);
    }

    private List<LatLng> change(List<com.google.maps.model.LatLng> li) {
        List<LatLng> newLi = new ArrayList<>();
        for (com.google.maps.model.LatLng latLng : li) {
            newLi.add(new LatLng(latLng.lat, latLng.lng));
        }
        return newLi;
    }

    @SuppressLint("StaticFieldLeak")
    private class CalculateRouteTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(NewRouteActivity.this);
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.show();

            if (m_endPoint != null) {
                m_endPoint.remove();
            }
            if (m_startPoint != null) {
                m_startPoint.remove();
            }
            if (m_middlePoint1 != null) {
                m_middlePoint1.remove();
            }
            if (m_middlePoint2 != null) {
                m_middlePoint2.remove();
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS);

            OkHttpClient client = okHttpBuilder.build();

            Request request = new Request.Builder()
                    .url(buildUrl())
                    .get()
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String json = null;
                    if (response.body() != null) {
                        json = response.body().string();
                    }
                    if (json != null && !json.contains("error_message")) {
                        return parseJsonToRouteInfoBuilder(json);
                    } else {
                        return false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }

        private boolean parseJsonToRouteInfoBuilder(String json) {
            try {
                if (placeStart != null) {
                    routeInfoBuilder.setPointStart(placeStart.getName());
                }
                if (placeEnd != null) {
                    routeInfoBuilder.setPointEnd(placeEnd.getName());
                }
                JSONObject responseJson = new JSONObject(json);
                JSONArray routes = responseJson.getJSONArray("routes");
                JSONObject route = routes.getJSONObject(0);
                JSONArray legs = route.getJSONArray("legs");

                for (int i = 0; i < legs.length(); i++) {
                    JSONObject leg = legs.getJSONObject(i);
                    JSONObject distance = leg.getJSONObject("distance");
                    double distanceInMeters = distance.getDouble("value");
                    routeInfoBuilder.setDistance(routeInfoBuilder.getDistance() + distanceInMeters);
                    routeItem.setDistanceInMeters(routeItem.getDistanceInMeters() + distanceInMeters);

                    JSONObject duration = leg.getJSONObject("duration");
                    double durationInSecs = duration.getDouble("value");
                    int durationInMins = (int) (durationInSecs / 60);
                    routeInfoBuilder.setDuration(routeInfoBuilder.getDuration() + durationInMins);
                    routeItem.setDurationInMins(routeItem.getDurationInMins() + durationInMins);
                }

                JSONObject poly = route.getJSONObject("overview_polyline");
                routeItem.setPolyLine(poly.getString("points"));
                List<LatLng> decodedPath = PolyUtil.decode(poly.getString("points"));
                routeItem.setLatLngList(decodedPath);

                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }

        private String buildUrl() {
            General.Point.Builder startPointBuilder = General.Point.newBuilder();
            LatLng start;
            if (placeStart == null) {
                Location myLocation = getMyLocation();
                if (myLocation == null) {
                    return "";
                }
                start = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                startPointBuilder.setLatitude(start.latitude);
                startPointBuilder.setLongitude(start.longitude);
            } else {
                start = placeStart.getLatLng();
                startPointBuilder.setLatitude(placeStart.getLatLng().latitude);
                startPointBuilder.setLongitude(placeStart.getLatLng().longitude);
            }
            routeInfoBuilder.addPoints(startPointBuilder.build());

            String waypoints = "";
            if (placeLocation1 != null) {
                waypoints = waypoints + placeLocation1.getLatLng().latitude + "," + placeLocation1.getLatLng().longitude;
                General.Point.Builder loc1PointBuilder = General.Point.newBuilder();
                loc1PointBuilder.setLatitude(placeLocation1.getLatLng().latitude);
                loc1PointBuilder.setLongitude(placeLocation1.getLatLng().longitude);

                routeInfoBuilder.addPoints(loc1PointBuilder.build());
                if (placeLocation2 != null) {
                    waypoints = waypoints + "|" + placeLocation2.getLatLng().latitude + "," + placeLocation2.getLatLng().longitude;
                    General.Point.Builder loc2PointBuilder = General.Point.newBuilder();
                    loc2PointBuilder.setLatitude(placeLocation2.getLatLng().latitude);
                    loc2PointBuilder.setLongitude(placeLocation2.getLatLng().longitude);
                    routeInfoBuilder.addPoints(loc2PointBuilder.build());
                }
            }

            General.Point.Builder endPointBuilder = General.Point.newBuilder();
            if (placeEnd != null) {
                endPointBuilder.setLatitude(placeEnd.getLatLng().latitude);
                endPointBuilder.setLongitude(placeEnd.getLatLng().longitude);
            }
            routeInfoBuilder.addPoints(endPointBuilder.build());

            return "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=" + start.latitude + "," + start.longitude +
                    "&destination=" + endPointBuilder.getLatitude() + "," + endPointBuilder.getLongitude() +
                    "&waypoints=" + waypoints +
                    "&sensor=false" +
                    "&mode=driving" +
                    "&key=" + getString(R.string.google_maps_key);
        }

        private com.google.maps.model.LatLng change(LatLng lat) {
            return new com.google.maps.model.LatLng(lat.latitude, lat.longitude);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                drawRouteInfo(routeItem.getLatLngList(), routeItem.getDistanceInKms());
                getRouteInfo();

                if (placeLocation1!=null){
                    drawMarkerCircle(placeLocation1.getLatLng());
                    drawMarker(placeLocation1.getLatLng(), R.drawable.marker_pin);
                }

                if (placeLocation2!=null){
                    drawMarkerCircle(placeLocation2.getLatLng());
                    drawMarker(placeLocation2.getLatLng(), R.drawable.marker_pin);
                }
            } else {
                progressDialog.dismiss();
                Toast.makeText(NewRouteActivity.this, R.string.error_retrieving_info_from_google_maps, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getRouteInfo() {
        if (ConnectionUtils.isOnline(getApplicationContext())) {
            getRouteInfoTask = new GetRouteInfoTask();
            getRouteInfoTask.execute();
        } else {
            ConnectionUtils.showOfflineToast();
            progressDialog.dismiss();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetRouteInfoTask extends AsyncTask<Void, Void, MyTrip.RouteInfoResponse> {

        @Override
        protected MyTrip.RouteInfoResponse doInBackground(Void... strings) {
            try {
                MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());

                MyTrip.RouteInfoResponse response = ApiCaller.doCall(Endpoints.ROUTE_INFO, msp.getBytes("token"), routeInfoBuilder.build(), MyTrip.RouteInfoResponse.class);

                routeItem.setConsumption(response.getConsumption());
                routeItem.setConsumptionAvg(response.getAvgConsumption());
                /*routeItem.setConsumptionAvg(0.21);
                routeItem.setConsumption((int)(routeItem.getDistanceInKms() * 21));*/

                return response;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(MyTrip.RouteInfoResponse result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            if (result != null) {
                manageButtonsUI();
                drawPois(result.getPoisList());
                manageUI();
                showRouteInfo();
            } else {
                Toast.makeText(NewRouteActivity.this, R.string.error_retrieving_info_from_server, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void drawPois(List<General.POI> poiList) {
        for (General.POI poi : poiList) {
            LatLng latLng = new LatLng(poi.getLatitude(), poi.getLongitude());
            try {
                drawMarker(latLng, R.drawable.marker_poi, poi.getTitle());
            } catch (ClassCastException cce) {
                //Log.e(TAG, "Yup, that again...", cce);
            }
        }
    }

    private void manageUI() {
        layoutLocations.setVisibility(View.GONE);
        layoutRouteInfo.setVisibility(View.VISIBLE);
    }

    private void showRouteInfo() {
        txtDuration.setText(routeItem.getDurationInMinsFormatted());
        txtDistance.setText(String.format(Locale.US, "%.1f km", routeItem.getDistanceInKms()));
        txtConsumption.setText(String.format(Locale.US, "%.2f l", routeItem.getConsumption()));
        if (routeItem.getConsumptionAvg()>100){
            txtConsumptionAvg.setText(String.format(Locale.US, "+%.1f km/l", 100.));
        }else{
            txtConsumptionAvg.setText(String.format(Locale.US, "%.1f km/l", routeItem.getConsumptionAvg()));
        }
        txtDrivingTechnique.setText(String.format(Locale.US, "%.1f", routeItem.getDrivingTechnique() / 10));
    }

    private void manageButtonsUI() {
        btnStart.setTextColor(getResources().getColor(R.color.quantum_black_100));
        btnStart.setEnabled(true);
        btnSave.setTextColor(getResources().getColor(R.color.colorPrimary));
        btnSave.setEnabled(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (calculateRouteTask != null) {
            calculateRouteTask.cancel(true);
        }
        if (getRouteInfoTask != null) {
            getRouteInfoTask.cancel(true);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onBackPressed() {
       if (routeItem != null && routeItem.getRouteType() == General.RouteType.PENDING) {
            DialogYesNo dyn = new DialogYesNo(NewRouteActivity.this, getString(R.string.dismiss_route), () -> {}, this::dismissRoute);
            dyn.setNoButtonText(getString(R.string.yes));
            dyn.setYesButtonText(getString(R.string.no));
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
        DeleteRouteTask drt = new DeleteRouteTask(routeItem.getId());
        drt.execute();
        finish();
    }

}
