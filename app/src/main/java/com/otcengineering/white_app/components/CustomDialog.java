package com.otcengineering.white_app.components;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.otcengineering.white_app.R;

public class CustomDialog extends Dialog {

    private ImageView imgIcon;
    private TextView txtMessage;
    private TextView btnOk;
    private FrameLayout img;

    private String message;
    private Boolean isError = false;
    private Runnable listener;

    public CustomDialog(Context context) {
        super(context, android.R.style.Theme_Light);
        setCanceledOnTouchOutside(false);
        if (getWindow() != null) {
            int colorBackground = R.color.custom_dialog_primary;
            getWindow().setBackgroundDrawableResource(colorBackground);
        }
    }

    public CustomDialog(Context context, int messageRes, boolean isError) {
        super(context, android.R.style.Theme_Light);
        setCanceledOnTouchOutside(false);
        if (getWindow() != null) {
            int colorBackground = R.color.custom_dialog_primary;
            getWindow().setBackgroundDrawableResource(colorBackground);
        }
        this.message = context.getString(messageRes);
        this.isError = isError;
    }

    public CustomDialog(Context context, String message, boolean isError) {
        super(context, android.R.style.Theme_Light);
        setCanceledOnTouchOutside(false);
        if (getWindow() != null) {
            int colorBackground = R.color.custom_dialog_primary;
            getWindow().setBackgroundDrawableResource(colorBackground);
        }
        this.message = message;
        this.isError = isError;
    }

    public CustomDialog(Context context, String message) {
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

    public void setOnOkListener(Runnable runnable) {
        this.listener = runnable;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_custom);
        imgIcon = findViewById(R.id.dialog_custom_imgIcon);
        txtMessage = findViewById(R.id.dialog_custom_txtMessage);
        btnOk = findViewById(R.id.dialog_custom_btnOk);
        img = findViewById(R.id.hide_image);
        txtMessage.setText(message);
        btnOk.setOnClickListener(view -> {
            dismiss();
            if (listener != null) {
                listener.run();
            }
        });
        if (this.isError != null)
            imgIcon.setImageResource(isError ? R.drawable.delete : R.drawable.check_blue);

        if (this.isError == null) {
            img.setVisibility(View.GONE);
        }
    }

    public TextView getButton() {
        return this.btnOk;
    }
}
