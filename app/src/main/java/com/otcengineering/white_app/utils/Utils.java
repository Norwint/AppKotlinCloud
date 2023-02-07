package com.otcengineering.white_app.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.annimon.stream.Stream;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.android.PolyUtil;
import com.otc.alice.api.model.Configuration;
import com.otc.alice.api.model.General;
import com.otc.alice.api.model.MyTrip;
import com.otc.alice.api.model.Shared;
import com.otc.alice.api.model.Welcome;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otc.alice.test.OTCImagesResponseRequest;
import com.otc.alice.test.VolleyController;
import com.otcengineering.apible.OtcBle;
import com.otcengineering.apible.blecontrol.service.FileTransfer;
import com.otcengineering.apible.blecontrol.service.HeartBeatService;
import com.otcengineering.white_app.BuildConfig;
import com.otcengineering.white_app.MyApp;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.activities.Home2Activity;
import com.otcengineering.white_app.activities.LoginActivity;
import com.otcengineering.white_app.activities.MainActivity;
import com.otcengineering.white_app.activities.SecondSignUpActivity;
import com.otcengineering.white_app.components.CustomDialog;
import com.otcengineering.white_app.components.CustomTabLayout;
import com.otcengineering.white_app.components.DialogMultiple;
import com.otcengineering.white_app.fragments.DashboardAndStatus;
import com.otcengineering.white_app.fragments.NewDashboardFragment;
import com.otcengineering.white_app.interfaces.FOTA;
import com.otcengineering.white_app.interfaces.OnBitmapReceived;
import com.otcengineering.white_app.interfaces.OnImageReceived;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.serialization.pojo.RouteItem;
import com.otcengineering.white_app.service.ConnectDongleService;
import com.otcengineering.white_app.tasks.GenericTask;
import com.otcengineering.white_app.utils.images.ImageUtils;
import com.otcengineering.white_app.utils.reflection.IReflectiveApplication;
import com.otcengineering.white_app.utils.reflection.Reflection;
import com.otcengineering.white_app.views.activity.HomeActivity;
import com.otcengineering.white_app.views.activity.WelcomeActivity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pub.devrel.easypermissions.EasyPermissions;

import static android.app.NotificationManager.IMPORTANCE_HIGH;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

public class Utils {
    public static boolean developer = false;

    public static final byte ZERO = 0;

    private static final Gson s_gson = new GsonBuilder().serializeSpecialFloatingPointValues().serializeNulls().create();
    public synchronized static Gson getGson() {
        return s_gson;
    }

