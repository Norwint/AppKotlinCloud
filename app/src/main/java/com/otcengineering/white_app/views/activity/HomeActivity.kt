package com.otcengineering.white_app.views.activity

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.AnyThread
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.protobuf.InvalidProtocolBufferException
import com.otc.alice.api.model.ConfigurationNewEra.VersionResponse
import com.otc.alice.api.model.DashboardAndStatus.VehicleCondition
import com.otc.alice.api.model.DashboardAndStatus.VehicleConditionDescription
import com.otc.alice.api.model.General
import com.otc.alice.api.model.LocationAndSecurity.LocationPhone
import com.otc.alice.api.model.MyDrive
import com.otc.alice.api.model.MyDrive.SummaryResponse
import com.otc.alice.api.model.ProfileAndSettings.*
import com.otc.alice.api.model.Shared
import com.otc.alice.api.model.Shared.OTCResponse
import com.otc.alice.api.model.Shared.OTCStatus
import com.otcengineering.apible.OtcBle
import com.otcengineering.apible.blecontrol.interfaces.BleControl
import com.otcengineering.apible.blecontrol.service.FileTransfer
import com.otcengineering.apible.blecontrol.service.HeartBeatService
import com.otcengineering.apible.blecontrol.utils.Logger
import com.otcengineering.white_app.BuildConfig
import com.otcengineering.white_app.MyApp.getContext
import com.otcengineering.white_app.R
import com.otcengineering.white_app.activities.QRActivity
import com.otcengineering.white_app.components.CustomDialog
import com.otcengineering.white_app.components.DialogYesNo
import com.otcengineering.white_app.components.NewTitleBar
import com.otcengineering.white_app.data.Vehicle
import com.otcengineering.white_app.databinding.ActivityHomeBinding
import com.otcengineering.white_app.fragments.*
import com.otcengineering.white_app.interfaces.FragmentBackPresser
import com.otcengineering.white_app.keyless.fragment.VehicleFragment
import com.otcengineering.white_app.network.CommunityNetwork
import com.otcengineering.white_app.network.ConfigurationNetwork
import com.otcengineering.white_app.network.Endpoints
import com.otcengineering.white_app.network.ProfileNetwork
import com.otcengineering.white_app.payment.fragment.OrdersFragment
import com.otcengineering.white_app.payment.fragment.OrdersMenuFragment
import com.otcengineering.white_app.payment.fragment.ShoppingFragment
import com.otcengineering.white_app.serialization.pojo.RouteItem
import com.otcengineering.white_app.tasks.BadgesTask.getVersionBadges
import com.otcengineering.white_app.tasks.GenericTask
import com.otcengineering.white_app.tasks.SendPushTokenTask
import com.otcengineering.white_app.tasks.TypedTask
import com.otcengineering.white_app.utils.*
import com.otcengineering.white_app.utils.interfaces.OnClickListener
import com.otcengineering.white_app.utils.interfaces.TypedCallback
import com.otcengineering.white_app.utils.payment.PaymentUtils
import com.otcengineering.white_app.utils.pushnotifications.MyFirebaseMessagingService
import com.otcengineering.white_app.viewModel.Network
import com.otcengineering.white_app.viewModel.OtcCallback
import com.otcengineering.white_app.viewModel.VehicleViewModel
import com.otcengineering.white_app.views.components.addSource
import java.util.*
import javax.annotation.Nonnull

public class HomeActivity : AppCompatActivity() {
    enum class TabType {
        MyDrive, Dashboard, Location, Community, More, Routes, Profile, Settings, Documents, Payment, Orders, Keyless, Notifications, Poll
    }

    private var myRoutesFragment: MyRoutesFragment? = null
    private var documentsFragment: DocumentsFragment? = null
    private var titleBar: NewTitleBar? = null
    private var layoutMore: LinearLayout? = null
    private var txtMyRoutes: TextView? = null
    private var txtProfile: TextView? = null
    private var txtSettings: TextView? = null
    private var txtDocuments: TextView? = null
    private var txtNotifications: TextView? = null
    private var txtCallToCallCenter: TextView? = null
    private var txtLogOut: TextView? = null
    private var profileName: TextView? = null
    private var profilePhone: TextView? = null
    private var profileMail: TextView? = null
    private var txtOrders: TextView? = null
    private var notificationsFrameCount: TextView? = null
    private var tabMyDrive: FrameLayout? = null
    private var tabDashboard: FrameLayout? = null
    private var tabLocation: FrameLayout? = null
    private var tabCommunity: FrameLayout? = null
    private var tabPoll: FrameLayout? = null
    private var notificationsFrame: FrameLayout? = null
    private var updatesFrame: FrameLayout? = null
    private var tabPayment: FrameLayout? = null
    private var tabKeyless: FrameLayout? = null
    private var tabMyDriveSelector: ImageView? = null
    private var tabDashboardSelector: ImageView? = null
    private var tabPollSelector: ImageView? = null
    private var tabLocationSelector: ImageView? = null
    private var tabPaymentSelector: ImageView? = null
    private var tabKeylessSelector: ImageView? = null
    private var tabCommunitySelector: ImageView? = null
    private var profilePicture: ImageView? = null
    private var tabSelected = TabType.More
    private var prevTabSelected = TabType.Location
    private var m_timerUpdate: Timer? = Timer()
    private val m_bleLoggerTimer = Timer()
    private var m_communityTab = -1
    private var m_communitySubtab = -1
    private var m_dashTab = -1
    private var m_vehCondition = false
    fun setCommunityTab(tab: Int) {
        m_communityTab = tab
    }

    fun setCommunitySubtab(subtab: Int) {
        m_communitySubtab = subtab
    }

