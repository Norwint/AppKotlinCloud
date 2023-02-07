package com.otcengineering.white_app.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.TextView;

import com.otcengineering.white_app.R;

import java.io.InputStream;

public class LicenseActivity extends BaseActivity {

    public LicenseActivity() {
        super("LicenseActivity");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage(getString(R.string.loading));
        pd.setCancelable(false);
        pd.show();

        TextView text = findViewById(R.id.text);
        StringBuilder license = new StringBuilder();
        try {
            InputStream is = getAssets().open("licenses");
            byte[] buffer = new byte[0x10000];
            int read;
            while ((read = is.read(buffer)) != -1) {
                license.append(new String(buffer, 0, read));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pd.dismiss();
            text.setText(license.toString());
        }
    }
}
