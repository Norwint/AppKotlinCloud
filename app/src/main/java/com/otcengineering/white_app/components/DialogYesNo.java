package com.otcengineering.white_app.components;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.otcengineering.white_app.R;
import com.otcengineering.white_app.utils.Utils;

public class DialogYesNo extends Dialog {

    private Runnable m_onYes, m_onNo;
    private TextView m_btnYes, m_btnNo;
    private String title;
    private String message;
    private int m_yesColor = 0, m_noColor = 0;
    private String m_yesText, m_noText;

    public DialogYesNo(Context ctx) {
        super(ctx, android.R.style.Theme_Light);
        setCanceledOnTouchOutside(false);
        if (getWindow() != null) {
            int colorBackground = R.color.custom_dialog_primary;
            getWindow().setBackgroundDrawableResource(colorBackground);
        }

        this.m_onYes = () -> {};
        this.m_onNo = () -> {};
    }

    public DialogYesNo(Context context, String message, Runnable onYes, Runnable onNo) {
        super(context, android.R.style.Theme_Light);
        setCanceledOnTouchOutside(false);
        if (getWindow() != null) {
            int colorBackground = R.color.custom_dialog_primary;
            getWindow().setBackgroundDrawableResource(colorBackground);
        }
        this.message = message;

        this.m_onYes = onYes;
        this.m_onNo = onNo;
    }

    public void setBackgroundColor(int color) {
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(color);
        }
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public void setYesButtonText(final String msg) {
        m_yesText = msg;
    }

    public void setNoButtonText(final String msg) {
        m_noText = msg;
    }

    public void setYesButtonColor(final int color) {
        m_yesColor = color;
    }

    public void setNoButtonColor(final int color) {
        m_noColor = color;
    }

    public void setYesButtonClickListener(Runnable run) {
        this.m_onYes = run;
    }

    public void setNoButtonClickListener(Runnable run) {
        this.m_onNo = run;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_yesno);

        TextView txtMessage = findViewById(R.id.dialog_custom_txtMessage);
        TextView txtTitle = findViewById(R.id.dialog_custom_title);
        FrameLayout img = findViewById(R.id.hide_image);

        m_btnYes = findViewById(R.id.dialog_custom_btnYes);
        m_btnNo = findViewById(R.id.dialog_custom_btnNo);

        txtMessage.setText(message);

        if (title != null && !title.isEmpty()) {
            txtTitle.setText(title);
            txtTitle.setVisibility(View.VISIBLE);
            img.setVisibility(View.GONE);
        } else {
            txtTitle.setVisibility(View.GONE);
        }

        if (m_yesColor != 0) {
            m_btnYes.setTextColor(m_yesColor);
        }

        if (m_noColor != 0) {
            m_btnNo.setTextColor(m_noColor);
        }

        if (m_yesText != null) {
            m_btnYes.setText(m_yesText);
        }

        if (m_noText != null) {
            m_btnNo.setText(m_noText);
        }

        m_btnYes.setOnClickListener(view -> {
            Utils.runOnMainThread(m_onYes);
            dismiss();
        });

        m_btnNo.setOnClickListener(v -> {
            Utils.runOnMainThread(m_onNo);
            dismiss();
        });
    }
}
