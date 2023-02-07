package com.otcengineering.white_app.tasks;

import android.os.AsyncTask;
import android.util.Log;

import java.util.HashMap;

public class MyAsyncTask extends AsyncTask<Void, Void, Void> {
    private Runnable m_pre, m_back, m_post;
    private HashMap<String, String> m_keyMap;

    public MyAsyncTask() {
        m_keyMap = new HashMap<>();
    }

    public void setOnBackground(Runnable task) {
        m_back = task;
    }

    public void setOnPreExecute(Runnable pre) {
        m_pre = pre;
    }

    public void setOnPostExecute(Runnable post) {
        m_post = post;
    }

    public void putValue(String key, String value) {
        m_keyMap.put(key, value);
    }

    public String getValue(String key) {
        return m_keyMap.containsKey(key) ? m_keyMap.get(key) : "";
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        try {
            if (m_pre != null) {
                m_pre.run();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            m_back.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        try {
            if (m_post != null) {
                m_post.run();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        if (m_back != null) {
            execute();
        } else {
            Log.e("MyAsyncTask", "OnBackground is null, ignoring this call.");
        }
    }
}
