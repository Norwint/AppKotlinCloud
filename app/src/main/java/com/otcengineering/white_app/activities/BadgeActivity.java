package com.otcengineering.white_app.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.otcengineering.white_app.R;
import com.otcengineering.white_app.components.TitleBar;
import com.otcengineering.white_app.utils.BadgeUtils;

public class BadgeActivity extends BaseActivity {

    private TitleBar titleBar;

    private TextView badgeName, badgeObjective, badgeDescription;
    private ImageView badgeImage;

    public BadgeActivity() {
        super("BadgeActivity");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badge);
        retrieveViews();
        setEvents();
        setData();
    }

    private void retrieveViews() {
        titleBar = findViewById(R.id.badge_titleBar);
        badgeImage = findViewById(R.id.badge_imageBadge);
        badgeName = findViewById(R.id.badge_nameBadge);
        badgeObjective = findViewById(R.id.badge_objectiveBadge);
        badgeDescription = findViewById(R.id.badge_descriptionBadge);
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

    private void setData() {
        badgeName.setText(getIntent().getStringExtra("badge_name"));
        badgeObjective.setText(getIntent().getStringExtra("badge_objective"));
        badgeDescription.setText(getIntent().getStringExtra("badge_description"));
        badgeImage.setImageResource(BadgeUtils.getImageId(getIntent().getStringExtra("badge_name")));
        if (getIntent().getIntExtra("badge_state",0) == 0) {
            badgeObjective.setAlpha(.5f);
            badgeName.setAlpha(.5f);
            badgeImage.setAlpha(.5f);
            badgeDescription.setAlpha(.5f);
        } else {
            badgeObjective.setAlpha(1f);
            badgeName.setAlpha(1f);
            badgeImage.setAlpha(1f);
            badgeDescription.setAlpha(1f);
        }
    }
}