    private var isMenuShown = false
    private val newContentReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            manageNewContentIndicator()
        }
    }
    private var m_getDongleSerialNumberTries = 0

    private val binding: ActivityHomeBinding by lazy { ActivityHomeBinding.inflate(layoutInflater) }

    fun swapVehicleListVisibility() {
        if (binding.vehicleList.root.visibility == View.GONE) {
            binding.vehicleList.root.visibility = View.VISIBLE
        } else if(binding.vehicleList.root.visibility == View.VISIBLE) {
            binding.vehicleList.root.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.activity = this

        binding.vehicleList.addVehicle.setOnClickListener {
            val intent = Intent(
                this,
                QRActivity::class.java
            )
            intent.putExtra("QR_PATTERN", "[A-Z0-9]{17}")
            startActivityForResult(intent, QRActivity.QR_RESULT)
        }

        binding.txtNotification.text = Common.sharedPreferences.getString(Preferences.vehicleName)

        with(binding.vehicleList.recyclerView) {
            addSource(
                R.layout.row_vehicle_list,
                VehicleViewModel(this@HomeActivity).listVehicles, object : OnClickListener<Vehicle> {
                    override fun onItemClick(view: View, t: Vehicle) {
                        if(view.id == R.id.options) {
                            VehicleSettingsActivity.newInstance(this@HomeActivity, t.id)
                        }
                    }
                }
            )
            setRecyclerListener {

            }

        }

        OtcBle.getInstance().context = getContext()
        OtcBle.getInstance().createBleLibrary()
        OtcBle.getInstance().bleControl = object : BleControl {
            override fun onConnect() {
                onConnectDongle()
            }

            override fun onDisconnect() {
                m_getDongleSerialNumberTries = 0
                OtcBle.getInstance().carStatus.clear()
                OtcBle.getInstance().serialNumber = null
            }
        }
        val msp = MySharedPreferences.createLogin(applicationContext)
        msp.putBoolean("loggin 2.0", true)
        retrieveViews()
        setEvents()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            val langSP = MySharedPreferences.createLanguage(this)
            if (langSP.getBoolean(LanguageUtils.PREFERENCES_LANG_CHANGE)) {
                langSP.remove(LanguageUtils.PREFERENCES_LANG_CHANGE)
                recreate()
            }
        }
        oneTimeData
        if (!MySharedPreferences.createDashboard(this).contains("ConditionCache")) {
            val getVehicleCondition = GenericTask(
                Endpoints.VEHICLE_CONDITION, null, true
            ) { otcResponse: OTCResponse ->
                if (otcResponse.status == OTCStatus.SUCCESS) {
                    val vc = otcResponse.data.unpack(
                        VehicleCondition::class.java
                    )
                    MySharedPreferences.createDashboard(this)
                        .putString("ConditionCache", Gson().toJson(vc))
                }
            }
            getVehicleCondition.execute()
        }
        if (intent != null && intent.extras != null) {
            val type = MyFirebaseMessagingService.NotificationType.valueOf(
                intent.extras!!.getString("NotificationType", "Normal")
            )
            Log.d("Notification", type.name)
            when (type) {
                MyFirebaseMessagingService.NotificationType.Geofencing -> {
                    MySharedPreferences.createLocationSecurity(this)
                        .putBoolean("HasToCheckCar", true)
                    tabLocation!!.performClick()
                    manageUI()
                }
                MyFirebaseMessagingService.NotificationType.ConnecTech -> {
                    m_communityTab = 3
                    m_communitySubtab = 0
                    tabCommunity!!.performClick()
                    manageUI()
                }
                MyFirebaseMessagingService.NotificationType.ConnecTechMsg -> {
                    m_communityTab = 3
                    m_communitySubtab = 1
                    tabCommunity!!.performClick()
                    manageUI()
                }
                MyFirebaseMessagingService.NotificationType.Dealer -> {
                    m_communityTab = 4
                    m_communitySubtab = 0
                    tabCommunity!!.performClick()
                    manageUI()
                }
                MyFirebaseMessagingService.NotificationType.DealerMsg -> {
                    m_communityTab = 4
                    m_communitySubtab = 1
                    tabCommunity!!.performClick()
                    manageUI()
                }
                MyFirebaseMessagingService.NotificationType.FriendInvitation -> {
                    m_communityTab = 2
                    m_communitySubtab = 2
                    tabCommunity!!.performClick()
                    manageUI()
                }
                MyFirebaseMessagingService.NotificationType.FriendPost -> {
                    m_communityTab = 2
                    m_communitySubtab = 1
                    tabCommunity!!.performClick()
                    manageUI()
                }
                MyFirebaseMessagingService.NotificationType.Status -> {
                    m_dashTab = 2
                    // tabDashboard.performClick();
                    // manageUI();
                    initializeInLocationAndSecurity()
                }
                MyFirebaseMessagingService.NotificationType.VehicleCondition -> {
                    m_vehCondition = true
                    // tabDashboard.performClick();
                    // manageUI();
                    initializeInLocationAndSecurity()
                }
                MyFirebaseMessagingService.NotificationType.Normal -> {
                    initializeInLocationAndSecurity()
                }
                else -> {
                    initializeInLocationAndSecurity()
                }
            }
        } else {
            initializeInLocationAndSecurity()
        }
        FirebaseMessaging.getInstance().isAutoInitEnabled = true
        initializeFirebase()
        profile
        m_bleLoggerTimer.scheduleAtFixedRate(object : TimerTask() {
            var addedLines = 0
            override fun run() {
                val resp = ConfigurationNetwork.isReportingEnabled()
                addedLines = Logger.getAddedLines()
                if (resp != null && resp && addedLines > 5) {
                    ConfigurationNetwork.uploadReport(Logger.getLog())
                }
                Logger.clearAddedLines()
            }
        }, 0, (10 * 60 * 1000).toLong())
        startService(Intent(this, FileTransfer::class.java))
        FileTransfer.hasToRead = true
        setProfileFromCache()

    }

    private fun initializeFirebase() {
        try {
            FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task: Task<InstanceIdResult?> ->
                try {
                    val result = task.result
                    if (result != null) {
                        val token = result.token
                        if (!token.isEmpty()) {
                            MySharedPreferences.createDefault(applicationContext)
                                .putString("token", token)
                            SendPushTokenTask().execute(this)
                        }
                    }
                } catch (e: Exception) {
                    val timer = Timer()
                    timer.schedule(object : TimerTask() {
                        override fun run() {
                            initializeFirebase()
                        }
                    }, 10000)
                }
            }
        } catch (e: Exception) {
            val timer = Timer()
            timer.schedule(object : TimerTask() {
                override fun run() {
                    initializeFirebase()
                }
            }, 10000)
        }
    }

    @AnyThread
    private fun onConnectDongle() {
        val homeThread = Thread(Runnable {
            while (!HeartBeatService.isRunning) {
                Utils.wait(this, 100)
                if (!OtcBle.getInstance().isConnected) return@Runnable
            }
            while (OtcBle.getInstance().serialNumber == null || OtcBle.getInstance().serialNumber.length != 16) {
                OtcBle.getInstance().readSN()
                if (!OtcBle.getInstance().isConnected) {
                    return@Runnable
                }
            }
            val ee = ExpirationExtension.newBuilder()
                .setDongleSerialNumber(OtcBle.getInstance().serialNumber)
                .setDongleMAC(OtcBle.getInstance().deviceMac)
                .build()
            val gt = GenericTask(
                Endpoints.LAST_DONGLE_CONNECTION, ee, true
            ) { otcResponse: OTCResponse ->
                if (otcResponse.status != OTCStatus.SUCCESS) {
                    ++m_getDongleSerialNumberTries
                    if (m_getDongleSerialNumberTries > 3) {
                        OtcBle.getInstance().disconnect()
                        Utils.runOnMainThread {
                            OtcBle.getInstance().clearDeviceMac()
                            MySharedPreferences.createLogin(applicationContext)
                                .remove("macBLE")
                            val dyn = CustomDialog(this@HomeActivity)
                            dyn.setTitle(resources.getString(R.string.dongle_connection_error))
                            dyn.setMessage(resources.getString(R.string.connection_contact_call_center))
                            dyn.show()
                            dyn.button
                                .setOnClickListener { v: View? ->
                                    Utils.logout(
                                        this@HomeActivity
                                    )
                                }
                            dyn.button.text = getString(R.string.ok)
                        }
                    } else {
                        onConnectDongle()
                    }
                }
            }
            gt.execute()
        }, "HomeThread")
        homeThread.start()
    }// Updates

    // Sumari setmanal

    // Dealer
    // Get data que canvia cada molt temps, podem posar més delay
    @get:UiThread
    private val slowData: Unit
        private get() {
            // Updates
            val gt2 = GenericTask(
                Endpoints.VERSION, null, true
            ) { rsp: OTCResponse? ->
                if (rsp != null) {
                    var resp: VersionResponse? = null
                    try {
                        resp = VersionResponse.parseFrom(
                            rsp.data.value
                        )
                    } catch (e: InvalidProtocolBufferException) {
                        e.printStackTrace()
                    }
                    if (resp != null) {
                        val parts =
                            BuildConfig.VERSION_NAME.split("\\.")
                                .toTypedArray()
                        val major = parts[0].toInt()
                        val minor = parts[1].toInt()
                        val build = parts[2].toInt()
                        val isLess = major < resp.androidMajor ||
                                major == resp.androidMajor && minor < resp.androidMinor ||
                                major == resp.androidMajor && minor == resp.androidMinor && build < resp.androidBuild
                        if (isLess) {
                            MySharedPreferences.createLogin(this@HomeActivity)
                                .putBoolean("LastVersion", false)
                        } else {
                            MySharedPreferences.createLogin(this@HomeActivity)
                                .putBoolean("LastVersion", true)
                        }
                        setUpdates(isLess)
                        var type = ""
                        when (resp.type) {
                            0 -> type = "a"
                            1 -> type = "b"
                            2 -> type = "rc"
                            3 -> type = "r"
                        }
                        val serverVersion = String.format(
                            Locale.US,
                            "%d.%d.%d %s",
                            resp.major,
                            resp.minor,
                            resp.build,
                            type
                        )
                        MySharedPreferences.createLogin(this@HomeActivity)
                            .putString("ServerVersion", serverVersion)
                    }
                }
            }
            gt2.execute()

            // Sumari setmanal
            val sum = MyDrive.Summary.newBuilder()
            sum.typeTime = General.TimeType.WEEKLY
            val getMyDriveInfo = GenericTask(
                Endpoints.SUMMARY, sum.build(), true
            ) { otcResponse: OTCResponse ->
                if (otcResponse.status == OTCStatus.SUCCESS) {
                    val rsp = otcResponse.data.unpack(
                        SummaryResponse::class.java
                    )
                    val local = rsp.bestLocalRanking
                    MySharedPreferences.createLogin(this@HomeActivity)
                        .putInteger("ProfileBestLocalWeekly", local)
                    MySharedPreferences.createLogin(this@HomeActivity)
                        .putDouble("ProfileSafetyWeekly", rsp.safetyDrivingTechnique)
                    MySharedPreferences.createLogin(this@HomeActivity).putDouble(
                        "ProfileEcoWeekly",
                        Utils.clamp(
                            rsp.ecoAverageConsumption / Constants.CAR_CONSUMPTION_BEST,
                            0.0,
                            1.0
                        )
                    )
                }
            }
            getMyDriveInfo.execute()

            // Dealer
            CommunityNetwork.getDealer(null)
        }// Clau del BLE

    // Teléfon del Call Center

    // Descripció del Vehicle Condition

    // Badges

    // Get data que no hauria de canviar
    @get:UiThread
    private val oneTimeData: Unit
        private get() {
            // Clau del BLE
            ConfigurationNetwork.fetchBluetoothConfiguration()

            // Teléfon del Call Center
            val getCallCenterPhone = GenericTask(
                Endpoints.PHONE, null, true
            ) { otcResponse: OTCResponse ->
                if (otcResponse.status == OTCStatus.SUCCESS) {
                    val phone =
                        otcResponse.data.unpack(LocationPhone::class.java)
                    MySharedPreferences.createLogin(this@HomeActivity)
                        .putString("CallCenterPhone", phone.phone)
                }
            }
            getCallCenterPhone.execute()

            // Descripció del Vehicle Condition
            val getVCD = TypedTask(Endpoints.VEHICLE_CONDITION_DESCRIPTION, null, true,
                VehicleConditionDescription::class.java,
                object : TypedCallback<VehicleConditionDescription?> {
                    override fun onSuccess(@Nonnull value: VehicleConditionDescription) {
                        MySharedPreferences.createDashboard(this@HomeActivity).putBytes(
                            Constants.Prefs.VEHICLE_CONDICION_DESCRIPTION,
                            value.toByteArray()
                        )
                    }

                    override fun onError(status: OTCStatus, str: String?) {}
                })
            getVCD.execute()

            // Badges
            getVersionBadges().execute()
        }// Notificacions

    // Get data que pot canviar
    @get:UiThread
    private val data: Unit
        private get() {
            val getSNS = TypedTask(Endpoints.SOCIAL_NETWORK, null, true,
                SocialNetworkStatus::class.java,
                object : TypedCallback<SocialNetworkStatus?> {
                    override fun onSuccess(@Nonnull sns: SocialNetworkStatus) {
                        Utils.runOnBackground {
                            val ctx: Context = this@HomeActivity
                            val change = PrefsManager.getInstance().getSettingValue(
                                Constants.Prefs.SETTINGS_RECENT_TRIP,
                                ctx
                            )
                            PrefsManager.getInstance().saveSettingValue(
                                Constants.Prefs.SETTINGS_RANKINGS,
                                sns.rankingEnabled,
                                ctx
                            )
                            PrefsManager.getInstance().saveSettingValue(
                                Constants.Prefs.SETTINGS_REWARDS,
                                sns.badgeEnabled,
                                ctx
                            )
                            PrefsManager.getInstance().saveSettingValue(
                                Constants.Prefs.SETTINGS_RECENT_TRIP,
                                sns.saveRecentTripEnabled,
                                ctx
                            )
                            PrefsManager.getInstance().saveSettingValue(
                                Constants.Prefs.SETTINGS_AUTOUPDATE,
                                sns.autoUpdateDongleEnabled,
                                ctx
                            )
                            if (change != sns.saveRecentTripEnabled) {
                                Utils.setDongleEnableRouteStorage(
                                    sns.saveRecentTripEnabled
                                )
                            }
                        }
                    }

                    override fun onError(status: OTCStatus, str: String?) {}
                })
            getSNS.execute()

            // Notificacions
            ProfileNetwork.getNotificationCount(this)
            setProfileFromCache()
        }

    fun goDocuments() {
        binding.txtDocuments.performClick()
        manageUI()
    }

    private fun callCallCenter() {
        val uri =
            "tel:" + MySharedPreferences.createLogin(this@HomeActivity).getString("CallCenterPhone")
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse(uri)
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        startActivity(intent)
    }

    @UiThread
    private fun setProfileFromCache() {
        Utils.runOnBackThread {
            val msp = MySharedPreferences.createLogin(applicationContext)
            val phone = String.format("T. %s", msp.getString("Tlf"))
            val mail = msp.getString("Email")
            val name = msp.getString("Nick")
            Utils.runOnMainThread {
                binding.profilePhone.text = phone
                binding.profileMail.text = mail
                binding.profileName.text = name
                if (msp.contains("UserImageId")) {
                    val img = msp.getLong("UserImageId")
                    Glide.with(this).load(img).placeholder(binding.profileImage.drawable)
                        .into(binding.profileImage)
                } else {
                    Glide.with(this@HomeActivity)
                        .load(getDrawable(R.drawable.user_placeholder_correct))
                        .placeholder(
                            binding.profileImage.drawable
                        ).into(binding.profileImage)
                }
            }
            manageNewContentIndicator()
        }
    }

    private val profile: Unit
        get() {
            val currencyCode =
                MySharedPreferences.createLogin(applicationContext).getString("CurrencyCode")
            val currencyCountry = MySharedPreferences.createLogin(
                applicationContext
            ).getString("CurrencyCountry")
            PaymentUtils.Currency.selectedCurrency = PaymentUtils.Currency.getByCode(currencyCode)
            PaymentUtils.Currency.selectedCountry = currencyCountry
            val gt = TypedTask(Endpoints.USER_INFO, null, true,
                UserDataResponse::class.java, object : TypedCallback<UserDataResponse?> {
                    override fun onSuccess(@Nonnull value: UserDataResponse) {
                        if (!value.dongleSerialNumber.isEmpty()) {
                            OtcBle.getInstance().setSN(value.dongleSerialNumber)
                            MySharedPreferences.createLogin(applicationContext)
                                .putString("SN", value.dongleSerialNumber)
                            PaymentUtils.Currency.selectedCurrency =
                                PaymentUtils.Currency.getByCode(value.currency)
                            when (value.countryId) {
                                1 -> {
                                    PaymentUtils.Currency.selectedCountry = "IN"
                                    PaymentUtils.Currency.selectedCountry = "ID"
                                    PaymentUtils.Currency.selectedCountry = "ES"
                                }
                                2 -> {
                                    PaymentUtils.Currency.selectedCountry = "ID"
                                    PaymentUtils.Currency.selectedCountry = "ES"
                                }
                                3 -> PaymentUtils.Currency.selectedCountry = "ES"
                            }
                            MySharedPreferences.createLogin(applicationContext)
                                .putString("CurrencyCode", value.currency)
                            MySharedPreferences.createLogin(applicationContext)
                                .putString("CurrencyCountry", PaymentUtils.Currency.selectedCountry)
                        }
                    }

                    override fun onError(status: OTCStatus, str: String?) {}
                })
            gt.execute()
        }

    fun clickCommunications() {
        tabCommunity!!.performClick()
    }

    private fun initializeInLocationAndSecurity() {
        tabLocation!!.performClick()
        manageUI()
    }

    private fun retrieveViews() {
        titleBar = findViewById(R.id.home_titleBar)
        layoutMore = findViewById(R.id.navigation_layoutMore)
        txtMyRoutes = findViewById(R.id.txtMyRoutes)
        txtProfile = findViewById(R.id.txtProfile)
        txtSettings = findViewById(R.id.txtSettings)
        txtDocuments = findViewById(R.id.txtDocuments)
        txtNotifications = findViewById(R.id.txtNotifications)
        txtCallToCallCenter = findViewById(R.id.txtCallCenter)
        txtLogOut = findViewById(R.id.txtLogout)
        txtOrders = findViewById(R.id.txtOrders)
        val txtVersion = findViewById<TextView>(R.id.txtVersion)
        tabMyDrive = findViewById(R.id.navigation_tabMyDrive)
        tabDashboard = findViewById(R.id.navigation_tabDashboard)
        tabLocation = findViewById(R.id.navigation_tabLocation)
        tabCommunity = findViewById(R.id.navigation_tabCommunity)
        tabPayment = findViewById(R.id.navigation_tabPayment)
        tabKeyless = findViewById(R.id.navigation_tabKeyless)
        tabPoll = findViewById(R.id.navigation_tabPoll)
        notificationsFrame = findViewById(R.id.notificationsFrame)
        notificationsFrameCount = findViewById(R.id.notificationsFrameCount)
        updatesFrame = findViewById(R.id.updatesFrame)
        profileName = findViewById(R.id.profileName)
        profilePhone = findViewById(R.id.profilePhone)
        profileMail = findViewById(R.id.profileMail)
        profilePicture = findViewById(R.id.profileImage)
        tabMyDriveSelector = findViewById(R.id.navigation_tabMyDriveSelector)
        tabDashboardSelector = findViewById(R.id.navigation_tabDashboardSelector)
        tabLocationSelector = findViewById(R.id.navigation_tabLocationSelector)
        tabCommunitySelector = findViewById(R.id.navigation_tabCommunitySelector)
        tabPaymentSelector = findViewById(R.id.navigation_tabPaymentSelector)
        tabKeylessSelector = findViewById(R.id.navigation_tabKeylessSelector)
        tabPollSelector = findViewById(R.id.navigation_tabPollSelection)
        txtVersion.text =
            String.format("%s ver %s", getString(R.string.app_name), BuildConfig.VERSION_NAME)
    }

    private fun setEvents() {
        tabMyDrive!!.setOnClickListener(createListenerForTab())
        tabDashboard!!.setOnClickListener(createListenerForTab())
        tabLocation!!.setOnClickListener(createListenerForTab())
        tabCommunity!!.setOnClickListener(createListenerForTab())
        tabPayment!!.setOnClickListener(createListenerForTab())
        tabKeyless!!.setOnClickListener(createListenerForTab())
        tabPoll!!.setOnClickListener(createListenerForTab())
        txtMyRoutes!!.setOnClickListener(createListenerForTab())
        txtProfile!!.setOnClickListener(createListenerForTab())
        txtSettings!!.setOnClickListener(createListenerForTab())
        txtDocuments!!.setOnClickListener(createListenerForTab())
        txtNotifications!!.setOnClickListener(createListenerForTab())
        txtCallToCallCenter!!.setOnClickListener(createListenerForTab())
        txtLogOut!!.setOnClickListener(createListenerForTab())
        txtOrders!!.setOnClickListener(createListenerForTab())
        layoutMore!!.isClickable = false
        val closeMenu = findViewById<View>(R.id.closeMenu)
        closeMenu.setOnClickListener { v: View? -> showOrHideMoreLayout() }
    }

    private fun createListenerForTab(): View.OnClickListener {
        return View.OnClickListener label@{ view: View ->
            hideLayoutMore()
            prevTabSelected = tabSelected
            when (view.id) {
                R.id.navigation_tabMyDrive -> {
                    tabSelected = TabType.MyDrive
                    if (tabSelected == prevTabSelected) return@label
                    configureTitleBar(R.string.title_my_drive)
                    changeFragment(MyDriveFragment())
                }
                R.id.navigation_tabDashboard -> {
                    tabSelected = TabType.Dashboard
                    if (tabSelected == prevTabSelected) return@label
                    configureTitleBar(R.string.dashboard_and_status)
                    val ds =
                        DashboardAndStatus()
                    if (m_dashTab != -1) {
                        ds.setTab(m_dashTab)
                        m_dashTab = -1
                    }
                    if (m_vehCondition) {
                        ds.setVehCondition(true)
                        m_vehCondition = false
                    }
                    changeFragment(ds)
                }
                R.id.navigation_tabPoll -> {
                    tabSelected = TabType.Poll
                    if (tabSelected == prevTabSelected) return@label
                    configureTitleBar(R.string.poll)
                    val pf = PollFragment()
                    changeFragment(pf)
                }
                R.id.navigation_tabKeyless -> {
                    tabSelected = TabType.Keyless
                    if (tabSelected == prevTabSelected) return@label
                    configureTitleBar(R.string.keyless)
                    val va = VehicleFragment()
                    changeFragment(va)
                }
                R.id.navigation_tabLocation -> {
                    tabSelected = TabType.Location
                    if (tabSelected == prevTabSelected) return@label
                    configureTitleBar(R.string.location_and_assistance, R.drawable.my_drive_icons_6)
                    changeFragment(LocationAndSecurityFragment())
                }
                R.id.navigation_tabPayment -> {
                    tabSelected = TabType.Payment
                    if (tabSelected == prevTabSelected) return@label
                    configureTitleBar(R.string.payment, R.drawable.my_drive_icons_6)
                    changeFragment(ShoppingFragment())
                }
                R.id.navigation_tabCommunity -> {
                    tabSelected = TabType.Community
                    if (tabSelected == prevTabSelected) return@label
                    configureTitleBar(R.string.title_communications)
                    val communicationsFragment = CommunicationsFragment()
                    communicationsFragment.setListener { `object`: Any ->
                        showPostMenu(
                            `object`
                        )
                    }
                    if (m_communityTab != -1) {
                        communicationsFragment.setTab(m_communityTab)
                        communicationsFragment.setSubTab(m_communitySubtab)
                        m_communityTab = -1
                    }
                    changeFragment(communicationsFragment)
                }
                R.id.txtMyRoutes -> {
                    tabSelected = TabType.Routes
                    if (tabSelected == prevTabSelected) return@label
                    configureTitleBar(R.string.title_my_routes)
                    myRoutesFragment = MyRoutesFragment()
                    myRoutesFragment!!.setListener { showEdit: Boolean, routeItem: RouteItem ->
                        showRoutesMenu(
                            showEdit,
                            routeItem
                        )
                    }
                    changeFragment(myRoutesFragment!!)
                }
                R.id.txtProfile -> {
                    tabSelected = TabType.Profile
                    if (tabSelected == prevTabSelected) return@label
                    configureTitleBar(R.string.title_my_profile)
                    changeFragment(MyProfileFragment())
                }
                R.id.txtSettings -> {
                    tabSelected = TabType.Settings
                    if (tabSelected == prevTabSelected) return@label
                    configureTitleBar(R.string.title_settings)
                    changeFragment(SettingsFragment())
                }
                R.id.txtDocuments -> {
                    tabSelected = TabType.Documents
                    if (tabSelected == prevTabSelected) return@label
                    txtDocuments!!.isSelected = true
                    configureTitleBar(R.string.title_documents)
                    documentsFragment = DocumentsFragment()
                    changeFragment(documentsFragment!!)
                }
                R.id.txtNotifications -> {
                    tabSelected = TabType.Notifications
                    if (tabSelected == prevTabSelected) return@label
                    txtNotifications!!.isSelected = true
                    configureTitleBar(R.string.title_notifications)
                    changeFragment(NotificationFragment())
                }
                R.id.txtLogout -> {
                    val dyn =
                        DialogYesNo(this, resources.getString(R.string.you_want_exit),
                            {
                                runOnUiThread {
                                    OtcBle.getInstance().disconnect()
                                    OtcBle.getInstance().clearDeviceMac()
                                    Utils.logout(this@HomeActivity)
                                }
                            }
                        ) {}
                    dyn.show()
                }
                R.id.txtCallCenter -> {
                    val dyn =
                        DialogYesNo(this, resources.getString(R.string.call_call_center),
                            { callCallCenter() }
                        ) {}
                    dyn.show()
                }
                R.id.txtOrders -> {
                    tabSelected = TabType.Orders
                    if (tabSelected == prevTabSelected) return@label
                    txtOrders!!.isSelected = true
                    configureTitleBar(R.string.title_activity_orders)
                    changeFragment(OrdersFragment { parent: OrdersFragment ->
                        showOrdersMenu(
                            parent
                        )
                    })
                }
            }
            manageUI()
        }
    }

    private fun manageUI() {
        tabMyDrive!!.isSelected = tabSelected == TabType.MyDrive
        tabMyDriveSelector!!.visibility =
            if (tabSelected == TabType.MyDrive) View.VISIBLE else View.INVISIBLE
        tabDashboard!!.isSelected = tabSelected == TabType.Dashboard
        tabDashboardSelector!!.visibility =
            if (tabSelected == TabType.Dashboard) View.VISIBLE else View.INVISIBLE
        tabLocation!!.isSelected = tabSelected == TabType.Location
        tabLocationSelector!!.visibility =
            if (tabSelected == TabType.Location) View.VISIBLE else View.INVISIBLE
        tabCommunity!!.isSelected = tabSelected == TabType.Community
        tabCommunitySelector!!.visibility =
            if (tabSelected == TabType.Community) View.VISIBLE else View.INVISIBLE
        tabPayment!!.isSelected = tabSelected == TabType.Payment
        tabPaymentSelector!!.visibility =
            if (tabSelected == TabType.Payment) View.VISIBLE else View.INVISIBLE
        txtMyRoutes!!.isSelected = prevTabSelected == TabType.Routes
        txtProfile!!.isSelected = prevTabSelected == TabType.Profile
        txtSettings!!.isSelected = prevTabSelected == TabType.Settings
        txtDocuments!!.isSelected = prevTabSelected == TabType.Documents
        txtNotifications!!.isSelected = prevTabSelected == TabType.Notifications
        txtOrders!!.isSelected = prevTabSelected == TabType.Orders
        tabKeyless!!.isSelected = tabSelected == TabType.Keyless
        tabKeylessSelector!!.visibility =
            if (tabSelected == TabType.Keyless) View.VISIBLE else View.INVISIBLE
        tabPoll!!.isSelected = tabSelected == TabType.Poll
        tabPollSelector!!.visibility =
            if (tabSelected == TabType.Poll) View.VISIBLE else View.INVISIBLE
        manageNewContentIndicator()
    }

    private fun manageNewContentIndicator() {
        val count = longArrayOf(
            MySharedPreferences.createLogin(this@HomeActivity).getLong("NotificationCount")
        )
        Utils.runOnMainThread {
            titleBar!!.setNotification(count[0].toInt())
            if (count[0] == 0L) {
                notificationsFrame!!.visibility = View.GONE
            } else {
                notificationsFrame!!.visibility = View.VISIBLE
                if (count[0] >= 1000) {
                    notificationsFrameCount!!.text = String.format(
                        Locale.US,
                        "+%d",
                        999
                    )
                } else {
                    notificationsFrameCount!!.text = String.format(
                        Locale.US,
                        "%d",
                        count[0]
                    )
                }
            }
        }
    }

    private fun setUpdates(upd: Boolean) {
        updatesFrame!!.visibility = if (upd) View.VISIBLE else View.GONE
    }

    fun configureTitleBar(stringRes: Int, vararg imgRes: Int) {
        titleBar!!.setTitle(stringRes)
        if (imgRes.size <= 0) {
            titleBar!!.hideImgRight2()
        }
        titleBar!!.setLeftButtonImage(R.drawable.menu_icons_10)
        titleBar!!.setListener(object : NewTitleBar.NewTitleBarListener {
            override fun onLeftClick() {
                prevTabSelected = tabSelected
                tabSelected = TabType.More
                showOrHideMoreLayout()
            }

            override fun onRight1Click() {}
            override fun onRight2Click() {}
        })
    }

    private fun changeFragment(fragment: Fragment) {
        try {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.content, fragment).commit()
        } catch (ignored: IllegalStateException) {
            // No s'ha pogut canviar de fragment, així que ñe
        } catch (ignored: IllegalArgumentException) {
        }
    }

    private fun showOrHideMoreLayout() {
        txtSettings!!.setText(R.string.title_settings)
        txtDocuments!!.setText(R.string.title_documents)
        if (layoutMore!!.visibility == View.VISIBLE) {
            binding.vehicleList.root.visibility = View.GONE
            hideLayoutMoreAndBackToPrevious()
        } else {
            layoutMore!!.visibility = View.VISIBLE
            binding.vehicleList.root.visibility = View.GONE
        }
    }

    private fun hideLayoutMore() {
        layoutMore!!.visibility = View.GONE
    }

    private fun hideLayoutMoreAndBackToPrevious() {
        hideLayoutMore()
        tabSelected = prevTabSelected
        manageUI()
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(newContentReceiver)
        m_timerUpdate!!.cancel()
        try {
            super.onPause()
        } catch (iae: IllegalArgumentException) {
            //Log.e("HomeActivity", "IllegalArgumentException", iae);
        }
    }

    override fun onResume() {
        try {
            super.onResume()
        } catch (ignored: IllegalArgumentException) {
        }
        setProfileFromCache()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            newContentReceiver,
            IntentFilter(Constants.Prefs.HAS_NEW_CONTENT)
        )
        commonData
        if (m_timerUpdate != null) {
            m_timerUpdate!!.cancel()
        }
        m_timerUpdate = Timer("HomeActivityTimer")
        m_timerUpdate!!.scheduleAtFixedRate(object : TimerTask() {
            var iters = 0
            var odos = 0
            override fun run() {
                // 30 segons
                if (iters % 30 == 0) {
                    Utils.runOnMainThread { data }
                }
                // 300 segons
                if (iters % 300 == 0) {
                    Utils.runOnMainThread { slowData }
                }
                try {
                    if (OtcBle.getInstance().carStatus.contains("KL15")) {
                        if (!MySharedPreferences.createDashboard(this@HomeActivity)
                                .getBoolean("milesMessage") && OtcBle.getInstance().carStatus.getBitVar(
                                "OdoScale"
                            )
                        ) {
                            ++odos
                            if (odos > 5) {
                                Utils.runOnMainThread {
                                    val cd = CustomDialog(this@HomeActivity)
                                    cd.setMessage(resources.getString(R.string.no_milles))
                                    cd.show()
                                }
                                MySharedPreferences.createDashboard(this@HomeActivity)
                                    .putBoolean("milesMessage", true)
                            }
                        } else if (!OtcBle.getInstance().carStatus.contains("OdoScale") || !OtcBle.getInstance().carStatus.getBitVar(
                                "OdoScale"
                            )
                        ) {
                            odos = 0
                            MySharedPreferences.createDashboard(this@HomeActivity)
                                .putBoolean("milesMessage", false)
                        }
                    }
                } catch (ignored: Exception) {
                }
                Utils.runOnMainThread {
                    if (Utils.developer) {
                        tabKeyless!!.visibility = View.VISIBLE
                    } else {
                        tabKeyless!!.visibility = View.GONE
                    }
                }
                ++iters
            }
        }, 0, 1000)
        if (MySharedPreferences.createLogin(getContext()).getBoolean("HasToShowPopup")) {
            val cd = CustomDialog(getContext())
            cd.setMessage(
                """
                    ${getString(R.string.months_no_connect)}
                    ${getString(R.string.days_0)}
                    """.trimIndent()
            )
            cd.setOnOkListener {
                Utils.logout(
                    applicationContext
                )
            }
            cd.show()
        } else if (MySharedPreferences.createLogin(getContext())
                .getBoolean("ShowPopupNewMobile")
        ) {
            Utils.changedPhone(this)
        }
        if (MySharedPreferences.createLogin(this).getBoolean("GoToDashboard")) {
            tabDashboard!!.performClick()
            MySharedPreferences.createLogin(this).remove("GoToDashboard")
        }
    }

    private val commonData: Unit
        private get() {
            val getProfile = GenericTask(
                Endpoints.USER_INFO, null, true
            ) { otcResponse: OTCResponse ->
                val msp =
                    MySharedPreferences.createLogin(applicationContext)
                if (otcResponse.status == OTCStatus.SUCCESS) {
                    val udr =
                        otcResponse.data.unpack(
                            UserDataResponse::class.java
                        )
                    profilePhone!!.text = String.format("T. %s", udr.phone)
                    profileMail!!.text = udr.email
                    profileName!!.text = udr.username
                    msp.putString("Nick", udr.username)
                    msp.putString("Tlf", udr.phone)
                    msp.putString(
                        "Email",
                        udr.email.lowercase(Locale.getDefault())
                    )
                    msp.putString("macBLE", udr.mac)
                    msp.putBoolean("Expired", udr.isExpired)
                    if (udr.imageId != 0L) {
                        val imgId = udr.imageId
                        Glide.with(this@HomeActivity).load(imgId).into(profilePicture!!)
                        msp.putLong("UserImageId", imgId)
                    } else {
                        msp.remove("UserImageId")
                        Glide.with(this@HomeActivity)
                            .load(getDrawable(R.drawable.user_placeholder_correct)).into(
                                profilePicture!!
                            )
                    }
                }
            }
            getProfile.execute()
        }

    override fun onBackPressed() {
        val frag = supportFragmentManager.findFragmentById(R.id.content)
        if (frag !is FragmentBackPresser || (frag as FragmentBackPresser).onBackPressed()) {
            if (tabSelected == TabType.More) {
                hideLayoutMoreAndBackToPrevious()
            } else if (isMenuShown) {
                isMenuShown = false
                hideMenu()
            } else {
                moveTaskToBack(true)
            }
        }
    }

    private fun hideMenu() {
        val menuFragment =
            supportFragmentManager.fragments[supportFragmentManager.fragments.size - 1]
        supportFragmentManager
            .beginTransaction()
            .remove(menuFragment)
            .commit()
    }

    private fun showOrdersMenu(parent: OrdersFragment) {
        val menu = OrdersMenuFragment(parent)
        showMenuFragment(menu)
    }

    private fun showRoutesMenu(showEdit: Boolean, routeItem: RouteItem) {
        val routesMenuFragment = RoutesMenuFragment()

        //if (routeItem.getRouteType().equals(General.RouteType.AUTOSAVED)){
        //routesMenuFragment.showSaveButton();
        //RoutesMenuFragment.btnSave.setVisibility(View.VISIBLE);
        //}
        routesMenuFragment.configure(showEdit, routeItem) {
            if (myRoutesFragment != null && myRoutesFragment!!.isVisible) {
                myRoutesFragment!!.refreshRouteList()
            }
        }
        showMenuFragment(routesMenuFragment)
    }

    private fun showPostMenu(`object`: Any) {
        val postMenuFragment = PostMenuFragment()
        postMenuFragment.setPostSelected(`object`)
        showMenuFragment(postMenuFragment)
    }

    private fun showMenuFragment(fragment: Fragment) {
        isMenuShown = true
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.home_contentMenu, fragment)
            .commit()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == WalletFragment.REQUEST_CODE_GALLERY && resultCode == RESULT_OK) {
            doActionsAfterGallery(data)
        } else if (requestCode == WalletFragment.REQUEST_CODE_CAMERA && resultCode == RESULT_OK) {
            doActionsAfterCamera()
        } else if (requestCode == QRActivity.QR_RESULT && resultCode == RESULT_OK) {
            val code = data!!.getStringExtra("QR_RESULT")

            val body = com.otc.alice.api.model.Vehicle.VehicleLink.newBuilder()
                .setVin(code)
                .build()

            Network.vehicle.linkVehicle(body).enqueue(object: OtcCallback<com.otc.alice.api.model.Vehicle.VehicleData>(
                com.otc.alice.api.model.Vehicle.VehicleData::class.java) {
                override fun response(response: com.otc.alice.api.model.Vehicle.VehicleData) {
                    Log.d("alert", "Linked")
                }

                override fun error(status: Shared.OTCStatus) {
                    Log.d("alert", "Not Linked: ${status.name}")
                }
            })
        }
    }

    private fun doActionsAfterGallery(data: Intent?) {
        if (documentsFragment != null && documentsFragment!!.isVisible) {
            documentsFragment!!.doActionsAfterGallery(data)
        }
    }

    private fun doActionsAfterCamera() {
        if (documentsFragment != null && documentsFragment!!.isVisible) {
            documentsFragment!!.doActionsAfterCamera()
        }
    }

    companion object {
        fun newInstance(ctx: Context) = ctx.startActivity(Intent(ctx, HomeActivity::class.java))
    }
}