package com.otcengineering.white_app.serialization;

import com.google.gson.reflect.TypeToken;
import com.otcengineering.white_app.MyApp;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.Utils;

import java.util.ArrayList;

public class SharedKey {
    private long id;
    private String code;
    private String creationDate;
    private String expirationDate;

    public static int count(long bookingId) {
        MySharedPreferences msp = MySharedPreferences.createSharedKey(MyApp.getContext());
        ArrayList<SharedKey> keys = Utils.getGson().fromJson(msp.getString("" + bookingId), new TypeToken<ArrayList<SharedKey>>() {}.getType());
        return keys != null ? keys.size() : 0;
    }

    public static SharedKey getKey(long bookingId) {
        MySharedPreferences msp = MySharedPreferences.createSharedKey(MyApp.getContext());
        ArrayList<SharedKey> keys = Utils.getGson().fromJson(msp.getString("" + bookingId), new TypeToken<ArrayList<SharedKey>>() {}.getType());
        SharedKey key = keys.get(0);
        keys.remove(0);
        msp.putString("" + bookingId, Utils.getGson().toJson(keys));
        return key;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public static void addSharedKey(long bookingId, SharedKey key) {
        MySharedPreferences msp = MySharedPreferences.createSharedKey(MyApp.getContext());
        ArrayList<SharedKey> keys = Utils.getGson().fromJson(msp.getString("" + bookingId), new TypeToken<ArrayList<SharedKey>>() {}.getType());
        if (keys == null) {
            keys = new ArrayList<>();
        }
        keys.add(key);
        msp.putString("" + bookingId, Utils.getGson().toJson(keys));
    }
}