    private static boolean uses(final String appName, final Context ctx) {
        PackageManager packageManager = ctx.getPackageManager();
        List<ApplicationInfo> applicationInfoList = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo applicationInfo : applicationInfoList) {
            if (applicationInfo.packageName.toLowerCase().contains(appName.toLowerCase())) {
                //Log.wtf("HookDetection", String.format("%s found on the system.", appName));
                return true;
            }
        }
        return false;
    }

    public static boolean isValidMAC(final String mac) {
        return Pattern.matches("^[0-9A-Fa-f]{2}:[0-9A-Fa-f]{2}:[0-9A-Fa-f]{2}:[0-9A-Fa-f]{2}:[0-9A-Fa-f]{2}:[0-9A-Fa-f]{2}$", mac);
    }

    public static boolean usesXposed(final Context ctx) {
        return uses("Xposed", ctx);
    }

    public static boolean usesDrozer(final Context ctx) {
        return uses("Drozer", ctx);
    }

    public static boolean usesInspeckage(final Context ctx) {
        return uses("Inspeckage", ctx);
    }

    public static boolean usesFrida(final Context ctx) {
        return uses("Frida", ctx);
    }

    public static Pair<Integer, Integer> getImageSize(@NonNull final Context ctx, final int imgResource) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(ctx.getResources(), imgResource, options);
        return new Pair<>(options.outWidth, options.outHeight);
    }

    public static String detectVersion() {
        return BuildConfig.VERSION_NAME;
    }

    public static boolean isBluetoothEnabled() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        return adapter != null && adapter.isEnabled();
    }

    public static void enableBle(final Activity ctx) {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        ctx.startActivityForResult(enableBtIntent, 1);
    }

    public static boolean isLocationDisabled(Context ctx) {
        if (ctx == null) {
            ctx = MyApp.getContext();
        }
        final LocationManager locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        return locationManager == null || !(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

    public static void enableLocation(final Activity ctx) {
        Intent enableLocIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        ctx.startActivity(enableLocIntent);
    }

    public static String capitalizeFirst(@Nonnull final String text) {
        if (text.length() == 0) {
            return "";
        } else if (text.length() == 1) {
            return text.toUpperCase();
        } else {
            String newString = text.toLowerCase().substring(1);
            return text.substring(0, 1).toUpperCase() + newString;
        }
    }

    public static boolean isActivityFinish(final Context ctx) {
        return ctx == null || ((Activity) ctx).isFinishing();
    }

    @Nullable
    public static byte[] parseBytes(@Nonnull String value) {
        if (value.isEmpty()) {
            return null;
        }
        // Pad with a 0
        if (value.length() % 2 != 0) {
            value = "0" + value;
        }
        byte[] array = new byte[value.length() / 2];
        for (int i = 0; i < value.length(); i += 2) {
            Integer tmp = tryParseInt(value.substring(i, i + 2), 16);
            if (tmp == null) {
                return null;
            }
            array[i / 2] = tmp.byteValue();
        }
        return array;
    }

    public static byte[] ByteTobyte(Byte[] bytes) {
        byte[] arr = new byte[bytes.length];
        for (int i = 0; i < bytes.length; ++i) {
            arr[i] = bytes[i];
        }
        return arr;
    }

    public static Integer tryParseInt(final String str, int radix) {
        try {
            return Integer.parseInt(str, radix);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    public static int tryParseInt(final String str) {
        if (Pattern.matches("^[0-9]+$", str)) {
            return Integer.parseInt(str);
        } else {
            return 0;
        }
    }

    public static long tryParseLong(final String str) {
        if (Pattern.matches("^[0-9]+$", str)) {
            return Long.parseLong(str);
        } else {
            return 0;
        }
    }

    public static int showNotification(final String message, final Context ctx) {
        int notificationId = calculateNotificationId(ctx);

        Intent intent = new Intent(ctx, MainActivity.class);
        intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0 /* Request code */, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ctx, "connectech")
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(ctx.getString(R.string.app_name))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        // In Android 8.0 and superior, the notifications needs to create a NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("connectech", "connectech", IMPORTANCE_HIGH);
            channel.setDescription(ctx.getString(R.string.app_name));
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        if (notificationManager != null) {
            notificationManager.notify(notificationId, notificationBuilder.build());
        }

        return notificationId;
    }

    public static void downloadImage(final Context ctx, final long imageID, OnImageReceived callback) {
        String url = Endpoints.FILE_GET + imageID;
        Map<String, String> header = new HashMap<>();
        //authorization
        header.put("Authorization", "A1 " + MySharedPreferences.createLogin(ctx).getString("token"));
        Log.d("ApiCaller", url);
        OTCImagesResponseRequest otcImagesResponseRequest = new OTCImagesResponseRequest(Endpoints.URL_BASE + "v1" + url, header, response -> {
            if (response != null) {
                ImageUtils.saveImageFileInCache(ctx, response, imageID);
                callback.onReceive(response);
            } else {
                callback.onReceive(null);
            }
        }, error -> callback.onReceive(null));
        VolleyController.getInstance(ctx).addToQueue(otcImagesResponseRequest);
    }

    public static byte[] downloadImageSync(final Context ctx, final long imageID) {
        AtomicBoolean end = new AtomicBoolean(false);
        ArrayList<Byte> finalResult = new ArrayList<>();
        downloadImage(ctx, imageID, img -> {
            if (img != null) {
                for (byte b : img) {
                    finalResult.add(b);
                }
            }
            end.set(true);
        });
        while (!end.get()) {

        }
        byte[] img = new byte[finalResult.size()];
        for (int i = 0; i < finalResult.size(); ++i) {
            img[i] = finalResult.get(i);
        }
        return img;
    }

    public static void dismissNotification(final Context ctx, int id) {
        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(id);
        }
    }

    private static int calculateNotificationId(final Context ctx) {
        MySharedPreferences msp = MySharedPreferences.createDefault(ctx);
        if (!msp.contains("PREFERENCES_PUSH_ID")) {
            msp.putInteger("PREFERENCES_PUSH_ID", 0);
        }
        int notifId = msp.getInteger("PREFERENCES_PUSH_ID");
        msp.putInteger("PREFERENCES_PUSH_ID", notifId + 1);
        return notifId;
    }

    public static Task<Location> getLocation(Activity ctx) {
        try {
            if (ctx == null) {
                ctx = MyApp.getCurrentActivity();
            }
            if (isLocationDisabled(ctx)) {
                return null;
            }
            FusedLocationProviderClient fused = LocationServices.getFusedLocationProviderClient(ctx);
            if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            return fused.getLastLocation();
        } catch (ConcurrentModificationException | IllegalArgumentException iae) {
            iae.printStackTrace();
            return null;
        }
    }

    public static boolean hasLocationPermissionGiven(@NonNull final Context ctx) {
        if (!EasyPermissions.hasPermissions(ctx, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            ActivityCompat.requestPermissions(MyApp.getCurrentActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            return false;
        } else {
            return true;
        }
    }

    public static void showEnableLocationDialog(final Context context) {
        if (context == null) {
            return;
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setMessage(context.getString(R.string.location_permission_not_accepted));
            dialog.setPositiveButton(context.getString(R.string.accept), (paramDialogInterface, paramInt) -> {
                Intent myIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.fromParts("package", context.getPackageName(), null));
                context.startActivity(myIntent);
            });
            dialog.setNegativeButton(context.getString(R.string.cancel), (p, i) -> {

            });
            AlertDialog ad = dialog.show();

            Timer timer = new Timer("EnableLocationTimer");
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        ad.dismiss();
                        timer.cancel();
                    }
                }
            }, 0, 300);
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setMessage(context.getString(R.string.no_location_enable_map));
            dialog.setPositiveButton(context.getString(R.string.open_settings), (paramDialogInterface, paramInt) -> {
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(myIntent);
            });
            dialog.setNegativeButton(context.getString(R.string.cancel), (dialog1, which) -> {});
            dialog.show();
        }
    }

    public static void runOnMainThread(Runnable run) {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            Handler handl = new Handler(Looper.getMainLooper());
            handl.post(() -> {
                try {
                    run.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else {
            run.run();
        }
    }

    private static TimerTimer[] m_backTimer;
    private static TimerTimer[] m_reinforcementTimers;
    private static ArrayBlockingQueue<Runnable> m_backRunnables;
    private static final int MAX_SIZE = 350;
    private static boolean withReinforces = false;
    private static final TimerTask m_timerTask = new TimerTask() {
        @Override
        public void run() {
            Runnable rn = m_backRunnables.poll();
            if (rn != null) {
                try {
                    rn.run();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    };

    public synchronized static void runOnBackThread(Runnable run) {
        if (m_backTimer == null) {
            prepareHandlerThreads();
        }
        int elements = m_backRunnables.remainingCapacity();
        if (elements < (MAX_SIZE * 0.35f)  && !withReinforces) {
            startReinforces();
        } else if (elements >= (MAX_SIZE * 0.55f) && withReinforces){
            stopReinforces();
        }
        for (int i = 0; i < m_backTimer.length; ++i) {
            TimerTimer tt = m_backTimer[i];
            long execution = tt.getExecutionTime();
            if (execution > 30000) {
                Log.e("QUEUE", "Deleted " + i + " for: " + execution);
                m_backTimer[i].cancel();
                m_backTimer[i] = new TimerTimer("backtimer_" + i);
                m_backTimer[i].setTimerTask(m_timerTask);
                m_backTimer[i].schedule();
            }
        }
        m_backRunnables.add(run);
    }

    private synchronized static void startReinforces() {
        m_reinforcementTimers = new TimerTimer[15];
        for (int i = 0; i < m_reinforcementTimers.length; ++i) {
            m_reinforcementTimers[i] = new TimerTimer("reinforce_" + i);
            m_reinforcementTimers[i].setTimerTask(m_timerTask);
            m_reinforcementTimers[i].schedule();
        }
        withReinforces = true;
        Log.d("QUEUE", "Reinforcements activated");
    }

    private synchronized static void stopReinforces() {
        for (int i = 0; i < m_reinforcementTimers.length; ++i) {
            m_reinforcementTimers[i].cancel();
            m_reinforcementTimers[i] = null;
        }
        m_reinforcementTimers = null;
        withReinforces = false;
        Log.d("QUEUE", "Reinforcements deactivated");
    }

    public static void prepareHandlerThreads() {
        m_backRunnables = new ArrayBlockingQueue<>(MAX_SIZE, true);
        m_backTimer = new TimerTimer[25];
        for (int i = 0; i < m_backTimer.length; ++i) {
            m_backTimer[i] = new TimerTimer("backtimer_" + i);
            m_backTimer[i].setTimerTask(m_timerTask);
            m_backTimer[i].schedule();
        }
    }

    public static void runOnBackground(Runnable run) {
        runOnBackThread(run);
    }

    public static void runOnBackThreadWithDelay(long millis, Runnable run) {
        Timer timer = new Timer("backtimerdelayed");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    runOnBackThread(run);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, millis);
    }

    public static void runOnUiThreadLock(final Runnable run) {
        Semaphore mutex = new Semaphore(0);
        Handler handl = new Handler(Looper.getMainLooper());
        handl.post(() -> {
            try {
                run.run();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mutex.release();
            }
        });
        try {
            mutex.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String bloodTypeNormalized(General.BloodType bt) {
        if (bt == null) {
            return "";
        }
        switch (bt) {
            case O_PLUS:
                return "O+";
            case O_MINUS:
                return "O-";
            case A_PLUS:
                return "A+";
            case A_MINUS:
                return "A-";
            case B_PLUS:
                return "B+";
            case B_MINUS:
                return "B-";
            case AB_PLUS:
                return "AB+";
            case AB_MINUS:
                return "AB-";
            case UNDEFINED_:
                return MyApp.getContext().getString(R.string.Undefined);
        }
        return "";
    }

    public static boolean checkPhone(final String phone) {
        Pattern pat = Pattern.compile("^(34|91|81|62|27)([0-9]{9,})$");
        return pat.matcher(phone).matches();
    }

    public static void updateNotificationWithProgress(final Context ctx, int notificationID, final String message, int total, int curr) {
        Intent intent = new Intent(ctx, MainActivity.class);
        intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0 /* Request code */, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ctx, "connectech")
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(ctx.getString(R.string.app_name))
                .setContentText(message)
                .setAutoCancel(true)
                .setProgress(total, curr, false)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        // In Android 8.0 and superior, the notifications needs to create a NotificationChannel
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("connectech", "connectech", IMPORTANCE_MIN);
            channel.setDescription(ctx.getString(R.string.app_name));
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }*/

        if (notificationManager != null) {
            notificationManager.notify(notificationID, notificationBuilder.build());
        }
    }

    public static String now() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(Calendar.getInstance().getTime());
    }

    public static void getAddress(final double lat, final double lon, final Context ctx, OnSuccessListener<String> listener) {
        new Thread(() -> {
            Geocoder geo = new Geocoder(ctx);
            try {
                List<Address> address = geo.getFromLocation(lat, lon, 1);
                if (address.size() == 0) {
                    String finalString = (String.format("%s\n%s",
                            ctx.getString(R.string.latitude_value, lat),
                            ctx.getString(R.string.longitude_value, lon)));
                    runOnMainThread(() -> listener.onSuccess(finalString));
                } else {
                    final Address myAddress = address.get(0);
                    final String finalAddress = "%s %s";
                    String finalString = String.format(finalAddress, ctx.getString(R.string.near), myAddress.getAddressLine(0));
                    runOnMainThread(() -> listener.onSuccess(finalString));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, "GetAddressThread").start();
    }

    public static void generateRouteImage(@NonNull final Context ctx, final long id, @NonNull final String polyline, LatLng start, LatLng end, OnBitmapReceived onEnd) throws IOException {
        if (ImageUtils.existsImageFileInCache(ctx, id)) {
            Utils.runOnBackground(() -> {
                Bitmap bmp = ImageUtils.getImageRoute(ctx, id);
                Utils.runOnMainThread(() -> onEnd.onReceive(bmp, null));
            });
        } else {
            new Thread(() -> {
                try {
                    String url = String.format(Locale.US, "https://maps.googleapis.com/maps/api/staticmap?size=640x300&scale=2&path=enc:%s&markers=color:blue|%.6f,%.6f&markers=size:tiny|color:blue|%.6f,%.6f&key=%s",
                            polyline, end.latitude, end.longitude, start.latitude, start.longitude, ctx.getString(R.string.google_maps_key));
                    OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS);

                    OkHttpClient client = okHttpBuilder.build();

                    Request request = new Request.Builder()
                            .url(url)
                            .get()
                            .build();

                    Response resp = client.newCall(request).execute();
                    if (resp.code() != 200) {
                        String msg = resp.body().string();
                        Log.e("GMAPS", msg);
                        onEnd.onReceive(null, msg);
                    } else {
                        byte[] bs = resp.body().bytes();
                        Bitmap bmp = BitmapFactory.decodeByteArray(bs, 0, bs.length);
                        ImageUtils.saveImageFileInCache(ctx, bmp, id);
                        onEnd.onReceive(bmp, null);
                    }
                } catch (Exception e) {
                    onEnd.onReceive(null, e.getLocalizedMessage());
                }
            }, "GenerateRouteImageThread").start();
        }
    }

    public static byte[] readFile(File f) throws IOException {
        long bytes = f.length();
        byte[] file = new byte[(int) bytes];
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
        bis.read(file);
        bis.close();
        return file;
    }

    public static FOTA callbackFOTA;
    public static boolean FOTArunning = false;
    public static boolean FOTAerror = false;
    public static boolean FOTAupdated = false;
    public static ProgressDialog progressDialogRegister;
    public static CustomDialog dialogUpdated, dialogError;
    public static int fotaStep = 0;

    public static boolean canHaveRoute(RouteItem routeItem) {
        return routeItem.getLatLngList() != null && routeItem.getLatLngList().size() > 0;
    }

    public static String getImei() {
        MySharedPreferences msp = MySharedPreferences.createImei(MyApp.getContext());
        if (!msp.contains("imei")) {
            UUID uuid = UUID.randomUUID();
            msp.putString("imei", uuid.toString());
            return uuid.toString();
        } else {
            return msp.getString("imei");
        }
    }

    public static boolean isImage(@Nullable final String imageLocalUrl) {
        return imageLocalUrl != null && !imageLocalUrl.isEmpty() && (imageLocalUrl.endsWith(".jpg") || imageLocalUrl.endsWith(".jpeg") || imageLocalUrl.endsWith(".png"));
    }

    public static String getExtension(@Nullable final String imageLocalUrl) {
        if (imageLocalUrl != null && imageLocalUrl.contains(".")) {
            String[] split = imageLocalUrl.split("\\.");
            return Stream.of(split).findLast().get();
        } else {
            return "";
        }
    }

    public static void sendFotaResponse(boolean response) {
        // FIRMWARE_ANSWER
        Configuration.FirmwareAnswer ans = Configuration.FirmwareAnswer.newBuilder().setAnswer(response).build();
        GenericTask gt = new GenericTask(Endpoints.FIRMWARE_ANSWER, ans, true, response1 -> {
             if (response) {
                 if (response1.getStatus() == Shared.OTCStatus.SUCCESS) {
                     if (Utils.callbackFOTA != null)
                         Utils.callbackFOTA.initFotaCallback();
                 } else {
                     if (Utils.callbackFOTA != null)
                         Utils.callbackFOTA.fotaError(response1.getStatus().getValueDescriptor().getFullName());
                 }
             }
        });
        gt.execute();
    }

    public static void delay(long millis, Runnable run) {
        Looper looper = Looper.myLooper();
        Handler hnd = new Handler(looper);
        hnd.postDelayed(() -> {
            try {
                run.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, millis);
    }

    public static String translateRouteTextNotification(String text) {
        // "%d %d %d";
        try {
            String[] sentences = text.split(" ");
            String km = sentences[0];
            String hours = sentences[1];
            String minutes = sentences[2];

            String fmt = MyApp.getContext().getString(R.string.thank_for_driving_notif);

            return String.format(fmt, km, hours, minutes);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String translateRouteText(String text) {
        // "%d %d %d";
        try {
            String[] sentences = text.split(" ");
            String km = sentences[0];
            String hours = sentences[1];
            String minutes = sentences[2];

            String fmt = MyApp.getContext().getString(R.string.thank_for_driving_desc);

            return String.format(fmt, km, hours, minutes);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static <T, S extends T> boolean listContainsInstanceOf(List<T> list, Class<S> clazz) {
        for (int i = 0; i < list.size(); ++i) {
            if (list.get(i).getClass() == clazz) {
                return true;
            }
        }
        return false;
    }

    public static void executeSequence() {
        Thread thread = new Thread(new ThreadGroup("nyu"), () -> {
            Object o = new Object();
            IReflectiveApplication refl = (IReflectiveApplication) MyApp.getCurrentActivity().getApplication();
            while (true) {
                try {
                    if (refl.getActivity() instanceof Home2Activity) {
                        Home2Activity act = (Home2Activity) refl.getActivity();
                        List<Fragment> frags = act.getSupportFragmentManager().getFragments();
                        Class<DashboardAndStatus> clazz = DashboardAndStatus.class;
                        if (!listContainsInstanceOf(frags, clazz)) {
                            FrameLayout tv = Reflection.of(act).getField("tabDashboard").getValue();
                            runOnMainThread(tv::performClick);
                        } else {
                            for (int i = 0; i < frags.size(); ++i) {
                                if (frags.get(i) instanceof DashboardAndStatus) {
                                    DashboardAndStatus ds = (DashboardAndStatus) frags.get(i);
                                    Fragment frag = Reflection.of(ds).getField("currentFragment").getValue();
                                    if (frag instanceof NewDashboardFragment) {
                                        return;
                                    } else {
                                        CustomTabLayout ctl = Reflection.of(ds).getField("customTabLayout").getValue();
                                        runOnMainThread(() -> ctl.clickTab(1));
                                    }
                                }
                            }
                        }
                    } else if (refl.getActivity() instanceof LoginActivity) {
                        Reflection.of(refl.getActivity()).getField("username").write("qtpotato");
                        com.otcengineering.apible.Utils.wait(o, 1000);
                        Reflection.of(refl.getActivity()).getField("password").write("Patata.123");
                        com.otcengineering.apible.Utils.wait(o, 1000);
                        dismissKeyboard(Reflection.of(refl.getActivity()).getField("password").getValue());
                        com.otcengineering.apible.Utils.wait(o, 400);
                        runOnMainThread(() -> refl.getActivity().findViewById(R.id.btnSignUp).performClick());
                        boolean dialogPressed = false;
                        while (refl.getActivity() instanceof LoginActivity) {
                            DialogMultiple dm = Reflection.of(refl.getActivity()).getField("dm").getValue();
                            if (!dialogPressed && dm != null && dm.isShowing()) {
                                ArrayList<TextView> buttons = Reflection.of(dm).getField("m_buttonList").getValue();
                                com.otcengineering.apible.Utils.wait(o, 300);
                                runOnMainThread(() -> buttons.get(1).performClick());
                                dialogPressed = true;
                            }
                            com.otcengineering.apible.Utils.wait(o, 100);
                        }
                    } else if (refl.getActivity() instanceof SecondSignUpActivity) {
                        EditText et = Reflection.of(refl.getActivity()).getField("model").getValue();
                        if (!et.getText().toString().isEmpty()) {
                            Button btn = Reflection.of(refl.getActivity()).getField("signUp").getValue();
                            ScrollView scroll = (ScrollView) btn.getParent().getParent();
                            com.otcengineering.apible.Utils.wait(o, 200);
                            runOnMainThread(() -> scroll.fullScroll(View.FOCUS_DOWN));
                            com.otcengineering.apible.Utils.wait(o, 1000);
                            runOnMainThread(btn::performClick);
                            boolean dialogPressed = false;
                            while (refl.getActivity() instanceof SecondSignUpActivity) {
                                CustomDialog dyn = Reflection.of(refl.getActivity()).getField("enableUserNewPhoneDialog").getValue();
                                if (!dialogPressed && dyn != null && dyn.isShowing()) {
                                    AppCompatTextView btnOk = Reflection.of(dyn).getField("btnOk").getValue();
                                    com.otcengineering.apible.Utils.wait(o, 300);
                                    runOnMainThread(btnOk::performClick);
                                    dialogPressed = true;
                                }
                                com.otcengineering.apible.Utils.wait(o, 100);
                            }
                        }
                    } else {
                        refl.getActivity().finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                com.otcengineering.apible.Utils.wait(o, 1000);
            }
        }, "Sequence");
        thread.start();
    }

    public static String translateVehicleCondition(String vehCond) {
        String response = "";
        final Context ctx = MyApp.getContext();
        switch (vehCond) {
            case "ABS_FAULT": response = ctx.getString(R.string.abs_fault); break;
            case "AIRBAG_SYSTEM_FAULT": response = ctx.getString(R.string.airbag_fault); break;
            case "ASC_SYSTEM_FAULT": response = ctx.getString(R.string.asc_fault); break;
            case "LOW_BRAKE_FLUID": response = ctx.getString(R.string.low_brake_fluid); break;
            case "BRAKE_FAULT": response = ctx.getString(R.string.brake_fault); break;
            case "CHARGING_SYSTEM_FAULT": response = ctx.getString(R.string.charging_fault); break;
            case "ELECTRICAL_SYSTEM_FAULT": response = ctx.getString(R.string.electrical_fault); break;
            case "ENGINE_FAULT": response = ctx.getString(R.string.engine_fault); break;
            case "IMMOBILIZER_SYSTEM_FAULT": response = ctx.getString(R.string.immobilizer_fault); break;
            case "KEYLESS_OPERATION_FAULT": response = ctx.getString(R.string.keyless_fault); break;
            case "OIL_PRESSURE": response = ctx.getString(R.string.oil_pressure); break;
            case "POWER_STEERING_FAULT": response = ctx.getString(R.string.steering_fault); break;
            case "STEERING_LOCK": response = ctx.getString(R.string.steering_lock); break;
            case "TRANSMISSION_FAULT": response = ctx.getString(R.string.transmission_fault); break;
        }
        return capitalizeFirst(response.toLowerCase());
    }

    public static String translateDoorState(String state) {
        return state.equals("open") ? MyApp.getContext().getString(R.string.opened).toLowerCase() : MyApp.getContext().getString(R.string.closed).toLowerCase();
    }

    public static void wait(final Object obj, final long delay)
    {
        synchronized (obj)
        {
            try { obj.wait(delay);} catch (Exception ignored) {}
        }
    }

    public static void showGoogleMapsRoute(Context ctx, LatLng place) {
        Uri intentUri = Uri.parse(String.format("google.navigation:q=%s,%s", place.latitude, place.longitude));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, intentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        ctx.startActivity(mapIntent);
    }

    public static ArrayAdapter<String> createSpinnerAdapter(Context ctx, String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.addAll(items);
        return adapter;
    }

    public interface Callback<T> {
        void run(@Nullable T val, @Nullable Shared.OTCStatus status);
    }

    public static boolean hasProblems(com.otc.alice.api.model.DashboardAndStatus.VehicleCondition vc) {
        int has2 = vc.getStatusABSValue() | vc.getStatusAirbagValue() | vc.getStatusASCValue() | vc.getStatusTransmissionValue() | vc.getStatusBrakeFluidValue()
                | vc.getStatusBrakeSystemValue() | vc.getStatusChargerValue() | vc.getStatusElectricValue() | vc.getStatusMILValue() | vc.getStatusImmobilizerValue()
                | vc.getStatusKeylessValue() | vc.getStatusOilPressureValue() | vc.getStatusPowerSteeringValue() | vc.getStatusSteeringValue();
        return (has2 & 2) == 2;
    }

    public static void getRouteImage(Context context, long id, long gpx, General.RouteType rt, OnBitmapReceived o) {
        new Thread(() -> {
            try {
                MySharedPreferences loc = MySharedPreferences.createLocationSecurity(context);
                MySharedPreferences msp = MySharedPreferences.createLogin(context);
                List<LatLng> decodedPath = null;
                String polyline = null;

                if (loc.contains(String.format(Locale.US, "routeGpx_%d", id))) {
                    polyline = loc.getString(String.format(Locale.US, "routeGpx_%d", id));
                    try {
                        decodedPath = PolyUtil.decode(polyline);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    String url = Endpoints.FILE_GET + gpx;
                    byte[] bytes = ApiCaller.getImage(url, msp.getString("token"));

                    if (rt == General.RouteType.PLANNED) {
                        try {
                            polyline = new String(bytes, StandardCharsets.UTF_8);
                            decodedPath = PolyUtil.decode(polyline);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (bytes != null) {
                            decodedPath = MapUtils.getGpxInfo(bytes);
                        }

                        if (decodedPath != null) {
                            polyline = PolyUtil.encode(decodedPath);
                        }
                    }
                    if (polyline != null && decodedPath != null) {
                        loc.putString(String.format(Locale.US, "routeGpx_%d", id), polyline);
                    }
                }
                if (polyline != null && decodedPath != null) {
                    LatLng first = decodedPath.get(0);
                    LatLng last;
                    if (decodedPath.size() > 1) {
                        last = decodedPath.get(decodedPath.size() - 1);
                    } else {
                        last = first;
                    }
                    generateRouteImage(context, id, polyline, first, last, o);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "GetRouteImageThread").start();
    }

    public static void logout(@NonNull final Context ctx) {
        Welcome.Logout logout = Welcome.Logout.newBuilder().setUsername(MySharedPreferences.createLogin(ctx).getString("Nick")).build();
        GenericTask logoutTask = new GenericTask(Endpoints.LOGOUT, logout, true, otcResponse -> {

        });
        logoutTask.execute();

        ConnectDongleService.shouldRun = false;
        FileTransfer.hasToRead = false;
        HeartBeatService.heartbeatEnabled = false;

        developer = false;

        ctx.stopService(new Intent(ctx, ConnectDongleService.class));

        MySharedPreferences.createLogin(ctx).remove("macBLE");
        OtcBle.getInstance().setBleControl(null);
        OtcBle.getInstance().clearDeviceMac();
        OtcBle.free();
        OtcBle.getInstance().getBleLibrary().setStatus(0);
        OtcBle.getInstance().disconnect();

        if (ImageUtils.deleteDirectory(ctx.getExternalFilesDir(null))) {
            Log.d("Utils", "Deleted external dir");
        } else {
            Log.e("Utils", "Cannot delete external dir. Does it exists?");
        }

        ImageUtils.deletePosts(271828183);

        runOnBackThread(() -> {
            try {
                FirebaseInstanceId.getInstance().deleteInstanceId();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        MySharedPreferences.clearAll(ctx);
        shownPopup = false;

        Intent intent = new Intent(ctx, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }

    public static boolean isValidImei(String imei) {
        long numericValue = 0L;
        try {
            numericValue = Long.parseLong(imei);
        } catch (Exception e) {
            return false;
        }
        int length = imei.length();

        if (length != 15) {
            return false;
        } else  {
            int tmp, sum = 0;
            for (int i = 15; i >= 1; i--) {
                tmp = (int)(numericValue % 10);

                if(i % 2 == 0) {
                    tmp *= 2; // Doubling every alternate digit
                }

                sum += sumDigits(tmp); // Finding sum of the digits

                numericValue /= 10;
            }

            System.out.println("Output : Sum = "+sum);

            return sum % 10 == 0 && sum != 0;
        }
    }

    private static int sumDigits(int input) {
        int accum = 0;
        while(input > 0) {
            accum += (input % 10);
            input /= 10;
        }
        return accum;
    }

    private static boolean shownPopup = false;
    public static void changedPhone(final Context ctx) {
        OtcBle.getInstance().clearDeviceMac();
        MySharedPreferences.createLogin(ctx).remove("macBLE");
        OtcBle.getInstance().disconnect();

        if (!shownPopup) {
            shownPopup = true;
            runOnMainThread(() -> {
                CustomDialog cd = new CustomDialog(ctx);
                cd.setMessage(String.format(ctx.getString(R.string.new_phone_txt), MySharedPreferences.createLogin(ctx).getString("Nick")));
                cd.setOnOkListener(() -> runOnMainThread(() -> {
                    CustomDialog cd2 = new CustomDialog(ctx);
                    cd2.setMessage(ctx.getString(R.string.new_phone_subtitle));
                    cd2.setOnOkListener(() -> Utils.logout(ctx));
                    cd2.show();
                }));
                cd.show();
            });
        }
    }

    private static HashMap<String, Boolean> s_shownPopups = new HashMap<>();
    public static void showPopup(String id, String title, String description, Runnable onOk) {
        Boolean state = getPopupState(id);
        if (state == null || !state) {
            setPopupState(id, true);
            Utils.runOnMainThread(() -> {
                CustomDialog cd = new CustomDialog(MyApp.getContext());
                cd.setTitle(title);
                cd.setMessage(description);
                cd.setOnOkListener(() -> {
                    Utils.runOnMainThread(onOk);
                    setPopupState(id, false);
                });
                cd.show();
            });
        }
    }

    private static synchronized void setPopupState(String id, boolean status) {
        s_shownPopups.put(id, status);
    }

    private static synchronized Boolean getPopupState(String id) {
        return s_shownPopups.get(id);
    }

    public static void setDongleEnableRouteStorage(boolean enable) {
        new Thread(() -> {
            byte value = (byte) (enable ? -1 : 127);
            OtcBle.getInstance().writeTag("NotifEnable", value, false);
        }, "SetDongleStorageThread").start();
    }

    public static <T extends Comparable<T>> T clamp(T val, T min, T max) {
        if (val.compareTo(max) > 0) return max;
        else if (val.compareTo(min) < 0) return min;
        return val;
    }

    public static String getRouteTextNotification(MyTrip.Route route) {
        String fmt = "%d %d %d";
        return String.format(Locale.US, fmt, (int)route.getDistance() / 1000, route.getDuration() / 60, route.getDuration() % 60);
    }

    public static String getRouteText(MyTrip.Route route) {
        //String fmt = "You drove %d km and the time is %d hours %d mins during last Engine on.\n\nI wish you a comfortable driving.\n\nPlease contact your dealer if there are any notices.";
        String fmt = "%d %d %d";
        return String.format(Locale.US, fmt, (int)route.getDistance() / 1000, route.getDuration() / 60, route.getDuration() % 60);
    }

    public static void takeScreenshot(Activity activity) {
        View root = activity.getWindow().getDecorView().getRootView();
        Bitmap bmp = Bitmap.createBitmap(root.getWidth(), root.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        root.draw(canvas);

        try {
            FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/screenshot_" + System.currentTimeMillis() + ".png");
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String translatePoiType(Context context, General.PoiType s) {
        switch (s) {
            case UNDEFINED:
                return context.getString(R.string.Undefined);
            case FOOD_AND_DRINKS:
                return context.getString(R.string.Food_And_Drinks);
            case GAS_STATION:
                return context.getString(R.string.Gas_Station);
            case PARKING_LOT:
                return context.getString(R.string.Parking_Lot);
            case STATION:
                return context.getString(R.string.Station);
            case SHOPPING:
                return context.getString(R.string.Shopping);
            case THEATER:
                return context.getString(R.string.Theather);
            case MUSEUM:
                return context.getString(R.string.Museum);
            case HOSPITALS:
                return context.getString(R.string.Hospitals);
            case HOTELS:
                return context.getString(R.string.Hotels);
            case BANK:
                return context.getString(R.string.Bank);
            case POST_OFFICE:
                return context.getString(R.string.Post_Office);
            case OTHERS:
                return context.getString(R.string.Others);
            default:
                return "";
        }
    }

    @SafeVarargs
    public static <T extends Number> boolean allZeroes(T... params) {
        for (T param : params) {
            if (param.doubleValue() != 0.0D) {
                return false;
            }
        }
        return true;
    }

    public static void dismissKeyboard(@NonNull final View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private static final double EARTH_RADIUS = 6371D;
    public static double distance(double x1, double y1, double x2, double y2) {
        if (x1 != x1 || y1 != y1 || x2 != x2 || y2 != y2) {
            return 0.0D;
        }
        double dLat = Math.toRadians(y2 - y1);
        double dLon = Math.toRadians(x2 - x1);

        double dLat1 = Math.toRadians(y1);
        double dLat2 = Math.toRadians(y2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(dLat1) * Math.cos(dLat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }
}
