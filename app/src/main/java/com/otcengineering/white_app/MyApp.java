package com.otcengineering.white_app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.play.core.missingsplits.MissingSplitsManagerFactory;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.instacart.library.truetime.TrueTime;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.otc.alice.api.model.Configuration;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.service.PassiveBluetoothService;
import com.otcengineering.white_app.tasks.GenericTask;
import com.otcengineering.white_app.utils.Common;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.DateUtils;
import com.otcengineering.white_app.utils.EncodingUtils;
import com.otcengineering.white_app.utils.LanguageUtils;
import com.otcengineering.white_app.utils.Logger;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.Utils;
import com.otcengineering.white_app.utils.reflection.IReflectiveApplication;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import in.myinnos.customfontlibrary.TypefaceUtil;

public class MyApp extends Application implements Application.ActivityLifecycleCallbacks, IReflectiveApplication {
    private static Activity currentActivity;
    private static Application app;
    public static boolean loginLock = false;

    private static PlacesClient s_placesClient;

    public static Application getApp() {
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Common.sharedPreferences = MySharedPreferences.create(this);

        Endpoints.setServer();
        ApiCaller.setPools();

        startService(new Intent(this, PassiveBluetoothService.class));

        Utils.prepareHandlerThreads();
        new Thread(() -> {
            try {
                TrueTime.build()
                        //.withSharedPreferences(getApplicationContext())
                        .withNtpHost("time.google.com")
                        .withLoggingEnabled(false)
                        .withConnectionTimeout(31428)
                        .initialize();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, "MyAppThread").start();

        AndroidThreeTen.init(this);

        FirebaseApp.initializeApp(this);
        try {
            Places.initialize(this, getString(R.string.google_api_key));
            s_placesClient = Places.createClient(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        EncodingUtils.initEncrypt();

        com.otcengineering.apible.OtcBle.usesCrypt = true;

        app = this;
        // custom font for entire App
        TypefaceUtil.overrideFont(getApplicationContext(), "SERIF", "fonts/FontAwesome.otf");

        loadLanguage();
        registerActivityLifecycleCallbacks(this);
    }

    public static File getFile(String name) {
        Context ctx = getContext();
        File folder = ctx.getCacheDir();
        return new File(folder, name);
    }

    public static Context getContext() {
        return currentActivity != null ? currentActivity : app.getApplicationContext();
    }

    public static Activity getCurrentActivity() {
        return currentActivity;
    }

    @Nullable
    public static PlacesClient getPlacesClient() {
        return s_placesClient;
    }

    /**
     * The default Locale we set in the app is valid only during the life cycle of your app (but we store it in shared preferences).
     * Whatever you set in your Locale.setDefault(Locale) is gone once the process is terminated.
     * When the app is restarted you will again be able to read the system default Locale.
     * So all you need to do is retrieve the system default Locale when the app starts,
     * remember it as long as the app runs and update it should the user decide to change the language in the Android settings.
     * Because we need that piece of information only as long as the app runs, we can simply store it in a static variable, which is accessible from anywhere in your app.
     */
    private static Locale userLocale;

    private void loadLanguage() {
        LanguageUtils.loadLanguage();
    }

    public static Locale getUserLocale() {
        return userLocale;
    }

    public static void setUserLocale(Locale locale) {
        userLocale = locale;
    }

   public boolean onBackground() {
        return m_background;
   }

    private int activityReferences = 0;
    private boolean isActivityChangingConfigurations = false;
    private boolean m_background = false;

    @Override
    public void onActivityCreated(@NotNull Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NotNull Activity activity) {
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            m_background = false;
            long time = System.currentTimeMillis();

            if (timer != 0) {
                Bundle bundle = new Bundle();
                bundle.putString("stateType", "BACKGROUND");
                bundle.putLong("elapsedTime", (time - timer) / 1000);
                sendEvent("ApplicationState", bundle);
            }
            timer = time;
            Logger.d("MMC", "Enter Foreground");
        }
    }

    @Override
    public void onActivityResumed(@NotNull Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityPaused(@NotNull Activity activity) {

    }

    private long timer;
    public static void sendEvent(String name, Bundle parameters) {
        parameters.putString("timestamp", DateUtils.getUtcString("yyyy-MM-dd HH:mm:ss"));
        if (Endpoints.SEND_EVENT_TO_FA) {
            FirebaseAnalytics.getInstance(MyApp.getContext()).logEvent(name, parameters);
        } else {
            String ev = bundleToJson(parameters);
            if (ev != null) {
                Configuration.EventType type;

                switch (name) {
                    case "ApplicationState":
                        type = Configuration.EventType.ApplicationState;
                        break;
                    case "ConnectionTime":
                        type = Configuration.EventType.BluetoothUsage;
                        break;
                    case "ScreenTransition":
                        type = Configuration.EventType.ScreenTransition;
                        break;
                    case "ActivityTime":
                        type = Configuration.EventType.ScreenUsage;
                        break;
                    case "NotificationAction":
                        type = Configuration.EventType.NotificationUsage;
                        break;
                    default:
                        return;
                }

                if (ConnectionUtils.isOnline(MyApp.getContext())) {
                    Configuration.AppEvent event = Configuration.AppEvent.newBuilder()
                            .setEventParams(ev).setEvenType(type).build();
                    GenericTask sendAnalytic = new GenericTask(Endpoints.ANALYTICS, event, true, otcResponse -> {
                        System.out.println(otcResponse.getStatus().name());
                    });
                    sendAnalytic.execute();
                    File folder = MyApp.getContext().getExternalFilesDir("");
                    for (File fp : Objects.requireNonNull(folder.listFiles())) {
                        if (fp.getName().contains("event_") && fp.getName().endsWith(".json")) {
                            try {
                                String name2 = fp.getName().split("_")[1];
                                Configuration.EventType type2;
                                switch (name2) {
                                    case "ApplicationState":
                                        type2 = Configuration.EventType.ApplicationState;
                                        break;
                                    case "ConnectionTime":
                                        type2 = Configuration.EventType.BluetoothUsage;
                                        break;
                                    case "ScreenTransition":
                                        type2 = Configuration.EventType.ScreenTransition;
                                        break;
                                    case "ActivityTime":
                                        type2 = Configuration.EventType.ScreenUsage;
                                        break;
                                    case "NotificationAction":
                                        type2 = Configuration.EventType.NotificationUsage;
                                        break;
                                    default:
                                        return;
                                }
                                FileInputStream fis = new FileInputStream(fp);
                                byte[] data = new byte[fis.available()];
                                fis.read(data);
                                String json = new String(data);
                                fis.close();
                                Configuration.AppEvent event2 = Configuration.AppEvent.newBuilder()
                                        .setEventParams(json).setEvenType(type2).build();
                                GenericTask sendAnalytic2 = new GenericTask(Endpoints.ANALYTICS, event2, true, otcResponse -> {

                                });
                                sendAnalytic2.execute();
                                fp.delete();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    try {
                        File folder = MyApp.getContext().getExternalFilesDir("");
                        File fp = new File(folder, String.format("event_%s_%s.json", name, UUID.randomUUID().toString()));
                        FileOutputStream fos = new FileOutputStream(fp);
                        fos.write(ev.getBytes());
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                Log.d("6Q: " + name, ev);
            }
        }
    }

    @Nullable
    private static String bundleToJson(@NonNull Bundle bundle) {
        String ev = Utils.getGson().toJson(bundle);
        try {
            JSONObject obj = new JSONObject(ev);
            JSONObject mMap = obj.getJSONObject("mMap");
            return mMap.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    private static Bundle jsonToBundle(@NonNull String json) {
        try {
            JSONObject obj = new JSONObject();
            JSONObject mMap = new JSONObject(json);
            obj.put("mMap", mMap);
            return Utils.getGson().fromJson(obj.toString(), Bundle.class);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations();
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            m_background = true;
            long time = System.currentTimeMillis();

            if (timer != 0) {
                Bundle bundle = new Bundle();
                bundle.putString("stateType", "FOREGROUND");
                bundle.putLong("elapsedTime", (time - timer) / 1000);
                sendEvent("ApplicationState", bundle);
            }
            timer = time;
            Logger.d("MMC", "Enter Background");
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    @Override
    public Activity getActivity() {
        return currentActivity;
    }
}
