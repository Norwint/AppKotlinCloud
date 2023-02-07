package com.otcengineering.white_app.activities;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;

import com.otc.alice.api.model.Shared;
import com.otc.alice.api.model.Welcome;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.utils.CloudErrorHandler;
import com.otcengineering.white_app.utils.ConnectionUtils;

public class SendPassActivity extends EventActivity {

    private EditText email;

    public SendPassActivity() {
        super("SendPassActivity");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_pass);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        email = findViewById(R.id.etEmail);
        Button sendPass = findViewById(R.id.btnSendPass);
        sendPass.setOnClickListener(v -> {
            if (ConnectionUtils.isOnline(getApplicationContext())) {
                new RetrieveFeedTask().execute(email.getText().toString());
            } else {
                ConnectionUtils.showOfflineToast();
            }
        });
    }

    class RetrieveFeedTask extends AsyncTask<String, String, Shared.OTCResponse> {
        @Override
        protected Shared.OTCResponse doInBackground(String... params) {
            try {
                Welcome.PasswordRecovery.Builder ua = Welcome.PasswordRecovery.newBuilder();
                ua.setEmail(params[0]);
                return ApiCaller.doCall(Endpoints.PASSWORD_RECOVERY, ua.build(), Shared.OTCResponse.class);
            } catch (Exception e) {
                //Log.e("SendPassActivity", "Exception", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Shared.OTCResponse result) {
            String title;
            String message;
            String button;

            if (result.getStatusValue() == 1) {
                title = "";
                message = getString(R.string.send_pass);
                button = getResources().getString(R.string.ok);
            } else {
                title = getResources().getString(R.string.server_error);
                message = CloudErrorHandler.handleError(result.getStatus());
                button = getResources().getString(R.string.close);
            }

            new AlertDialog.Builder(SendPassActivity.this)
                    .setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(button, (dialog, which) -> {
                        if(result.getStatus() == Shared.OTCStatus.SUCCESS) {
                            finish();
                        }
                    }).show();


        }
    }
}
