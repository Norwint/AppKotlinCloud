package com.otcengineering.white_app.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.otc.alice.api.model.Shared.OTCResponse;
import com.otc.alice.api.model.Welcome;
import com.otc.alice.api.model.Welcome.UserActivation;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.utils.CloudErrorHandler;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.NetworkManager;
import com.otcengineering.white_app.utils.Utils;

import java.util.Locale;

public class LoadingSignUpActivity extends EventActivity {

    private Button changePhone;
    private Button getNew;
    private EditText nick;
    private TextView textoLoading;
    private String title, phoneChanged;

    int finalizado = 0;

    private ProgressDialog progressDialogRegister;

    public LoadingSignUpActivity() {
        super("LoadingSignUpActivity");
    }
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_sign_up);
        nick = findViewById(R.id.etUsername);
        getNew = findViewById(R.id.getMessage);
        changePhone = findViewById(R.id.changePhone);
        Button loading = findViewById(R.id.btnLoading);
        loading.setOnClickListener(v -> {
            if (ConnectionUtils.isOnline(getApplicationContext())) {
                new RetrieveFeedTask().execute
                        (MySharedPreferences.createLogin(getApplicationContext()).getString("Nick"),
                                nick.getText().toString());
            } else {
                ConnectionUtils.showOfflineToast();
            }
        });

        textoLoading = findViewById(R.id.txtUsername);

        title = textoLoading.getText().toString();

        MySharedPreferences pref = MySharedPreferences.createLogin(this);
        textoLoading.setText(String.format("%s\n%s", title, pref.getString("Tlf")));

        timer = new CountDownTimer(59000, 1000) {
            long test = 0;
            public void onTick(long millisUntilFinished) {
                test = millisUntilFinished / 1000;
                getNew.setText(Html.fromHtml("<font color='#99999e'>" + getString(R.string.resend_sms) + " / 00:" + String.format(Locale.getDefault(), "%02d", test) + "</font>"));
                changePhone.setText(Html.fromHtml("<font color='#99999e'>" + getString(R.string.change_phone) + "</font>"));
            }

            public void onFinish() {
                getNew.setText(Html.fromHtml("<font color='#002d7a'>" + getString(R.string.resend_sms) + "</font>"));
                changePhone.setText(Html.fromHtml("<font color='#002d7a'>" + getString(R.string.change_phone) + "</font>"));
                finalizado = 1;
            }
        }.start();

        changePhone.setOnClickListener(arg0 -> {
            if (finalizado == 1) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoadingSignUpActivity.this);
                builder.setTitle("Phone");

                final EditText input = new EditText(LoadingSignUpActivity.this);
                input.setInputType(InputType.TYPE_CLASS_NUMBER);

                int color;

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    color = ContextCompat.getColor(getApplicationContext(), R.color.colorBlue);
                } else {
                    color = getColor(R.color.colorBlue);
                }

                input.setTextColor(color);
                input.setBackgroundTintList(ColorStateList.valueOf(color));

                builder.setView(input);

                builder.setPositiveButton(getResources().getString(R.string.ok), (dialog, which) -> {
                    String phone = input.getText().toString();
                    if (!com.otcengineering.white_app.utils.Utils.checkPhone(phone)) {
                        showError(getResources().getString(R.string.server_error), getResources().getString(R.string.malformed_phone));
                    } else {
                        phoneChanged = "+" + input.getText().toString();
                        if (ConnectionUtils.isOnline(getApplicationContext())) {
                            new ReSendSMS().execute();
                        } else {
                            ConnectionUtils.showOfflineToast();
                        }
                    }
                });

                builder.setNegativeButton(getResources().getString(R.string.cancelar), (dialog, which) -> dialog.cancel());

                AlertDialog a = builder.create();
                a.show();
                Button buttonPositive = a.getButton(DialogInterface.BUTTON_POSITIVE);
                buttonPositive.setTextColor(color);


                Button buttonNegative = a.getButton(DialogInterface.BUTTON_NEGATIVE);
                buttonNegative.setTextColor(color);
            }
        });

        getNew.setOnClickListener(arg0 -> {
            if (finalizado == 1) {
                timer.start();
                finalizado = 0;
                if (ConnectionUtils.isOnline(getApplicationContext())) {
                    new ReSendSMS().execute();
                } else {
                    ConnectionUtils.showOfflineToast();
                }
            }
        });

        progressDialogRegister = new ProgressDialog(LoadingSignUpActivity.this);
        progressDialogRegister.setCancelable(false);
        progressDialogRegister.setCanceledOnTouchOutside(false);
    }

    class ReSendSMS extends AsyncTask<String, String, OTCResponse> {
        @Override
        protected OTCResponse doInBackground(String... params) {
            try {
                Welcome.SmsResend.Builder ua = Welcome.SmsResend.newBuilder();
                MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());
                ua.setUsername(msp.getString("Nick"));

                if (phoneChanged != null) {
                    if (!phoneChanged.isEmpty()) {
                        ua.setPhone(phoneChanged);
                    }
                }

                return ApiCaller.doCall(Endpoints.RESEND_SMS, ua.build(), OTCResponse.class);
            } catch (Exception e) {
                //Log.e("LoadingSignUp", "Exception", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(OTCResponse result) {
            if (result == null) {
                return;
            }
            if (result.getStatusValue() != 1) {
                new AlertDialog.Builder(LoadingSignUpActivity.this)
                        .setTitle(getResources().getString(R.string.server_error))
                        .setMessage(CloudErrorHandler.handleError(result.getStatus().getNumber()))
                        .setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.cancelar), (dialog, which) -> {
                            // Whatever...
                        }).show();
            } else {
                if (phoneChanged != null) {
                    textoLoading.setText(String.format("%s\n%s", title, phoneChanged));
                }
                timer.start();
                finalizado = 0;
            }
        }
    }

    class RetrieveFeedTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialogRegister.setTitle(getResources().getString(R.string.loading_title));
            progressDialogRegister.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                UserActivation.Builder ua = UserActivation.newBuilder();

                ua.setUsername(params[0]);
                ua.setSecret(params[1]);

                OTCResponse response = ApiCaller.doCall(Endpoints.ACTIVATE, ua.build(), OTCResponse.class);
                return String.valueOf(response);
            } catch (Exception e) {
                //Log.e("LoadingSignUp", "Exception", e);
                return String.valueOf(e);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            String[] parts = result.split("\n");
            //INVALID_PHONE_ACTIVATION_CODE
            progressDialogRegister.dismiss();

            if (parts[0].equals("status: SUCCESS")) {
                progressDialogRegister.show();
                if (ConnectionUtils.isOnline(getApplicationContext())) {
                    Utils.runOnBackThread(() -> {
                        NetworkManager.login(LoadingSignUpActivity.this, null, null);
                        Utils.runOnMainThread(() -> {
                            Intent intent = new Intent(LoadingSignUpActivity.this, SecondSignUpActivity.class);

                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

                            startActivity(intent);
                        });
                    });
                } else {
                    ConnectionUtils.showOfflineToast();
                }
            } else if (parts[0].equals("status: REQUIRED_FIELDS_MISSING")) {
                if(parts.length >= 2) {
                    if (parts[1].equals("message: \"username\"")) {
                        showError(getResources().getString(R.string.username_error), parts[1]);
                    } else if (parts[1].equals("message: \"secret\"")) {
                        showError(getResources().getString(R.string.secret_error), getResources().getString(R.string.secret_number_empty));
                    }
                }
            } else if (parts[0].equals("status: INVALID_USERNAME_OR_PASSWORD")) {
                showError(getResources().getString(R.string.username_password_error),parts[0]);
            } else if (nick.getText().toString().length()==0){
                showError(getResources().getString(R.string.secret_error),getResources().getString(R.string.secret_number_empty));
            }
            else {
                showError(getResources().getString(R.string.secret_error),getResources().getString(R.string.secret_number_incorrect));
            }
        }
    }


    class SetToken extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                Welcome.Login.Builder loginB = Welcome.Login.newBuilder();
                MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());
                loginB.setUsername(msp.getString("Nick"));
                loginB.setPassword(msp.getString("Pass"));
                loginB.setMobileIMEI(Utils.getImei());

                Welcome.LoginResponse crr = ApiCaller.doCall(Endpoints.LOGIN, loginB.build(), Welcome.LoginResponse.class);
                return crr.getApiToken();
            } catch (Exception e) {
                //Log.e("LoadingSignUp", "Exception", e);
                return String.valueOf(e);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialogRegister.dismiss();
            if (result.length() > 2) {
                MySharedPreferences.createLogin(getApplicationContext()).putString("token", result);
                Intent intent = new Intent(LoadingSignUpActivity.this, SecondSignUpActivity.class);

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(intent);
            }
        }
    }

    private void showError(String title, String message) {
        new AlertDialog.Builder(LoadingSignUpActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.close), (dialog, which) -> {
                    // Whatever...
                }).show();
    }
}
