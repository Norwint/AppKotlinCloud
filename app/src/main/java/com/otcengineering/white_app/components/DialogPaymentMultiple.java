package com.otcengineering.white_app.components;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.otcengineering.white_app.R;

import java.util.ArrayList;

public class DialogPaymentMultiple extends Dialog {
    private String titleStr = "", descStr = "";
    private ArrayList<TextView> m_buttonList;

    public DialogPaymentMultiple(@NonNull Context context) {
        super(context);

        setCanceledOnTouchOutside(false);
        m_buttonList = new ArrayList<>();
    }

    public DialogPaymentMultiple addButton(String text, Runnable onClickListener) {
        TextView btn = new TextView(getContext());
        btn.setText(text.toUpperCase());
        btn.setOnClickListener(v -> {
            if (onClickListener != null) {
                onClickListener.run();
            }
            dismiss();
        });
        btn.setTextColor(ContextCompat.getColor(getContext(), R.color.custom_dialog_light_blue));
        btn.setGravity(Gravity.CENTER);
        btn.setTextSize(16);
        btn.setLines(2);
        btn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));

        m_buttonList.add(btn);
        return this;
    }

    public DialogPaymentMultiple setTitle(String title) {
        titleStr = title;
        return this;
    }

    public DialogPaymentMultiple setDescription(String description) {
        descStr = description;
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_multiple);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        LinearLayout buttons = findViewById(R.id.buttons);
        TextView title = findViewById(R.id.dialog_custom_title);
        TextView desc = findViewById(R.id.dialog_custom_txtMessage);

        title.setText(titleStr);
        desc.setText(descStr);

        buttons.setOrientation(LinearLayout.VERTICAL);
        buttons.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        for (int i = 0; i < m_buttonList.size(); ++i) {
            TextView btn = m_buttonList.get(i);
            buttons.addView(btn);
            if (i < m_buttonList.size() - 1) {
                View view = new View(getContext());
                LinearLayout.LayoutParams params;
                params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1, 0);
                params.setMargins(0, 10, 0, 10);
                view.setLayoutParams(params);
                view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.layout_border));
                buttons.addView(view);
            }
        }
    }
}
