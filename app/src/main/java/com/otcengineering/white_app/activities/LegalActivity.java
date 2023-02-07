package com.otcengineering.white_app.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;


import com.otc.alice.api.model.Shared;
import com.otc.alice.api.model.Welcome;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.MyApp;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.tasks.TypedTask;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.Utils;
import com.otcengineering.white_app.utils.interfaces.TypedCallback;

import javax.annotation.Nonnull;


public class LegalActivity extends EventActivity {

    private TextView descripcion;
    private Button aceptar;
    private int n;
    private static boolean legalTerm = true;
    private static boolean dataTerm = true;
    private static boolean disclaTerm = true;

    public LegalActivity() {
        super("LegalActivity");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legal);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);

        TextView head = findViewById(R.id.txtDescripcion);
        descripcion = findViewById(R.id.txtDescripcionLegal);
        aceptar = findViewById(R.id.btnAceptar);

        Bundle dif = getIntent().getExtras();
        descripcion.setMovementMethod(new ScrollingMovementMethod());

        n = 0;

        if (dif.getInt("num") == 5) {
            setTitle(R.string.data_protection);
            head.setText(getResources().getString(R.string.privacy_info));
            n = dif.getInt("num");
        } else if (dif.getInt("num") == 6) {
            setTitle(R.string.legal);
            head.setText(getResources().getString(R.string.connectech_ser));
            n = dif.getInt("num");
        } else if (dif.getInt("num") == 7) {
            setTitle(R.string.disclaimer);
            head.setText(getResources().getString(R.string.disclaimer));
            n = dif.getInt("num");
        }
        if (ConnectionUtils.isOnline(getApplicationContext())) {
            TypedTask<Welcome.TermsAcceptanceResponse> getTermsByUserLanguage = new TypedTask<>(Endpoints.GET_TERMS_ACCEPTANCE_USER_LANG, null, true, Welcome.TermsAcceptanceResponse.class, new TypedCallback<Welcome.TermsAcceptanceResponse>() {
                @Override
                public void onSuccess(@Nonnull Welcome.TermsAcceptanceResponse value) {
                    descripcion.setText(Html.fromHtml(value.getTerms(n - 5).getText()));
                }

                @Override
                public void onError(@Nonnull Shared.OTCStatus status, @Nullable String message) {
                    if (status == Shared.OTCStatus.USER_PROFILE_REQUIRED) {
                        new GetTerms().execute();
                    }
                }
            });
            getTermsByUserLanguage.execute();
        } else {
            ConnectionUtils.showOfflineToast();
        }
        metodoAceptar(n);
    }

    private void metodoAceptar(final int n) {
        aceptar = findViewById(R.id.btnAceptar);

        if (n == 6 && !legalTerm){
            aceptar.setText(getResources().getString(R.string.accepted));
            aceptar.setBackground(getResources().getDrawable(R.drawable.button_shape_background_blue));
        }
        if (n == 5 && !dataTerm){
            aceptar.setText(getResources().getString(R.string.accepted));
            aceptar.setBackground(getResources().getDrawable(R.drawable.button_shape_background_blue));
        }
        if (n == 7 && !disclaTerm){
            aceptar.setText(getResources().getString(R.string.accepted));
            aceptar.setBackground(getResources().getDrawable(R.drawable.button_shape_background_blue));
        }

        aceptar.setOnClickListener(v -> {
            MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());
            if (n == 5) {
                dataTerm = false;
                msp.putBoolean("data", true);
            } else if (n == 6) {
                legalTerm = false;
                msp.putBoolean("legal", true);
            } else if (n == 7) {
                disclaTerm = false;
                msp.putBoolean("discla", true);
            }
            finish();
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    class GetTerms extends AsyncTask<Object, Object, Welcome.TermsAcceptanceResponse> {
        @Override
        protected Welcome.TermsAcceptanceResponse doInBackground(Object... params) {
            try {
                String lang = MyApp.getUserLocale().getLanguage();

                if (lang.equals("in")) {
                    lang = "ba";
                } else {
                    lang = "en";
                }

                return ApiCaller.doCall(Endpoints.GET_TERMS_ACCEPTANCE_LANG + lang, false,null, Welcome.TermsAcceptanceResponse.class);
                //return ApiCaller.doCall(Endpoints.GET_TERMS_ACCEPTANCE, null, Welcome.TermsAcceptanceResponse.class);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Welcome.TermsAcceptanceResponse response) {
            if (response == null) {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Cannot load Terms.", Toast.LENGTH_LONG).show());
                if (!Utils.isActivityFinish(LegalActivity.this)) {
                    finish();
                }
            } else {
                if (n == 5) {
                    descripcion.setText(Html.fromHtml(response.getTerms(0).getText()));
                } else if (n == 6) {
                    descripcion.setText(Html.fromHtml(response.getTerms(1).getText()));
                } else if (n == 7) {
                    descripcion.setText(Html.fromHtml(response.getTerms(2).getText()));
                }
            }
        }
    }
}
