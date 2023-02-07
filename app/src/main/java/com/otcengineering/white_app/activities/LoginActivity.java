package com.otcengineering.white_app.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.app.ActionBar;

import com.otc.alice.api.model.Shared;
import com.otc.alice.api.model.Shared.OTCStatus;
import com.otc.alice.api.model.Welcome;
import com.otc.alice.api.model.Welcome.LoginResponse;
import com.otcengineering.apible.OtcBle;
import com.otcengineering.white_app.BuildConfig;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.components.DialogMultiple;
import com.otcengineering.white_app.network.ConfigurationNetwork;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.tasks.GenericTask;
import com.otcengineering.white_app.tasks.TypedTask;
import com.otcengineering.white_app.utils.CloudErrorHandler;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.Utils;
import com.otcengineering.white_app.utils.interfaces.TypedCallback;

import java.util.Calendar;

import javax.annotation.Nonnull;

public class LoginActivity extends EventActivity {
    private TextView error1;
    private TextView error2;
    private EditText username;
    private EditText password;
    private ProgressDialog progressDialogRegister;

    public LoginActivity() {
        super("LoginActivity");
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_sign_up_v2);
        progressDialogRegister = new ProgressDialog(LoginActivity.this);
        progressDialogRegister.setCancelable(false);
        progressDialogRegister.setCanceledOnTouchOutside(false);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Regular.ttf");
        Typeface face1 = Typeface.createFromAsset(getAssets(), "fonts/Montserrat-Regular.ttf");

        TextView sign = findViewById(R.id.txtSignUp);

