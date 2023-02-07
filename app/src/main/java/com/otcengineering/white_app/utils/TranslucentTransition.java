package com.otcengineering.white_app.utils;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class TranslucentTransition {
    private View toApply;
    // start representa quan arriba al 100% i end quan arriba al 0%
    private int frameStart, frameEnd;
    // temps sense transició
    private int delay;

    private int ticks;

    private Timer timer;

    private final int FRAMERATE = 60;

    private float alpha;

    private boolean running = false;

    public TranslucentTransition(@NonNull View view, float start, float end, float duration) {
        this.toApply = view;
        this.frameStart = (int) (FRAMERATE * start);
        this.frameEnd = (int) (FRAMERATE * end);
        this.delay = (int)(FRAMERATE * (duration - end - start));
    }

    public void start() {
        if (running) return;
        running = true;
        ticks = 0;
        toApply.setAlpha(0);
        toApply.setOnClickListener(v -> {
            ticks = delay + frameStart;
            toApply.setOnClickListener(null);
        });
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                alpha = Utils.clamp(alpha, 0.0f, 1.0f);
                Utils.runOnMainThread(() -> toApply.setVisibility(View.VISIBLE));
                if (ticks < frameStart) {
                    alpha += (1.0f / frameStart);
                    Utils.runOnMainThread(() -> toApply.setAlpha(alpha));
                } else if (ticks > delay + frameStart) {
                    alpha -= (1.0f / frameEnd);
                    toApply.setOnClickListener(null);
                    Utils.runOnMainThread(() -> toApply.setAlpha(alpha));
                }
                Log.d("TT", String.format(Locale.FRANCE, "%f", alpha));
                ++ticks;
                if (ticks >= delay + frameEnd + frameStart) {
                    Utils.runOnMainThread(() -> toApply.setVisibility(View.GONE));
                    toApply.setOnClickListener(null);
                    running = false;
                    timer.cancel();
                }
            }
        }, 0, 1000 / FRAMERATE);
    }
}
