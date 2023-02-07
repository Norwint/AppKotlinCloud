package com.otcengineering.white_app.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.protobuf.ByteString;
import com.otc.alice.api.model.General;
import com.otc.alice.api.model.MyTrip;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.components.PoiRow;
import com.otcengineering.white_app.components.TitleBar;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.serialization.pojo.PoiImageWrapper;
import com.otcengineering.white_app.serialization.pojo.PoiWrapper;
import com.otcengineering.white_app.serialization.pojo.RouteItem;
import com.otcengineering.white_app.tasks.GetPoiInfoTask;
import com.otcengineering.white_app.tasks.TypedTask;
import com.otcengineering.white_app.utils.CloudErrorHandler;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.DateUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.PrefsManager;
import com.otcengineering.white_app.utils.Utils;
import com.otcengineering.white_app.utils.images.ImageRetriever;
import com.otcengineering.white_app.utils.images.ImageUtils;
import com.otcengineering.white_app.utils.interfaces.TypedCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

/**
 * Created by cenci7
 */

public class SaveRouteActivity extends BaseActivity {

    private static final int REQUEST_CODE_NEW_POI = 1;
    private static final int REQUEST_CODE_POI_TYPE = 2;
    private static final int REQUEST_CODE_GALLERY = 3;
    private static final int REQUEST_CODE_CAMERA = 4;

    private TitleBar titleBar;
    private EditText editTitle;
    private EditText editDescription;
    private TextView txtChars;
    private TextView txtDuration, txtDistance, txtConsumption, txtConsumptionAvg,
            txtDrivingTechnique;
    private LinearLayout layoutPoi;
    private Button btnNewPoint;
    private Button btnSave;
    private Button btnNoSave;

    private RouteItem routeItem;

    private String imagePath;

    private List<PoiRow> poiRowList = new ArrayList<>();

    private ProgressDialog progressDialog;
    private String polyLine;

    private GetPoiInfoTask getPoiInfoTask;
    private EditRouteTask editRouteTask;

    private int poiPositionSelected;
    private PoiWrapper poiSelected;
    private boolean edit = false;

    private LatLng m_location;

    public SaveRouteActivity() {
        super("SaveRouteActivity");
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Task<Location> loc = Utils.getLocation(this);
        if (loc != null) {
            loc.addOnSuccessListener(this, (l) -> this.m_location = new LatLng(l.getLatitude(), l.getLongitude()));
        }

        setContentView(R.layout.activity_save_route);
        retrieveExtras();
        retrieveViews();
        setEvents();
        showRouteInfo();
    }

    private void retrieveExtras() {
        if (getIntent() != null && getIntent().getExtras() != null) {
            routeItem = new RouteItem(getIntent().getExtras().getString(Constants.Extras.ROUTE));
            polyLine = getIntent().getExtras().getString(Constants.Extras.ROUTE_POLYLINE);
            edit = getIntent().getExtras().getBoolean("Edit", false);
        } else {
            String json = ImageUtils.getPost(0, Constants.Extras.ROUTE);
            routeItem = new RouteItem(json);
            polyLine = routeItem.getPolyLine();
            ImageUtils.deletePost(0, Constants.Extras.ROUTE);
        }
    }

    private void retrieveViews() {
        titleBar = findViewById(R.id.save_route_titleBar);
        editTitle = findViewById(R.id.save_route_editTitle);
        editDescription = findViewById(R.id.save_route_editDescription);
        txtChars = findViewById(R.id.save_route_txtChars);
        txtDuration = findViewById(R.id.save_route_txtDuration);
        txtDistance = findViewById(R.id.save_route_txtDistance);
        txtConsumption = findViewById(R.id.save_route_txtConsumption);
        txtConsumptionAvg = findViewById(R.id.save_route_txtConsumptionAvg);
        txtDrivingTechnique = findViewById(R.id.save_route_txtDrivingTechnique);
        layoutPoi = findViewById(R.id.save_route_layoutPoi);
        btnNewPoint = findViewById(R.id.save_route_btnNewPoint);
        btnSave = findViewById(R.id.save_route_btnSave);
        btnNoSave = findViewById(R.id.save_route_btnNoSave);
    }


    @SuppressLint("ClickableViewAccessibility")
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

        editTitle.setOnTouchListener(createListenerForRightDrawable());

        editTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                manageButtonSaveUI(1);
            }
        });

        editDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                int charsAvailable = Constants.MAX_CHARS - editable.toString().length();
                if (charsAvailable < 0) {
                    txtChars.setText(R.string.max_chars);
                    txtChars.setTextColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    txtChars.setText(String.valueOf(charsAvailable));
                    txtChars.setTextColor(getResources().getColor(R.color.black_30_alpha));
                }
                manageButtonSaveUI(charsAvailable);
            }
        });

        btnNewPoint.setOnClickListener(view -> openNewPoi());

        btnSave.setOnClickListener(view -> saveRoute());

        btnNoSave.setOnClickListener(v -> discardRoute());
    }

    private void discardRoute() {
        PrefsManager.getInstance().deleteRouteInProgress(getApplicationContext());
        finish();
    }

    @NonNull
    private View.OnTouchListener createListenerForRightDrawable() {
        return (v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (editTitle.getRight() - editTitle.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    editTitle.setText("");
                    return true;
                }
            }
            return false;
        };
    }

    private void manageButtonSaveUI(int charsAvailable) {
        boolean enableButton = charsAvailable > 0 && !editTitle.getText().toString().isEmpty();
        btnSave.setTextColor(enableButton ? getResources().getColor(R.color.textButton) : getResources().getColor(R.color.layout_border));
        btnSave.setEnabled(enableButton);
    }

    private void showAddImageDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle(R.string.select_action);
        String[] pictureDialogItems = {
                getString(R.string.select_from_gallery),
                getString(R.string.capture_from_camera)};
        pictureDialog.setItems(pictureDialogItems,
                (dialog, which) -> {
                    switch (which) {
                        case 0:
                            choosePhotoFromGallery();
                            break;
                        case 1:
                            takePhotoFromCamera();
                            break;
                    }
                });
        pictureDialog.show();
    }

    private void takePhotoFromCamera() {
        initializeImagePath();
        ImageRetriever.functionCamera(imagePath, REQUEST_CODE_CAMERA, this);
    }

    public void choosePhotoFromGallery() {
        initializeImagePath();
        ImageRetriever.functionGallery(REQUEST_CODE_GALLERY, this);
    }

    private void initializeImagePath() {
        imagePath = ImageUtils.getPoiImageName(getPoiPosition(), this);
    }

    private long getPoiPosition() {
        return poiSelected.getInternalID();
    }

    private void showRouteInfo() {
        editTitle.setText(routeItem.getTitle());
        String description = routeItem.getDescription();
        editDescription.setText(description);

        txtDuration.setText(routeItem.getDurationInMinsFormatted());

        txtDistance.setText(String.format(Locale.US, "%.1f km", routeItem.getDistanceInKms()));
        txtConsumption.setText(String.format(Locale.US, "%.2f l", routeItem.getConsumption()));
        if (routeItem.getConsumptionAvg() > 100){
            txtConsumptionAvg.setText(String.format(Locale.US, "+%.1f km/l", 100.0));
        } else{
            txtConsumptionAvg.setText(String.format(Locale.US, "%.1f km/l", routeItem.getConsumptionAvg()));
        }
        txtDrivingTechnique.setText(String.format(Locale.US, "%.1f", routeItem.getDrivingTechnique() / 10));

        fillPoiList();
    }

    private void fillPoiList() {
        layoutPoi.removeAllViews();
        poiRowList.clear();
        List<PoiWrapper> poiList = routeItem.getPoiList();
        for (int i = 0; i < poiList.size(); i++) {
            PoiWrapper poi = poiList.get(i);
            if (poi.getStatus() != PoiWrapper.PoiStatus.Deleted) {
                if (poi.getPoi().getImagesCount() > 0) {
                    PoiImageWrapper piw = new PoiImageWrapper(poi.getPoi().getImages(0));
                    poi.getImages().add(piw);
                }
                PoiRow poiRow = new PoiRow(this, poi, i, new PoiRow.PoiRowListener() {
                    @Override
                    public void onDelete(int position) {
                        routeItem.getPoiList().get(position).setStatus(PoiWrapper.PoiStatus.Deleted);
                        fillPoiList();
                    }

                    @Override
                    public void onTypeChanged(int position) {
                        updatePoiSelected(position);
                        openPoiType();
                    }

                    @Override
                    public void onImageAdded(int position) {
                        updatePoiSelected(position);
                        showAddImageDialog();
                    }

                    @Override
                    public void onImageDeleted(int position) {
                        updatePoiSelected(position);
                        clearPoiImages();
                    }
                });
                layoutPoi.addView(poiRow);
                poiRowList.add(poiRow);
            }
        }
    }

    public void clearPoiImages() {
        List<PoiWrapper> poiList = routeItem.getPoiList();
        List<PoiWrapper> newPoiList = new ArrayList<>();
        for (PoiWrapper poi : poiList) {
            if (poi.getPoi().getPoiId() == poiSelected.getPoi().getPoiId()) {
                for (PoiImageWrapper wr : poi.getImages()) {
                    wr.setStatus(PoiImageWrapper.ImageStatus.Deleted);
                }
                General.POI.Builder builder = poiRowList.get(poiPositionSelected).createPoiBuilderWithCurrentValues();
                builder.clearImages();
                PoiWrapper wrap = new PoiWrapper(builder.build());
                wrap.setStatus(poi.getStatus());
                newPoiList.add(wrap);
            } else {
                newPoiList.add(poi);
            }
        }
        routeItem.setPoiList(newPoiList);
        fillPoiList();
    }

    private void updatePoiSelected(int position) {
        poiPositionSelected = position;
        poiSelected = routeItem.getPoiList().get(position);
    }

    private void changePoiType(General.PoiType poiType) {
        List<PoiWrapper> poiList = routeItem.getPoiList();
        List<PoiWrapper> newPoiList = new ArrayList<>();
        for (PoiWrapper poi : poiList) {
            if (poi.getPoi().getPoiId() == poiSelected.getPoi().getPoiId()) {
                General.POI updatedPoi = updatePoiType(poiType);
                PoiWrapper wrap = new PoiWrapper(updatedPoi);
                wrap.setStatus(poi.getStatus());
                newPoiList.add(wrap);
            } else {
                newPoiList.add(poi);
            }
        }
        routeItem.setPoiList(newPoiList);
        fillPoiList();
    }

    public General.POI updatePoiType(General.PoiType poiType) {
        General.POI.Builder builder = poiRowList.get(poiPositionSelected).createPoiBuilderWithCurrentValues();
        builder.setType(poiType);
        return builder.build();
    }

    private void openPoiType() {
        Intent intent = new Intent(this, PoiTypeActivity.class);
        startActivityForResult(intent, REQUEST_CODE_POI_TYPE);
    }

    private void openNewPoi() {
        Intent intent = new Intent(this, NewPoiActivity.class);
        intent.putExtra(Constants.Extras.ROUTE, routeItem.toString());
        intent.putExtra(Constants.Extras.POI_LOCATION, m_location);
        startActivityForResult(intent, REQUEST_CODE_NEW_POI);
    }

    private boolean isEditMode() {
        return routeItem.getId() != 0;
    }

    private void saveRoute() {
        try {
            Utils.dismissKeyboard(this.getWindow().getCurrentFocus());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (ConnectionUtils.isOnline(getApplicationContext())) {
            if (!edit && routeItem.getRouteType() == General.RouteType.PLANNED) {
                saveNewRoute();
            } else {
                editRoute();
            }
        } else {
            ConnectionUtils.showOfflineToast();
        }
    }

    private void saveNewRoute() {
        if (ConnectionUtils.isOnline(getApplicationContext())) {
            new Thread(() -> {
                try {
                    saveNewRouteNoTask();
                } catch (ApiCaller.OTCException e) {
                    e.printStackTrace();
                }
            }, "SaveNewRouteThread").start();

        } else {
            ConnectionUtils.showOfflineToast();
        }
    }

    private void editRoute() {
        if (ConnectionUtils.isOnline(getApplicationContext())) {
            MyTrip.RouteId routeId = MyTrip.RouteId.newBuilder().setRouteId(routeItem.getId()).build();
            TypedTask<MyTrip.RouteId> getSourceRoute = new TypedTask<>(Endpoints.GET_SOURCE_ROUTE, routeId, true, MyTrip.RouteId.class, new TypedCallback<MyTrip.RouteId>() {
                @Override
                public void onSuccess(@Nonnull MyTrip.RouteId value) {
                    routeItem.setId(value.getRouteId());
                    editRouteTask = new EditRouteTask();
                    editRouteTask.execute();
                }

                @Override
                public void onError(@Nonnull Shared.OTCStatus status, @Nullable String message) {
                    if (!Utils.isActivityFinish(SaveRouteActivity.this)) {
                        Toast.makeText(getApplicationContext(), "Error: " + CloudErrorHandler.handleError(status), Toast.LENGTH_LONG).show();
                    }
                }
            });
            getSourceRoute.execute();
        } else {
            ConnectionUtils.showOfflineToast();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_NEW_POI && resultCode == RESULT_OK) {
            PoiWrapper poi = (PoiWrapper) data.getExtras().get(Constants.Extras.POI);
            poi.setStatus(PoiWrapper.PoiStatus.Added);
            if (data.getExtras().containsKey("PoiImg")) {
                String img = data.getExtras().getString("PoiImg");
                PoiImageWrapper piw = Utils.getGson().fromJson(img, PoiImageWrapper.class);
                poi.getImages().add(piw);
            }
            addPoiToRoute(poi);
            fillPoiList();
        } else if (requestCode == REQUEST_CODE_POI_TYPE && resultCode == RESULT_OK
                && data != null && data.getExtras() != null) {
            General.PoiType poiType = (General.PoiType) data.getExtras().get(Constants.Extras.POI_TYPE);
            // changePoiType(poiType);
            try {
                poiSelected.setPoi(poiSelected.getPoi().toBuilder().setType(poiType).build());
                fillPoiList();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK) {
            doActionsAfterGallery(data);
        } else if (requestCode == REQUEST_CODE_CAMERA && resultCode == RESULT_OK) {
            doActionsAfterCamera();
        }
    }

    private void doActionsAfterCamera() {
        addPoiImageToRecycler();
    }

    private void doActionsAfterGallery(Intent data) {
        if (data != null) {
            Uri imageUri = data.getData();
            String imageLocalUrl = ImageUtils.getRealPathFromURI(imageUri, this);
            // When a image is selected from the gallery it is stored in our path (imagePath)
            if (imageLocalUrl != null && !imageLocalUrl.equals("")) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    ImageUtils.saveBitmapIntoFile(imagePath, bitmap, Bitmap.CompressFormat.JPEG);
                    addPoiImageToRecycler();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, R.string.error_default, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addPoiImageToRecycler() {
        poiRowList.get(poiPositionSelected).updateImages();
        PoiImageWrapper wp = new PoiImageWrapper();
        wp.setStatus(PoiImageWrapper.ImageStatus.Added);
        poiSelected.getImages().add(wp);

    }

    private void addPoiToRoute(PoiWrapper poi) {
        if (routeItem.getPoiList() == null) {
            routeItem.setPoiList(new ArrayList<>());
        }
        List<PoiWrapper> poiList = new ArrayList<>(routeItem.getPoiList());
        poiList.add(poi);
        routeItem.setPoiList(poiList);
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(SaveRouteActivity.this);
            progressDialog.setMessage(getString(R.string.loading));
        }
        try {
            runOnUiThread(() -> {
                try {
                    progressDialog.show();
                } catch (Exception e) {

                }
            });
        } catch (Exception e) {
            //Log.e("SaveRouteActivity", "Exception", e);
        }
    }

    private void dismissProgressDialog() {
        if (progressDialog != null) {
            Utils.runOnMainThread(progressDialog::dismiss);
        }
    }

    private void saveNewRouteNoTask() throws ApiCaller.OTCException {
        runOnUiThread(this::showProgressDialog);

        runOnUiThread(() -> {
            if (progressDialog != null) {
                progressDialog.setMessage(getString(R.string.saving_route));
            }
        });

        MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());
        MyTrip.RouteNew.Builder routeNewBuilder = MyTrip.RouteNew.newBuilder();
        routeNewBuilder.setType(routeItem.getRouteType());
        if (routeItem.getDateStart() == null) {
            routeNewBuilder.setDateStart(DateUtils.getUtcString("yyyy-MM-dd HH:mm:ss"));
        } else {
            routeNewBuilder.setDateStart(DateUtils.dateTimeToString(routeItem.getDateStart(), "yyyy-MM-dd HH:mm:ss"));
        }

        if (routeItem.getDateEnd() == null) {
            routeNewBuilder.setDateEnd(DateUtils.getUtcString("yyyy-MM-dd HH:mm:ss"));
        } else {
            routeNewBuilder.setDateEnd(DateUtils.dateTimeToString(routeItem.getDateEnd(), "yyyy-MM-dd HH:mm:ss"));
        }

        if (routeItem.getDateStart() == null) {
            routeNewBuilder.setLocalDateStart(DateUtils.getLocalString("yyyy-MM-dd HH:mm:ss"));
        } else {
            routeNewBuilder.setLocalDateStart(DateUtils.utcToString(routeItem.getDateStart(), "yyyy-MM-dd HH:mm:ss"));
        }
        String title = editTitle.getText().toString();
        routeNewBuilder.setTitle(title);
        String description = editDescription.getText().toString();
        routeNewBuilder.setDescription(description);
        if (routeItem.isPlanned()) {
            routeNewBuilder.setDuration(routeItem.getDurationInMins());
            routeNewBuilder.setDistance((routeItem.getDistanceInMeters()));
            routeNewBuilder.setConsumption((int) (100 * routeItem.getConsumption()));
            routeNewBuilder.setAvgConsumption(routeItem.getConsumptionAvg());
        }
        MyTrip.RouteId response = ApiCaller.doCall(Endpoints.ROUTE_NEW, msp.getBytes("token"), routeNewBuilder.build(), MyTrip.RouteId.class);
        routeItem.setId(response.getRouteId());

        if (response.getRouteId() > 0) {
            if (ConnectionUtils.isOnline(getApplicationContext())) {
                addGpx();
            } else {
                ConnectionUtils.showOfflineToast();
            }
        } else {
            runOnUiThread(this::dismissProgressDialog);
            Toast.makeText(SaveRouteActivity.this, R.string.error_default + getResources().getString(R.string.add_route), Toast.LENGTH_SHORT).show();
        }
    }

    private void addGpx() {
        try {
            MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());

            if (polyLine != null && !polyLine.isEmpty()) {
                MyTrip.AddGpx.Builder routeGPX = MyTrip.AddGpx.newBuilder();
                routeGPX.setRouteId(routeItem.getId());
                byte[] routeGoogle = polyLine.getBytes(StandardCharsets.UTF_8);
                routeGPX.setData(ByteString.copyFrom(routeGoogle));
                Shared.OTCResponse response = ApiCaller.doCall(Endpoints.ROUTE_ADD_GPX, msp.getBytes("token"), routeGPX.build(), Shared.OTCResponse.class);
                boolean success = response != null && response.getStatus() == Shared.OTCStatus.SUCCESS;
                if (success || routeItem.getRouteType().getNumber() == General.RouteType.DONE_VALUE) {
                    savePois();
                } else {
                    runOnUiThread(() -> {
                        if (!Utils.isActivityFinish(this)) {
                            Toast.makeText(getApplicationContext(), "Error: " + CloudErrorHandler.handleError(response.getStatus()), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } else {
                savePois();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class EditRouteTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());
                MyTrip.RouteUpdate.Builder routeUpdateBuilder = MyTrip.RouteUpdate.newBuilder();
                routeUpdateBuilder.setRouteId(routeItem.getId());
                String title = editTitle.getText().toString();
                routeUpdateBuilder.setTitle(title);
                String description = editDescription.getText().toString();
                routeUpdateBuilder.setDescription(description);
                Shared.OTCResponse response = ApiCaller.doCall(Endpoints.ROUTE_UPDATE, msp.getBytes("token"), routeUpdateBuilder.build(), Shared.OTCResponse.class);
                return response.getStatus() == Shared.OTCStatus.SUCCESS;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                if (ConnectionUtils.isOnline(getApplicationContext())) {
                    // new AddGPXTask().execute();
                    savePois();
                } else {
                    ConnectionUtils.showOfflineToast();
                }
            } else {
                dismissProgressDialog();
                Toast.makeText(SaveRouteActivity.this, R.string.error_default + getResources().getString(R.string.edit_route), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AddGPXTask extends AsyncTask<Void, Void, Shared.OTCResponse> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected Shared.OTCResponse doInBackground(Void... params) {
            try {
                MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());
                MyTrip.AddGpx.Builder routeGPX = MyTrip.AddGpx.newBuilder();
                routeGPX.setRouteId(routeItem.getId());
                byte[] routeGoogle = polyLine.getBytes(StandardCharsets.UTF_8);
                routeGPX.setData(ByteString.copyFrom(routeGoogle));

                return ApiCaller.doCall(Endpoints.ROUTE_ADD_GPX, msp.getBytes("token"), routeGPX.build(), Shared.OTCResponse.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Shared.OTCResponse result) {
            super.onPostExecute(result);
            savePois();
        }
    }

    private void savePois() {
        new Thread(() -> {
            runOnUiThread(() -> progressDialog.setMessage(getString(R.string.save_pois)));
            List<PoiWrapper> poiList = routeItem.getPoiList();
            for (int i = 0; i < poiList.size(); i++) {
                PoiWrapper poi = poiList.get(i);
                savePoi(poi);
            }
            if (routeItem.getRouteType() == General.RouteType.PENDING) {
                RouteItem ro = PrefsManager.getInstance().getRouteInProgress(this);
                if (ro != null && routeItem.getId() == ro.getId()) {
                    PrefsManager.getInstance().deleteRouteInProgress(getApplicationContext());
                }
            }
            runOnUiThread(this::dismissProgressDialog);
            finish();
        }, "SavePoisThread").start();
    }

    private void savePoi(PoiWrapper wrap) {
        if (ConnectionUtils.isOnline(getApplicationContext())) {
            try {
                MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());

                if (wrap.getStatus() != PoiWrapper.PoiStatus.Deleted) {
                    General.POI poi = wrap.getPoi();
                    General.POI.Builder builder = poi.toBuilder();
                    builder.setRouId(routeItem.getId());

                    MyTrip.PoiId result = ApiCaller.doCall(Endpoints.ROUTE_ADD_POI, msp.getBytes("token"), builder.build(), MyTrip.PoiId.class);
                    if (result != null) {
                        if (wrap.getStatus() == PoiWrapper.PoiStatus.Added && poi.getImagesCount() > 0) {
                            byte[] imageBytes = Utils.downloadImageSync(this, poi.getImages(0));
                            ImageUtils.saveImageFileInCache(this, imageBytes, poi.getImages(0));
                            savePoiImage(wrap.getInternalID(), result.getPoiId());
                        } else {
                            for (PoiImageWrapper imgWrp : wrap.getImages()) {
                                if (imgWrp.getStatus() == PoiImageWrapper.ImageStatus.Deleted && imgWrp.getID() > 0) {
                                    MyTrip.PoiImage img = MyTrip.PoiImage.newBuilder()
                                            .setFilId(imgWrp.getID()).build();
                                    ApiCaller.doCall(Endpoints.ROUTE_POI_DELETE_IMAGE, true, img, Shared.OTCResponse.class);
                                } else if (imgWrp.getStatus() == PoiImageWrapper.ImageStatus.Added) {
                                    savePoiImage(wrap.getInternalID(), result.getPoiId());
                                }
                            }
                        }
                    }
                } else {
                    if (wrap.getPoi().getPoiId() > 0) {
                        General.POI poi = wrap.getPoi();
                        if (poi.getImagesCount() > 0) {
                            for (int i = 0; i < poi.getImagesCount(); ++i) {
                                MyTrip.PoiImage img = MyTrip.PoiImage.newBuilder()
                                        .setFilId(poi.getImages(i)).build();
                                ApiCaller.doCall(Endpoints.ROUTE_POI_DELETE_IMAGE, true, img, Shared.OTCResponse.class);
                            }
                        }
                        ApiCaller.doCall(Endpoints.ROUTE_DELETE_POI, true, poi, Shared.OTCResponse.class);
                    }
                }
            } catch (ApiCaller.OTCException e) {
                e.printStackTrace();
            }
        } else {
            ConnectionUtils.showOfflineToast();
        }
    }

    private void savePoiImage(long poiIndex, long poiId) throws ApiCaller.OTCException {
        String poiImage = ImageUtils.getPoiImageName(poiIndex, this);
        if (poiImage != null) {
            if (ConnectionUtils.isOnline(getApplicationContext())) {
                MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());

                MyTrip.PoiImage.Builder builder = MyTrip.PoiImage.newBuilder();
                builder.setPoiId(poiId);

                File file = new File(poiImage);
                builder.setName(file.getName());

                Bitmap bmp = BitmapFactory.decodeFile(poiImage);
                bmp = ImageUtils.CheckServerSize(bmp);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG, 90, baos);

                ByteString byteString = ByteString.copyFrom(baos.toByteArray());
                builder.setData(byteString);

                Shared.OTCResponse response = ApiCaller.doCall(Endpoints.ROUTE_POI_ADD_IMAGE, msp.getBytes("token"), builder.build(), Shared.OTCResponse.class);
                if (response.getStatus() != Shared.OTCStatus.SUCCESS) {
                    try {
                        Toast.makeText(SaveRouteActivity.this, R.string.error_default + " " + response.getMessage(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                ConnectionUtils.showOfflineToast();
            }
        }
    }

    @Override
    public void onBackPressed() {
        dismissProgressDialog();
        super.onBackPressed();
        ImageUtils.deleteImagesDirectory(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (getPoiInfoTask != null) {
            getPoiInfoTask.cancel(true);
        }
        if (editRouteTask != null) {
            editRouteTask.cancel(true);
        }
    }
}
