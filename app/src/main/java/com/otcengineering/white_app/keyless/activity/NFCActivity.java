package com.otcengineering.white_app.keyless.activity;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.Nullable;

import com.otcengineering.white_app.R;
import com.otcengineering.white_app.activities.BaseActivity;

import java.util.Timer;
import java.util.TimerTask;

public class NFCActivity extends BaseActivity {
    public NFCActivity() {
        super("NFCActivity");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_nfc);

        findViews();
        setEvents();
        checkNfc();
    }

    private void checkNfc() {
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
        if (adapter == null) {
            showCustomDialogError("Device not compatible with NFC.");
        } else {
            if (!adapter.isEnabled()) {
                startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        checkNfc();
                    }
                }, 1000);
            }
        }
    }

    private void findViews() {

    }

    private void setEvents() {

    }
}
