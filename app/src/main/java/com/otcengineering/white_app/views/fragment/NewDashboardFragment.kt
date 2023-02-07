package com.otcengineering.white_app.views.fragment

import android.R.attr.button
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.protobuf.InvalidProtocolBufferException
import com.otc.alice.api.model.Community.PostStats
import com.otc.alice.api.model.DashboardAndStatus.*
import com.otc.alice.api.model.General
import com.otc.alice.api.model.General.VehicleStatus
import com.otc.alice.api.model.MyDrive
import com.otc.alice.api.model.MyDrive.SummaryResponse
import com.otc.alice.api.model.ProfileAndSettings.UserNotifications
import com.otc.alice.api.model.Shared.OTCResponse
import com.otc.alice.api.model.Shared.OTCStatus
import com.otcengineering.apible.OtcBle
import com.otcengineering.white_app.MyApp
import com.otcengineering.white_app.R
import com.otcengineering.white_app.activities.ChartActivity
import com.otcengineering.white_app.activities.ConditionActivity
import com.otcengineering.white_app.activities.NewRouteActivity
import com.otcengineering.white_app.components.TitleBar
import com.otcengineering.white_app.databinding.FragmentNewDashboardBinding
import com.otcengineering.white_app.fragments.EventFragment
import com.otcengineering.white_app.fragments.cached.NewDashboardCache
import com.otcengineering.white_app.keyless.activity.VehicleSchedulerActivity
import com.otcengineering.white_app.network.Endpoints
import com.otcengineering.white_app.network.ProfileNetwork
import com.otcengineering.white_app.tasks.GenericTask
import com.otcengineering.white_app.tasks.TypedTask
import com.otcengineering.white_app.utils.*
import com.otcengineering.white_app.utils.interfaces.TypedCallback
import com.otcengineering.white_app.views.activity.HomeActivity
import java.util.*
import javax.annotation.Nonnull


class NewDashboardFragment : EventFragment("DashboardActivity") {

    private var m_timer: Timer? = null
    private var m_timer2: Timer? = null
    private var action = -1
    private var notificationId: Long = -1

    // Cached variables
    private var m_cache: NewDashboardCache?
    private var m_vehCondition = false

