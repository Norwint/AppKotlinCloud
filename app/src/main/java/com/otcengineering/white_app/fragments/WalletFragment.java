package com.otcengineering.white_app.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.ByteString;
import com.otc.alice.api.model.DashboardAndStatus;
import com.otc.alice.api.model.Shared;
import com.otc.alice.api.model.Wallet;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.activities.PictureActivity;
import com.otcengineering.white_app.components.CustomDialog;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.utils.CloudErrorHandler;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.Utils;
import com.otcengineering.white_app.utils.images.ImageRetriever;
import com.otcengineering.white_app.utils.images.ImageUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class WalletFragment extends EventFragment {

    public static final int REQUEST_CODE_GALLERY = 1;
    public static final int REQUEST_CODE_CAMERA = 2;

    private ScrollView scrollView;
    private FrameLayout btnScrollUp;

    private Button btnEdit;
    private LinearLayout layoutEdit;
    private TextView btnCancel, btnSelectAll, btnDelete;

    private ImageView imgDriving1, imgDriving2, imgDriving3;
    private ImageView imgInsurance1, imgInsurance2, imgInsurance3;
    private ImageView imgRegistration1, imgRegistration2, imgRegistration3;
    private ImageView imgTax1, imgTax2, imgTax3;
    private ImageView imgMaintenance1, imgMaintenance2, imgMaintenance3;
    private ImageView imgCar1, imgCar2, imgCar3;

    private ImageView imgDriving1add, imgDriving2add, imgDriving3add;
    private ImageView imgInsurance1add, imgInsurance2add, imgInsurance3add;
    private ImageView imgRegistration1add, imgRegistration2add, imgRegistration3add;
    private ImageView imgTax1add, imgTax2add, imgTax3add;
    private ImageView imgMaintenance1add, imgMaintenance2add, imgMaintenance3add;
    private ImageView imgCar1add, imgCar2add, imgCar3add;

    private ProgressBar imgDriving1loading, imgDriving2loading, imgDriving3loading;
    private ProgressBar imgInsurance1loading, imgInsurance2loading, imgInsurance3loading;
    private ProgressBar imgRegistration1loading, imgRegistration2loading, imgRegistration3loading;
    private ProgressBar imgTax1loading, imgTax2loading, imgTax3loading;
    private ProgressBar imgMaintenance1loading, imgMaintenance2loading, imgMaintenance3loading;
    private ProgressBar imgCar1loading, imgCar2loading, imgCar3loading;

    private FrameLayout imgDriving1unselected, imgDriving2unselected, imgDriving3unselected;
    private FrameLayout imgInsurance1unselected, imgInsurance2unselected, imgInsurance3unselected;
    private FrameLayout imgRegistration1unselected, imgRegistration2unselected, imgRegistration3unselected;
    private FrameLayout imgTax1unselected, imgTax2unselected, imgTax3unselected;
    private FrameLayout imgMaintenance1unselected, imgMaintenance2unselected, imgMaintenance3unselected;
    private FrameLayout imgCar1unselected, imgCar2unselected, imgCar3unselected;

    private FrameLayout imgDriving1selected, imgDriving2selected, imgDriving3selected;
    private FrameLayout imgInsurance1selected, imgInsurance2selected, imgInsurance3selected;
    private FrameLayout imgRegistration1selected, imgRegistration2selected, imgRegistration3selected;
    private FrameLayout imgTax1selected, imgTax2selected, imgTax3selected;
    private FrameLayout imgMaintenance1selected, imgMaintenance2selected, imgMaintenance3selected;
    private FrameLayout imgCar1selected, imgCar2selected, imgCar3selected;

    private ImageView imgViewSelected;

    private String imagePath;

    private boolean isEditMode;

    private List<Wallet.Doc> docs;

    public WalletFragment() {
        super("DocumentsActivity");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_wallet, container, false);
        retrieveViews(v);
        setEvents();
        getCache();
        getUserWallet();
        return v;
    }

    private void getCache() {
        Gson gson = new Gson();
        String json = MySharedPreferences.createGeneral(getContext()).getString("StorageCache");
        if (!json.isEmpty()) {
            Type type = new TypeToken<List<Wallet.Doc>>() {}.getType();
            List<Wallet.Doc> docs = gson.fromJson(json, type);
            this.docs = docs;
            showImages();
        }
    }

    private void retrieveViews(View v) {
        scrollView = v.findViewById(R.id.wallet_scrollView);
        btnScrollUp = v.findViewById(R.id.wallet_btnScrollUp);

        btnEdit = v.findViewById(R.id.wallet_btnEdit);
        layoutEdit = v.findViewById(R.id.wallet_layoutEdit);
        btnCancel = v.findViewById(R.id.wallet_btnCancel);
        btnSelectAll = v.findViewById(R.id.wallet_btnSelectAll);
        btnDelete = v.findViewById(R.id.wallet_btnDelete);

        imgDriving1 = v.findViewById(R.id.wallet_driving_license_img1);
        imgDriving2 = v.findViewById(R.id.wallet_driving_license_img2);
        imgDriving3 = v.findViewById(R.id.wallet_driving_license_img3);
        imgInsurance1 = v.findViewById(R.id.wallet_insurance_img1);
        imgInsurance2 = v.findViewById(R.id.wallet_insurance_img2);
        imgInsurance3 = v.findViewById(R.id.wallet_insurance_img3);
        imgRegistration1 = v.findViewById(R.id.wallet_registration_img1);
        imgRegistration2 = v.findViewById(R.id.wallet_registration_img2);
        imgRegistration3 = v.findViewById(R.id.wallet_registration_img3);
        imgTax1 = v.findViewById(R.id.wallet_tax_img1);
        imgTax2 = v.findViewById(R.id.wallet_tax_img2);
        imgTax3 = v.findViewById(R.id.wallet_tax_img3);
        imgMaintenance1 = v.findViewById(R.id.wallet_maintenance_img1);
        imgMaintenance2 = v.findViewById(R.id.wallet_maintenance_img2);
        imgMaintenance3 = v.findViewById(R.id.wallet_maintenance_img3);
        imgCar1 = v.findViewById(R.id.wallet_car_img1);
        imgCar2 = v.findViewById(R.id.wallet_car_img2);
        imgCar3 = v.findViewById(R.id.wallet_car_img3);

        imgDriving1add = v.findViewById(R.id.wallet_driving_license_img1add);
        imgDriving2add = v.findViewById(R.id.wallet_driving_license_img2add);
        imgDriving3add = v.findViewById(R.id.wallet_driving_license_img3add);
        imgInsurance1add = v.findViewById(R.id.wallet_insurance_img1add);
        imgInsurance2add = v.findViewById(R.id.wallet_insurance_img2add);
        imgInsurance3add = v.findViewById(R.id.wallet_insurance_img3add);
        imgRegistration1add = v.findViewById(R.id.wallet_registration_img1add);
        imgRegistration2add = v.findViewById(R.id.wallet_registration_img2add);
        imgRegistration3add = v.findViewById(R.id.wallet_registration_img3add);
        imgTax1add = v.findViewById(R.id.wallet_tax_img1add);
        imgTax2add = v.findViewById(R.id.wallet_tax_img2add);
        imgTax3add = v.findViewById(R.id.wallet_tax_img3add);
        imgMaintenance1add = v.findViewById(R.id.wallet_maintenance_img1add);
        imgMaintenance2add = v.findViewById(R.id.wallet_maintenance_img2add);
        imgMaintenance3add = v.findViewById(R.id.wallet_maintenance_img3add);
        imgCar1add = v.findViewById(R.id.wallet_car_img1add);
        imgCar2add = v.findViewById(R.id.wallet_car_img2add);
        imgCar3add = v.findViewById(R.id.wallet_car_img3add);

        imgDriving1loading = v.findViewById(R.id.wallet_driving_license_img1loading);
        imgDriving2loading = v.findViewById(R.id.wallet_driving_license_img2loading);
        imgDriving3loading = v.findViewById(R.id.wallet_driving_license_img3loading);
        imgInsurance1loading = v.findViewById(R.id.wallet_insurance_img1loading);
        imgInsurance2loading = v.findViewById(R.id.wallet_insurance_img2loading);
        imgInsurance3loading = v.findViewById(R.id.wallet_insurance_img3loading);
        imgRegistration1loading = v.findViewById(R.id.wallet_registration_img1loading);
        imgRegistration2loading = v.findViewById(R.id.wallet_registration_img2loading);
        imgRegistration3loading = v.findViewById(R.id.wallet_registration_img3loading);
        imgTax1loading = v.findViewById(R.id.wallet_tax_img1loading);
        imgTax2loading = v.findViewById(R.id.wallet_tax_img2loading);
        imgTax3loading = v.findViewById(R.id.wallet_tax_img3loading);
        imgMaintenance1loading = v.findViewById(R.id.wallet_maintenance_img1loading);
        imgMaintenance2loading = v.findViewById(R.id.wallet_maintenance_img2loading);
        imgMaintenance3loading = v.findViewById(R.id.wallet_maintenance_img3loading);
        imgCar1loading = v.findViewById(R.id.wallet_car_img1loading);
        imgCar2loading = v.findViewById(R.id.wallet_car_img2loading);
        imgCar3loading = v.findViewById(R.id.wallet_car_img3loading);

        imgDriving1unselected = v.findViewById(R.id.wallet_driving_license_img1unselected);
        imgDriving2unselected = v.findViewById(R.id.wallet_driving_license_img2unselected);
        imgDriving3unselected = v.findViewById(R.id.wallet_driving_license_img3unselected);
        imgInsurance1unselected = v.findViewById(R.id.wallet_insurance_img1unselected);
        imgInsurance2unselected = v.findViewById(R.id.wallet_insurance_img2unselected);
        imgInsurance3unselected = v.findViewById(R.id.wallet_insurance_img3unselected);
        imgRegistration1unselected = v.findViewById(R.id.wallet_registration_img1unselected);
        imgRegistration2unselected = v.findViewById(R.id.wallet_registration_img2unselected);
        imgRegistration3unselected = v.findViewById(R.id.wallet_registration_img3unselected);
        imgTax1unselected = v.findViewById(R.id.wallet_tax_img1unselected);
        imgTax2unselected = v.findViewById(R.id.wallet_tax_img2unselected);
        imgTax3unselected = v.findViewById(R.id.wallet_tax_img3unselected);
        imgMaintenance1unselected = v.findViewById(R.id.wallet_maintenance_img1unselected);
        imgMaintenance2unselected = v.findViewById(R.id.wallet_maintenance_img2unselected);
        imgMaintenance3unselected = v.findViewById(R.id.wallet_maintenance_img3unselected);
        imgCar1unselected = v.findViewById(R.id.wallet_car_img1unselected);
        imgCar2unselected = v.findViewById(R.id.wallet_car_img2unselected);
        imgCar3unselected = v.findViewById(R.id.wallet_car_img3unselected);

        imgDriving1selected = v.findViewById(R.id.wallet_driving_license_img1selected);
        imgDriving2selected = v.findViewById(R.id.wallet_driving_license_img2selected);
        imgDriving3selected = v.findViewById(R.id.wallet_driving_license_img3selected);
        imgInsurance1selected = v.findViewById(R.id.wallet_insurance_img1selected);
        imgInsurance2selected = v.findViewById(R.id.wallet_insurance_img2selected);
        imgInsurance3selected = v.findViewById(R.id.wallet_insurance_img3selected);
        imgRegistration1selected = v.findViewById(R.id.wallet_registration_img1selected);
        imgRegistration2selected = v.findViewById(R.id.wallet_registration_img2selected);
        imgRegistration3selected = v.findViewById(R.id.wallet_registration_img3selected);
        imgTax1selected = v.findViewById(R.id.wallet_tax_img1selected);
        imgTax2selected = v.findViewById(R.id.wallet_tax_img2selected);
        imgTax3selected = v.findViewById(R.id.wallet_tax_img3selected);
        imgMaintenance1selected = v.findViewById(R.id.wallet_maintenance_img1selected);
        imgMaintenance2selected = v.findViewById(R.id.wallet_maintenance_img2selected);
        imgMaintenance3selected = v.findViewById(R.id.wallet_maintenance_img3selected);
        imgCar1selected = v.findViewById(R.id.wallet_car_img1selected);
        imgCar2selected = v.findViewById(R.id.wallet_car_img2selected);
        imgCar3selected = v.findViewById(R.id.wallet_car_img3selected);
    }

    private void setEvents() {
        btnEdit.setOnClickListener(view -> {
            if (!isEditMode) {
                showEditMode();
            } else {
                hideEditMode();
            }
        });

        btnCancel.setOnClickListener(view -> hideEditMode());

        btnSelectAll.setOnClickListener(view -> selectAll());

        btnDelete.setOnClickListener(view -> deleteImagesSelected());

        imgDriving1.setOnClickListener(createImageListener());
        imgDriving2.setOnClickListener(createImageListener());
        imgDriving3.setOnClickListener(createImageListener());
        imgInsurance1.setOnClickListener(createImageListener());
        imgInsurance2.setOnClickListener(createImageListener());
        imgInsurance3.setOnClickListener(createImageListener());
        imgRegistration1.setOnClickListener(createImageListener());
        imgRegistration2.setOnClickListener(createImageListener());
        imgRegistration3.setOnClickListener(createImageListener());
        imgTax1.setOnClickListener(createImageListener());
        imgTax2.setOnClickListener(createImageListener());
        imgTax3.setOnClickListener(createImageListener());
        imgMaintenance1.setOnClickListener(createImageListener());
        imgMaintenance2.setOnClickListener(createImageListener());
        imgMaintenance3.setOnClickListener(createImageListener());
        imgCar1.setOnClickListener(createImageListener());
        imgCar2.setOnClickListener(createImageListener());
        imgCar3.setOnClickListener(createImageListener());

        imgDriving1add.setOnClickListener(createAddImageListener());
        imgDriving2add.setOnClickListener(createAddImageListener());
        imgDriving3add.setOnClickListener(createAddImageListener());
        imgInsurance1add.setOnClickListener(createAddImageListener());
        imgInsurance2add.setOnClickListener(createAddImageListener());
        imgInsurance3add.setOnClickListener(createAddImageListener());
        imgRegistration1add.setOnClickListener(createAddImageListener());
        imgRegistration2add.setOnClickListener(createAddImageListener());
        imgRegistration3add.setOnClickListener(createAddImageListener());
        imgTax1add.setOnClickListener(createAddImageListener());
        imgTax2add.setOnClickListener(createAddImageListener());
        imgTax3add.setOnClickListener(createAddImageListener());
        imgMaintenance1add.setOnClickListener(createAddImageListener());
        imgMaintenance2add.setOnClickListener(createAddImageListener());
        imgMaintenance3add.setOnClickListener(createAddImageListener());
        imgCar1add.setOnClickListener(createAddImageListener());
        imgCar2add.setOnClickListener(createAddImageListener());
        imgCar3add.setOnClickListener(createAddImageListener());

        MySharedPreferences dhp = MySharedPreferences.createDashboard(getContext());
        if (dhp.contains("CarPictureId") && dhp.getLong("CarPictureId") != 0) {
            imgDriving1add.setImageDrawable(null);
            Long imgId = dhp.getLong("CarPictureId");
            Glide.with(this).load(imgId).into(imgDriving1);
            Glide.with(this).load(imgId).into(imgDriving1add);
        } else {
            Glide.with(this).load(R.drawable.car_otc).into(imgDriving1add);
        }
        btnScrollUp.setOnClickListener(view -> scrollView.smoothScrollTo(0, 0));

        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            int scrollY = scrollView.getScrollY();
            btnScrollUp.setVisibility(scrollY > 0 ? View.VISIBLE : View.GONE);
        });
    }

    private void hideEditMode() {
        isEditMode = false;
        layoutEdit.setVisibility(View.GONE);
        hideSelectionMode();
    }

    private void showEditMode() {
        isEditMode = true;
        layoutEdit.setVisibility(View.VISIBLE);
        showSelectionMode();
    }

    private void getUserWallet() {
        if (ConnectionUtils.isOnline(getContext())) {
            hideEditMode();
            GetUserWalletTask getUserWalletTask = new GetUserWalletTask();
            getUserWalletTask.execute();
        } else {
            ConnectionUtils.showOfflineToast();
        }
    }

    private View.OnClickListener createAddImageListener() {
        return view -> {
            imgViewSelected = getImageViewSelected(view);
            showAddImageDialog();
        };
    }

    private ImageView getImageViewSelected(View view) {
        switch (view.getId()) {
            case R.id.wallet_driving_license_img1add:
            case R.id.wallet_driving_license_img1:
                return imgDriving1;
            case R.id.wallet_driving_license_img2add:
            case R.id.wallet_driving_license_img2:
                return imgDriving2;
            case R.id.wallet_driving_license_img3add:
            case R.id.wallet_driving_license_img3:
                return imgDriving3;
            case R.id.wallet_insurance_img1add:
            case R.id.wallet_insurance_img1:
                return imgInsurance1;
            case R.id.wallet_insurance_img2add:
            case R.id.wallet_insurance_img2:
                return imgInsurance2;
            case R.id.wallet_insurance_img3add:
            case R.id.wallet_insurance_img3:
                return imgInsurance3;
            case R.id.wallet_registration_img1add:
            case R.id.wallet_registration_img1:
                return imgRegistration1;
            case R.id.wallet_registration_img2add:
            case R.id.wallet_registration_img2:
                return imgRegistration2;
            case R.id.wallet_registration_img3add:
            case R.id.wallet_registration_img3:
                return imgRegistration3;
            case R.id.wallet_tax_img1add:
            case R.id.wallet_tax_img1:
                return imgTax1;
            case R.id.wallet_tax_img2:
            case R.id.wallet_tax_img2add:
                return imgTax2;
            case R.id.wallet_tax_img3:
            case R.id.wallet_tax_img3add:
                return imgTax3;
            case R.id.wallet_maintenance_img1:
            case R.id.wallet_maintenance_img1add:
                return imgMaintenance1;
            case R.id.wallet_maintenance_img2:
            case R.id.wallet_maintenance_img2add:
                return imgMaintenance2;
            case R.id.wallet_maintenance_img3add:
            case R.id.wallet_maintenance_img3:
                return imgMaintenance3;
            case R.id.wallet_car_img1add:
            case R.id.wallet_car_img1:
                return imgCar1;
            case R.id.wallet_car_img2add:
            case R.id.wallet_car_img2:
                return imgCar2;
            case R.id.wallet_car_img3add:
            case R.id.wallet_car_img3:
                return imgCar3;
        }
        return imgDriving1;
    }

    private View.OnClickListener createImageListener() {
        return view -> {
            imgViewSelected = getImageViewSelected(view);
            if (isEditMode) {
                changeSelected();
            } else {
                openImage();
            }
        };
    }

    private void openImage() {
        Intent intent = new Intent(getActivity(), PictureActivity.class);
        intent.putExtra(Constants.Extras.IMAGE, getImageSelected());
        startActivity(intent);
    }

    private long getImageSelected() {
        Wallet.DocType docType = getDocType();
        int position = getImagePosition();
        Wallet.Doc walletDoc = getWalletDoc(docType, position);
        return walletDoc != null ? walletDoc.getId() : 0;
    }

    private int getImagePosition() {
        switch (imgViewSelected.getId()) {
            case R.id.wallet_driving_license_img1:
            case R.id.wallet_insurance_img1:
            case R.id.wallet_registration_img1:
            case R.id.wallet_tax_img1:
            case R.id.wallet_maintenance_img1:
            case R.id.wallet_car_img1:
                return 1;
            case R.id.wallet_driving_license_img2:
            case R.id.wallet_insurance_img2:
            case R.id.wallet_registration_img2:
            case R.id.wallet_tax_img2:
            case R.id.wallet_maintenance_img2:
            case R.id.wallet_car_img2:
                return 2;
            case R.id.wallet_driving_license_img3:
            case R.id.wallet_insurance_img3:
            case R.id.wallet_registration_img3:
            case R.id.wallet_tax_img3:
            case R.id.wallet_maintenance_img3:
            case R.id.wallet_car_img3:
                return 3;
        }
        return 0;
    }

    private void showAddImageDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(getActivity());
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
        ImageRetriever.functionCamera(imagePath, REQUEST_CODE_CAMERA, getActivity());
    }

    private void choosePhotoFromGallery() {
        initializeImagePath();
        ImageRetriever.functionGallery(REQUEST_CODE_GALLERY, getActivity());
    }

    private void initializeImagePath() {
        imagePath = ImageUtils.getWalletImageName(getActivity());
    }


    void doActionsAfterCamera() {
        showImageLocal(imagePath);
    }

    void doActionsAfterGallery(Intent data) {
        if (data != null) {
            Uri imageUri = data.getData();
            String imageLocalUrl = ImageUtils.getRealPathFromURI(imageUri, getActivity());
            if (imageLocalUrl != null && !imageLocalUrl.equals("") && Utils.isImage(imageLocalUrl)) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                    ImageUtils.saveBitmapIntoFile(imagePath, bitmap, Bitmap.CompressFormat.JPEG);
                    bitmap = null;
                    showImageLocal(imageLocalUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                if (!Utils.isImage(imageLocalUrl)) {
                    String ext = Utils.getExtension(imageLocalUrl);
                    Toast.makeText(getActivity(), String.format(getString(R.string.extension_not_good), ext), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), R.string.error_default, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void showImageLocal(String imagePath) {
        uploadImageWithoutTask(imagePath);
        final FragmentActivity act = getActivity();
        if (act != null) {
            try {
                Bitmap bmp = ImageUtils.getImageCorrectedFromRotationOfEXIF(imagePath);
                bmp = ImageUtils.CheckServerSize(bmp);
                Glide.with(act)
                        .load(bmp)
                        .into(new SimpleTarget<Drawable>() {
                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                imgViewSelected.setImageDrawable(resource);
                                //updateImageBytes(resource);
                                changeAddButtonAndImageVisibility(imgViewSelected, false);
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void deleteImagesSelected() {
        if (getImagesSelected().size() > 0) {
            if (ConnectionUtils.isOnline(getContext())) {
                DeleteImageTask deleteImageTask = new DeleteImageTask();
                deleteImageTask.execute();
            } else {
                ConnectionUtils.showOfflineToast();
            }
        } else {
            hideEditMode();
        }
    }

    private void showImagePlaceholder(ImageView imageView) {
        if (imageView != null) {
            Glide.with(getActivity())
                    .load(R.drawable.photo_placeholder_square)
                    .into(imageView);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class DeleteImageTask extends AsyncTask<Void, Void, Boolean> {
        ProgressDialog pd;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(getContext());
            pd.setMessage(getString(R.string.loading));
            pd.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                MySharedPreferences msp = MySharedPreferences.createLogin(getActivity());
                Wallet.DeleteDoc.Builder builder = Wallet.DeleteDoc.newBuilder();
                List<Long> imagesSelected = getImagesSelected();
                builder.addAllDocsId(imagesSelected);
                Shared.OTCResponse response = ApiCaller.doCall(Endpoints.DELETE, msp.getBytes("token"), builder.build(), Shared.OTCResponse.class);
                boolean success = response.getStatus() == Shared.OTCStatus.SUCCESS;

                MySharedPreferences dhp = MySharedPreferences.createDashboard(getContext());
                long myCarId = dhp.getLong("CarPictureId");
                if (success && imagesSelected.contains(myCarId)) {
                    dhp.remove("CarPictureId");
                    Utils.runOnMainThread(() -> Glide.with(WalletFragment.this).load(R.drawable.car_otc).into(imgDriving1add));
                }

                return success;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            pd.dismiss();
            if (result) {
                showCustomDialog(R.string.deleted_images, dialogInterface -> getUserWallet());
            } else {
                showCustomDialogError();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetUserWalletTask extends AsyncTask<Void, Void, List<Wallet.Doc>> {

        @Override
        protected List<Wallet.Doc> doInBackground(Void... voids) {
            try {
                MySharedPreferences msp = MySharedPreferences.createLogin(getActivity());
                Wallet.Docs response = ApiCaller.doCall(Endpoints.STORAGE_USER, msp.getBytes("token"), null, Wallet.Docs.class);
                return response.getDocsList();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Wallet.Doc> result) {
            super.onPostExecute(result);
            if (result != null) {
                docs = result;
                Gson gson = new Gson();
                String json = gson.toJson(result);
                MySharedPreferences.createGeneral(getContext()).putString("StorageCache", json);

                if (!Utils.isActivityFinish(getContext())) {
                    resetUI();
                    showImages();
                }
            }
        }
    }

    private void showImages() {
        for (int i = 0; i < docs.size(); i++) {
            Wallet.Doc doc = docs.get(i);
            if (doc.getType() == Wallet.DocType.CAR_PICTURES) {
                showDrivingImage(doc);
            } else if (doc.getType() == Wallet.DocType.DRIVING_LICENSE) {
                showInsuranceImage(doc);
            } else if (doc.getType() == Wallet.DocType.REGISTRATION) {
                showRegistrationImage(doc);
            } else if (doc.getType() == Wallet.DocType.DEALER_INFO_AND_MAINTENANCE) {
                showTaxImage(doc);
            } else if (doc.getType() == Wallet.DocType.FINANCE_INSURANCE) {
                showMaintenanceImage(doc);
            } else if (doc.getType() == Wallet.DocType.OTHER) {
                showCarImage(doc);
            }
        }
    }

    private void showDrivingImage(Wallet.Doc doc) {
        switch (doc.getIndex()) {
            case 1:
                changeAddButtonAndImageVisibility(imgDriving1, true);
                getImage(doc.getId(), imgDriving1);
                break;
            case 2:
                changeAddButtonAndImageVisibility(imgDriving2, true);
                getImage(doc.getId(), imgDriving2);
                break;
            case 3:
                changeAddButtonAndImageVisibility(imgDriving3, true);
                getImage(doc.getId(), imgDriving3);
                break;
        }
    }

    private void showInsuranceImage(Wallet.Doc doc) {
        switch (doc.getIndex()) {
            case 1:
                changeAddButtonAndImageVisibility(imgInsurance1, true);
                getImage(doc.getId(), imgInsurance1);
                break;
            case 2:
                changeAddButtonAndImageVisibility(imgInsurance2, true);
                getImage(doc.getId(), imgInsurance2);
                break;
            case 3:
                changeAddButtonAndImageVisibility(imgInsurance3, true);
                getImage(doc.getId(), imgInsurance3);
                break;
        }
    }

    private void showRegistrationImage(Wallet.Doc doc) {
        switch (doc.getIndex()) {
            case 1:
                changeAddButtonAndImageVisibility(imgRegistration1, true);
                getImage(doc.getId(), imgRegistration1);
                break;
            case 2:
                changeAddButtonAndImageVisibility(imgRegistration2, true);
                getImage(doc.getId(), imgRegistration2);
                break;
            case 3:
                changeAddButtonAndImageVisibility(imgRegistration3, true);
                getImage(doc.getId(), imgRegistration3);
                break;
        }
    }

    private void showTaxImage(Wallet.Doc doc) {
        switch (doc.getIndex()) {
            case 1:
                changeAddButtonAndImageVisibility(imgTax1, true);
                getImage(doc.getId(), imgTax1);
                break;
            case 2:
                changeAddButtonAndImageVisibility(imgTax2, true);
                getImage(doc.getId(), imgTax2);
                break;
            case 3:
                changeAddButtonAndImageVisibility(imgTax3, true);
                getImage(doc.getId(), imgTax3);
                break;
        }
    }

    private void showMaintenanceImage(Wallet.Doc doc) {
        switch (doc.getIndex()) {
            case 1:
                changeAddButtonAndImageVisibility(imgMaintenance1, true);
                getImage(doc.getId(), imgMaintenance1);
                break;
            case 2:
                changeAddButtonAndImageVisibility(imgMaintenance2, true);
                getImage(doc.getId(), imgMaintenance2);
                break;
            case 3:
                changeAddButtonAndImageVisibility(imgMaintenance3, true);
                getImage(doc.getId(), imgMaintenance3);
                break;
        }
    }

    private void showCarImage(Wallet.Doc doc) {
        switch (doc.getIndex()) {
            case 1:
                changeAddButtonAndImageVisibility(imgCar1, true);
                getImage(doc.getId(), imgCar1);
                break;
            case 2:
                changeAddButtonAndImageVisibility(imgCar2, true);
                getImage(doc.getId(), imgCar2);
                break;
            case 3:
                changeAddButtonAndImageVisibility(imgCar3, true);
                getImage(doc.getId(), imgCar3);
                break;
        }
    }

    private void getImage(long imageId, ImageView imageView) {
        if (isVisible()) {
            Glide.with(this).load(imageId).into(imageView);
        }
    }

    private void uploadImageWithoutTask(final String file) {
        new Thread(() -> {
            try {
                MySharedPreferences msp = MySharedPreferences.createLogin(getActivity());
                Wallet.UploadDoc.Builder builder = Wallet.UploadDoc.newBuilder();
                builder.setType(getDocType());
                // Set Index
                builder.setIndex(getImagePosition());

                Bitmap bmp = ImageUtils.getImageCorrectedFromRotationOfEXIF(file);
                bmp = ImageUtils.CheckServerSize(bmp);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                builder.setData(ByteString.copyFrom(baos.toByteArray()));
                baos = null;
                bmp = null;
                System.gc();
                builder.setName(ImageUtils.getWalletImageName(getActivity()));
                Shared.OTCResponse response = ApiCaller.doCallWithProgress(Endpoints.UPLOAD, msp.getString("token"), builder.build(), Shared.OTCResponse.class, getContext());

                if (response != null) {
                    if (response.getStatusValue() != 1) {
                        final Activity act = getActivity();
                        if (act != null) {
                            act.runOnUiThread(() -> {
                                CustomDialog cd = new CustomDialog(act, getString(R.string.no_upload_img) + "Error : " + CloudErrorHandler.handleError(response.getStatus()), true);
                                cd.show();
                            });
                        }
                    } else {
                        // S'ha pujat la imatge del cotxe
                        if (getDocType() == Wallet.DocType.CAR_PICTURES && getImagePosition() == 1) {
                            DashboardAndStatus.CarPhoto photo = ApiCaller.doCall(Endpoints.DASHBOARD_CAR_PHOTO, true, null, DashboardAndStatus.CarPhoto.class);
                            if (photo != null) {
                                MySharedPreferences.createDashboard(getContext()).putLong("CarPictureId", photo.getFileId());
                            }
                        }
                        getUserWallet();
                    }
                }

            } catch (RuntimeException | ApiCaller.OTCException e) {
                e.printStackTrace();
            }
        }, "UploadImageThread").start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        imgDriving1 = null;
        imgDriving2 = null;
        imgDriving3 = null;
        imgInsurance1 = null;
        imgInsurance2 = null;
        imgInsurance3 = null;
        imgRegistration1 = null;
        imgRegistration2 = null;
        imgRegistration3 = null;
        imgTax1 = null;
        imgTax2 = null;
        imgTax3 = null;
        imgMaintenance1 = null;
        imgMaintenance2 = null;
        imgMaintenance3 = null;
        imgCar1 = null;
        imgCar2 = null;
        imgCar3 = null;
    }

    private Wallet.DocType getDocType() {
        switch (imgViewSelected.getId()) {
            case R.id.wallet_driving_license_img1:
            case R.id.wallet_driving_license_img2:
            case R.id.wallet_driving_license_img3:
                return Wallet.DocType.CAR_PICTURES;
            case R.id.wallet_insurance_img1:
            case R.id.wallet_insurance_img2:
            case R.id.wallet_insurance_img3:
                return Wallet.DocType.DRIVING_LICENSE;
            case R.id.wallet_registration_img1:
            case R.id.wallet_registration_img2:
            case R.id.wallet_registration_img3:
                return Wallet.DocType.REGISTRATION;
            case R.id.wallet_tax_img1:
            case R.id.wallet_tax_img2:
            case R.id.wallet_tax_img3:
                return Wallet.DocType.DEALER_INFO_AND_MAINTENANCE;
            case R.id.wallet_maintenance_img1:
            case R.id.wallet_maintenance_img2:
            case R.id.wallet_maintenance_img3:
                return Wallet.DocType.FINANCE_INSURANCE;
            case R.id.wallet_car_img1:
            case R.id.wallet_car_img2:
            case R.id.wallet_car_img3:
                return Wallet.DocType.OTHER;
        }
        return null;
    }

    private void changeAddButtonAndImageVisibility(ImageView imageView, boolean isLoading) {
        switch (imageView.getId()) {
            case R.id.wallet_driving_license_img1:
                imgDriving1.setVisibility(View.VISIBLE);
                imgDriving1add.setVisibility(View.GONE);
                imgDriving1loading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                break;
            case R.id.wallet_driving_license_img2:
                imgDriving2.setVisibility(View.VISIBLE);
                imgDriving2add.setVisibility(View.GONE);
                imgDriving2loading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                break;
            case R.id.wallet_driving_license_img3:
                imgDriving3.setVisibility(View.VISIBLE);
                imgDriving3add.setVisibility(View.GONE);
                imgDriving3loading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                break;
            case R.id.wallet_insurance_img1:
                imgInsurance1.setVisibility(View.VISIBLE);
                imgInsurance1add.setVisibility(View.GONE);
                imgInsurance1loading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                break;
            case R.id.wallet_insurance_img2:
                imgInsurance2.setVisibility(View.VISIBLE);
                imgInsurance2add.setVisibility(View.GONE);
                imgInsurance2loading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                break;
            case R.id.wallet_insurance_img3:
                imgInsurance3.setVisibility(View.VISIBLE);
                imgInsurance3add.setVisibility(View.GONE);
                imgInsurance3loading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                break;
            case R.id.wallet_registration_img1:
                imgRegistration1.setVisibility(View.VISIBLE);
                imgRegistration1add.setVisibility(View.GONE);
                imgRegistration1loading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                break;
            case R.id.wallet_registration_img2:
                imgRegistration2.setVisibility(View.VISIBLE);
                imgRegistration2add.setVisibility(View.GONE);
                imgRegistration2loading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                break;
            case R.id.wallet_registration_img3:
                imgRegistration3.setVisibility(View.VISIBLE);
                imgRegistration3add.setVisibility(View.GONE);
                imgRegistration3loading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                break;
            case R.id.wallet_tax_img1:
                imgTax1.setVisibility(View.VISIBLE);
                imgTax1add.setVisibility(View.GONE);
                imgTax1loading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                break;
            case R.id.wallet_tax_img2:
                imgTax2.setVisibility(View.VISIBLE);
                imgTax2add.setVisibility(View.GONE);
                imgTax2loading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                break;
            case R.id.wallet_tax_img3:
                imgTax3.setVisibility(View.VISIBLE);
                imgTax3add.setVisibility(View.GONE);
                imgTax3loading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                break;
            case R.id.wallet_maintenance_img1:
                imgMaintenance1.setVisibility(View.VISIBLE);
                imgMaintenance1add.setVisibility(View.GONE);
                imgMaintenance1loading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                break;
            case R.id.wallet_maintenance_img2:
                imgMaintenance2.setVisibility(View.VISIBLE);
                imgMaintenance2add.setVisibility(View.GONE);
                imgMaintenance2loading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                break;
            case R.id.wallet_maintenance_img3:
                imgMaintenance3.setVisibility(View.VISIBLE);
                imgMaintenance3add.setVisibility(View.GONE);
                imgMaintenance3loading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                break;
            case R.id.wallet_car_img1:
                imgCar1.setVisibility(View.VISIBLE);
                imgCar1add.setVisibility(View.GONE);
                imgCar1loading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                break;
            case R.id.wallet_car_img2:
                imgCar2.setVisibility(View.VISIBLE);
                imgCar2add.setVisibility(View.GONE);
                imgCar2loading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                break;
            case R.id.wallet_car_img3:
                imgCar3.setVisibility(View.VISIBLE);
                imgCar3add.setVisibility(View.GONE);
                imgCar3loading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                break;
        }
    }

    private void selectAll() {
        imgDriving1selected.setVisibility(hasImage(imgDriving1) ? View.VISIBLE : View.GONE);
        imgDriving2selected.setVisibility(hasImage(imgDriving2) ? View.VISIBLE : View.GONE);
        imgDriving3selected.setVisibility(hasImage(imgDriving3) ? View.VISIBLE : View.GONE);
        imgInsurance1selected.setVisibility(hasImage(imgInsurance1) ? View.VISIBLE : View.GONE);
        imgInsurance2selected.setVisibility(hasImage(imgInsurance2) ? View.VISIBLE : View.GONE);
        imgInsurance3selected.setVisibility(hasImage(imgInsurance3) ? View.VISIBLE : View.GONE);
        imgRegistration1selected.setVisibility(hasImage(imgRegistration1) ? View.VISIBLE : View.GONE);
        imgRegistration2selected.setVisibility(hasImage(imgRegistration2) ? View.VISIBLE : View.GONE);
        imgRegistration3selected.setVisibility(hasImage(imgRegistration3) ? View.VISIBLE : View.GONE);
        imgTax1selected.setVisibility(hasImage(imgTax1) ? View.VISIBLE : View.GONE);
        imgTax2selected.setVisibility(hasImage(imgTax2) ? View.VISIBLE : View.GONE);
        imgTax3selected.setVisibility(hasImage(imgTax3) ? View.VISIBLE : View.GONE);
        imgMaintenance1selected.setVisibility(hasImage(imgMaintenance1) ? View.VISIBLE : View.GONE);
        imgMaintenance2selected.setVisibility(hasImage(imgMaintenance2) ? View.VISIBLE : View.GONE);
        imgMaintenance3selected.setVisibility(hasImage(imgMaintenance3) ? View.VISIBLE : View.GONE);
        imgCar1selected.setVisibility(hasImage(imgCar1) ? View.VISIBLE : View.GONE);
        imgCar2selected.setVisibility(hasImage(imgCar2) ? View.VISIBLE : View.GONE);
        imgCar3selected.setVisibility(hasImage(imgCar3) ? View.VISIBLE : View.GONE);

        imgDriving1unselected.setVisibility(View.GONE);
        imgDriving2unselected.setVisibility(View.GONE);
        imgDriving3unselected.setVisibility(View.GONE);
        imgInsurance1unselected.setVisibility(View.GONE);
        imgInsurance2unselected.setVisibility(View.GONE);
        imgInsurance3unselected.setVisibility(View.GONE);
        imgRegistration1unselected.setVisibility(View.GONE);
        imgRegistration2unselected.setVisibility(View.GONE);
        imgRegistration3unselected.setVisibility(View.GONE);
        imgTax1unselected.setVisibility(View.GONE);
        imgTax2unselected.setVisibility(View.GONE);
        imgTax3unselected.setVisibility(View.GONE);
        imgMaintenance1unselected.setVisibility(View.GONE);
        imgMaintenance2unselected.setVisibility(View.GONE);
        imgMaintenance3unselected.setVisibility(View.GONE);
        imgCar1unselected.setVisibility(View.GONE);
        imgCar2unselected.setVisibility(View.GONE);
        imgCar3unselected.setVisibility(View.GONE);
    }

    private void showSelectionMode() {
        imgDriving1unselected.setVisibility(hasImage(imgDriving1) ? View.VISIBLE : View.GONE);
        imgDriving2unselected.setVisibility(hasImage(imgDriving2) ? View.VISIBLE : View.GONE);
        imgDriving3unselected.setVisibility(hasImage(imgDriving3) ? View.VISIBLE : View.GONE);
        imgInsurance1unselected.setVisibility(hasImage(imgInsurance1) ? View.VISIBLE : View.GONE);
        imgInsurance2unselected.setVisibility(hasImage(imgInsurance2) ? View.VISIBLE : View.GONE);
        imgInsurance3unselected.setVisibility(hasImage(imgInsurance3) ? View.VISIBLE : View.GONE);
        imgRegistration1unselected.setVisibility(hasImage(imgRegistration1) ? View.VISIBLE : View.GONE);
        imgRegistration2unselected.setVisibility(hasImage(imgRegistration2) ? View.VISIBLE : View.GONE);
        imgRegistration3unselected.setVisibility(hasImage(imgRegistration3) ? View.VISIBLE : View.GONE);
        imgTax1unselected.setVisibility(hasImage(imgTax1) ? View.VISIBLE : View.GONE);
        imgTax2unselected.setVisibility(hasImage(imgTax2) ? View.VISIBLE : View.GONE);
        imgTax3unselected.setVisibility(hasImage(imgTax3) ? View.VISIBLE : View.GONE);
        imgMaintenance1unselected.setVisibility(hasImage(imgMaintenance1) ? View.VISIBLE : View.GONE);
        imgMaintenance2unselected.setVisibility(hasImage(imgMaintenance2) ? View.VISIBLE : View.GONE);
        imgMaintenance3unselected.setVisibility(hasImage(imgMaintenance3) ? View.VISIBLE : View.GONE);
        imgCar1unselected.setVisibility(hasImage(imgCar1) ? View.VISIBLE : View.GONE);
        imgCar2unselected.setVisibility(hasImage(imgCar2) ? View.VISIBLE : View.GONE);
        imgCar3unselected.setVisibility(hasImage(imgCar3) ? View.VISIBLE : View.GONE);
    }

    private void hideSelectionMode() {
        imgDriving1unselected.setVisibility(View.GONE);
        imgDriving2unselected.setVisibility(View.GONE);
        imgDriving3unselected.setVisibility(View.GONE);
        imgInsurance1unselected.setVisibility(View.GONE);
        imgInsurance2unselected.setVisibility(View.GONE);
        imgInsurance3unselected.setVisibility(View.GONE);
        imgRegistration1unselected.setVisibility(View.GONE);
        imgRegistration2unselected.setVisibility(View.GONE);
        imgRegistration3unselected.setVisibility(View.GONE);
        imgTax1unselected.setVisibility(View.GONE);
        imgTax2unselected.setVisibility(View.GONE);
        imgTax3unselected.setVisibility(View.GONE);
        imgMaintenance1unselected.setVisibility(View.GONE);
        imgMaintenance2unselected.setVisibility(View.GONE);
        imgMaintenance3unselected.setVisibility(View.GONE);
        imgCar1unselected.setVisibility(View.GONE);
        imgCar2unselected.setVisibility(View.GONE);
        imgCar3unselected.setVisibility(View.GONE);

        imgDriving1selected.setVisibility(View.GONE);
        imgDriving2selected.setVisibility(View.GONE);
        imgDriving3selected.setVisibility(View.GONE);
        imgInsurance1selected.setVisibility(View.GONE);
        imgInsurance2selected.setVisibility(View.GONE);
        imgInsurance3selected.setVisibility(View.GONE);
        imgRegistration1selected.setVisibility(View.GONE);
        imgRegistration2selected.setVisibility(View.GONE);
        imgRegistration3selected.setVisibility(View.GONE);
        imgTax1selected.setVisibility(View.GONE);
        imgTax2selected.setVisibility(View.GONE);
        imgTax3selected.setVisibility(View.GONE);
        imgMaintenance1selected.setVisibility(View.GONE);
        imgMaintenance2selected.setVisibility(View.GONE);
        imgMaintenance3selected.setVisibility(View.GONE);
        imgCar1selected.setVisibility(View.GONE);
        imgCar2selected.setVisibility(View.GONE);
        imgCar3selected.setVisibility(View.GONE);
    }

    private void changeSelected() {
        boolean selected;
        switch (imgViewSelected.getId()) {
            case R.id.wallet_driving_license_img1:
                selected = imgDriving1selected.getVisibility() == View.VISIBLE;
                imgDriving1unselected.setVisibility(selected ? View.VISIBLE : View.GONE);
                imgDriving1selected.setVisibility(selected ? View.GONE : View.VISIBLE);
                break;
            case R.id.wallet_driving_license_img2:
                selected = imgDriving2selected.getVisibility() == View.VISIBLE;
                imgDriving2unselected.setVisibility(selected ? View.VISIBLE : View.GONE);
                imgDriving2selected.setVisibility(selected ? View.GONE : View.VISIBLE);
                break;
            case R.id.wallet_driving_license_img3:
                selected = imgDriving3selected.getVisibility() == View.VISIBLE;
                imgDriving3unselected.setVisibility(selected ? View.VISIBLE : View.GONE);
                imgDriving3selected.setVisibility(selected ? View.GONE : View.VISIBLE);
                break;
            case R.id.wallet_insurance_img1:
                selected = imgInsurance1selected.getVisibility() == View.VISIBLE;
                imgInsurance1unselected.setVisibility(selected ? View.VISIBLE : View.GONE);
                imgInsurance1selected.setVisibility(selected ? View.GONE : View.VISIBLE);
                break;
            case R.id.wallet_insurance_img2:
                selected = imgInsurance2selected.getVisibility() == View.VISIBLE;
                imgInsurance2unselected.setVisibility(selected ? View.VISIBLE : View.GONE);
                imgInsurance2selected.setVisibility(selected ? View.GONE : View.VISIBLE);
                break;
            case R.id.wallet_insurance_img3:
                selected = imgInsurance3selected.getVisibility() == View.VISIBLE;
                imgInsurance3unselected.setVisibility(selected ? View.VISIBLE : View.GONE);
                imgInsurance3selected.setVisibility(selected ? View.GONE : View.VISIBLE);
                break;
            case R.id.wallet_registration_img1:
                selected = imgRegistration1selected.getVisibility() == View.VISIBLE;
                imgRegistration1unselected.setVisibility(selected ? View.VISIBLE : View.GONE);
                imgRegistration1selected.setVisibility(selected ? View.GONE : View.VISIBLE);
                break;
            case R.id.wallet_registration_img2:
                selected = imgDriving1selected.getVisibility() == View.VISIBLE;
                imgRegistration2unselected.setVisibility(selected ? View.VISIBLE : View.GONE);
                imgRegistration2selected.setVisibility(selected ? View.GONE : View.VISIBLE);
                break;
            case R.id.wallet_registration_img3:
                selected = imgRegistration3selected.getVisibility() == View.VISIBLE;
                imgRegistration3unselected.setVisibility(selected ? View.VISIBLE : View.GONE);
                imgRegistration3selected.setVisibility(selected ? View.GONE : View.VISIBLE);
                break;
            case R.id.wallet_tax_img1:
                selected = imgTax1selected.getVisibility() == View.VISIBLE;
                imgTax1unselected.setVisibility(selected ? View.VISIBLE : View.GONE);
                imgTax1selected.setVisibility(selected ? View.GONE : View.VISIBLE);
                break;
            case R.id.wallet_tax_img2:
                selected = imgTax2selected.getVisibility() == View.VISIBLE;
                imgTax2unselected.setVisibility(selected ? View.VISIBLE : View.GONE);
                imgTax2selected.setVisibility(selected ? View.GONE : View.VISIBLE);
                break;
            case R.id.wallet_tax_img3:
                selected = imgTax3selected.getVisibility() == View.VISIBLE;
                imgTax3unselected.setVisibility(selected ? View.VISIBLE : View.GONE);
                imgTax3selected.setVisibility(selected ? View.GONE : View.VISIBLE);
                break;
            case R.id.wallet_maintenance_img1:
                selected = imgMaintenance1selected.getVisibility() == View.VISIBLE;
                imgMaintenance1unselected.setVisibility(selected ? View.VISIBLE : View.GONE);
                imgMaintenance1selected.setVisibility(selected ? View.GONE : View.VISIBLE);
                break;
            case R.id.wallet_maintenance_img2:
                selected = imgMaintenance2selected.getVisibility() == View.VISIBLE;
                imgMaintenance2unselected.setVisibility(selected ? View.VISIBLE : View.GONE);
                imgMaintenance2selected.setVisibility(selected ? View.GONE : View.VISIBLE);
                break;
            case R.id.wallet_maintenance_img3:
                selected = imgMaintenance3selected.getVisibility() == View.VISIBLE;
                imgMaintenance3unselected.setVisibility(selected ? View.VISIBLE : View.GONE);
                imgMaintenance3selected.setVisibility(selected ? View.GONE : View.VISIBLE);
                break;
            case R.id.wallet_car_img1:
                selected = imgCar1selected.getVisibility() == View.VISIBLE;
                imgCar1unselected.setVisibility(selected ? View.VISIBLE : View.GONE);
                imgCar1selected.setVisibility(selected ? View.GONE : View.VISIBLE);
                break;
            case R.id.wallet_car_img2:
                selected = imgCar2selected.getVisibility() == View.VISIBLE;
                imgCar2unselected.setVisibility(selected ? View.VISIBLE : View.GONE);
                imgCar2selected.setVisibility(selected ? View.GONE : View.VISIBLE);
                break;
            case R.id.wallet_car_img3:
                selected = imgCar3selected.getVisibility() == View.VISIBLE;
                imgCar3unselected.setVisibility(selected ? View.VISIBLE : View.GONE);
                imgCar3selected.setVisibility(selected ? View.GONE : View.VISIBLE);
                break;
        }
    }

    private boolean hasImage(ImageView imageView) {
        return imageView.getVisibility() == View.VISIBLE;
    }

    private void resetUI() {
        imgDriving1.setVisibility(View.GONE);
        imgDriving1loading.setVisibility(View.GONE);
        imgDriving1add.setVisibility(View.VISIBLE);
        imgDriving2.setVisibility(View.GONE);
        imgDriving2loading.setVisibility(View.GONE);
        imgDriving2add.setVisibility(View.VISIBLE);
        imgDriving3.setVisibility(View.GONE);
        imgDriving3loading.setVisibility(View.GONE);
        imgDriving3add.setVisibility(View.VISIBLE);
        imgInsurance1.setVisibility(View.GONE);
        imgInsurance1loading.setVisibility(View.GONE);
        imgInsurance1add.setVisibility(View.VISIBLE);
        imgInsurance2.setVisibility(View.GONE);
        imgInsurance2loading.setVisibility(View.GONE);
        imgInsurance2add.setVisibility(View.VISIBLE);
        imgInsurance3.setVisibility(View.GONE);
        imgInsurance3loading.setVisibility(View.GONE);
        imgInsurance3add.setVisibility(View.VISIBLE);
        imgRegistration1.setVisibility(View.GONE);
        imgRegistration1loading.setVisibility(View.GONE);
        imgRegistration1add.setVisibility(View.VISIBLE);
        imgRegistration2.setVisibility(View.GONE);
        imgRegistration2loading.setVisibility(View.GONE);
        imgRegistration2add.setVisibility(View.VISIBLE);
        imgRegistration3.setVisibility(View.GONE);
        imgRegistration3loading.setVisibility(View.GONE);
        imgRegistration3add.setVisibility(View.VISIBLE);
        imgTax1.setVisibility(View.GONE);
        imgTax1loading.setVisibility(View.GONE);
        imgTax1add.setVisibility(View.VISIBLE);
        imgTax2.setVisibility(View.GONE);
        imgTax2loading.setVisibility(View.GONE);
        imgTax2add.setVisibility(View.VISIBLE);
        imgTax3.setVisibility(View.GONE);
        imgTax3loading.setVisibility(View.GONE);
        imgTax3add.setVisibility(View.VISIBLE);
        imgMaintenance1.setVisibility(View.GONE);
        imgMaintenance1loading.setVisibility(View.GONE);
        imgMaintenance1add.setVisibility(View.VISIBLE);
        imgMaintenance2.setVisibility(View.GONE);
        imgMaintenance2loading.setVisibility(View.GONE);
        imgMaintenance2add.setVisibility(View.VISIBLE);
        imgMaintenance3.setVisibility(View.GONE);
        imgMaintenance3loading.setVisibility(View.GONE);
        imgMaintenance3add.setVisibility(View.VISIBLE);
        imgCar1.setVisibility(View.GONE);
        imgCar1loading.setVisibility(View.GONE);
        imgCar1add.setVisibility(View.VISIBLE);
        imgCar2.setVisibility(View.GONE);
        imgCar2loading.setVisibility(View.GONE);
        imgCar2add.setVisibility(View.VISIBLE);
        imgCar3.setVisibility(View.GONE);
        imgCar3loading.setVisibility(View.GONE);
        imgCar3add.setVisibility(View.VISIBLE);
    }

    private boolean isSelected(FrameLayout frameLayout) {
        return frameLayout.getVisibility() == View.VISIBLE;
    }

    private List<Long> getImagesSelected() {
        List<Long> imagesSelected = new ArrayList<>();
        imagesSelected.addAll(getDrivingImagesSelected());
        imagesSelected.addAll(getInsuranceImagesSelected());
        imagesSelected.addAll(getRegistrationImagesSelected());
        imagesSelected.addAll(getTaxImagesSelected());
        imagesSelected.addAll(getMaintenanceImagesSelected());
        imagesSelected.addAll(getCarImagesSelected());
        return imagesSelected;
    }

    private Wallet.Doc getWalletDoc(Wallet.DocType docType, int index) {
        if (docs != null) {
            for (int i = 0; i < docs.size(); i++) {
                Wallet.Doc doc = docs.get(i);
                if (doc.getType() == docType) {
                    if (index == doc.getIndex()) {
                        return doc;
                    }
                }
            }
        }

        return null;
    }

    private List<Long> getDrivingImagesSelected() {
        List<Long> imagesSelected = new ArrayList<>();
        if (hasImage(imgDriving1) && isSelected(imgDriving1selected)) {
            Wallet.Doc walletDoc = getWalletDoc(Wallet.DocType.CAR_PICTURES, 1);
            if (walletDoc != null) {
                imagesSelected.add(walletDoc.getId());
            }
        }
        if (hasImage(imgDriving2) && isSelected(imgDriving2selected)) {
            Wallet.Doc walletDoc = getWalletDoc(Wallet.DocType.CAR_PICTURES, 2);
            if (walletDoc != null) {
                imagesSelected.add(walletDoc.getId());
            }
        }
        if (hasImage(imgDriving3) && isSelected(imgDriving3selected)) {
            Wallet.Doc walletDoc = getWalletDoc(Wallet.DocType.CAR_PICTURES, 3);
            if (walletDoc != null) {
                imagesSelected.add(walletDoc.getId());
            }
        }
        return imagesSelected;
    }

    private List<Long> getInsuranceImagesSelected() {
        List<Long> imagesSelected = new ArrayList<>();
        if (hasImage(imgInsurance1) && isSelected(imgInsurance1selected)) {
            Wallet.Doc walletDoc = getWalletDoc(Wallet.DocType.DRIVING_LICENSE, 1);
            if (walletDoc != null) {
                imagesSelected.add(walletDoc.getId());
            }
        }
        if (hasImage(imgInsurance2) && isSelected(imgInsurance2selected)) {
            Wallet.Doc walletDoc = getWalletDoc(Wallet.DocType.DRIVING_LICENSE, 2);
            if (walletDoc != null) {
                imagesSelected.add(walletDoc.getId());
            }
        }
        if (hasImage(imgInsurance3) && isSelected(imgInsurance3selected)) {
            Wallet.Doc walletDoc = getWalletDoc(Wallet.DocType.DRIVING_LICENSE, 3);
            if (walletDoc != null) {
                imagesSelected.add(walletDoc.getId());
            }
        }
        return imagesSelected;
    }

    private List<Long> getRegistrationImagesSelected() {
        List<Long> imagesSelected = new ArrayList<>();
        if (hasImage(imgRegistration1) && isSelected(imgRegistration1selected)) {
            Wallet.Doc walletDoc = getWalletDoc(Wallet.DocType.REGISTRATION, 1);
            if (walletDoc != null) {
                imagesSelected.add(walletDoc.getId());
            }
        }
        if (hasImage(imgRegistration2) && isSelected(imgRegistration2selected)) {
            Wallet.Doc walletDoc = getWalletDoc(Wallet.DocType.REGISTRATION, 2);
            if (walletDoc != null) {
                imagesSelected.add(walletDoc.getId());
            }
        }
        if (hasImage(imgRegistration3) && isSelected(imgRegistration3selected)) {
            Wallet.Doc walletDoc = getWalletDoc(Wallet.DocType.REGISTRATION, 3);
            if (walletDoc != null) {
                imagesSelected.add(walletDoc.getId());
            }
        }
        return imagesSelected;
    }

    private List<Long> getTaxImagesSelected() {
        List<Long> imagesSelected = new ArrayList<>();
        if (hasImage(imgTax1) && isSelected(imgTax1selected)) {
            Wallet.Doc walletDoc = getWalletDoc(Wallet.DocType.DEALER_INFO_AND_MAINTENANCE, 1);
            if (walletDoc != null) {
                imagesSelected.add(walletDoc.getId());
            }
        }
        if (hasImage(imgTax2) && isSelected(imgTax2selected)) {
            Wallet.Doc walletDoc = getWalletDoc(Wallet.DocType.DEALER_INFO_AND_MAINTENANCE, 2);
            if (walletDoc != null) {
                imagesSelected.add(walletDoc.getId());
            }
        }
        if (hasImage(imgTax3) && isSelected(imgTax3selected)) {
            Wallet.Doc walletDoc = getWalletDoc(Wallet.DocType.DEALER_INFO_AND_MAINTENANCE, 3);
            if (walletDoc != null) {
                imagesSelected.add(walletDoc.getId());
            }
        }
        return imagesSelected;
    }

    private List<Long> getMaintenanceImagesSelected() {
        List<Long> imagesSelected = new ArrayList<>();
        if (hasImage(imgMaintenance1) && isSelected(imgMaintenance1selected)) {
            Wallet.Doc walletDoc = getWalletDoc(Wallet.DocType.FINANCE_INSURANCE, 1);
            if (walletDoc != null) {
                imagesSelected.add(walletDoc.getId());
            }
        }
        if (hasImage(imgMaintenance2) && isSelected(imgMaintenance2selected)) {
            Wallet.Doc walletDoc = getWalletDoc(Wallet.DocType.FINANCE_INSURANCE, 2);
            if (walletDoc != null) {
                imagesSelected.add(walletDoc.getId());
            }
        }
        if (hasImage(imgMaintenance3) && isSelected(imgMaintenance3selected)) {
            Wallet.Doc walletDoc = getWalletDoc(Wallet.DocType.FINANCE_INSURANCE, 3);
            if (walletDoc != null) {
                imagesSelected.add(walletDoc.getId());
            }
        }
        return imagesSelected;
    }

    private List<Long> getCarImagesSelected() {
        List<Long> imagesSelected = new ArrayList<>();
        if (hasImage(imgCar1) && isSelected(imgCar1selected)) {
            Wallet.Doc walletDoc = getWalletDoc(Wallet.DocType.OTHER, 1);
            if (walletDoc != null) {
                imagesSelected.add(walletDoc.getId());
            }
        }
        if (hasImage(imgCar2) && isSelected(imgCar2selected)) {
            Wallet.Doc walletDoc = getWalletDoc(Wallet.DocType.OTHER, 2);
            if (walletDoc != null) {
                imagesSelected.add(walletDoc.getId());
            }
        }
        if (hasImage(imgCar3) && isSelected(imgCar3selected)) {
            Wallet.Doc walletDoc = getWalletDoc(Wallet.DocType.OTHER, 3);
            if (walletDoc != null) {
                imagesSelected.add(walletDoc.getId());
            }
        }
        return imagesSelected;
    }

    void reset() {
        WalletFragment fragment = (WalletFragment)
                getFragmentManager().findFragmentById(R.id.documents_layoutContainer);

        getFragmentManager().beginTransaction()
                .detach(fragment)
                .attach(fragment)
                .commit();
    }

}
