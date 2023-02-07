package com.otcengineering.white_app.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.google.protobuf.ByteString;
import com.otc.alice.api.model.Community;
import com.otc.alice.api.model.General;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.components.DialogYesNo;
import com.otcengineering.white_app.components.TitleBar;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.serialization.pojo.EcoItem;
import com.otcengineering.white_app.serialization.pojo.MileageItem;
import com.otcengineering.white_app.serialization.pojo.RouteItem;
import com.otcengineering.white_app.serialization.pojo.SafetyItem;
import com.otcengineering.white_app.utils.CloudErrorHandler;
import com.otcengineering.white_app.utils.DateUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.images.ImageRetriever;
import com.otcengineering.white_app.utils.images.ImageUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Created by cenci7
 */

public class SendPostActivity extends BaseActivity {

    private static final int REQUEST_CODE_GALLERY = 1;
    private static final int REQUEST_CODE_CAMERA = 2;
    private static final int REQUEST_CODE_ROUTE_SELECTION = 3;

    private TitleBar titleBar;
    private Button btnPostType;
    private TextView txtTitle, txtDate, txtUp, txtDown;
    private LinearLayout layoutCheck;
    private ToggleButton checkCommunity, checkFriends;
    private EditText editMessage, editVideo;
    private TextView txtChars;
    private LinearLayout layoutMileageEcoSafety, layoutImage, layoutVideo, layoutRoute, layoutRouteInfo;
    private ImageView imgPost, btnAddImage, btnDeleteImage, btnAddRoute, imgMap;
    private TextView txtTitleRoute, txtDuration, txtDistance, txtConsumption, txtConsumptionAvg,
            txtDrivingTechnique;
    private Button btnSendPost;

    private String imagePath;
    private byte[] imageBytes;

    private RouteItem routeItem;

    private MileageItem mileageItem;
    private EcoItem ecoItem;
    private SafetyItem safetyItem;

    private Community.PostType postType = Community.PostType.TEXT;

    private ScrollView scrollView;
    private boolean keyboardOpen = false;
    private boolean isRoute = false;

