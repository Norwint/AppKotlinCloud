package com.otcengineering.white_app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.otc.alice.api.model.Community;
import com.otc.alice.api.model.General;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.adapter.ConditionAdapter;
import com.otcengineering.white_app.components.TitleBar;
import com.otcengineering.white_app.utils.PrefsManager;

public class ConditionDescriptionActivity extends BaseActivity {
    private String m_phone;

    private TextView title, description, date, text;
    private ImageView img, condition;

    public ConditionDescriptionActivity() {
        super("ConditionDescription");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_condition_description);

        getViews();
        setData();

        Community.DealerResponse dealerInfo = PrefsManager.getInstance().getDealerInfo(this);
        m_phone = dealerInfo.getPhone();
    }

    private void setData() {
        // Get Extras
        Bundle extras = getIntent().getExtras();

        int item = extras.getInt("Item");
        String desc = extras.getString("Desc");
        int state = extras.getInt("State");
        String date = extras.getString("Date");

        ConditionAdapter.Conditions conds = ConditionAdapter.Conditions.forNumber(item);
        General.SignalMode sm = General.SignalMode.forNumber(state);

        String descr = "";
        int icon = ConditionAdapter.getEnabledCondition(conds);

        if (sm != null) {
            switch (sm) {
                case DISABLED:
                    descr = getResources().getString(R.string.not_available);
                    break;
                case WORKING:
                    descr = "";
                    break;
                case PROBLEM:
                    descr = getResources().getString(R.string.service_required);
                    break;
            }
        } else {
            descr = "";
        }

        text.setText(desc);
        title.setText(conds.getTitle());
        description.setText(descr);
        if (descr.isEmpty()) {
            description.setVisibility(View.GONE);
        }
        Glide.with(this).load(icon).into(img);
        Glide.with(this).load(ConditionAdapter.getResourceForSignalMode(sm)).into(condition);
        this.date.setText(date);
    }

    private void getViews() {
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        date = findViewById(R.id.data);
        text = findViewById(R.id.text);

        img = findViewById(R.id.image);
        condition = findViewById(R.id.condition);

        TitleBar titleBar = findViewById(R.id.conddesc_titleBar);

        titleBar.setListener(new TitleBar.TitleBarListener() {
            @Override
            public void onLeftClick() {
                finish();
            }

            @Override
            public void onRight1Click() {

            }

            @Override
            public void onRight2Click() {

            }
        });
    }

    public void callToDealer(View sender) {
        String uri = "tel:" + m_phone;
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse(uri));
        startActivity(intent);
    }
}