        sign.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });
        username = findViewById(R.id.etUsername);
        password = findViewById(R.id.etPassword);
        error1 = findViewById(R.id.txtError1);
        error2 = findViewById(R.id.txtError2);
        Button login = findViewById(R.id.btnSignUp);
        TextView txtUser = findViewById(R.id.txtUser);
        TextView txtPass = findViewById(R.id.txtPass);
        TextView obligation = findViewById(R.id.txtLoginDes);
        TextView forgot = findViewById(R.id.txtForgot);

        TextView btnLogin = findViewById(R.id.txtLogin);

        txtUser.setTypeface(face);
        username.setTypeface(face);
        txtPass.setTypeface(face);
        password.setTypeface(face);
        error1.setTypeface(face);
        error2.setTypeface(face);
        forgot.setTypeface(face);
        obligation.setTypeface(face);
        sign.setTypeface(face);
        btnLogin.setTypeface(face);
        login.setTypeface(face1);

        if (BuildConfig.DEBUG) {
            obligation.setOnClickListener(v -> Utils.executeSequence());
        }

        try {
            OtcBle.getInstance().setContext(getApplicationContext());
            OtcBle.getInstance().createBleLibrary();
        } catch (Exception e){
            //Log.e("LoginSignUpV2", "Exception", e);
        }

        login.setOnClickListener(v -> login(new TypedCallback<Shared.OTCResponse>() {
            @Override
            public void onSuccess(@Nonnull Shared.OTCResponse value) {
                TypedTask<Welcome.PasswordIsTemporal> checkPasswordTemporal = new TypedTask<>(Endpoints.PASSWORD_IS_TEMPORAL, null, true,
                        Welcome.PasswordIsTemporal.class, new TypedCallback<Welcome.PasswordIsTemporal>() {
                    @Override
                    public void onSuccess(@Nonnull Welcome.PasswordIsTemporal value) {
                        if (value.getIsTemporal()) {
                            showPopupChangePassword();
                        } else {
                            checkUserStatus();
                        }
                    }

                    @Override
                    public void onError(@Nonnull Shared.OTCStatus status, @Nullable String message) {
                        if (status == Shared.OTCStatus.USER_PROFILE_REQUIRED) {
                            checkUserStatus();
                        } else {
                            pd.dismiss();
                            showError(getString(R.string.server_error), CloudErrorHandler.handleError(status));
                        }
                    }
                });
                checkPasswordTemporal.execute();
            }

            @Override
            public void onError(@NonNull Shared.OTCStatus status, String str) {
                if (status != Shared.OTCStatus.NEW_MOBILE) {
                    showError(getString(R.string.server_error), CloudErrorHandler.handleError(status));
                }
            }
        }));

        forgot.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SendPassActivity.class);
            startActivity(intent);
        });

        username.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            error1.setVisibility(View.INVISIBLE);

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (username.getRight() - username.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    // your action here
                    username.setText("");
                    error1.setVisibility(View.INVISIBLE);
                    return true;
                }
            }
            return false;
        });

        password.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            error2.setVisibility(View.INVISIBLE);

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (password.getRight() - password.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    // your action here
                    password.setText("");
                    error2.setVisibility(View.INVISIBLE);
                    return true;
                }
            }
            return false;
        });
    }

    private void showPopupChangePassword() {
        pd.dismiss();
        DialogMultiple dm = new DialogMultiple(this);
        dm.setDescription(getString(R.string.pass_temp_one_hour));
        dm.addButton(getString(R.string.change_password), () -> {
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            intent.putExtra("OldPass", password.getText().toString());
            startActivity(intent);
        });
        dm.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == 1234) {
            ConfigurationNetwork.fetchBluetoothConfiguration();

            MySharedPreferences.createLogin(getApplicationContext()).putBoolean("DeleteInfo", mDeleteInfo);
            MySharedPreferences.createLogin(getApplicationContext()).putBoolean("NewPhone", true);

            Intent intent = new Intent(LoginActivity.this, SecondSignUpActivity.class);
            startActivity(intent);
        }
    }

    private boolean mDeleteInfo;

    private void showPopupChangePasswordWithChangeMobile(boolean deleteInfo) {
        mDeleteInfo = deleteInfo;
        pd.dismiss();
        DialogMultiple dm = new DialogMultiple(this);
        dm.setDescription(getString(R.string.pass_temp_one_hour));
        dm.addButton(getString(R.string.change_password), () -> {
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            intent.putExtra("OldPass", password.getText().toString());
            startActivityForResult(intent, 1234);
        });
        dm.show();
    }

    private void checkUserStatus() {
        GenericTask trms = new GenericTask(Endpoints.USER_TERMS, null, true, otcResponse1 -> {
            MySharedPreferences msp = MySharedPreferences.createLogin(LoginActivity.this);
            pd.dismiss();
            switch (otcResponse1.getStatus()) {
                case USER_PROFILE_REQUIRED: showError(getResources().getString(R.string.server_error), getString(R.string.user_profile_required)); break;
                case REQUIRED_FIELDS_MISSING: showError("Error", getResources().getString(R.string.required_field_missing));
                case INVALID_USERNAME_OR_PASSWORD: /*showError(getResources().getString(R.string.username_password_error), getResources().getString(R.string.username_password_invalid));*/ break;
                case USER_DISABLED: {
                    msp.putString("Nick", username.getText().toString().replace(" ", ""));
                    msp.putString("Pass", password.getText().toString());

                    //showError(getResources().getString(R.string.user_error), String.valueOf(R.string.user_disabled));

                    Intent intent = new Intent(LoginActivity.this, LoadingSignUpActivity.class);
                    startActivity(intent);

                    break;
                }
                case USER_BLOCKED: showError(getString(R.string.user_blocked), getString(R.string.contact_call_center)); break;
                case USER_NOT_ENABLED: {
                    msp.putString("Nick", username.getText().toString().replace(" ", ""));
                    msp.putString("Pass", password.getText().toString());
                    Intent intent = new Intent(LoginActivity.this, SecondSignUpActivity.class);
                    startActivity(intent);

                    break;
                }
                case SUCCESS: {
                    Welcome.DeviceSpecs ds = Welcome.DeviceSpecs.newBuilder()
                            .setAppVersion(BuildConfig.VERSION_NAME)
                            .setMobileSO(String.format("Android %s", Build.VERSION.RELEASE))
                            .setMobileIMEI(Utils.getImei())
                            .build();
                    GenericTask gt = new GenericTask(Endpoints.DEVICE_SPECS, ds, true, (devSpecs) -> {});
                    gt.execute();

                    Intent intent = new Intent(LoginActivity.this, Home2Activity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

                    String macBLE = msp.getString("macBLE");
                    if (!macBLE.equals("")) {
                        OtcBle.getInstance().setDeviceMac(macBLE);
                        try {
                            OtcBle.getInstance().connect();
                        } catch (NullPointerException e) {
                            //Log.e("LoginSignUpV2", "Exception", e);
                        }
                    }
                    startActivity(intent);
                    break;
                }
                default: {
                    showError("Server Error", CloudErrorHandler.handleError(otcResponse1.getStatus().getNumber())); break;
                }
            }
        });
        trms.execute();
    }

    private ProgressDialog pd;
    private void login(TypedCallback<Shared.OTCResponse> onResponse) {
        if (username.getText().toString().isEmpty() || password.getText().toString().isEmpty()) {
            onResponse.onError(Shared.OTCStatus.INVALID_USERNAME_OR_PASSWORD, "");
            return;
        }
        Utils.runOnMainThread(() -> {
            pd = new ProgressDialog(LoginActivity.this);
            pd.setMessage(getString(R.string.loading));
            pd.show();
        });
        if (ConnectionUtils.isOnline(getApplicationContext())) {
            Welcome.Login pre = Welcome.Login.newBuilder()
                    .setMobileIMEI(Utils.getImei())
                    .setUsername(username.getText().toString().replace(" ", ""))
                    .setPassword(password.getText().toString())
                    .build();
            GenericTask loginTask = new GenericTask(Endpoints.LOGIN, pre, false, otcResponse -> {
                MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());

                if (otcResponse.getStatus() == Shared.OTCStatus.SUCCESS) {
                    LoginResponse lr = otcResponse.getData().unpack(LoginResponse.class);
                    msp.putBytes("token", lr.getApiTokenBytes().toByteArray());
                    msp.putString("Nick", username.getText().toString().replace(" ", ""));
                    msp.putString("Pass", password.getText().toString());
                    msp.putString("macBLE", lr.getMac());
                    msp.putLong("token_date", Calendar.getInstance().getTime().getTime());
                    msp.putLong("ID", lr.getUserId());

                    ConfigurationNetwork.fetchBluetoothConfiguration();
                    onResponse.onSuccess(otcResponse);
                } else if (otcResponse.getStatus() == Shared.OTCStatus.INVALID_USERNAME_OR_PASSWORD) {
                    //showError(getResources().getString(R.string.username_password_error), getResources().getString(R.string.username_password_invalid));
                    onResponse.onError(Shared.OTCStatus.INVALID_USERNAME_OR_PASSWORD, otcResponse.getMessage());
                } else if (otcResponse.getStatus() == Shared.OTCStatus.NEW_MOBILE) {
                    onResponse.onError(Shared.OTCStatus.NEW_MOBILE, otcResponse.getMessage());
                    pd.dismiss();
                    showPopupNewMobile();
                } else if (otcResponse.getStatus() == Shared.OTCStatus.USER_DISABLED) {
                    msp.putString("Nick", username.getText().toString().replace(" ", ""));
                    msp.putString("Pass", password.getText().toString());

                    pd.dismiss();
                    Intent intent = new Intent(LoginActivity.this, LoadingSignUpActivity.class);
                    startActivity(intent);
                } else if (otcResponse.getStatus() == Shared.OTCStatus.USER_NOT_ENABLED) {
                    msp.putString("Nick", username.getText().toString().replace(" ", ""));
                    msp.putString("Pass", password.getText().toString());
                    pd.dismiss();
                    Intent intent = new Intent(LoginActivity.this, SecondSignUpActivity.class);
                    startActivity(intent);
                } else if (otcResponse.getStatus() == Shared.OTCStatus.USER_BLOCKED) {
                    onResponse.onError(otcResponse.getStatus(), otcResponse.getMessage());
                }
                pd.dismiss();
            });
            loginTask.execute();
        } else {
            ConnectionUtils.showOfflineToast();
        }
    }

    private DialogMultiple dm;
    private void showPopupNewMobile() {
        /*AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(getString(R.string.account_in_other_device)).setMessage(getString(R.string.you_want_change));
        builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> onYesPressed());
        builder.setNeutralButton(getString(R.string.logout), (dialog, which) -> dialog.dismiss());
        builder.setNegativeButton(getString(R.string.no), (dialog, which) -> onNoPressed());

        builder.show();*/
        dm = new DialogMultiple(this);
        dm.setTitle(getString(R.string.account_in_other_device))
                .setDescription(getString(R.string.you_want_change))
                .addButton(getString(R.string.yes), this::onYesPressed)
                .addButton(getString(R.string.no), this::onNoPressed)
                .addButton(getString(R.string.logout), dm::dismiss);
        dm.show();
    }

    private void onYesPressed() {
        changePhone(true);
    }

    private void onNoPressed() {
        changePhone(false);
    }

    private void enableUserAndGoToHome() {
        TypedTask<Shared.OTCResponse> enableUser = new TypedTask<>(Endpoints.ENABLE_USER, Welcome.UserEnabled.newBuilder()
                .setUsername(username.getText().toString())
                .build(), true, Shared.OTCResponse.class, new TypedCallback<Shared.OTCResponse>() {
            @Override
            public void onSuccess(@NonNull Shared.OTCResponse value) {
                pd.dismiss();
                startActivity(new Intent(LoginActivity.this, Home2Activity.class));
            }

            @Override
            public void onError(@NonNull OTCStatus status, @Nullable String message) {
                pd.dismiss();
                showError("Error", CloudErrorHandler.handleError(status));
            }
        });
        enableUser.execute();
    }

    @UiThread
    private void changePhone(boolean deleteInfo) {
        Utils.runOnMainThread(() -> {
            pd.show();
            Welcome.ChangePhone login = Welcome.ChangePhone.newBuilder()
                    .setUsername(username.getText().toString().replace(" ", ""))
                    .setPassword(password.getText().toString())
                    .setMobileIMEI(Utils.getImei())
                    .build();
            TypedTask<Shared.OTCResponse> lr = new TypedTask<>(Endpoints.CHANGE_PHONE, login, false, Shared.OTCResponse.class,
                new TypedCallback<Shared.OTCResponse>() {
                    @Override
                    public void onSuccess(@Nonnull @NonNull Shared.OTCResponse value) {
                        login(new TypedCallback<Shared.OTCResponse>() {
                            @Override
                            public void onSuccess(@Nonnull @NonNull Shared.OTCResponse value) {
                                TypedTask<Welcome.PasswordIsTemporal> checkPasswordTemporal = new TypedTask<>(Endpoints.PASSWORD_IS_TEMPORAL, null, true,
                                        Welcome.PasswordIsTemporal.class, new TypedCallback<Welcome.PasswordIsTemporal>() {
                                    @Override
                                    public void onSuccess(@Nonnull Welcome.PasswordIsTemporal value) {
                                        if (value.getIsTemporal()) {
                                            showPopupChangePasswordWithChangeMobile(deleteInfo);
                                        } else {
                                            pd.dismiss();
                                            ConfigurationNetwork.fetchBluetoothConfiguration();

                                            enableUserAndGoToHome();

                                            //MySharedPreferences.createLogin(getApplicationContext()).putBoolean("DeleteInfo", deleteInfo);
                                            //MySharedPreferences.createLogin(getApplicationContext()).putBoolean("NewPhone", true);

                                            //Intent intent = new Intent(LoginActivity.this, SecondSignUpActivity.class);
                                            //startActivity(intent);
                                        }
                                    }

                                    @Override
                                    public void onError(@Nonnull Shared.OTCStatus status, @Nullable String message) {
                                        if (status == Shared.OTCStatus.USER_PROFILE_REQUIRED) {
                                            pd.dismiss();
                                            ConfigurationNetwork.fetchBluetoothConfiguration();

                                            enableUserAndGoToHome();
                                        } else {
                                            pd.dismiss();
                                            showError(getString(R.string.server_error), CloudErrorHandler.handleError(status));
                                        }
                                    }
                                });
                                checkPasswordTemporal.execute();
                            }

                            @Override
                            public void onError(@NonNull Shared.OTCStatus status, String str) {
                                pd.dismiss();
                                showError("Error", CloudErrorHandler.handleError(status));
                            }
                        });
                    }

                    @Override
                    public void onError(@NonNull Shared.OTCStatus status, String str) {
                        pd.dismiss();
                        showError("Error", CloudErrorHandler.handleError(status));
                    }
                });
            lr.execute();
        });
    }

    private void showError(String title, String message) {
        new AlertDialog.Builder(LoginActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.close), (dialog, which) -> {
                    // Whatever...
                }).show();

    }
}