    public SendPostActivity() {
        super("SendPostActivity");
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_post);
        retrieveViews();
        retrieveExtras();
        configureUI();
        setEvents();
        configureTitle();
    }

    private void retrieveViews() {
        scrollView = findViewById(R.id.scrollView);

        titleBar = findViewById(R.id.send_post_titleBar);
        btnPostType = findViewById(R.id.send_post_btnPostType);
        txtTitle = findViewById(R.id.send_post_txtTitle);
        txtDate = findViewById(R.id.send_post_txtDate);
        txtUp = findViewById(R.id.send_post_txtUp);
        txtDown = findViewById(R.id.send_post_txtDown);
        layoutCheck = findViewById(R.id.send_post_layoutCheck);
        checkCommunity = findViewById(R.id.send_post_checkCommunity);
        checkFriends = findViewById(R.id.send_post_checkFriends);
        editMessage = findViewById(R.id.send_post_editMessage);
        editVideo = findViewById(R.id.send_post_editVideo);
        txtChars = findViewById(R.id.send_post_txtChars);
        btnSendPost = findViewById(R.id.send_post_btnSave);
        layoutMileageEcoSafety = findViewById(R.id.send_post_layoutMileageEcoSafety);
        layoutImage = findViewById(R.id.send_post_layoutImage);
        layoutVideo = findViewById(R.id.send_post_layoutVideo);
        layoutRoute = findViewById(R.id.send_post_layoutRoute);
        layoutRouteInfo = findViewById(R.id.send_post_layoutRouteInfo);
        imgPost = findViewById(R.id.send_post_imgPost);
        btnAddImage = findViewById(R.id.send_post_btnAddImage);
        btnDeleteImage = findViewById(R.id.send_post_btnDeleteImage);
        btnAddRoute = findViewById(R.id.send_post_btnAddRoute);
        txtTitleRoute = findViewById(R.id.send_post_txtTitleRoute);
        txtDuration = findViewById(R.id.send_post_txtDuration);
        txtDistance = findViewById(R.id.send_post_txtDistance);
        txtConsumption = findViewById(R.id.send_post_txtConsumption);
        txtConsumptionAvg = findViewById(R.id.send_post_txtConsumptionAvg);
        txtDrivingTechnique = findViewById(R.id.send_post_txtDrivingTechnique);
        imgMap = findViewById(R.id.send_post_imgMap);
    }

    private void retrieveExtras() {
        try {
            if (getIntent() != null && getIntent().getExtras() != null) {
                if (getIntent().getExtras().containsKey(Constants.Extras.ROUTE)) {
                    routeItem = new RouteItem(getIntent().getExtras().getString(Constants.Extras.ROUTE));
                    postType = Community.PostType.ROUTE;
                    isRoute = true;
                }

                mileageItem = (MileageItem) getIntent().getExtras().getSerializable(Constants.Extras.MILEAGE_ITEM);
                if (mileageItem != null) {
                    postType = Community.PostType.RANKING_MILEAGE;
                }

                ecoItem = (EcoItem) getIntent().getExtras().getSerializable(Constants.Extras.ECO_ITEM);
                if (ecoItem != null) {
                    postType = Community.PostType.RANKING_ECO;
                }

                safetyItem = (SafetyItem) getIntent().getExtras().getSerializable(Constants.Extras.SAFETY_ITEM);
                if (safetyItem != null) {
                    postType = Community.PostType.RANKING_SAFETY;
                }
            }
        } catch (RuntimeException re) {
            //Log.e("SendPostActivity", "RuntimeException", re);
        }
    }

    private void configureUI() {
        if (postType == Community.PostType.TEXT) {
            txtTitle.setVisibility(View.GONE);
            layoutMileageEcoSafety.setVisibility(View.GONE);
            layoutImage.setVisibility(View.GONE);
            layoutVideo.setVisibility(View.GONE);
            layoutRoute.setVisibility(View.GONE);
            btnSendPost.setText(R.string.send_post);
        } else if (postType == Community.PostType.IMAGE) {
            txtTitle.setVisibility(View.GONE);
            layoutMileageEcoSafety.setVisibility(View.GONE);
            layoutImage.setVisibility(View.VISIBLE);
            layoutVideo.setVisibility(View.GONE);
            layoutRoute.setVisibility(View.GONE);
            btnSendPost.setText(R.string.send_post);
        } else if (postType == Community.PostType.VIDEO) {
            txtTitle.setVisibility(View.GONE);
            layoutMileageEcoSafety.setVisibility(View.GONE);
            layoutImage.setVisibility(View.GONE);
            layoutVideo.setVisibility(View.VISIBLE);
            layoutRoute.setVisibility(View.GONE);
            btnSendPost.setText(R.string.send_post);
        } else if (postType == Community.PostType.ROUTE) {
            txtTitle.setVisibility(View.GONE);
            layoutMileageEcoSafety.setVisibility(View.GONE);
            layoutImage.setVisibility(View.GONE);
            layoutVideo.setVisibility(View.GONE);
            layoutRoute.setVisibility(View.VISIBLE);
            btnSendPost.setText(R.string.send_post);
        } else if (postType == Community.PostType.RANKING_MILEAGE
                || postType == Community.PostType.RANKING_ECO
                || postType == Community.PostType.RANKING_SAFETY) {
            txtTitle.setVisibility(View.VISIBLE);
            btnPostType.setVisibility(View.GONE);
            layoutMileageEcoSafety.setVisibility(View.VISIBLE);
            layoutImage.setVisibility(View.GONE);
            layoutVideo.setVisibility(View.GONE);
            layoutRoute.setVisibility(View.GONE);
            btnSendPost.setText(R.string.send_post);
            showMileageEcoSafetyInfo();
        }

        if (routeItem != null) {
            btnPostType.setVisibility(View.GONE);
            showRouteInfo();
        }
    }

    private void showMileageEcoSafetyInfo() {
        manageButtonSaveUI(0);

        if (mileageItem != null) {
            txtDate.setText(String.format("%s.\n%s.",
                    DateUtils.getDayAndDayOfWeekFormatted(mileageItem.getDate()),
                    DateUtils.getMonthFormatted(mileageItem.getDate())));
            txtUp.setText(String.format(Locale.US, "%.1f km" + " - %s",
                    mileageItem.getDistanceInKms(),
                    mileageItem.getDuration()));
            txtDown.setVisibility(View.VISIBLE);
            txtDown.setText(String.format(Locale.US, "Global: Top %d /Local: Top %d",
                    mileageItem.getGlobalRanking(),
                    mileageItem.getLocalRanking()));
        } else if (ecoItem != null) {
            txtDate.setText(String.format("%s.\n%s.",
                    DateUtils.getDayAndDayOfWeekFormatted(ecoItem.getDate()),
                    DateUtils.getMonthFormatted(ecoItem.getDate())));
            txtUp.setText(String.format(Locale.US, "%.1f l" + " - %.1f km/l - %s",
                    ecoItem.getTotalConsumption() / 100,
                    ecoItem.getAverageConsumption() * 100,
                    ecoItem.getDuration()));
            txtDown.setVisibility(View.VISIBLE);
            txtDown.setText(String.format(Locale.US, "Global: Top %d /Local: Top %d",
                    ecoItem.getGlobalRanking(),
                    ecoItem.getLocalRanking()));
        } else if (safetyItem != null) {
            txtDate.setText(String.format("%s.\n%s.",
                    DateUtils.getDayAndDayOfWeekFormatted(safetyItem.getDate()),
                    DateUtils.getMonthFormatted(safetyItem.getDate())));
            txtUp.setText(String.format(Locale.US, getResources().getString(R.string.score) + " %.1f - %s",
                    safetyItem.getDrivingTechinique(),
                    safetyItem.getDuration()));
            txtDown.setVisibility(View.GONE);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setEvents() {
        editMessage.setOnClickListener(v -> {
            if (isRoute){
                hideKeyboardRoute();
            }

            keyboardOpen = !keyboardOpen;


            new android.os.Handler().postDelayed(
                    () -> scrollView.scrollTo(0, editMessage.getTop()),
                    300);

        });


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

        btnPostType.setOnClickListener(view -> {
            registerForContextMenu(view);
            openContextMenu(view);
        });

        checkCommunity.setOnClickListener(view -> {
            if (checkFriends.isChecked()) {
                checkFriends.setChecked(false);
            }
            checkCommunity.setChecked(true);
        });

        checkFriends.setOnClickListener(view -> {
            if (checkCommunity.isChecked()) {
                checkCommunity.setChecked(false);
            }
            checkFriends.setChecked(true);
        });

        editMessage.addTextChangedListener(new TextWatcher() {
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
                    txtChars.setTextColor(getResources().getColor(R.color.textButton));
                } else {
                    txtChars.setText(String.valueOf(charsAvailable));
                    txtChars.setTextColor(getResources().getColor(R.color.black_30_alpha));
                }
                manageButtonSaveUI(charsAvailable);
            }
        });

        editVideo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                manageButtonSaveUI(0);
            }
        });

        btnAddImage.setOnClickListener(view -> showAddImageDialog());

        btnDeleteImage.setOnClickListener(view -> deleteImage());

        btnAddRoute.setOnClickListener(view -> openRouteSelection());

        btnSendPost.setOnClickListener(view -> sendPost());

        layoutRoute.setOnClickListener(v -> openRouteDetails());
    }

    private void configureTitle() {
        if (routeItem != null) {
            General.RouteType routeType = routeItem.getRouteType();
            if (routeType == General.RouteType.AUTOSAVED) {
                titleBar.setTitle(R.string.share_autosaved_route);
            } else if (routeType == General.RouteType.PLANNED) {
                titleBar.setTitle(R.string.share_planned_route);
            } else if (routeType == General.RouteType.DONE) {
                titleBar.setTitle(R.string.share_done_route);
            }
        } else if (mileageItem != null) {
            titleBar.setTitle(getString(R.string.share) + " " + getString(R.string.mileage));
            txtTitle.setText(String.format("%s %s", getString(R.string.mileage), getString(R.string.post)));
        } else if (ecoItem != null) {
            titleBar.setTitle(getString(R.string.share) + " " + getString(R.string.eco_driving));
            txtTitle.setText(String.format("%s %s", getString(R.string.eco_driving), getString(R.string.post)));
        } else if (safetyItem != null) {
            titleBar.setTitle(getString(R.string.share) + " " + getString(R.string.safety_driving));
            txtTitle.setText(String.format("%s %s", getString(R.string.safety_driving), getString(R.string.post)));
        }
    }

    private void openRouteSelection() {
        Intent intent = new Intent(this, RouteSelectionActivity.class);
        startActivityForResult(intent, REQUEST_CODE_ROUTE_SELECTION);
    }

    private void deleteImage() {
        ImageUtils.deleteImageFile(this, imagePath);
        imagePath = null;
        btnAddImage.setVisibility(View.VISIBLE);
        btnDeleteImage.setVisibility(View.GONE);
        imgPost.setVisibility(View.GONE);
    }

    private void sendPost() {
        DialogYesNo dyn = new DialogYesNo(this);
        dyn.setYesButtonClickListener(() -> {
            SendPostTask spt = new SendPostTask();
            spt.execute();
        });
        dyn.setMessage(getString(R.string.you_send_post));

        dyn.setBackgroundColor(R.color.colorPrimaryTrans);
        dyn.show();
    }

    private void manageButtonSaveUI(int charsAvailable) {
        boolean enableButton = isPostDataSuccess(charsAvailable);
        btnSendPost.setTextColor(enableButton ? getResources().getColor(R.color.textButton) : getResources().getColor(R.color.layout_border));
        btnSendPost.setEnabled(enableButton);
    }

    private boolean isPostDataSuccess(int charsAvailable) {
        if (postType == Community.PostType.TEXT) {
            return charsAvailable > 0 && !editMessage.getText().toString().isEmpty();
        }

        if (postType == Community.PostType.IMAGE) {
            return imagePath != null;
        }

        if (postType == Community.PostType.VIDEO) {
            return !editVideo.getText().toString().isEmpty();
        }

        if (postType == Community.PostType.ROUTE) {
            return routeItem != null;
        }

        return postType == Community.PostType.RANKING_MILEAGE
                || postType == Community.PostType.RANKING_ECO
                || postType == Community.PostType.RANKING_SAFETY
                || postType == Community.PostType.LOCATION;

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
        imagePath = ImageUtils.getPostImageName(this);
    }

    private void showRouteInfo() {
        manageButtonSaveUI(0);
        btnAddRoute.setVisibility(View.GONE);
        layoutRouteInfo.setVisibility(View.VISIBLE);

        if (routeItem.getTitle() == null || routeItem.getTitle().isEmpty()) {
            txtTitleRoute.setText(String.format("%sh", DateUtils.dateTimeToString(routeItem.getDateStart(), "dd/MM/yyyy HH:mm:ss")));
        } else {
            txtTitleRoute.setText(routeItem.getTitle());
        }

        Bitmap bmp;

        if (ImageUtils.existsImageFileInCache(getApplicationContext(), routeItem.getId())) {
            bmp = ImageUtils.getImageRoute(getApplicationContext(), routeItem.getId());
        } else {
            bmp = BitmapFactory.decodeResource(getResources(), R.drawable.map_landscape);
        }

        imgMap.setImageBitmap(bmp);

        txtDuration.setText(routeItem.getDurationInMinsFormatted());
        txtDistance.setText(String.format(Locale.US, "%.1f km", routeItem.getDistanceInKms()));
        txtConsumption.setText(String.format(Locale.US, "%.0f l", routeItem.getConsumption()));
        if (routeItem.getConsumptionAvg() > 100) {
            txtConsumptionAvg.setText(String.format(Locale.US, "+%.1f km/l", 100.0));
        } else {
            txtConsumptionAvg.setText(String.format(Locale.US, "%.1f km/l", routeItem.getConsumptionAvg()));
        }
        txtDrivingTechnique.setText(String.format(Locale.US, "%.1f", routeItem.getDrivingTechnique() / 10));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK) {
            doActionsAfterGallery(data);
        } else if (requestCode == REQUEST_CODE_CAMERA && resultCode == RESULT_OK) {
            doActionsAfterCamera();
        } else if (requestCode == REQUEST_CODE_ROUTE_SELECTION && resultCode == RESULT_OK) {
            if (data.getExtras() != null) {
                routeItem = new RouteItem(data.getExtras().getString(Constants.Extras.ROUTE));
                showRouteInfo();
            }
        }
    }

    private void doActionsAfterCamera() {
        showImage(imagePath);
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
                    showImage(imagePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, R.string.error_default, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showImage(String imagePath) {
        manageButtonSaveUI(0);
        btnAddImage.setVisibility(View.GONE);
        btnDeleteImage.setVisibility(View.VISIBLE);
        imgPost.setVisibility(View.VISIBLE);
        Bitmap bmp = ImageUtils.getImageCorrectedFromRotationOfEXIF(imagePath);
        updateImageBytes(bmp);
        Glide.with(this)
                .load(bmp)
                .into(imgPost);
    }

    private void updateImageBytes(Bitmap bmp) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        imageBytes = byteArrayOutputStream.toByteArray();
    }

    private void showImagePlaceholder() {
        Glide.with(this).load(R.drawable.photo_placeholder_landscape).into(imgPost);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        int viewId = v.getId();
        if (viewId == R.id.send_post_btnPostType) {
            menu.add(0, viewId, 0, R.string.post_type_text);
            menu.add(0, viewId, 0, R.string.post_type_image);
            menu.add(0, viewId, 0, R.string.post_type_video);
            menu.add(0, viewId, 0, R.string.post_type_route);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.send_post_btnPostType) {
            String postTypeName = item.getTitle().toString();
            btnPostType.setText(postTypeName);
            if (postTypeName.equalsIgnoreCase(getString(R.string.post_type_text))) {
                postType = Community.PostType.TEXT;
            } else if (postTypeName.equalsIgnoreCase(getString(R.string.post_type_image))) {
                postType = Community.PostType.IMAGE;
            } else if (postTypeName.equalsIgnoreCase(getString(R.string.post_type_video))) {
                postType = Community.PostType.VIDEO;
            } else if (postTypeName.equalsIgnoreCase(getString(R.string.post_type_route))) {
                postType = Community.PostType.ROUTE;
            }
            changePostType();
        }
        return true;
    }

    private void changePostType() {
        //reset fields
        deleteImage();
        routeItem = null;
        mileageItem = null;
        ecoItem = null;
        safetyItem = null;
        editVideo.setText("");

        configureUI();
    }

    private class SendPostTask extends AsyncTask<Void, Void, Shared.OTCStatus> {
        private final ProgressDialog progressDialog = new ProgressDialog(SendPostActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage(getString(R.string.loading));
            // progressDialog.show();
        }

        @Override
        protected Shared.OTCStatus doInBackground(Void... voids) {
            try {
                MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());

                Community.SendPost.Builder builder = Community.SendPost.newBuilder();
                builder.setType(postType);

                builder.setMessage(editMessage.getText().toString());

                if (imagePath != null) {
                    builder.setImage(ByteString.copyFrom(imageBytes));
                    builder.setImageName(ImageUtils.getPostImageName(SendPostActivity.this));
                }

                if (postType == Community.PostType.VIDEO) {
                    Pattern regex = Pattern.compile("^((http(s)?://)?((w){3}\\.|m\\.)?(youtube\\.com)?/(watch\\?v=).+|(http(s)?://)?(youtu\\.be/).+)");
                    if (!regex.matcher(editVideo.getText().toString()).find()) {
                        return Shared.OTCStatus.MALFORMED_YOUTUBE_URL;
                    }
                    builder.setVideoUrl(editVideo.getText().toString());
                } else if (postType == Community.PostType.ROUTE) {
                    builder.setRouteId(routeItem.getId());
                } else if (postType == Community.PostType.RANKING_MILEAGE) {
                    builder.setGlobalPosition(mileageItem.getGlobalRanking());
                    builder.setLocalPosition(mileageItem.getLocalRanking());
                    builder.setDuration(mileageItem.getDuration());
                    builder.setDistance(mileageItem.getDistance());
                    builder.setDateRankingStart(DateUtils.dateToString(mileageItem.getDate(), DateUtils.FMT_SRV_DATE));
                    builder.setDateRankingEnd(DateUtils.dateToString(mileageItem.getDate(), DateUtils.FMT_SRV_DATE));
                } else if (postType == Community.PostType.RANKING_ECO) {
                    builder.setGlobalPosition(ecoItem.getGlobalRanking());
                    builder.setLocalPosition(ecoItem.getLocalRanking());
                    builder.setDuration(ecoItem.getDuration());
                    builder.setTotalConsumption(ecoItem.getTotalConsumption());
                    builder.setAverageConsumption(ecoItem.getAverageConsumption());
                    builder.setDateRankingStart(DateUtils.dateToString(ecoItem.getDate(), DateUtils.FMT_SRV_DATE));
                    builder.setDateRankingEnd(DateUtils.dateToString(ecoItem.getDate(), DateUtils.FMT_SRV_DATE));
                } else if (postType == Community.PostType.RANKING_SAFETY) {
                    builder.setDuration(safetyItem.getDuration());
                    builder.setDrivingTechnique(safetyItem.getDrivingTechinique());
                    builder.setDateRankingStart(DateUtils.dateToString(safetyItem.getDate(), DateUtils.FMT_SRV_DATE));
                    builder.setDateRankingEnd(DateUtils.dateToString(safetyItem.getDate(), DateUtils.FMT_SRV_DATE));
                }
                builder.setVisibility(checkFriends.isChecked() ? Community.PostVisibility.FRIENDS : Community.PostVisibility.COMMUNITY);

                Shared.OTCResponse resp = ApiCaller.doCallWithProgress(Endpoints.SEND_POST, msp.getString("token"), builder.build(), Shared.OTCResponse.class, SendPostActivity.this);

                return resp.getStatus();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Shared.OTCStatus.SERVER_ERROR;
        }

        @Override
        protected void onPostExecute(Shared.OTCStatus result) {
            super.onPostExecute(result);
            /*if (progressDialog != null && !Utils.isActivityFinish(SendPostActivity.this)) {
                progressDialog.dismiss();
            }*/
            if (result == null) {
                showCustomDialogError(getString(R.string.error_try_again));
                return;
            }
            if (result == Shared.OTCStatus.SUCCESS) {
                showCustomDialog(R.string.properly_posted, dialogInterface -> {
                    ImageUtils.deleteImageFile(SendPostActivity.this, imagePath);
                    finish();
                });
            } else {
                if (result == Shared.OTCStatus.MALFORMED_YOUTUBE_URL) {
                    showCustomDialogError(getString(R.string.yt_bad_url));
                } else if (result == Shared.OTCStatus.MESSAGE_EXCEEDS_MAX_LENGTH) {
                    showCustomDialogError(getString(R.string.long_message));
                } else {
                    showCustomDialogError(getString(R.string.no_share_post) + CloudErrorHandler.handleError(result.getNumber()));
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ImageUtils.deleteImageFile(this, imagePath);
    }

    private void openRouteDetails() {
        if (routeItem != null) {
            Intent intent = new Intent(this, RouteDetailsActivity.class);
            intent.putExtra(Constants.Extras.ROUTE, routeItem.toString());
            startActivity(intent);
        }
    }

    private void hideKeyboardRoute() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (keyboardOpen) {
            if (imm != null) {
                imm.hideSoftInputFromWindow(editMessage.getApplicationWindowToken(), 0);
            }
            //writeToLog("Software Keyboard was shown");
        } else {
            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            //scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            //writeToLog("Software Keyboard was not shown");
        }
    }
}
