package com.otcengineering.white_app.tasks;

import android.os.AsyncTask;

public class BackgroundTask extends AsyncTask<Void, Void, Void> {
    private final Runnable runnable;

    public BackgroundTask(Runnable runnable) {
        this.runnable = runnable;
    }
    @Override
    protected Void doInBackground(Void... voids) {
        runnable.run();
        return null;
    }
}
