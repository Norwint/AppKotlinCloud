package com.otcengineering.white_app.components;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.otcengineering.white_app.R;
import com.otcengineering.white_app.interfaces.OnAcceptQuantityListener;
import com.otcengineering.white_app.utils.Utils;

import java.util.Locale;

public class DialogNumericUpDown extends Dialog {

    private OnAcceptQuantityListener mListener;
    private TextView m_btnYes, m_btnNo;
    private TextView mCount;
    private String title;
    private String message;
    private int cnt = 1;

    public DialogNumericUpDown(Context ctx) {
        super(ctx, android.R.style.Theme_Light);
        setCanceledOnTouchOutside(false);
        if (getWindow() != null) {
            int colorBackground = R.color.custom_dialog_primary;
            getWindow().setBackgroundDrawableResource(colorBackground);
        }
    }

    public DialogNumericUpDown(Context context, String message) {
        super(context, android.R.style.Theme_Light);
        setCanceledOnTouchOutside(false);
        if (getWindow() != null) {
            int colorBackground = R.color.custom_dialog_primary;
            getWindow().setBackgroundDrawableResource(colorBackground);
        }
        this.message = message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public void setOnAcceptQuantityListener(OnAcceptQuantityListener run) {
        this.mListener = run;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_numericupdown);

        TextView txtMessage = findViewById(R.id.dialog_custom_txtMessage);
        TextView txtTitle = findViewById(R.id.dialog_custom_title);
        FrameLayout img = findViewById(R.id.hide_image);

        m_btnYes = findViewById(R.id.dialog_custom_btnYes);
        m_btnNo = findViewById(R.id.dialog_custom_btnNo);
        mCount = findViewById(R.id.itemAmount);

        findViewById(R.id.itemPlus).setOnClickListener(v -> {
            if (cnt < 100) setCount(++cnt);
        });

        findViewById(R.id.itemMinus).setOnClickListener(v -> {
            if (cnt > 1) setCount(--cnt);
        });

        txtMessage.setText(message);

        if (title != null && !title.isEmpty()) {
            txtTitle.setText(title);
            txtTitle.setVisibility(View.VISIBLE);
            img.setVisibility(View.GONE);
        }

        m_btnYes.setOnClickListener(view -> {
            Utils.runOnMainThread(() -> {
                if (mListener != null) {
                    mListener.onAccept(cnt);
                }
            });
            dismiss();
        });

        m_btnNo.setOnClickListener(v -> {
            dismiss();
        });

        setCount(cnt);
    }

    private void setCount(int newCount) {
        cnt = newCount;
        mCount.setText(String.format(Locale.US, "%d", cnt));
    }
}
