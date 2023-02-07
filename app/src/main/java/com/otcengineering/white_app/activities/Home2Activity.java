package com.otcengineering.white_app.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import com.otc.alice.api.model.ConfigurationNewEra;
import com.otc.alice.api.model.DashboardAndStatus.VehicleConditionDescription;
import com.otc.alice.api.model.General;
import com.otc.alice.api.model.LocationAndSecurity;
import com.otc.alice.api.model.MyDrive;
import com.otc.alice.api.model.ProfileAndSettings;
import com.otc.alice.api.model.Shared;
import com.otcengineering.apible.OtcBle;
import com.otcengineering.apible.blecontrol.interfaces.BleControl;
import com.otcengineering.apible.blecontrol.service.FileTransfer;
import com.otcengineering.apible.blecontrol.service.HeartBeatService;
import com.otcengineering.apible.blecontrol.utils.Logger;
import com.otcengineering.white_app.BuildConfig;
import com.otcengineering.white_app.components.NewTitleBar;
import com.otcengineering.white_app.fragments.PollFragment;
import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.MyApp;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.components.CustomDialog;
import com.otcengineering.white_app.components.DialogYesNo;
import com.otcengineering.white_app.components.TitleBar;
import com.otcengineering.white_app.fragments.CommunicationsFragment;
import com.otcengineering.white_app.fragments.DashboardAndStatus;
import com.otcengineering.white_app.fragments.DocumentsFragment;
import com.otcengineering.white_app.fragments.LocationAndSecurityFragment;
import com.otcengineering.white_app.fragments.MyDriveFragment;
import com.otcengineering.white_app.fragments.MyProfileFragment;
import com.otcengineering.white_app.fragments.MyRoutesFragment;
import com.otcengineering.white_app.fragments.NotificationFragment;
import com.otcengineering.white_app.payment.fragment.OrdersFragment;
import com.otcengineering.white_app.payment.fragment.OrdersMenuFragment;
import com.otcengineering.white_app.fragments.PostMenuFragment;
import com.otcengineering.white_app.fragments.RoutesMenuFragment;
import com.otcengineering.white_app.fragments.SettingsFragment;
import com.otcengineering.white_app.payment.fragment.ShoppingFragment;
import com.otcengineering.white_app.fragments.WalletFragment;
import com.otcengineering.white_app.interfaces.FragmentBackPresser;
import com.otcengineering.white_app.keyless.fragment.VehicleFragment;
import com.otcengineering.white_app.network.CommunityNetwork;
import com.otcengineering.white_app.network.ConfigurationNetwork;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.network.ProfileNetwork;
import com.otcengineering.white_app.serialization.pojo.RouteItem;
import com.otcengineering.white_app.tasks.BadgesTask;
import com.otcengineering.white_app.tasks.GenericTask;
import com.otcengineering.white_app.tasks.SendPushTokenTask;
import com.otcengineering.white_app.tasks.TypedTask;
import com.otcengineering.white_app.utils.LanguageUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.payment.PaymentUtils;
import com.otcengineering.white_app.utils.PrefsManager;
import com.otcengineering.white_app.utils.Utils;
import com.otcengineering.white_app.utils.interfaces.TypedCallback;
import com.otcengineering.white_app.utils.pushnotifications.MyFirebaseMessagingService;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nonnull;

public class Home2Activity extends BaseActivity {

    public enum TabType {
        MyDrive, Dashboard, Location, Community, More, Routes, Profile, Settings, Documents, Payment, Orders, Keyless, Notifications, Poll
    }

    private MyRoutesFragment myRoutesFragment;
    private DocumentsFragment documentsFragment;

    private NewTitleBar titleBar;
    private LinearLayout layoutMore;
    private TextView txtMyRoutes;
    private TextView txtProfile;
    private TextView txtSettings;
    private TextView txtDocuments;
    private TextView txtNotifications;
    private TextView txtCallToCallCenter;
    private TextView txtLogOut;
    private TextView profileName;
    private TextView profilePhone;
    private TextView profileMail;
    private TextView txtOrders;
    private TextView notificationsFrameCount;
    private FrameLayout tabMyDrive, tabDashboard, tabLocation, tabCommunity, tabPoll, notificationsFrame, updatesFrame, tabPayment, tabKeyless;
    private ImageView tabMyDriveSelector, tabDashboardSelector, tabPollSelector, tabLocationSelector, tabPaymentSelector, tabKeylessSelector,
            tabCommunitySelector, profilePicture;

    private TabType tabSelected = TabType.More;
    private TabType prevTabSelected = TabType.Location;

    private Timer m_timerUpdate = new Timer();
    private Timer m_bleLoggerTimer = new Timer();

    private int m_communityTab = -1;
    private int m_communitySubtab = -1;
    private int m_dashTab = -1;
    private boolean m_vehCondition = false;

    public void setCommunityTab(int tab) {
        m_communityTab = tab;
    }

    public void setCommunitySubtab(int subtab) {
        m_communitySubtab = subtab;
    }

    private boolean isMenuShown;

