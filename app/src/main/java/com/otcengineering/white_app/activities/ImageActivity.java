package com.otcengineering.white_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageButton;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.otcengineering.apible.Crypt;
import com.otcengineering.white_app.R;

import java.io.IOException;
import java.util.regex.Pattern;

public class ImageActivity extends BaseActivity {
    public static final int IMAGE_RESULT = 690;
    private SurfaceView mSurfaceView;
    private ImageButton imageView2;

    public ImageActivity() {
        super("QR Activity");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        mSurfaceView = findViewById(R.id.surfaceView);
        startViews();
    }

    private void startViews() {
        BarcodeDetector detector = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.QR_CODE).build();

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float ratio = ((float)metrics.heightPixels / (float)metrics.widthPixels);

        CameraSource src = new CameraSource.Builder(this, detector).setRequestedPreviewSize((int) (ratio * 1000), 1000).setAutoFocusEnabled(true).build();

        imageView2.setOnClickListener(v -> {
            src.takePicture(() -> {}, bytes -> {
                
            });
        });

        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    src.start(mSurfaceView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                src.stop();
            }
        });
    }
}
