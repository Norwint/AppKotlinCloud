package com.otcengineering.white_app.components;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.otc.alice.api.model.Community;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.tasks.GenericTask;

public class Menu extends Dialog {
    private long usrId;

    private Runnable onUnblock;

    public Menu(@NonNull Context context, long usrId) {
        super(context, android.R.style.Theme_Black_NoTitleBar);

        setCanceledOnTouchOutside(false);
        this.usrId = usrId;
    }

    public void setOnUnblock(Runnable rnn) {
        onUnblock = rnn;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.fragment_menu_unblock);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView unblock = findViewById(R.id.unblock);
        TextView cancel = findViewById(R.id.cancel);

        unblock.setOnClickListener(v -> {
            Community.BlockUser bl = Community.BlockUser.newBuilder().setType(Community.BlockType.UNLOCK).setUserId(usrId).build();
            GenericTask gt = new GenericTask(Endpoints.BLOCK_USER, bl, true, otcResponse -> {
                if (onUnblock != null) onUnblock.run();
                dismiss();
            });
            gt.execute();
        });

        cancel.setOnClickListener(v -> dismiss());
    }
}
