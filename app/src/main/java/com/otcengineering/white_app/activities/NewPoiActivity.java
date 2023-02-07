package com.otcengineering.white_app.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.otc.alice.api.model.General;
import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.adapter.PoiImageAdapter;
import com.otcengineering.white_app.components.TitleBar;
import com.otcengineering.white_app.serialization.pojo.PoiImageWrapper;
import com.otcengineering.white_app.serialization.pojo.PoiWrapper;
import com.otcengineering.white_app.serialization.pojo.RouteItem;
import com.otcengineering.white_app.utils.Utils;
import com.otcengineering.white_app.utils.images.ImageRetriever;
import com.otcengineering.white_app.utils.images.ImageUtils;

import java.io.IOException;
import java.util.List;

public class NewPoiActivity extends MapBaseActivity {
    private static final int REQUEST_CODE_POI_TYPE = 1;
    private static final int REQUEST_CODE_GALLERY = 2;
    private static final int REQUEST_CODE_CAMERA = 3;

    private TitleBar titleBar;
    private FrameLayout btnLocation;
    private EditText editPoiTitle;
    private EditText editPoiType;
    private RecyclerView recyclerImages;
    private ImageView btnAddImage;
    private Button btnSave;

    private PoiWrapper wrapper;

    private PoiImageAdapter adapter;

    private RouteItem routeItem;

    private LatLng poiLocation;

    private General.PoiType poiType;

    private PoiImageWrapper imageWrapper;

    public NewPoiActivity() {
        super("NewPoiActivity");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_poi);
        retrieveExtras();
        retrieveViews();
        setEvents();
        configureAdapter();
        startGoogleMap(savedInstanceState);
        wrapper = new PoiWrapper();
    }

    private void retrieveExtras() {
        if (getIntent() != null && getIntent().getExtras() != null) {
            routeItem = new RouteItem(getIntent().getExtras().getString(Constants.Extras.ROUTE));
            poiLocation = getIntent().getExtras().getParcelable(Constants.Extras.POI_LOCATION);
        }
    }

    private void retrieveViews() {
        titleBar = findViewById(R.id.new_poi_titleBar);
        btnLocation = findViewById(R.id.new_poi_btnLocation);
        recyclerImages = findViewById(R.id.new_poi_recyclerImages);
        mapView = findViewById(R.id.new_poi_map);
        editPoiTitle = findViewById(R.id.new_poi_editPoiTitle);
        editPoiType = findViewById(R.id.new_poi_editPoiType);
        btnAddImage = findViewById(R.id.new_poi_btnAddImage);
        btnSave = findViewById(R.id.new_poi_btnSave);
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

        editPoiTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                manageSaveUI();
            }
        });

        editPoiTitle.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (editPoiTitle.getRight() - editPoiTitle.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    editPoiTitle.setText("");
                    return true;
                }
            }
            return false;
        });

        editPoiType.setOnFocusChangeListener((view, focus) -> {
            if (focus) {
                openPoiType();
            }
        });

        editPoiType.setOnClickListener(view -> openPoiType());

        editPoiType.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                manageSaveUI();
            }
        });

        btnLocation.setOnClickListener(view -> centerMapInMyLocationWithAnimation());

        btnAddImage.setOnClickListener(view -> showAddImageDialog());

        btnSave.setOnClickListener(view -> savePoi());
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
        ImageRetriever.functionCamera(imageWrapper.getImage(), REQUEST_CODE_CAMERA, this);
    }

    public void choosePhotoFromGallery() {
        initializeImagePath();
        ImageRetriever.functionGallery(REQUEST_CODE_GALLERY, this);
    }

    private void initializeImagePath() {
        imageWrapper = new PoiImageWrapper();
        imageWrapper.setStatus(PoiImageWrapper.ImageStatus.Added);
        String tmp = ImageUtils.getPoiImageName(wrapper.getInternalID(), this);
        imageWrapper.setImage(tmp);
    }

    private int getPoiPosition() {
        return routeItem.getPoiList().size() + 1;
    }

    private int getPictureIndex() {
        return adapter.getItemCount();
    }

    private void savePoi() {
        if(poiType == null) return;
        General.POI.Builder builder = General.POI.newBuilder();
        String title = editPoiTitle.getText().toString();
        builder.setTitle(title);
        builder.setType(poiType);
        if (poiLocation != null) {
            builder.setLatitude(poiLocation.latitude);
            builder.setLongitude(poiLocation.longitude);
        }

        wrapper.setPoi(builder.build());

        Intent data = new Intent();
        data.putExtra(Constants.Extras.POI, wrapper);
        if (imageWrapper != null) {
            Gson gson = Utils.getGson();
            data.putExtra("PoiImg", gson.toJson(imageWrapper));
        }
        setResult(RESULT_OK, data);
        finish();
    }

    private void openPoiType() {
        Intent intent = new Intent(this, PoiTypeActivity.class);
        startActivityForResult(intent, REQUEST_CODE_POI_TYPE);
    }

    private void manageSaveUI() {
        if (!editPoiTitle.getText().toString().isEmpty()
                && !editPoiType.getText().toString().isEmpty()) {
            btnSave.setTextColor(getResources().getColor(R.color.textButton));
            btnSave.setEnabled(true);
        }
    }

    private void configureAdapter() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerImages.setLayoutManager(layoutManager);
        adapter = new PoiImageAdapter(this, () -> btnAddImage.setVisibility(View.VISIBLE));
        recyclerImages.setAdapter(adapter);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
        List<LatLng> latLngList = routeItem.getLatLngList();
        drawRouteInfo(latLngList, routeItem.getDistanceInKms());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //if (data == null) return;

        if (resultCode != RESULT_OK) return;

        if (requestCode == REQUEST_CODE_POI_TYPE) {
            if (data.getExtras() != null) {
                poiType = (General.PoiType) data.getExtras().get(Constants.Extras.POI_TYPE);
                editPoiType.setText(Utils.translatePoiType(this, poiType));
            }
        } else if (requestCode == REQUEST_CODE_GALLERY) {
            doActionsAfterGallery(data);
        } else if (requestCode == REQUEST_CODE_CAMERA) {
            doActionsAfterCamera();
        }
    }

    private void doActionsAfterCamera() {
        addPoiImageToRecycler(imageWrapper.getImage());
    }

    private void doActionsAfterGallery(Intent data) {
        if (data != null) {
            Uri imageUri = data.getData();
            String imageLocalUrl = ImageUtils.getRealPathFromURI(imageUri, this);
            if (imageLocalUrl != null && !imageLocalUrl.equals("")) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    ImageUtils.saveBitmapIntoFile(imageWrapper.getImage(), bitmap, Bitmap.CompressFormat.JPEG);
                    addPoiImageToRecycler(imageWrapper.getImage());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, R.string.error_default, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addPoiImageToRecycler(String image) {
        adapter.addItem(image);
        adapter.notifyDataSetChanged();
        btnAddImage.setVisibility(View.GONE); // to allow only 1 picture in every POI
    }
}
