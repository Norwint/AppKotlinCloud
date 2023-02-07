package com.otcengineering.white_app.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Base64;

import androidx.annotation.NonNull;

import com.otc.alice.api.model.BadgeProto;
import com.otcengineering.white_app.MyApp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.annotation.Nonnull;

public class MySharedPreferences {
    private SharedPreferences mSharedPreferences;
    private static final String SHARED_KEY = "SharedKey";
    private static final String LOGIN = "SignUp";
    private static final String LANGUAGE = "LanguageActivity";
    private static final String FILTER = "FiltersFavourites";
    private static final String SOCIAL = "social";
    private static final String GENERAL = "generalStats";
    private static final String SECURITY = "security";
    private static final String LOCATION_SECURITY = "LocationSecurity";
    private static final String DASHBOARD = "Dashboard";
    private static final String STATUS = "status";
    private static final String FOTA = "fota";
    private static final String IMAGE = "ImgCache";
    private static final String ROUTES = "RouteCache";
    private static final String SURVEY = "SurveyCache";

    // Default shared preferences
    private MySharedPreferences(@NonNull final Context ctx) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    // Named shared preferences
    private MySharedPreferences(final Context ctx, @NonNull final String category) {
        try {
            mSharedPreferences = ctx.getSharedPreferences(category, Context.MODE_PRIVATE);
        } catch (NullPointerException npe) {
            //Log.e("MySharedPreferences", "NullPointerException", npe);
        }
    }

    public static MySharedPreferences create() {
        return MySharedPreferences.create(null);
    }

    public static MySharedPreferences create(final Context ctx) {
        if (ctx == null) {
            return new MySharedPreferences(MyApp.getContext());
        } else {
            return new MySharedPreferences(ctx);
        }
    }

    private static MySharedPreferences create(final Context ctx, final String name) {
        if (ctx == null) {
            return new MySharedPreferences(MyApp.getContext(), name);
        } else {
            return new MySharedPreferences(ctx, name);
        }
    }

    public static MySharedPreferences createDefault(final Context ctx) {
        return create(ctx);
    }

    public static MySharedPreferences createLogin(final Context ctx) {
        return create(ctx, LOGIN);
    }

    public static MySharedPreferences createSurvey(Context ctx) {
        return create(ctx, SURVEY);
    }

    public static MySharedPreferences createLanguage(final Context ctx) {
        return create(ctx, LANGUAGE);
    }

    public static MySharedPreferences createImei(final Context ctx) {
        return create(ctx, "FINAL");
    }

    public static MySharedPreferences createFilter(final Context ctx) {
        return create(ctx, FILTER);
    }

    public static MySharedPreferences createSocial(final Context ctx) {
        return create(ctx, SOCIAL);
    }

    public static MySharedPreferences createGeneral(final Context ctx) {
        return create(ctx, GENERAL);
    }

    public static MySharedPreferences createSecurity(final Context ctx) {
        return create(ctx, SECURITY);
    }

    public static MySharedPreferences createLocationSecurity(final Context ctx) {
        return create(ctx, LOCATION_SECURITY);
    }

    public static MySharedPreferences createDashboard(final Context ctx) {
        return create(ctx, DASHBOARD);
    }

    public static MySharedPreferences createStatus(final Context ctx) {
        return create(ctx, STATUS);
    }

    public static MySharedPreferences createFota(final Context ctx) {
        return create(ctx, FOTA);
    }

    public static MySharedPreferences createImage(Context context) {
        return create(context, IMAGE);
    }

    public static MySharedPreferences createRoutes(Context ctx) {
        return create(ctx, ROUTES);
    }

    public static MySharedPreferences createSharedKey(Context ctx) {
        return create(ctx, SHARED_KEY);
    }

    public void putString(@NonNull final String key, @NonNull final String value) {
        mSharedPreferences.edit().putString(key, EncodingUtils.encode(value)).apply();
    }

    public void putInteger(@NonNull final String key, final int value) {
        putString(key, Integer.toString(value));
    }

    public void putLong(@NonNull final String key, final long value) {
        putString(key, Long.toString(value));
    }

    public void putFloat(@NonNull final String key, final float value) {
        putString(key, Float.toString(value));
    }

    public void putBoolean(@NonNull final String key, final boolean value) {
        putString(key, value ? "true" : "false");
    }

    public void putDouble(@NonNull final String key, final double value) {
        putString(key, Double.toString(value));
    }

