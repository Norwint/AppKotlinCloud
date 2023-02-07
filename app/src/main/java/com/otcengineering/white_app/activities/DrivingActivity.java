package com.otcengineering.white_app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.otc.alice.api.model.Community;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.components.TitleBar;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.tasks.TypedTask;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.Utils;
import com.otcengineering.white_app.utils.interfaces.TypedCallback;

import javax.annotation.Nonnull;

public class DrivingActivity extends EventActivity {
    private TextView text;
    private ImageView carPhoto;
    private String phone;
    private TitleBar titleBar;

    public DrivingActivity() {
        super("DrivingActivity");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driving);

        text = findViewById(R.id.text);
        carPhoto = findViewById(R.id.carPhoto);
        titleBar = findViewById(R.id.driving_titlebar);

        String txt = getIntent().getExtras().getString("Text");
        if (txt != null) {
            text.setText(Utils.translateRouteText(txt));
        }

        TypedTask<Community.DealerResponse> getDealerInfo = new TypedTask<>(Endpoints.GET_DEALER, null, true, Community.DealerResponse.class, new TypedCallback<Community.DealerResponse>() {
            @Override
            public void onSuccess(@Nonnull Community.DealerResponse value) {
                phone = value.getPhone();
            }

            @Override
            public void onError(Shared.OTCStatus status, String str) {

            }
        });
        getDealerInfo.execute();
        configureTitleBar("Thank you for driving");

        Long imgId = MySharedPreferences.createDashboard(this).getLong("CarPictureId");
        setCarPicture(imgId);
    }

    public void callDealer(View view) {
        String uri = "tel:" + phone;
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse(uri));
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isTaskRoot()) {
            Intent intent = new Intent(this, Home2Activity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    public void configureTitleBar(String stringRes) {
        titleBar.setTitle(stringRes);
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

    private void setCarPicture(Long image) {
        Glide.with(this)
                .load(image)
                .placeholder(R.drawable.car_otc)
                .apply(new RequestOptions().transform(new RoundedCorners(20)))
                .into(carPhoto);
    }
}
