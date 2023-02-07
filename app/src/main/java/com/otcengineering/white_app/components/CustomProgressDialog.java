package com.otcengineering.white_app.components;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;

import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.otcengineering.white_app.R;

import java.util.Locale;

public class CustomProgressDialog extends Dialog {
    private TextView title;
    private TextView progressText;
    private ProgressBar progressBar;

    private Runnable onCancelCallback;

    public CustomProgressDialog(@NonNull Context context) {
        super(context, android.R.style.Theme_Light);
        setCanceledOnTouchOutside(false);
        final Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.color.black_60_alpha);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.progress_dialog);

        title = findViewById(R.id.titleText);
        progressText = findViewById(R.id.progressText);
        progressBar = findViewById(R.id.progressBar);
        Button cancel = findViewById(R.id.cancelButton);
        cancel.setOnClickListener(v -> {
            if (onCancelCallback != null) {
                onCancelCallback.run();
            }
            this.cancel();
        });
        cancel.setText(R.string.cancel);
    }

    public void setOnCancelCallback(@NonNull Runnable onCancelCallback) {
        this.onCancelCallback = onCancelCallback;
    }

    public void setTitle(@NonNull final String title) {
        this.title.setText(title);
    }

    public void update(final float progress) {
        progressText.setText(String.format(Locale.getDefault(), "%.1f%%", progress * 100));
        progressBar.setProgress((int)(progress * 100));
    }
}
