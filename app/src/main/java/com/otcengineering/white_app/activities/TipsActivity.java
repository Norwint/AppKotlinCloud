package com.otcengineering.white_app.activities;

import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hrskrs.instadotlib.InstaDotView;
import com.otc.alice.api.model.MyDrive;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.components.TitleBar;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.tasks.GenericTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class TipsActivity extends EventActivity {

    private TitleBar titleBar;
    private CircleImageView imageView;

    private float x1;

    private int pages;
    private int currentPage;
    public InstaDotView instaDotView;
    private MyDrive.TipsResponse tips;
    private MyDrive.DriveType type;

    public TipsActivity() {
        super("TipsActivity");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tips);
        retrieveViews();
        retrieveExtras();
        setEvents();
        currentPage = 0;

        try {
            Glide.with(getApplicationContext()).load(R.drawable.car_otc).into(imageView);
        } catch (Exception ignored) {
        }

        MyDrive.Tips.Builder TipGet = MyDrive.Tips.newBuilder();
        TipGet.setType(type);
        GenericTask getTips = new GenericTask(Endpoints.TIPS, TipGet.build(), true, otcResponse -> {
            if (otcResponse.getStatus() == Shared.OTCStatus.SUCCESS) {
                MyDrive.TipsResponse rsp = otcResponse.getData().unpack(MyDrive.TipsResponse.class);
                setDataTips(rsp);
            }
        });
        getTips.execute();
    }

    private void retrieveViews() {
        titleBar = findViewById(R.id.tips_titleBar);
        imageView = findViewById(R.id.imageTip);
        instaDotView = findViewById(R.id.instadot);
    }

    private void retrieveExtras() {
        if (getIntent() != null && getIntent().getExtras() != null) {
            String title = "";
            int tp = getIntent().getExtras().getInt("type");
            switch (tp) {
                case 1: type = MyDrive.DriveType.MILEAGE; title = getString(R.string.mileage); break;
                case 2: type = MyDrive.DriveType.ECO; title = getString(R.string.eco_driving); break;
                case 3: type = MyDrive.DriveType.SAFETY; title = getString(R.string.safety_driving); break;
            }
            titleBar.setTitle(String.format("%s %s", title, getResources().getString(R.string.techniques)));

        } else {
            type = MyDrive.DriveType.MILEAGE;
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

    public boolean onTouchEvent(MotionEvent touchevent) {
        switch (touchevent.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                x1 = touchevent.getX();
                break;
            }
            case MotionEvent.ACTION_UP: {
                float x2 = touchevent.getX();
                if (x1 < x2) {
                    if (currentPage > 0) {
                        currentPage--;
                        setDataTips(null);
                        instaDotView.onPageChange(currentPage);
                    }
                } else if (x1 > x2) {
                    if (currentPage < (pages - 1)) {
                        currentPage++;
                        setDataTips(null);
                        instaDotView.onPageChange(currentPage);
                    }
                }
                break;
            }
        }
        return false;
    }

    public void setDataTips(MyDrive.TipsResponse response) {
        if (response != null) {
            if (tips == null) {
                tips = response;
                instaDotView.setNoOfPages(response.getTipsCount());
                pages = response.getTipsCount();
            }
        }

        TextView title = findViewById(R.id.textView9);
        TextView content = findViewById(R.id.textView10);

        if (tips != null) {
            title.setText(tips.getTips(currentPage).getName());
            content.setText(tips.getTips(currentPage).getText());

            /*if (ConnectionUtils.isOnline(getApplicationContext())) {
                long imageId = tips.GetTerms(currentPage).getImageId();
                ImageUtils.getImage(this, imageId, new Callback<String>() {
                    @Override
                    public void onSuccess(String success) {
                        Glide.with(getBaseContext()).load(ImageUtils.getImageFromCache(TipsActivity.this, success)).into(imageView);
                    }

                    @Override
                    public void onError(Shared.OTCStatus status) {
                        Glide.with(TipsActivity.this).load(R.drawable.car_placeholder).into(imageView);
                    }
                });
            } else {
                ConnectionUtils.showOfflineToast();
            }*/
        }
    }

}
