package com.otcengineering.white_app.activities;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import com.otc.alice.api.model.Community;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.components.TitleBar;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;

/**
 * Created by cenci7
 */

public class InviteActivity extends BaseActivity {

    private TitleBar titleBar;
    private TextView txtName;
    private EditText editMessage;
    private TextView txtChars;
    private Button btnInvite;
    private ImageView imgUser;

    private Community.UserCommunity user;

    public InviteActivity() {
        super("InviteActivity");
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        retrieveViews();
        retrieveExtras();
        setEvents();
    }

    private void retrieveViews() {
        titleBar = findViewById(R.id.invite_titleBar);
        txtName = findViewById(R.id.invite_txtName);
        editMessage = findViewById(R.id.invite_editMessage);
        txtChars = findViewById(R.id.invite_txtChars);
        btnInvite = findViewById(R.id.invite_btnInvite);
        imgUser = findViewById(R.id.info_imgUser);
    }

    private void retrieveExtras() {
        if (getIntent() != null && getIntent().getExtras() != null) {
            user = (Community.UserCommunity) getIntent().getExtras().getSerializable(Constants.Extras.USER);
            txtName.setText(user.getName());
            Glide.with(this).load(user.getImage()).placeholder(R.drawable.user_placeholder_correct).into(imgUser);
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
                    txtChars.setTextColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    txtChars.setText(String.valueOf(charsAvailable));
                    txtChars.setTextColor(getResources().getColor(R.color.black_30_alpha));
                }
                manageButtonSaveUI(charsAvailable);
            }
        });

        btnInvite.setOnClickListener(view -> sendInvitation());
    }

    private void sendInvitation() {
        if (ConnectionUtils.isOnline(getApplicationContext())) {
            new SendFriendRequestTask().execute();
        } else {
            ConnectionUtils.showOfflineToast();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class SendFriendRequestTask extends AsyncTask<Void, Void, Shared.OTCResponse> {
        @Override
        protected Shared.OTCResponse doInBackground(Void... params) {
            try {
                MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());
                Community.SendRequest.Builder builder = Community.SendRequest.newBuilder();
                builder.setUserId(user.getUserId());
                builder.setMessage(editMessage.getText().toString());

                return ApiCaller.doCall(Endpoints.SEND_REQUEST, msp.getBytes("token"), builder.build(), Shared.OTCResponse.class);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Shared.OTCResponse response) {
            super.onPostExecute(response);
            if (response != null && response.getStatus() == Shared.OTCStatus.SUCCESS) {
                MySharedPreferences msp = MySharedPreferences.createSocial(getApplicationContext());
                msp.putBoolean("user_" + user.getUserId(), true);
                showCustomDialog(R.string.invited_correctly, dialogInterface -> finish());
            } else {
                showCustomDialogError();
            }
        }
    }

    private void manageButtonSaveUI(int charsAvailable) {
        boolean enableButton = charsAvailable > 0 && !editMessage.getText().toString().isEmpty();
        btnInvite.setTextColor(enableButton ? getResources().getColor(R.color.colorPrimary) : getResources().getColor(R.color.layout_border));
        btnInvite.setEnabled(enableButton);
    }
}
