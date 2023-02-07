package com.otcengineering.white_app.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.otc.alice.api.model.Shared;
import com.otc.alice.api.model.Welcome;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.components.DialogMultiple;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.tasks.TypedTask;
import com.otcengineering.white_app.utils.CloudErrorHandler;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.interfaces.TypedCallback;

import javax.annotation.Nonnull;

public class ChangePasswordActivity extends AppCompatActivity {
    private String oldPass;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        oldPass = getIntent().getStringExtra("OldPass");

        pd = new ProgressDialog(this);
        pd.setMessage(getString(R.string.loading));
        pd.setCancelable(false);

        EditText passwordA, passwordB;

        passwordA = findViewById(R.id.new_pass);
        passwordB = findViewById(R.id.retype_pass);

        Button saveBtn = findViewById(R.id.save);
        saveBtn.setOnClickListener(v -> {
            pd.show();
            String passA = passwordA.getText().toString();
            String passB = passwordB.getText().toString();

            if (passA.equals(passB)) {
                Welcome.PasswordUpdate pu = Welcome.PasswordUpdate.newBuilder()
                        .setNewPassword(passA)
                        .setOldPassword(oldPass)
                        .build();
                TypedTask<Shared.OTCResponse> changePass = new TypedTask<>(Endpoints.PASSWORD_UPDATE, pu, true, Shared.OTCResponse.class, new TypedCallback<Shared.OTCResponse>() {
                    @Override
                    public void onSuccess(@Nonnull Shared.OTCResponse value) {
                        DialogMultiple dm = new DialogMultiple(ChangePasswordActivity.this);
                        dm.setTitle(getString(R.string.change_password));
                        dm.setDescription(CloudErrorHandler.handleError(value.getStatus()));
                        dm.addButton(getString(R.string.ok), () -> {
                            pd.dismiss();
                            MySharedPreferences.createLogin(ChangePasswordActivity.this).remove("token");
                            finish();
                        });
                        dm.show();
                    }

                    @Override
                    public void onError(@Nonnull Shared.OTCStatus status, @Nullable String message) {
                        pd.dismiss();
                        showError(getString(R.string.server_error), CloudErrorHandler.handleError(status));
                    }
                });
                changePass.execute();
            } else {
                pd.dismiss();
                showError(getString(R.string.password_error), getString(R.string.retype_pass_error));
            }
        });
    }

    private void showError(String title, String message) {
        DialogMultiple dm = new DialogMultiple(this);

        dm.setTitle(title);
        dm.setDescription(message);
        dm.addButton(getString(R.string.ok), () -> {});

        dm.show();
    }
}
