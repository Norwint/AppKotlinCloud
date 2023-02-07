package com.otcengineering.white_app.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.components.TitleBar;
import com.otcengineering.white_app.utils.Utils;
import com.otcengineering.white_app.utils.images.ImageUtils;

public class PictureActivity extends BaseActivity {

    private TitleBar titleBar;
    private ImageView imgPortrait;

    private Long imageId;

    public PictureActivity() {
        super("PictureActivity");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        retrieveViews();
        retrieveExtras();
        setEvents();
        if (imageId != null) {
            Glide.with(this).load(imageId).into(imgPortrait);
        }
    }

    private void retrieveViews() {
        titleBar = findViewById(R.id.picture_titleBar);
        imgPortrait = findViewById(R.id.picture_imgPortrait);
    }

    private void retrieveExtras() {
        if (getIntent() != null && getIntent().getExtras() != null) {
            imageId = getIntent().getExtras().getLong(Constants.Extras.IMAGE, 0);
        }
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
    }

    private void getImage() {
        String imageFilePathInCache = ImageUtils.getImageFilePathInCache(this, imageId);
        if (imageFilePathInCache != null) {
            showImage(imageFilePathInCache);
        } else {
            downloadImage();
        }
    }

    private void downloadImage() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.show();
        Utils.downloadImage(getApplicationContext(), imageId, img -> {
            progressDialog.dismiss();
            showImage(ImageUtils.getImageFilePathInCache(PictureActivity.this, imageId));
        });
    }

    private void showImage(String imagePath) {
        if (imagePath != null) {
            Glide.with(this)
                    .load(ImageUtils.getImageFromCache(getApplicationContext(), imagePath))
                    .into(imgPortrait);
        } else {
            showImagePlaceholder();
        }
    }

    private void showImagePlaceholder() {
        Glide.with(this)
                .load(R.drawable.photo_placeholder_square)
                .into(imgPortrait);
    }
}
