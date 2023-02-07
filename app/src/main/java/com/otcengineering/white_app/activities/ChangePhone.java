package com.otcengineering.white_app.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.otc.alice.api.model.Shared;
import com.otc.alice.api.model.Welcome;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.utils.CloudErrorHandler;

import java.util.Locale;

public class ChangePhone extends BaseActivity {
    public ChangePhone() {
        super("ChangePhone");
    }

    private Handler m_timeHandler;
    private Runnable m_run;
    private long m_timeEnd;
    private boolean m_finished = false;
    private Button m_resendButton;
    private ProgressDialog m_dialog;
    private EditText m_code;
    private String phone, nickname;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_sign_up);

        phone = getIntent().getExtras().getString("phone");
        nickname = getIntent().getExtras().getString("nick");

        TextView txt = findViewById(R.id.txtUsername);
        txt.setText(String.format("%s %s", txt.getText().toString(), phone));

        m_dialog = new ProgressDialog(this);
        m_dialog.setCancelable(false);
        m_dialog.setCanceledOnTouchOutside(false);

        m_code = findViewById(R.id.etUsername);

        findViewById(R.id.btnLoading).setOnClickListener(v -> confirmSMS(m_code.getText().toString()));

        m_resendButton = findViewById(R.id.getMessage);
        m_resendButton.setOnClickListener(v ->
        {
            if (m_finished)
            {
                resendSMS();
                createHandler();
            }
        });

        m_resendButton.setText(Html.fromHtml(String.format("<font color='#99999e'>%s / 00:59</font>", getString(R.string.resend_sms))));

        findViewById(R.id.changePhone).setVisibility(View.GONE);

        createHandler();
    }

    private void confirmSMS(String s) {
        if (s.length() != 4) {
            final Context ctx = getApplicationContext();
            Toast.makeText(ctx, R.string.activation_4_digits, Toast.LENGTH_LONG).show();
            return;
        }

        m_dialog.setTitle(R.string.loading);
        m_dialog.show();

        new Thread(() -> {
            try {
                Welcome.UserActivation.Builder builder = Welcome.UserActivation.newBuilder();
                builder.setUsername(nickname);
                builder.setSecret(s);
                Shared.OTCResponse response = ApiCaller.doCall(Endpoints.ACTIVATE, builder.build(), Shared.OTCResponse.class);

                int res = response.getStatusValue();
                m_dialog.dismiss();

                if (res == 1) {
                    runOnUiThread(this::onBackPressed);
                } else {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Server error: " + CloudErrorHandler.handleError(res), Toast.LENGTH_LONG).show());
                }
            } catch (Exception e) {
            }
        }, "ChangePhoneThread").start();
    }

    private void resendSMS() {
        m_dialog.setTitle(R.string.loading);
        m_dialog.show();
        new Thread(() -> {
            try {
                Welcome.SmsResend.Builder builder = Welcome.SmsResend.newBuilder();
                builder.setUsername(nickname);
                builder.setUsername(phone);
                Shared.OTCResponse response = ApiCaller.doCall(Endpoints.RESEND_SMS, builder.build(), Shared.OTCResponse.class);
                runOnUiThread(() -> m_dialog.hide());
                if (response == null) {
                    return;
                }

                if (response.getStatusValue() == 1)
                {
                    runOnUiThread(this::createHandler);
                }
            } catch (Exception e) {
                //Log.e(TAG, "Exception", e);
            }
        }, "ChangePhoneThread2").start();
    }

    @Override
    protected void onDestroy() {
        m_timeHandler.removeCallbacks(m_run);
        super.onDestroy();
    }

    private void createHandler() {
        m_timeEnd = System.currentTimeMillis() + 60000;
        m_finished = false;
        m_timeHandler = new Handler();
        m_run = () ->
        {
            long diff = m_timeEnd - System.currentTimeMillis();
            if (diff <= 0)
            {
                m_finished = true;
            }
            else
            {
                int seconds = (int) (diff / 1000);
                m_resendButton.setText(Html.fromHtml(String.format(Locale.getDefault(), "<font color='#99999e'>%s / 00:%02d</font>", getString(R.string.resend_sms), seconds)));
                m_timeHandler.postDelayed(m_run, 500);
            }
        };
        m_timeHandler.postDelayed(m_run, 500);
    }
}