    private val binding: FragmentNewDashboardBinding by lazy {
        FragmentNewDashboardBinding.inflate(
            layoutInflater
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding.fragment = this
        setEvents()
        setCachedValues()
        binding.carPicture.setImageDrawable(null)
        setDefaultCarPicture()
        dashboardData
        val getImage = GenericTask(
            Endpoints.DASHBOARD_CAR_PHOTO, null, true
        ) { otcResponse: OTCResponse ->
            if (otcResponse.status == OTCStatus.SUCCESS) {
                val cp =
                    otcResponse.data.unpack(CarPhoto::class.java)
                MySharedPreferences.createDashboard(context)
                    .putLong("CarPictureId", cp.fileId)
                val fileId = cp.fileId
                setCarPicture(fileId)
            } else {
                MySharedPreferences.createDashboard(context).remove("CarPictureId")
                setDefaultCarPicture()
            }
        }
        getImage.execute()

        return binding.root
    }

    private val dashboardData: Unit
        private get() {
            val getDashboard = GenericTask(
                Endpoints.DASHBOARD, null, true
            ) { otcResponse: OTCResponse ->
                if (otcResponse.status == OTCStatus.SUCCESS) {
                    var resp =
                        otcResponse.data.unpack(
                            DashboardResponse::class.java
                        )
                    setFinance(
                        resp.loanMaturity,
                        String.format(
                            Locale.US,
                            "%d / %d %s",
                            resp.payedLoans,
                            resp.totalLoans,
                            getString(R.string.times)
                        ),
                        String.format(
                            Locale.US,
                            "%,d",
                            resp.tradeInPrice
                        )
                    )
                    val json = MySharedPreferences.createLogin(context)
                        .getString("StatusCache")
                    var vs: VehicleStatus? = null
                    if (!json.isEmpty()) {
                        try {
                            vs = Gson().fromJson(
                                json,
                                VehicleStatus::class.java
                            )
                        } catch (jse: JsonSyntaxException) {
                        }
                    }
                    if (vs != null && DateUtils.compareDates(
                            resp.dateUpdate
                                .substring(0, resp.dateUpdate.indexOf(".")),
                            vs.date,
                            "yyyy-MM-dd HH:mm:ss",
                            "yyyy-MM-dd HH:mm:ss"
                        ) == DateUtils.DATE_BEFORE
                    ) {
                        setUpdate(
                            DateUtils.utcStringToLocalString(
                                vs.date,
                                "yyyy-MM-dd HH:mm:ss",
                                "dd/MM/yyyy - HH:mm"
                            )
                        )
                        if (!OtcBle.getInstance().isConnected) {
                            setMileage(
                                String.format(
                                    Locale.US,
                                    "%d km",
                                    vs.odometer
                                )
                            )
                            setFuel(vs.fuelLevel)
                        }
                        val builder =
                            DashboardResponse.newBuilder(
                                resp
                            )
                        builder.totalMileage = vs.odometer
                        builder.dateUpdate = vs.date
                        builder.fuelLevel = vs.fuelLevel
                        resp = builder.build()
                    } else {
                        if (!OtcBle.getInstance().isConnected) {
                            setUpdate(
                                DateUtils.utcStringToLocalString(
                                    resp.dateUpdate
                                        .substring(0, resp.dateUpdate.indexOf(".")),
                                    "yyyy-MM-dd HH:mm:ss",
                                    "dd/MM/yyyy - HH:mm"
                                )
                            )
                            setMileage(
                                String.format(
                                    Locale.US,
                                    "%d km",
                                    resp.totalMileage
                                )
                            )
                            setFuel(resp.fuelLevel)
                        }
                    }
                    setTextCarInfo(
                        makeCarAge(resp.ageYears, resp.ageMonths),
                        resp.inServiceTiming
                    )
                    val mDriving =
                        minutesToHourMin(resp.monthlyDrivingMinutes)
                    val tDriving =
                        minutesToHourMin(resp.totalDrivingMinutes)
                    val msp =
                        MySharedPreferences.createLogin(context)
                    setCarDrive(
                        String.format("%s / %s", mDriving, tDriving),
                        String.format(
                            Locale.US,
                            "%01.02f L / %01.02f L",
                            resp.monthlyFuelConsume / 100.0f,
                            resp.totalFuelConsume / 100.0f
                        ),
                        String.format(
                            Locale.US,
                            "%d",
                            msp.getInteger("ProfileBestLocalWeekly")
                        )
                    )
                    MySharedPreferences.createDashboard(context)
                        .putBytes("DashboardCache", resp.toByteArray())
                }
            }
            getDashboard.execute()
            val getStats = GenericTask(
                Endpoints.POST_STATS, null, true
            ) { otcResponse: OTCResponse ->
                if (otcResponse.status == OTCStatus.SUCCESS) {
                    val ps =
                        otcResponse.data.unpack(PostStats::class.java)
                    MySharedPreferences.createDashboard(context)
                        .putBytes("PostStatsCache", ps.toByteArray())
                    setTextDrive(
                        String.format(
                            Locale.US,
                            "%d/%d",
                            ps.monthlyPosts,
                            ps.totalPosts
                        ),
                        String.format(
                            Locale.US,
                            "%d/%d",
                            ps.monthlyLikes,
                            ps.totalLikes
                        )
                    )
                }
            }
            getStats.execute()
            val summary = MyDrive.Summary.newBuilder().setTypeTime(General.TimeType.WEEKLY).build()
            val getMyDrive = TypedTask(Endpoints.SUMMARY, summary, true,
                SummaryResponse::class.java, object : TypedCallback<SummaryResponse?> {
                    override fun onSuccess(@Nonnull value: SummaryResponse) {
                        val msp = MySharedPreferences.createDashboard(context)
                        msp.putDouble("ProfileSafetyWeekly", value.safetyDrivingTechnique)
                        msp.putDouble(
                            "ProfileEcoWeekly",
                            Utils.clamp(
                                value.ecoAverageConsumption / Constants.CAR_CONSUMPTION_BEST,
                                0.0,
                                1.0
                            )
                        )
                        msp.putInteger("ProfileBestLocalWeekly", value.bestLocalRanking)
                        setDriving(
                            msp.getDouble("ProfileSafetyWeekly"),
                            msp.getDouble("ProfileEcoWeekly")
                        )
                    }

                    override fun onError(status: OTCStatus, str: String?) {}
                })
            getMyDrive.execute()
            if (!OtcBle.getInstance().isConnected) {
                val getVehicleCondition = TypedTask(Endpoints.VEHICLE_CONDITION, null, true,
                    VehicleCondition::class.java,
                    object : TypedCallback<VehicleCondition?> {
                        override fun onSuccess(@Nonnull value: VehicleCondition) {
                            setCarCondition(Utils.hasProblems(value))
                        }

                        override fun onError(status: OTCStatus, str: String?) {}
                    })
                getVehicleCondition.execute()
            }
            Utils.runOnMainThread { loadLastNoti() }
        }

    private fun loadLastNoti() {
        if (MySharedPreferences.createLogin(context).getLong("NotificationCount") >= 1) {
            val page = General.Page.newBuilder().setPage(1).build()
            val getNotifications = TypedTask(Endpoints.GET_USER_NOTIFICATIONS, page, true,
                UserNotifications::class.java,
                object : TypedCallback<UserNotifications?> {
                    override fun onSuccess(@Nonnull value: UserNotifications) {
                        Utils.runOnBackThread {
                            for (i in 0 until value.notificationListCount) {
                                if (value.getNotificationList(i)
                                        .typeValue >= 6 && value.getNotificationList(i)
                                        .typeValue <= 9
                                ) {
                                    if (!value.getNotificationList(i).readed) {
                                        Utils.runOnMainThread {
                                            action =
                                                value.getNotificationList(i).typeValue - 6
                                            notificationId = value.getNotificationList(i).id
                                            binding.imgNotification.visibility = View.VISIBLE
                                            binding.txtNotification.text =
                                                getString(R.string.you_have_new_message)
                                        }
                                        return@runOnBackThread
                                    }
                                }
                            }
                            Utils.runOnMainThread {
                                binding.txtNotification.text = ""
                                binding.imgNotification.visibility = View.GONE
                            }
                        }
                    }

                    override fun onError(status: OTCStatus, str: String?) {}
                })
            getNotifications.execute()
        } else {
            binding.txtNotification.text = ""
            binding.imgNotification.visibility = View.GONE
        }
    }

    private fun setCachedValues() {
        try {
            setDriving(m_cache!!.safety, m_cache!!.eco)
            setFinance(
                m_cache!!.loanMaturity,
                String.format(
                    Locale.US,
                    "%d / %d %s",
                    m_cache!!.paidLoans,
                    m_cache!!.totalLoans,
                    getString(R.string.times)
                ),
                ""
            )
            setUpdate(m_cache!!.update)
            setTextCarInfo(makeCarAge(m_cache!!.years, m_cache!!.months), m_cache!!.inServiceTiming)
            if (!OtcBle.getInstance().isConnected) {
                setMileage(String.format(Locale.US, "%d km", m_cache!!.mileage))
                setFuel(m_cache!!.fuel)
            }
            val mDriving = minutesToHourMin(m_cache!!.monthlyMinutes)
            val tDriving = minutesToHourMin(m_cache!!.totalMinutes)
            setCarDrive(
                String.format("%s / %s", mDriving, tDriving),
                String.format(
                    Locale.US,
                    "%01.02f L / %01.02f L",
                    m_cache!!.monthlyFuel,
                    m_cache!!.totalFuel
                ),
                String.format(
                    Locale.US, "%d", m_cache!!.bestLocalWeekly
                )
            )
            if (m_cache!!.pictureID > 0) {
                setCarPicture(m_cache!!.pictureID)
            }
            setTextDrive(
                String.format(Locale.US, "%d/%d", m_cache!!.monthlyPosts, m_cache!!.totalPosts),
                String.format(
                    Locale.US, "%d/%d", m_cache!!.monthlyLikes, m_cache!!.totalLikes
                )
            )
            m_cache = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun makeCarAge(years: Int, months: Int): String {
        var sYear: String? = null
        var sMonth: String? = null
        if (years > 0) {
            sYear = if (years == 1) {
                getString(R.string.one_year)
            } else {
                getString(R.string.n_years, years)
            }
        }
        if (months > 0) {
            sMonth = if (months == 1) {
                getString(R.string.one_month)
            } else {
                getString(R.string.n_months, months)
            }
        }
        return if (sYear != null && sMonth != null) {
            String.format("%s / %s", sYear, sMonth)
        } else if (sYear == null && sMonth != null) {
            sMonth
        } else sYear ?: getString(R.string.zero_m_zero_y)
    }

    private fun minutesToHourMin(minutes: Int): String {
        val hours = minutes / 60
        val min = minutes % 60
        val negative = if (minutes < 0) "-" else ""
        return String.format(Locale.US, "%s%02d:%02d", negative, Math.abs(hours), Math.abs(min))
    }

    override fun onResume() {
        super.onResume()
        m_timer = Timer("DashboardTimer")
        m_timer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                Utils.runOnMainThread {
                    if (OtcBle.getInstance()
                            .isConnected && OtcBle.getInstance().carStatus.contains("Fuel")
                    ) {
                        setFuel(OtcBle.getInstance().carStatus.getByteVar("Fuel").toInt())
                        setMileage(
                            String.format(
                                Locale.US,
                                "%d km",
                                OtcBle.getInstance().carStatus.getIntVar("Odometer")
                            )
                        )
                        setUpdate(DateUtils.getLocalString("dd/MM/yyyy - HH:mm"))
                    }
                }
                val cs = OtcBle.getInstance().carStatus
                if (OtcBle.getInstance().isConnected && cs.getBitVar("KL15") != null) {
                    try {
                        val vc =
                            cs.getBitVar("EngineNotif") || cs.getBitVar("EpsNotif") || cs.getBitVar(
                                "AscNotif"
                            ) || cs.getBitVar("BrakeSystemNotif") ||
                                    cs.getBitVar("AbsNotif") || cs.getBitVar("OssImmoNotif") || cs.getBitVar(
                                "KosNotif"
                            ) || cs.getBitVar("SrsNotif") ||
                                    cs.getBitVar("AtNotif") || cs.getBitVar("OilNotif") || cs.getBitVar(
                                "ChargeNotif"
                            ) || cs.getBitVar("BrakeFluidNotif") ||
                                    cs.getBitVar("OssElecNotif") || cs.getBitVar("OssSteeNotif")
                        Utils.runOnMainThread {
                            setCarCondition(
                                vc
                            )
                        }
                    } catch (npe: NullPointerException) {
                        // No em toquis els pebrots, Java
                        npe.printStackTrace()
                    }
                }
            }
        }, 0, 250)
        m_timer2 = Timer("DashboardTimer2")
        m_timer2!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                dashboardData
            }
        }, 10000, 10000)
        binding.activeRoute.visibility = if (PrefsManager.getInstance()
                .getRouteInProgress(context) != null
        ) View.VISIBLE else View.GONE
        if (m_vehCondition) {
            m_vehCondition = false
            startActivity(Intent(MyApp.getCurrentActivity(), ConditionActivity::class.java))
        }
    }

    override fun onPause() {
        super.onPause()
        m_timer!!.cancel()
        m_timer2!!.cancel()
    }

    private fun setEvents() {
        binding.carConditionLayout.setOnClickListener { v: View? ->
            if (Utils.developer) startActivity(
                Intent(
                    context,
                    VehicleSchedulerActivity::class.java
                )
            ) else startActivity(
                Intent(
                    activity,
                    ConditionActivity::class.java
                )
            )
        }
        binding.carPicture.setOnClickListener { v: View? ->
            val ha = activity as HomeActivity?
            ha?.goDocuments()
        }
        binding.layoutEco.setOnClickListener { v: View? ->
            openChart(
                Constants.ChartMode.ECO
            )
        }
        binding.layoutSafety.setOnClickListener { v: View? ->
            openChart(
                Constants.ChartMode.SAFETY
            )
        }
        binding.layoutMileage.setOnClickListener { v: View? ->
            openChart(
                Constants.ChartMode.MILEAGE
            )
        }
        binding.layoutNotification.setOnClickListener { v: View? ->
            if (action > -1) {
                ProfileNetwork.setNotificationRead(notificationId)
                val l_action = action
                notificationId = -1
                action = -1
                val home = activity as HomeActivity?
                if (home != null) {
                    home.setCommunityTab(2 + (2 + l_action) / 2)
                    home.setCommunitySubtab(l_action % 2)
                    home.clickCommunications()
                }
            }
        }
        binding.activeRoute.setOnClickListener { v: View? ->
            val intent = Intent(context, NewRouteActivity::class.java)
            intent.putExtra("RouteActive", true)
            startActivity(intent)
        }
    }

    private fun openChart(mode: Int) {
        val intent = Intent(context, ChartActivity::class.java)
        intent.putExtra(Constants.Extras.CHART_MODE, mode)
        intent.putExtra(Constants.Extras.TIME_TYPE, Constants.TimeType.WEEKLY)
        startActivity(intent)
    }

    @UiThread
    private fun setDefaultCarPicture() {
        val msp = MySharedPreferences.createDashboard(context)
        val imgId = msp.getLong("CarPictureId")
        if (imgId > 0) {
            setCarPicture(imgId)
        } else {
            Glide.with(this).load(R.drawable.car_otc)
                .apply(RequestOptions().transform(RoundedCorners(20))).into(
                    binding.carPicture
                )
        }
    }

    @UiThread
    private fun setCarPicture(fileId: Long) {
        Glide.with(this)
            .load(fileId)
            .placeholder(binding.carPicture.drawable)
            .apply(RequestOptions().transform(RoundedCorners(20)))
            .into(binding.carPicture)
    }

    @UiThread
    private fun setTextCarInfo(carAge: String, serviceTiming: Boolean) {
        // this.carAge.setText(carAge);
        binding.carServiceTiming.text =
            if (serviceTiming) getString(R.string.maintenance_timing) else getString(R.string.no_maintenance_timing)
        binding.carServiceTiming.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (serviceTiming) R.color.quantum_black_100 else R.color.colorPrimary
            )
        )
        binding.maintenanceLayout.background = ContextCompat.getDrawable(
            requireContext(),
            if (serviceTiming) R.drawable.maintenance_red else R.drawable.my_edittext_bg
        )
    }

    @UiThread
    private fun setMileage(mileage: String) {
        // this.carMileage.setText(mileage);
        if (OtcBle.getInstance().isConnected) {
            binding.carMileage.text = String.format(
                Locale.US,
                "%d km",
                OtcBle.getInstance().carStatus.getIntVar("Odometer")
            )
            binding.carAge.text = String.format(
                Locale.US, "%01.01f%%", OtcBle.getInstance().carStatus.rawData["SOC"]!!
                    .intValue / 10.0f
            )
        } else {
            binding.carMileage.text = "--- km"
            binding.carAge.text = "--- %"
        }
    }

    @UiThread
    private fun setTextDrive(posts: String, likes: String) {
        binding.txtPosts.text = posts
        binding.txtLikes.text = likes
    }

    @UiThread
    private fun setCarDrive(drivingTime: String, fuel: String, rank: String) {
        binding.txtDrivingTime.text = drivingTime
        binding.txtLiters.text = fuel
        binding.txtLocalRanking.text = rank
    }

    @UiThread
    private fun setDriving(safety: Double, eco: Double) {
        binding.progressSafety.setProgress(safety.toInt())
        binding.progressEco.setProgress((eco * 100).toInt())
        binding.txtSafety.text =
            String.format(Locale.US, "%s\n%1.1f", getString(R.string.score), safety / 10)
        binding.txtEco.text =
            String.format(Locale.US, "%s\n%1.1f", getString(R.string.score), eco * 10)
    }

    @UiThread
    private fun setUpdate(update: String) {
        var update: String? = update
        if (update == null) {
            update = ""
        }
        binding.carUpdated.text = String.format("%s: %s", getString(R.string.update), update)
    }

    @UiThread
    private fun setFuel(fuel: Int) {
        binding.carFuel.setProgress(fuel)
    }

    @UiThread
    private fun setFinance(maturity: String, times: String, price: String) {
        binding.txtMaturity.text = getString(R.string.maturity_n, maturity)
        binding.txtTimes.text = times
        binding.txtPrice.text = price
    }

    private fun setCarCondition(nok: Boolean) {
        Glide.with(this).load(if (nok) R.drawable.dashboard_icons2 else R.drawable.dashboard_icons5)
            .into(
                binding.carCondition
            )
        binding.carConditionLayout.setBackgroundResource(if (nok) R.drawable.condition_error else R.drawable.my_edittext_bg)
        binding.textView41.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (nok) R.color.error else R.color.colorPrimary
            )
        )
        binding.carCondition.imageTintList =
            if (nok) ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.error
                )
            ) else null
        binding.imageView16.imageTintList =
            if (nok) ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.error
                )
            ) else null
    }

    fun setVehicleCondition(cnd: Boolean) {
        m_vehCondition = cnd
    }

    init {
        m_cache = NewDashboardCache()
        val msp = MySharedPreferences.createDashboard(context)
        val msp2 = MySharedPreferences.createLogin(context)
        val json = msp2.getString("StatusCache")
        if (!json.isEmpty()) {
            try {
                val vs = Gson().fromJson(json, VehicleStatus::class.java)
                if (vs != null) {
                    m_cache!!.update = DateUtils.reparseDateTime(
                        vs.date,
                        "yyyy-MM-dd HH:mm:ss",
                        "dd/MM/yyyy - HH:mm"
                    )
                    m_cache!!.fuel = vs.fuelLevel
                    m_cache!!.mileage = vs.odometer
                }
            } catch (jse: JsonSyntaxException) {
                //Log.e("CarStatusFragment", "JsonSyntaxException", jse);
            }
        }
        m_cache!!.safety = msp2.getDouble("ProfileSafetyWeekly")
        m_cache!!.eco = msp2.getDouble("ProfileEcoWeekly")
        if (msp.contains("CarPictureId")) {
            m_cache!!.pictureID = msp.getLong("CarPictureId")
        }
        Utils.runOnBackThread {
            if (msp.contains("DashboardCache")) {
                val arr = msp.getBytes("DashboardCache")
                val resp: DashboardResponse
                try {
                    resp =
                        DashboardResponse.parseFrom(arr)
                    m_cache!!.loanMaturity = resp.loanMaturity
                    m_cache!!.paidLoans = resp.payedLoans
                    m_cache!!.totalLoans = resp.totalLoans
                    m_cache!!.update =
                        DateUtils.utcStringToLocalString(
                            resp.dateUpdate,
                            "yyyy-MM-dd HH:mm:ss.S",
                            "dd/MM/yyyy - HH:mm"
                        )
                    m_cache!!.years = resp.ageYears
                    m_cache!!.months = resp.ageMonths
                    m_cache!!.inServiceTiming = resp.inServiceTiming
                    m_cache!!.mileage = resp.totalMileage
                    m_cache!!.fuel = resp.fuelLevel
                    m_cache!!.monthlyMinutes = resp.monthlyDrivingMinutes
                    m_cache!!.totalMinutes = resp.totalDrivingMinutes
                    m_cache!!.monthlyFuel = resp.monthlyFuelConsume / 100.0f
                    m_cache!!.totalFuel = resp.totalFuelConsume / 100.0f
                    m_cache!!.bestLocalWeekly = msp.getInteger("ProfileBestLocalWeekly")
                } catch (e: InvalidProtocolBufferException) {
                    e.printStackTrace()
                }
            }
            if (msp.contains("PostStatsCache")) {
                val bs = msp.getBytes("PostStatsCache")
                try {
                    val ps = PostStats.parseFrom(bs)
                    m_cache!!.monthlyPosts = ps.monthlyPosts
                    m_cache!!.totalPosts = ps.totalPosts
                    m_cache!!.monthlyLikes = ps.monthlyLikes
                    m_cache!!.totalLikes = ps.totalLikes
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}