    public void putBytes(@NonNull final String key, final byte[] value) {
        byte[] encrypted = value;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            encrypted = EncodingUtils.encrypt(value);
        }
        if (encrypted != null) {
            String b64 = Base64.encodeToString(encrypted, Base64.DEFAULT);
            mSharedPreferences.edit().putString(key, b64).apply();
            Arrays.fill(encrypted, (byte)0);
        }
    }

    public void putRaw(@Nonnull final String key, final byte[] value) {
        String b64 = Base64.encodeToString(value, Base64.DEFAULT);
        mSharedPreferences.edit().putString(key, b64).apply();
    }

    public @NonNull String getString(@NonNull final String key) {
        String str = mSharedPreferences.getString(key, "");
        if (str.isEmpty()) {
            return "";
        } else {
            return EncodingUtils.decode(str);
        }
    }

    public @NonNull char[] getCharArray(@NonNull final String key) {
        String val = mSharedPreferences.getString(key, "");
        return EncodingUtils.decode(val).toCharArray();
    }

    public int getInteger(@NonNull final String key) {
        String strVal = getString(key);
        if (strVal.isEmpty()) {
            return 0;
        }
        return Utils.tryParseInt(strVal);
    }

    public long getLong(@NonNull final String key) {
        String strVal = getString(key);
        if (strVal.isEmpty()) {
            return 0;
        }
        return Utils.tryParseLong(strVal);
    }

    public float getFloat(@NonNull final String key) {
        String strVal = getString(key);
        if (strVal.isEmpty()) {
            return Float.NaN;
        }
        return Float.parseFloat(strVal);
    }

    public double getDouble(@NonNull final String key) {
        String strVal = getString(key);
        if (strVal.isEmpty()) {
            return Double.NaN;
        }
        return Double.parseDouble(strVal);
    }

    public boolean getBoolean(@NonNull final String key) {
        return getString(key).equals("true");
    }

    public synchronized byte[] getBytes(@NonNull final String key) {
        String b64 = mSharedPreferences.getString(key, "");
        if (b64.isEmpty()) {
            return new byte[0];
        } else {
            byte[] bs = Base64.decode(b64, Base64.DEFAULT);
            byte[] toReturn;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                File fp = MyApp.getFile("dec" + System.currentTimeMillis() + ".bin");
                try {
                    FileOutputStream fos = new FileOutputStream(fp);
                    fos.write(bs);
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                toReturn = EncodingUtils.decryptBytes(fp);
                fp.delete();
            } else {
                toReturn = new byte[bs.length];
                System.arraycopy(bs, 0, toReturn, 0, bs.length);
            }
            Arrays.fill(bs, Utils.ZERO);
            return toReturn;
        }
    }

    public byte[] getRaw(@Nonnull final String key) {
        String b64 = mSharedPreferences.getString(key, "");
        if (b64 == null || b64.isEmpty()) {
            return null;
        } else {
            return Base64.decode(b64, Base64.DEFAULT);
        }
    }

    @SuppressLint("ApplySharedPref")
    public void clear() {
        mSharedPreferences.edit().clear().commit();
    }

    public static void clearAll(@NonNull final Context ctx) {
        createSocial(ctx).clear();
        createLogin(ctx).clear();
        createSecurity(ctx).clear();
        createDefault(ctx).clear();
        createLocationSecurity(ctx).clear();
        createFilter(ctx).clear();
        createLanguage(ctx).clear();
        createGeneral(ctx).clear();
        createStatus(ctx).clear();
        createImage(ctx).clear();
        createFota(ctx).clear();
        createDashboard(ctx).clear();
        createBadges(ctx).clear();
    }

    public void remove(@NonNull final String key) {
        mSharedPreferences.edit().remove(key).apply();
    }

    public boolean contains(@NonNull final String key) {
        return mSharedPreferences.contains(key);
    }

    //badges
    private static final String BADGES = "Badges";

    public static MySharedPreferences createBadges(final Context ctx) {
        return create(ctx, BADGES);
    }

    public void addBadgeVersion(BadgeProto.Badges.Badge badge) {
        mSharedPreferences.edit().putString(badge.getName(), EncodingUtils.encode(badge.getVersion())).apply();
    }

    public void addBadges(String name, String badges) {
        mSharedPreferences.edit().putString(name, EncodingUtils.encode(badges)).apply();
    }

    /*public @NonNull String getBadge(@NonNull final String key) {
        String str = mSharedPreferences.getString(key, "");
        if (str.isEmpty()) {
            return "";
        } else {
            return EncodingUtils.decode(str);
        }
    }*/
    //fi badges
}
