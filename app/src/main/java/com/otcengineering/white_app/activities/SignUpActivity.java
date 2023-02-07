package com.otcengineering.white_app.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;

import com.otc.alice.api.model.Shared;
import com.otc.alice.api.model.Welcome;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.MyApp;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.components.DialogMultiple;
import com.otcengineering.white_app.network.ConfigurationNetwork;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.utils.CloudErrorHandler;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.Utils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class SignUpActivity extends EventActivity {
    private Button continu;
    private TextView login, signup, obligation, nicksp, emailsp, passsp, retypesp, mobilesp;
    private EditText nick;
    private EditText email;
    private EditText pass;
    private EditText retype;
    private EditText phone;

    private String originalPhone;

    private String emptyField;
    private String invalidChar;
    private Set<String> diccionari = new HashSet<>();
    private Set<String> dictionaryNumbers = new HashSet<>();

    private ProgressDialog progressDialogRegister;

    public SignUpActivity() {
        super("SignUpActivity");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_sign_up_v2);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        loadDictionary();

        retrieveViews();
        createAndAssiggnedTypefaces();

        progressDialogRegister = new ProgressDialog(SignUpActivity.this);
        progressDialogRegister.setCancelable(false);
        progressDialogRegister.setCanceledOnTouchOutside(false);

        continu.setOnClickListener(v -> {
            if (checkEmptysFields()) {
                showError(emptyField + " " + getResources().getString(R.string.error_default), getResources().getString(R.string.field) + " " + emptyField + " " + getResources().getString(R.string.is_empty));
            } else if (checkInvalidNickName() && invalidChar.equals(" ")) {
                showError(getResources().getString(R.string.nickname_error), getResources().getString(R.string.nickname_no_white));
            } else if (checkInvalidNickName()) {
                showError(getResources().getString(R.string.nickname_error), invalidChar + " " + getResources().getString(R.string.not_valid_for_nickname));
            } else if (nick.getText().toString().length() < 5) {
                showError(getResources().getString(R.string.nickname_error), getString(R.string.nickname_bigger_than_4_chars));
            } else if (checkInvalidEmail()) {
                showError(getResources().getString(R.string.email_error), getResources().getString(R.string.email_not_correct));
            } else if (phone.getText().toString().length() < 11) {
                showError(getResources().getString(R.string.phone_error), getResources().getString(R.string.phone_lower_11));
            } else if (checkInvalidPhone(phone.getText().toString())) {
                showError(getResources().getString(R.string.phone_error), getResources().getString(R.string.phone_invalid_format));
            } else if (pass.getText().length() < 10) {
                showError(getResources().getString(R.string.password_error), getResources().getString(R.string.error_password));
            } else if (!checkPassword(pass.getText().toString())) {
                showError(getResources().getString(R.string.password_error), getResources().getString(R.string.error_password));
            } else if (pass.getText().length() >= 10 && pass.getText().toString().equals(retype.getText().toString())) {
                if (ConnectionUtils.isOnline(getApplicationContext())) {
                    new SignUp().execute(nick.getText().toString(), email.getText().toString(), pass.getText().toString(), phone.getText().toString());
                } else {
                    ConnectionUtils.showOfflineToast();
                }
            } else {
                showError(getResources().getString(R.string.password_error), getResources().getString(R.string.error_retype));
            }
        });

        login.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        nick.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (nick.getRight() - nick.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    // your action here
                    nick.setText("");
                    return true;
                }
            }
            return false;
        });

        pass.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (pass.getRight() - pass.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    // your action here
                    pass.setText("");
                    return true;
                }
            }
            return false;
        });

        retype.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (retype.getRight() - retype.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    // your action here
                    retype.setText("");
                    return true;
                }
            }
            return false;
        });

        email.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (email.getRight() - email.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    // your action here
                    email.setText("");
                    return true;
                }
            }
            return false;
        });


        phone.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (phone.getRight() - phone.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    // your action here
                    phone.setText("");
                    return true;
                }
            }
            return false;
        });
    }

    private void retrieveViews() {
        login = findViewById(R.id.txtLogin);
        nick = findViewById(R.id.etNickName);
        email = findViewById(R.id.etEmailAddress);
        pass = findViewById(R.id.etSignupPass);
        retype = findViewById(R.id.etRetype);
        phone = findViewById(R.id.etPhoneNumber);
        continu = findViewById(R.id.btnSave);
        signup = findViewById(R.id.txtSignUp);
        obligation = findViewById(R.id.txtSignUpDes);
        nicksp = findViewById(R.id.txtUser);
        emailsp = findViewById(R.id.txtMail);
        passsp = findViewById(R.id.txtPass);
        retypesp = findViewById(R.id.txtRetype);
        mobilesp = findViewById(R.id.txtPhone);
    }

    private boolean checkInvalidPhone(final String phone) {
        Pattern pat = Pattern.compile("^(34|91|81|62|27)([0-9]{9,})$");
        return !pat.matcher(phone).matches();
    }

    private boolean checkPassword(String s) {
        boolean containsNumber = false;
        boolean containsLetter = false;

        String valid;

        for (int i = 0; i < s.length(); i++) {
            valid = s.charAt(i) + "";
            valid = valid.toLowerCase();
            if (dictionaryNumbers.contains(valid)) {
                containsNumber = true;
            } else {
                containsLetter = true;
            }
        }

        return containsLetter && containsNumber;
    }

    private void createAndAssiggnedTypefaces() {
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Regular.ttf");
        Typeface face1 = Typeface.createFromAsset(getAssets(), "fonts/Montserrat-Regular.ttf");

        nick.setTypeface(face);
        email.setTypeface(face);
        pass.setTypeface(face);
        retype.setTypeface(face);
        phone.setTypeface(face);
        signup.setTypeface(face);
        nicksp.setTypeface(face);
        obligation.setTypeface(face);
        emailsp.setTypeface(face);
        passsp.setTypeface(face);
        retypesp.setTypeface(face);
        mobilesp.setTypeface(face);
        continu.setTypeface(face1);
    }


    public boolean checkInvalidEmail() {
        Pattern pat = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        return !pat.matcher(email.getText().toString()).find();
        //return Pattern.matches("^.+@[\\w.]+\\.\\w{2,}$", email.getText().toString());
    }

    public boolean checkEmptysFields() {
        boolean isEmpty = false;
        if (nick.getText().length() == 0) {
            emptyField = getResources().getString(R.string.nickname);
            isEmpty = true;
        } else if (email.getText().length() == 0) {
            emptyField = getResources().getString(R.string.email);
            isEmpty = true;
        } else if (pass.getText().length() == 0) {
            emptyField = getResources().getString(R.string.password);
            isEmpty = true;
        } else if (retype.getText().length() == 0) {
            emptyField = getResources().getString(R.string.retype_pass);
            isEmpty = true;
        } else if (phone.getText().length() == 0) {
            emptyField = getResources().getString(R.string.mobile_phone);
            isEmpty = true;
        }

        return isEmpty;
    }

    public void loadDictionary() {
        String[] numbers = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        String[] allowedChars = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};

        diccionari.addAll(Arrays.asList(allowedChars));
        dictionaryNumbers.addAll(Arrays.asList(numbers));
    }

    public boolean checkInvalidNickName() {
        boolean isCorrect = true;
        String valid;
        for (int i = 0; i < nick.length(); i++) {
            valid = nick.getText().toString().charAt(i) + "";
            valid = valid.toLowerCase();
            if (!diccionari.contains(valid)) {
                isCorrect = false;
                invalidChar = nick.getText().toString().charAt(i) + "";
                break;
            }
        }

        return !isCorrect;
    }

    class SignUp extends AsyncTask<String, String, Shared.OTCResponse> {
        Shared.OTCResponse response;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialogRegister.setTitle(getResources().getString(R.string.loading_title));
            progressDialogRegister.show();
        }

        @Override
        protected Shared.OTCResponse doInBackground(String... params) {
            Welcome.UserRegistration.Builder userReg = Welcome.UserRegistration.newBuilder();

            String lang = MyApp.getUserLocale().getLanguage();

            if (lang.equals("in")) {
                lang = "ba";
            } else {
                lang = "en";
            }

            userReg.setUsername(lang + "@" + params[0]);
            userReg.setPassword(params[2]);
            userReg.setEmail(params[1]);
            userReg.setMobilePhoneNumber("+" + params[3]);
            userReg.setMobileIMEI(Utils.getImei());
            try {
                response = ApiCaller.doCall(Endpoints.REGISTER, userReg.build(), Shared.OTCResponse.class);
            } catch (ApiCaller.OTCException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(Shared.OTCResponse result) {
            if (result == null) {
                progressDialogRegister.dismiss();
                showError(getResources().getString(R.string.server_error), getString(R.string.server_timeout));
            } else {
                switch (result.getStatus()) {
                    case SUCCESS:
                        MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());
                        msp.clear();
                        msp.putString("Nick", nick.getText().toString());
                        msp.putString("Pass", pass.getText().toString());
                        msp.putString("Email", email.getText().toString());
                        msp.putString("Tlf", phone.getText().toString());
                        if (ConnectionUtils.isOnline(getApplicationContext())) {
                            new SetToken().execute();
                        } else {
                            ConnectionUtils.showOfflineToast();
                        }
                        break;
                    case EMAIL_ALREADY_USED: {
                        progressDialogRegister.dismiss();

                        DialogMultiple dm = new DialogMultiple(SignUpActivity.this);
                        dm.setDescription(getString(R.string.email_exists_sign_up));
                        dm.addButton(getString(R.string.reentry_mail), () -> {});
                        dm.addButton(getString(R.string.login), () -> {
                            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                            startActivity(intent);
                            overridePendingTransition(0, 0);
                            finish();
                        });
                        dm.show();
                    }
                    break;
                    default:
                        progressDialogRegister.dismiss();
                        showError(getResources().getString(R.string.server_error), CloudErrorHandler.handleError(result.getStatus()));
                        break;
                }
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

                Welcome.LoginResponse crr = ApiCaller.doCall(Endpoints.LOGIN, loginB.build(), Welcome.LoginResponse.class);
                return crr.getApiToken();
            } catch (Exception e) {
                return String.valueOf(e);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.length() > 2) {
                progressDialogRegister.show();
                MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());
                msp.putString("token", result);
                msp.putLong("token_date", Calendar.getInstance().getTime().getTime());

                ConfigurationNetwork.fetchBluetoothConfiguration();

                Intent intent = new Intent(SignUpActivity.this, LoadingSignUpActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            } else {
                progressDialogRegister.show();

            }
        }
    }

    private void showError(String title, String message) {
        new AlertDialog.Builder(SignUpActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.close), (dialog, which) -> {
                    // Whatever...
                }).show();
    }
}