    private BroadcastReceiver newContentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            manageNewContentIndicator();
        }
    };

    public Home2Activity() {
        super("HomeActivity");
    }

    private int m_getDongleSerialNumberTries = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OtcBle.getInstance().setContext(MyApp.getContext());
        OtcBle.getInstance().createBleLibrary();
        OtcBle.getInstance().setBleControl(new BleControl() {
            @Override
            public void onConnect() {
                onConnectDongle();
            }

            @Override
            public void onDisconnect() {
                m_getDongleSerialNumberTries = 0;
                OtcBle.getInstance().carStatus.clear();
                OtcBle.getInstance().serialNumber = null;
            }
        });

        MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());
        msp.putBoolean("loggin 2.0", true);

        setContentView(R.layout.activity_home);
        retrieveViews();
        setEvents();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            MySharedPreferences langSP = MySharedPreferences.createLanguage(this);
            if (langSP.getBoolean(LanguageUtils.PREFERENCES_LANG_CHANGE)) {
                langSP.remove(LanguageUtils.PREFERENCES_LANG_CHANGE);
                recreate();
            }
        }

        getOneTimeData();

        if (!MySharedPreferences.createDashboard(this).contains("ConditionCache")) {
            GenericTask getVehicleCondition = new GenericTask(Endpoints.VEHICLE_CONDITION, null, true, otcResponse -> {
                if (otcResponse.getStatus() == Shared.OTCStatus.SUCCESS) {
                    com.otc.alice.api.model.DashboardAndStatus.VehicleCondition vc = otcResponse.getData().unpack(com.otc.alice.api.model.DashboardAndStatus.VehicleCondition.class);
                    MySharedPreferences.createDashboard(this).putString("ConditionCache", new Gson().toJson(vc));
                }
            });
            getVehicleCondition.execute();
        }

        if (getIntent() != null && getIntent().getExtras() != null) {
            MyFirebaseMessagingService.NotificationType type = MyFirebaseMessagingService.NotificationType.valueOf(getIntent().getExtras().getString("NotificationType", "Normal"));
            Log.d("Notification", type.name());
            switch (type) {
                case Geofencing: {
                    MySharedPreferences.createLocationSecurity(this).putBoolean("HasToCheckCar", true);
                    tabLocation.performClick();
                    manageUI();
                    break;
                }
                case ConnecTech: {
                    m_communityTab = 3;
                    m_communitySubtab = 0;
                    tabCommunity.performClick();
                    manageUI();
                    break;
                }
                case ConnecTechMsg: {
                    m_communityTab = 3;
                    m_communitySubtab = 1;
                    tabCommunity.performClick();
                    manageUI();
                    break;
                }
                case Dealer: {
                    m_communityTab = 4;
                    m_communitySubtab = 0;
                    tabCommunity.performClick();
                    manageUI();
                    break;
                }
                case DealerMsg: {
                    m_communityTab = 4;
                    m_communitySubtab = 1;
                    tabCommunity.performClick();
                    manageUI();
                    break;
                }
                case FriendInvitation: {
                    m_communityTab = 2;
                    m_communitySubtab = 2;
                    tabCommunity.performClick();
                    manageUI();
                    break;
                }
                case FriendPost: {
                    m_communityTab = 2;
                    m_communitySubtab = 1;
                    tabCommunity.performClick();
                    manageUI();
                    break;
                }
                case Status: {
                    m_dashTab = 2;
                    // tabDashboard.performClick();
                    // manageUI();
                    initializeInLocationAndSecurity();
                    break;
                }

                case VehicleCondition: {
                    m_vehCondition = true;
                    // tabDashboard.performClick();
                    // manageUI();
                    initializeInLocationAndSecurity();
                    break;
                }

                case Normal:
                default: {
                    initializeInLocationAndSecurity();
                    break;
                }
            }
        } else {
            initializeInLocationAndSecurity();
        }

        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        initializeFirebase();

        getProfile();

        m_bleLoggerTimer.scheduleAtFixedRate(new TimerTask() {
            int addedLines = 0;

            @Override
            public void run() {
                Boolean resp = ConfigurationNetwork.isReportingEnabled();
                addedLines = Logger.getAddedLines();
                if (resp != null && resp && addedLines > 5) {
                    ConfigurationNetwork.uploadReport(Logger.getLog());
                }
                Logger.clearAddedLines();
            }
        }, 0, 10 * 60 * 1000);

        startService(new Intent(this, FileTransfer.class));
        FileTransfer.hasToRead = true;
        setProfileFromCache();
    }

    private void initializeFirebase() {
        try {
            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
                try {
                    InstanceIdResult result = task.getResult();
                    if (result != null) {
                        String token = result.getToken();
                        if (!token.isEmpty()) {
                            MySharedPreferences.createDefault(getApplicationContext()).putString("token", token);
                            new SendPushTokenTask().execute(this);
                        }
                    }
                } catch (Exception e) {
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            initializeFirebase();
                        }
                    }, 10000);
                }
            });
        } catch (Exception e) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    initializeFirebase();
                }
            }, 10000);
        }
    }

    @AnyThread
    private void onConnectDongle() {
        Thread homeThread = new Thread(() -> {
            while (!HeartBeatService.isRunning) {
                Utils.wait(this, 100);
                if (!OtcBle.getInstance().isConnected()) return;
            }
            while (OtcBle.getInstance().serialNumber == null || OtcBle.getInstance().serialNumber.length() != 16) {
                OtcBle.getInstance().readSN();
                if (!OtcBle.getInstance().isConnected()) {
                    return;
                }
            }
            ProfileAndSettings.ExpirationExtension ee = ProfileAndSettings.ExpirationExtension.newBuilder().
                    setDongleSerialNumber(OtcBle.getInstance().serialNumber)
                    .setDongleMAC(OtcBle.getInstance().getDeviceMac())
                    .build();
            GenericTask gt = new GenericTask(Endpoints.LAST_DONGLE_CONNECTION, ee, true, otcResponse -> {
                if (otcResponse.getStatus() != Shared.OTCStatus.SUCCESS) {
                    ++m_getDongleSerialNumberTries;
                    if (m_getDongleSerialNumberTries > 3) {
                        OtcBle.getInstance().disconnect();
                        Utils.runOnMainThread(() -> {
                            OtcBle.getInstance().clearDeviceMac();
                            MySharedPreferences.createLogin(getApplicationContext()).remove("macBLE");
                            CustomDialog dyn = new CustomDialog(Home2Activity.this);
                            dyn.setTitle(getResources().getString(R.string.dongle_connection_error));
                            dyn.setMessage(getResources().getString(R.string.connection_contact_call_center));
                            dyn.show();
                            dyn.getButton().setOnClickListener((v) -> Utils.logout(Home2Activity.this));
                            dyn.getButton().setText(getString(R.string.ok));
                        });
                    } else {
                        onConnectDongle();
                    }
                }
            });
            gt.execute();
        }, "HomeThread");
        homeThread.start();
    }

    // Get data que canvia cada molt temps, podem posar més delay
    @UiThread
    private void getSlowData() {
        // Updates
        GenericTask gt2 = new GenericTask(Endpoints.VERSION, null, true, (rsp) -> {
            if (rsp != null) {
                ConfigurationNewEra.VersionResponse resp = null;
                try {
                    resp = ConfigurationNewEra.VersionResponse.parseFrom(rsp.getData().getValue());
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }

                if (resp != null) {
                    String[] parts = BuildConfig.VERSION_NAME.split("\\.");
                    int major = Integer.parseInt(parts[0]);
                    int minor = Integer.parseInt(parts[1]);
                    int build = Integer.parseInt(parts[2]);

                    boolean isLess = major < resp.getAndroidMajor() ||
                            ((major == resp.getAndroidMajor()) && (minor < resp.getAndroidMinor())) ||
                            ((major == resp.getAndroidMajor()) && (minor == resp.getAndroidMinor()) && (build < resp.getAndroidBuild()));
                    if (isLess) {
                        MySharedPreferences.createLogin(Home2Activity.this).putBoolean("LastVersion", false);
                    } else {
                        MySharedPreferences.createLogin(Home2Activity.this).putBoolean("LastVersion", true);
                    }
                    setUpdates(isLess);

                    String type = "";
                    switch (resp.getType()) {
                        case 0:
                            type = "a";
                            break;
                        case 1:
                            type = "b";
                            break;
                        case 2:
                            type = "rc";
                            break;
                        case 3:
                            type = "r";
                            break;
                    }
                    String serverVersion = String.format(Locale.US, "%d.%d.%d %s", resp.getMajor(), resp.getMinor(), resp.getBuild(), type);
                    MySharedPreferences.createLogin(Home2Activity.this).putString("ServerVersion", serverVersion);
                }
            }
        });
        gt2.execute();

        // Sumari setmanal
        MyDrive.Summary.Builder sum = MyDrive.Summary.newBuilder();
        sum.setTypeTime(General.TimeType.WEEKLY);
        GenericTask getMyDriveInfo = new GenericTask(Endpoints.SUMMARY, sum.build(), true, otcResponse -> {
            if (otcResponse.getStatus() == Shared.OTCStatus.SUCCESS) {
                MyDrive.SummaryResponse rsp = otcResponse.getData().unpack(MyDrive.SummaryResponse.class);
                int local = rsp.getBestLocalRanking();
                MySharedPreferences.createLogin(Home2Activity.this).putInteger("ProfileBestLocalWeekly", local);
                MySharedPreferences.createLogin(Home2Activity.this).putDouble("ProfileSafetyWeekly", rsp.getSafetyDrivingTechnique());
                MySharedPreferences.createLogin(Home2Activity.this).putDouble("ProfileEcoWeekly", Utils.clamp(rsp.getEcoAverageConsumption() / Constants.CAR_CONSUMPTION_BEST, 0D, 1D));
            }
        });
        getMyDriveInfo.execute();

        // Dealer
        CommunityNetwork.getDealer(null);
    }

    // Get data que no hauria de canviar
    @UiThread
    private void getOneTimeData() {
        // Clau del BLE
        ConfigurationNetwork.fetchBluetoothConfiguration();

        // Teléfon del Call Center
        GenericTask getCallCenterPhone = new GenericTask(Endpoints.PHONE, null, true, otcResponse -> {
            if (otcResponse.getStatus() == Shared.OTCStatus.SUCCESS) {
                LocationAndSecurity.LocationPhone phone = otcResponse.getData().unpack(LocationAndSecurity.LocationPhone.class);
                MySharedPreferences.createLogin(Home2Activity.this).putString("CallCenterPhone", phone.getPhone());
            }
        });
        getCallCenterPhone.execute();

        // Descripció del Vehicle Condition
        TypedTask<VehicleConditionDescription> getVCD = new TypedTask<>(Endpoints.VEHICLE_CONDITION_DESCRIPTION, null, true,
                com.otc.alice.api.model.DashboardAndStatus.VehicleConditionDescription.class,
                new TypedCallback<VehicleConditionDescription>() {
                    @Override
                    public void onSuccess(@Nonnull @NonNull VehicleConditionDescription value) {
                        MySharedPreferences.createDashboard(Home2Activity.this).putBytes(Constants.Prefs.VEHICLE_CONDICION_DESCRIPTION, value.toByteArray());
                    }

                    @Override
                    public void onError(@NonNull Shared.OTCStatus status, String str) {

                    }
                });
        getVCD.execute();

        // Badges
        new BadgesTask.getVersionBadges().execute();
    }

    // Get data que pot canviar
    @UiThread
    private void getData() {
        TypedTask<ProfileAndSettings.SocialNetworkStatus> getSNS = new TypedTask<>(Endpoints.SOCIAL_NETWORK, null, true, ProfileAndSettings.SocialNetworkStatus.class,
                new TypedCallback<ProfileAndSettings.SocialNetworkStatus>() {
                    @Override
                    public void onSuccess(@Nonnull @NonNull ProfileAndSettings.SocialNetworkStatus sns) {
                        Utils.runOnBackground(() -> {
                            Context ctx = Home2Activity.this;
                            boolean change = PrefsManager.getInstance().getSettingValue(Constants.Prefs.SETTINGS_RECENT_TRIP, ctx);
                            PrefsManager.getInstance().saveSettingValue(Constants.Prefs.SETTINGS_RANKINGS, sns.getRankingEnabled(), ctx);
                            PrefsManager.getInstance().saveSettingValue(Constants.Prefs.SETTINGS_REWARDS, sns.getBadgeEnabled(), ctx);
                            PrefsManager.getInstance().saveSettingValue(Constants.Prefs.SETTINGS_RECENT_TRIP, sns.getSaveRecentTripEnabled(), ctx);
                            PrefsManager.getInstance().saveSettingValue(Constants.Prefs.SETTINGS_AUTOUPDATE, sns.getAutoUpdateDongleEnabled(), ctx);
                            if (change != sns.getSaveRecentTripEnabled()) {
                                Utils.setDongleEnableRouteStorage(sns.getSaveRecentTripEnabled());
                            }
                        });
                    }

                    @Override
                    public void onError(@NonNull Shared.OTCStatus status, String str) {

                    }
                });
        getSNS.execute();

        // Notificacions
        ProfileNetwork.getNotificationCount(this);

        setProfileFromCache();
    }

    public void goDocuments() {
        this.txtDocuments.performClick();
        manageUI();
    }

    private void callCallCenter() {
        String uri = "tel:" + MySharedPreferences.createLogin(Home2Activity.this).getString("CallCenterPhone");
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse(uri));
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(intent);
    }

    @UiThread
    private void setProfileFromCache() {
        Utils.runOnBackThread(() -> {
            MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());

            String phone = String.format("T. %s", msp.getString("Tlf"));
            String mail = msp.getString("Email");
            String name = msp.getString("Nick");

            Utils.runOnMainThread(() -> {
                profilePhone.setText(phone);
                profileMail.setText(mail);
                profileName.setText(name);

                if (msp.contains("UserImageId")) {
                    Long img = msp.getLong("UserImageId");
                    Glide.with(this).load(img).placeholder(profilePicture.getDrawable()).into(profilePicture);
                } else {
                    Glide.with(Home2Activity.this).load(getDrawable(R.drawable.user_placeholder_correct)).placeholder(profilePicture.getDrawable()).into(profilePicture);
                }
            });

            manageNewContentIndicator();
        });
    }

    private void getProfile() {
        String currencyCode = MySharedPreferences.createLogin(getApplicationContext()).getString("CurrencyCode");
        String currencyCountry = MySharedPreferences.createLogin(getApplicationContext()).getString("CurrencyCountry");

        PaymentUtils.Currency.selectedCurrency = PaymentUtils.Currency.getByCode(currencyCode);
        PaymentUtils.Currency.selectedCountry = currencyCountry;

        TypedTask<ProfileAndSettings.UserDataResponse> gt = new TypedTask<>(Endpoints.USER_INFO, null, true, ProfileAndSettings.UserDataResponse.class, new TypedCallback<ProfileAndSettings.UserDataResponse>() {
            @Override
            public void onSuccess(@Nonnull @NonNull ProfileAndSettings.UserDataResponse value) {
                if (!value.getDongleSerialNumber().isEmpty()) {
                    OtcBle.getInstance().setSN(value.getDongleSerialNumber());
                    MySharedPreferences.createLogin(getApplicationContext()).putString("SN", value.getDongleSerialNumber());

                    PaymentUtils.Currency.selectedCurrency = PaymentUtils.Currency.getByCode(value.getCurrency());
                    switch (value.getCountryId()) {
                        case 1: PaymentUtils.Currency.selectedCountry = "IN";
                        case 2: PaymentUtils.Currency.selectedCountry = "ID";
                        case 3: PaymentUtils.Currency.selectedCountry = "ES";
                    }
                    MySharedPreferences.createLogin(getApplicationContext()).putString("CurrencyCode", value.getCurrency());
                    MySharedPreferences.createLogin(getApplicationContext()).putString("CurrencyCountry", PaymentUtils.Currency.selectedCountry);
                }
            }

            @Override
            public void onError(@NonNull Shared.OTCStatus status, String str) {

            }
        });
        gt.execute();
    }

    public void clickCommunications() {
        tabCommunity.performClick();
    }

    private void initializeInLocationAndSecurity() {
        tabLocation.performClick();
        manageUI();
    }

    private void retrieveViews() {
        titleBar = findViewById(R.id.home_titleBar);

        layoutMore = findViewById(R.id.navigation_layoutMore);
        txtMyRoutes = findViewById(R.id.txtMyRoutes);
        txtProfile = findViewById(R.id.txtProfile);
        txtSettings = findViewById(R.id.txtSettings);
        txtDocuments = findViewById(R.id.txtDocuments);
        txtNotifications = findViewById(R.id.txtNotifications);
        txtCallToCallCenter = findViewById(R.id.txtCallCenter);
        txtLogOut = findViewById(R.id.txtLogout);
        txtOrders = findViewById(R.id.txtOrders);
        TextView txtVersion = findViewById(R.id.txtVersion);
        tabMyDrive = findViewById(R.id.navigation_tabMyDrive);
        tabDashboard = findViewById(R.id.navigation_tabDashboard);
        tabLocation = findViewById(R.id.navigation_tabLocation);
        tabCommunity = findViewById(R.id.navigation_tabCommunity);
        tabPayment = findViewById(R.id.navigation_tabPayment);
        tabKeyless = findViewById(R.id.navigation_tabKeyless);
        tabPoll = findViewById(R.id.navigation_tabPoll);

        notificationsFrame = findViewById(R.id.notificationsFrame);
        notificationsFrameCount = findViewById(R.id.notificationsFrameCount);

        updatesFrame = findViewById(R.id.updatesFrame);

        profileName = findViewById(R.id.profileName);
        profilePhone = findViewById(R.id.profilePhone);
        profileMail = findViewById(R.id.profileMail);
        profilePicture = findViewById(R.id.profileImage);

        tabMyDriveSelector = findViewById(R.id.navigation_tabMyDriveSelector);
        tabDashboardSelector = findViewById(R.id.navigation_tabDashboardSelector);
        tabLocationSelector = findViewById(R.id.navigation_tabLocationSelector);
        tabCommunitySelector = findViewById(R.id.navigation_tabCommunitySelector);
        tabPaymentSelector = findViewById(R.id.navigation_tabPaymentSelector);
        tabKeylessSelector = findViewById(R.id.navigation_tabKeylessSelector);
        tabPollSelector = findViewById(R.id.navigation_tabPollSelection);

        txtVersion.setText(String.format("%s ver %s", getString(R.string.app_name), BuildConfig.VERSION_NAME));
    }

    private void setEvents() {
        tabMyDrive.setOnClickListener(createListenerForTab());
        tabDashboard.setOnClickListener(createListenerForTab());
        tabLocation.setOnClickListener(createListenerForTab());
        tabCommunity.setOnClickListener(createListenerForTab());
        tabPayment.setOnClickListener(createListenerForTab());
        tabKeyless.setOnClickListener(createListenerForTab());
        tabPoll.setOnClickListener(createListenerForTab());

        txtMyRoutes.setOnClickListener(createListenerForTab());
        txtProfile.setOnClickListener(createListenerForTab());
        txtSettings.setOnClickListener(createListenerForTab());
        txtDocuments.setOnClickListener(createListenerForTab());
        txtNotifications.setOnClickListener(createListenerForTab());
        txtCallToCallCenter.setOnClickListener(createListenerForTab());
        txtLogOut.setOnClickListener(createListenerForTab());
        txtOrders.setOnClickListener(createListenerForTab());

        layoutMore.setClickable(false);

        View closeMenu = findViewById(R.id.closeMenu);
        closeMenu.setOnClickListener(v -> showOrHideMoreLayout());
    }

    private View.OnClickListener createListenerForTab() {
        return view -> {
            hideLayoutMore();
            prevTabSelected = tabSelected;
            switch (view.getId()) {
                case R.id.navigation_tabMyDrive:
                    tabSelected = TabType.MyDrive;
                    if (tabSelected == prevTabSelected) return;
                    configureTitleBar(R.string.title_my_drive);
                    changeFragment(new MyDriveFragment());
                    break;
                case R.id.navigation_tabDashboard:
                    tabSelected = TabType.Dashboard;
                    if (tabSelected == prevTabSelected) return;
                    configureTitleBar(R.string.dashboard_and_status);
                    DashboardAndStatus ds = new DashboardAndStatus();

                    if (m_dashTab != -1) {
                        ds.setTab(m_dashTab);
                        m_dashTab = -1;
                    }
                    if (m_vehCondition) {
                        ds.setVehCondition(true);
                        m_vehCondition = false;
                    }
                    changeFragment(ds);
                    break;
                case R.id.navigation_tabPoll:
                    tabSelected = TabType.Poll;
                    if (tabSelected == prevTabSelected) return;
                    configureTitleBar(R.string.poll);
                    PollFragment pf = new PollFragment();
                    changeFragment(pf);
                    break;
                case R.id.navigation_tabKeyless:
                    tabSelected = TabType.Keyless;
                    if (tabSelected == prevTabSelected) return;
                    configureTitleBar(R.string.keyless);
                    VehicleFragment va = new VehicleFragment();
                    changeFragment(va);
                    break;
                case R.id.navigation_tabLocation:
                    tabSelected = TabType.Location;
                    if (tabSelected == prevTabSelected) return;
                    configureTitleBar(R.string.location_and_assistance, R.drawable.my_drive_icons_6);
                    changeFragment(new LocationAndSecurityFragment());
                    break;
                case R.id.navigation_tabPayment:
                    tabSelected = TabType.Payment;
                    if (tabSelected == prevTabSelected) return;
                    configureTitleBar(R.string.payment, R.drawable.my_drive_icons_6);
                    changeFragment(new ShoppingFragment());
                    break;
                case R.id.navigation_tabCommunity:
                    tabSelected = TabType.Community;
                    if (tabSelected == prevTabSelected) return;
                    configureTitleBar(R.string.title_communications);
                    CommunicationsFragment communicationsFragment = new CommunicationsFragment();
                    communicationsFragment.setListener(this::showPostMenu);
                    if (m_communityTab != -1) {
                        communicationsFragment.setTab(m_communityTab);
                        communicationsFragment.setSubTab(m_communitySubtab);
                        m_communityTab = -1;
                    }
                    changeFragment(communicationsFragment);
                    break;
                case R.id.txtMyRoutes:
                    tabSelected = TabType.Routes;
                    if (tabSelected == prevTabSelected) return;
                    configureTitleBar(R.string.title_my_routes);
                    myRoutesFragment = new MyRoutesFragment();
                    myRoutesFragment.setListener(this::showRoutesMenu);
                    changeFragment(myRoutesFragment);
                    break;
                case R.id.txtProfile:
                    tabSelected = TabType.Profile;
                    if (tabSelected == prevTabSelected) return;
                    configureTitleBar(R.string.title_my_profile);
                    changeFragment(new MyProfileFragment());
                    break;
                case R.id.txtSettings:
                    tabSelected = TabType.Settings;
                    if (tabSelected == prevTabSelected) return;
                    configureTitleBar(R.string.title_settings);
                    changeFragment(new SettingsFragment());
                    break;
                case R.id.txtDocuments:
                    tabSelected = TabType.Documents;
                    if (tabSelected == prevTabSelected) return;
                    txtDocuments.setSelected(true);
                    configureTitleBar(R.string.title_documents);
                    documentsFragment = new DocumentsFragment();
                    changeFragment(documentsFragment);
                    break;
                case R.id.txtNotifications:
                    tabSelected = TabType.Notifications;
                    if (tabSelected == prevTabSelected) return;
                    txtNotifications.setSelected(true);
                    configureTitleBar(R.string.title_notifications);
                    changeFragment(new NotificationFragment());
                    break;
                case R.id.txtLogout: {
                    DialogYesNo dyn = new DialogYesNo(this, getResources().getString(R.string.you_want_exit), () -> runOnUiThread(() -> {
                        OtcBle.getInstance().disconnect();
                        OtcBle.getInstance().clearDeviceMac();
                        Utils.logout(Home2Activity.this);
                    }), () -> {});
                    dyn.show();
                    break;
                }
                case R.id.txtCallCenter:
                    DialogYesNo dyn = new DialogYesNo(this, getResources().getString(R.string.call_call_center), this::callCallCenter, () -> {});
                    dyn.show();
                    break;

                case R.id.txtOrders:
                    tabSelected = TabType.Orders;
                    if (tabSelected == prevTabSelected) return;
                    txtOrders.setSelected(true);
                    configureTitleBar(R.string.title_activity_orders);
                    changeFragment(new OrdersFragment(this::showOrdersMenu));
                    break;
            }
            manageUI();
        };
    }

    private void manageUI() {
        tabMyDrive.setSelected(tabSelected == TabType.MyDrive);
        tabMyDriveSelector.setVisibility(tabSelected == TabType.MyDrive ? View.VISIBLE : View.INVISIBLE);
        tabDashboard.setSelected(tabSelected == TabType.Dashboard);
        tabDashboardSelector.setVisibility(tabSelected == TabType.Dashboard ? View.VISIBLE : View.INVISIBLE);
        tabLocation.setSelected(tabSelected == TabType.Location);
        tabLocationSelector.setVisibility(tabSelected == TabType.Location ? View.VISIBLE : View.INVISIBLE);
        tabCommunity.setSelected(tabSelected == TabType.Community);
        tabCommunitySelector.setVisibility(tabSelected == TabType.Community ? View.VISIBLE : View.INVISIBLE);
        tabPayment.setSelected(tabSelected == TabType.Payment);
        tabPaymentSelector.setVisibility(tabSelected == TabType.Payment ? View.VISIBLE : View.INVISIBLE);
        txtMyRoutes.setSelected(prevTabSelected == TabType.Routes);
        txtProfile.setSelected(prevTabSelected == TabType.Profile);
        txtSettings.setSelected(prevTabSelected == TabType.Settings);
        txtDocuments.setSelected(prevTabSelected == TabType.Documents);
        txtNotifications.setSelected(prevTabSelected == TabType.Notifications);
        txtOrders.setSelected(prevTabSelected == TabType.Orders);
        tabKeyless.setSelected(tabSelected == TabType.Keyless);
        tabKeylessSelector.setVisibility(tabSelected == TabType.Keyless ? View.VISIBLE : View.INVISIBLE);
        tabPoll.setSelected(tabSelected == TabType.Poll);
        tabPollSelector.setVisibility(tabSelected == TabType.Poll ? View.VISIBLE : View.INVISIBLE);
        manageNewContentIndicator();
    }

    private void manageNewContentIndicator() {
        final long[] count = {MySharedPreferences.createLogin(Home2Activity.this).getLong("NotificationCount")};
        Utils.runOnMainThread(() -> {
            titleBar.setNotification((int) count[0]);

            if (count[0] == 0) {
                notificationsFrame.setVisibility(View.GONE);
            } else {
                notificationsFrame.setVisibility(View.VISIBLE);
                if (count[0] >= 1000) {
                    notificationsFrameCount.setText(String.format(Locale.US, "+%d", 999));
                } else {
                    notificationsFrameCount.setText(String.format(Locale.US, "%d", count[0]));
                }
            }
        });
    }

    private void setUpdates(boolean upd) {
        updatesFrame.setVisibility(upd ? View.VISIBLE : View.GONE);
    }

    public void configureTitleBar(int stringRes, int... imgRes) {
        titleBar.setTitle(stringRes);
        if (imgRes.length <= 0) {
            titleBar.hideImgRight2();
        }
        titleBar.setLeftButtonImage(R.drawable.menu_icons_10);
        titleBar.setListener(new NewTitleBar.NewTitleBarListener() {
            @Override
            public void onLeftClick() {
                prevTabSelected = tabSelected;
                tabSelected = TabType.More;
                showOrHideMoreLayout();
            }

            @Override
            public void onRight1Click() {

            }

            @Override
            public void onRight2Click() {

            }
        });
    }

    private void changeFragment(Fragment fragment) {
        try {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content, fragment).commit();
        } catch (IllegalStateException | IllegalArgumentException ignored) {
            // No s'ha pogut canviar de fragment, així que ñe
        }
    }

    private void showOrHideMoreLayout() {
        txtSettings.setText(R.string.title_settings);
        txtDocuments.setText(R.string.title_documents);
        if (layoutMore.getVisibility() == View.VISIBLE) {
            hideLayoutMoreAndBackToPrevious();
        } else {
            layoutMore.setVisibility(View.VISIBLE);
        }
    }

    private void hideLayoutMore() {
        layoutMore.setVisibility(View.GONE);
    }

    private void hideLayoutMoreAndBackToPrevious() {
        hideLayoutMore();
        tabSelected = prevTabSelected;
        manageUI();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(newContentReceiver);
        m_timerUpdate.cancel();
        try {
            super.onPause();
        } catch (IllegalArgumentException iae) {
            //Log.e("HomeActivity", "IllegalArgumentException", iae);
        }
    }

    @Override
    protected void onResume() {
        try {
            super.onResume();
        } catch (IllegalArgumentException ignored) {
        }
        setProfileFromCache();
        LocalBroadcastManager.getInstance(this).registerReceiver(newContentReceiver,
                new IntentFilter(Constants.Prefs.HAS_NEW_CONTENT));

        getCommonData();
        if (m_timerUpdate != null) {
            m_timerUpdate.cancel();
        }
        m_timerUpdate = new Timer("HomeActivityTimer");
        m_timerUpdate.scheduleAtFixedRate(new TimerTask() {
            int iters = 0;
            int odos = 0;
            @Override
            public void run() {
                // 30 segons
                if (iters % 30 == 0) {
                    Utils.runOnMainThread(Home2Activity.this::getData);
                }
                // 300 segons
                if (iters % 300 == 0) {
                    Utils.runOnMainThread(Home2Activity.this::getSlowData);
                }
                try {
                    if (OtcBle.getInstance().carStatus.contains("KL15")) {
                        if (!MySharedPreferences.createDashboard(Home2Activity.this).getBoolean("milesMessage") && OtcBle.getInstance().carStatus.getBitVar("OdoScale")) {
                            ++odos;
                            if (odos > 5) {
                                Utils.runOnMainThread(() -> {
                                    CustomDialog cd = new CustomDialog(Home2Activity.this);
                                    cd.setMessage(getResources().getString(R.string.no_milles));
                                    cd.show();
                                });
                                MySharedPreferences.createDashboard(Home2Activity.this).putBoolean("milesMessage", true);
                            }
                        } else if (!OtcBle.getInstance().carStatus.contains("OdoScale") || !OtcBle.getInstance().carStatus.getBitVar("OdoScale")) {
                            odos = 0;
                            MySharedPreferences.createDashboard(Home2Activity.this).putBoolean("milesMessage", false);
                        }
                    }
                } catch (Exception ignored) {
                }

                Utils.runOnMainThread(() -> {
                    if (Utils.developer) {
                        tabKeyless.setVisibility(View.VISIBLE);
                    } else {
                        tabKeyless.setVisibility(View.GONE);
                    }
                });

                ++iters;
            }
        }, 0, 1000);

        if (MySharedPreferences.createLogin(MyApp.getContext()).getBoolean("HasToShowPopup")) {
            CustomDialog cd = new CustomDialog(MyApp.getContext());
            cd.setMessage(getString(R.string.months_no_connect) + "\n" + getString(R.string.days_0));
            cd.setOnOkListener(() -> Utils.logout(getApplicationContext()));
            cd.show();
        } else if (MySharedPreferences.createLogin(MyApp.getContext()).getBoolean("ShowPopupNewMobile")) {
            Utils.changedPhone(this);
        }
        if (MySharedPreferences.createLogin(this).getBoolean("GoToDashboard")) {
            tabDashboard.performClick();
            MySharedPreferences.createLogin(this).remove("GoToDashboard");
        }
    }

    private void getCommonData() {
        GenericTask getProfile = new GenericTask(Endpoints.USER_INFO, null, true, otcResponse -> {
            MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());

            if (otcResponse.getStatus() == Shared.OTCStatus.SUCCESS) {
                ProfileAndSettings.UserDataResponse udr = otcResponse.getData().unpack(ProfileAndSettings.UserDataResponse.class);
                profilePhone.setText(String.format("T. %s", udr.getPhone()));
                profileMail.setText(udr.getEmail());
                profileName.setText(udr.getUsername());

                msp.putString("Nick", udr.getUsername());
                msp.putString("Tlf", udr.getPhone());
                msp.putString("Email", udr.getEmail().toLowerCase());
                msp.putString("macBLE", udr.getMac());
                msp.putBoolean("Expired", udr.getIsExpired());

                if (udr.getImageId() != 0) {
                    Long imgId = udr.getImageId();
                    Glide.with(Home2Activity.this).load(imgId).into(profilePicture);
                    msp.putLong("UserImageId", imgId);
                } else {
                    msp.remove("UserImageId");
                    Glide.with(Home2Activity.this).load(getDrawable(R.drawable.user_placeholder_correct)).into(profilePicture);
                }
            }
        });
        getProfile.execute();
    }

    @Override
    public void onBackPressed() {
        Fragment frag = getSupportFragmentManager().findFragmentById(R.id.content);
        if ((!(frag instanceof FragmentBackPresser)) || ((FragmentBackPresser) frag).onBackPressed()) {
            if (tabSelected == TabType.More) {
                hideLayoutMoreAndBackToPrevious();
            } else if (isMenuShown) {
                isMenuShown = false;
                hideMenu();
            } else {
                moveTaskToBack(true);
            }
        }
    }

    private void hideMenu() {
        Fragment menuFragment = getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getFragments().size() - 1);
        getSupportFragmentManager()
                .beginTransaction()
                .remove(menuFragment)
                .commit();
    }

    private void showOrdersMenu(OrdersFragment parent) {
        OrdersMenuFragment menu = new OrdersMenuFragment(parent);
        showMenuFragment(menu);
    }

    private void showRoutesMenu(boolean showEdit, RouteItem routeItem) {
        RoutesMenuFragment routesMenuFragment = new RoutesMenuFragment();

        //if (routeItem.getRouteType().equals(General.RouteType.AUTOSAVED)){
            //routesMenuFragment.showSaveButton();
            //RoutesMenuFragment.btnSave.setVisibility(View.VISIBLE);
        //}

        routesMenuFragment.configure(showEdit, routeItem, () -> {
            if (myRoutesFragment != null && myRoutesFragment.isVisible()) {
                myRoutesFragment.refreshRouteList();
            }
        });

        showMenuFragment(routesMenuFragment);
    }

    private void showPostMenu(Object object) {
        PostMenuFragment postMenuFragment = new PostMenuFragment();
        postMenuFragment.setPostSelected(object);
        showMenuFragment(postMenuFragment);
    }

    private void showMenuFragment(Fragment fragment) {
        isMenuShown = true;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_contentMenu, fragment)
                .commit();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == WalletFragment.REQUEST_CODE_GALLERY && resultCode == RESULT_OK) {
            doActionsAfterGallery(data);
        } else if (requestCode == WalletFragment.REQUEST_CODE_CAMERA && resultCode == RESULT_OK) {
            doActionsAfterCamera();
        }
    }

    private void doActionsAfterGallery(Intent data) {
        if (documentsFragment != null && documentsFragment.isVisible()) {
            documentsFragment.doActionsAfterGallery(data);
        }
    }

    private void doActionsAfterCamera() {
        if (documentsFragment != null && documentsFragment.isVisible()) {
            documentsFragment.doActionsAfterCamera();
        }
    }

}
