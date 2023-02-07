package com.otcengineering.white_app.keyless.activity;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.otcengineering.white_app.R;
import com.otcengineering.white_app.activities.BaseActivity;
import com.otcengineering.white_app.activities.QRActivity;
import com.otcengineering.white_app.interfaces.NetworkCallback;
import com.otcengineering.white_app.keyless.adapter.BookingAdapter;
import com.otcengineering.white_app.components.TitleBar;
import com.otcengineering.white_app.serialization.VehicleBooking;
import com.otcengineering.white_app.serialization.VehicleBookingScheduler;
import com.otcengineering.white_app.tasks.NetTask;
import com.otcengineering.white_app.utils.DateUtils;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.TextStyle;

import java.util.BitSet;
import java.util.Locale;
import java.util.TimeZone;

public class VehicleSchedulerActivity extends BaseActivity {
    private Button mButtonNext, mButtonBack;
    private ImageButton mQrButton;
    private ConstraintLayout mLayoutCarId, mLayoutCalendar, mLayoutSchedule, mLayoutConfirm;
    private CalendarView mCalendarView;
    private TitleBar mTitleBar;
    private RecyclerView mTimePicker;
    private EditText mEtVehicleId;
    private TextView mSummary;
    private BookingAdapter mAdapter;
    private LocalDate mDate;

    private String mVehicleId;
    private VehicleBooking mBooking;
    private String mSelectedDate;

    private int mStep = 0, mMaxSteps = 3;

    public VehicleSchedulerActivity() {
        super("VehicleSchedulerActivity");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_scheduler);

        getViews();
        setEvents();
        setLayouts();
        checkButtons();
    }

    private void setLayouts() {
        mLayoutCarId.setVisibility(mStep == 0 ? View.VISIBLE : View.GONE);
        mLayoutCalendar.setVisibility(mStep == 1 ? View.VISIBLE : View.GONE);
        mLayoutSchedule.setVisibility(mStep == 2 ? View.VISIBLE : View.GONE);
        mLayoutConfirm.setVisibility(mStep == 3 ? View.VISIBLE : View.GONE);
    }

    private void setEvents() {
        mButtonNext.setOnClickListener(v -> {
            switch (mStep) {
                case 0: if (mVehicleId != null || !mEtVehicleId.getText().toString().isEmpty()) ++mStep; break;
                case 1: checkStep(); break;
                case 2: setSummary(); break;
                case 3: break;
                default: return;
            }
            checkButtons();
            setLayouts();
        });

        mButtonBack.setOnClickListener(v -> {
            switch (mStep) {
                case 0: break;
                case 1: --mStep; break;
                case 2: mAdapter.clear(); --mStep; break;
                case 3: onBackPressed(); break;
                default: return;
            }
            checkButtons();
            setLayouts();
        });

        mQrButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, QRActivity.class);
            intent.putExtra("QR_PATTERN", "vehicle:\\d+");
            startActivityForResult(intent, QRActivity.QR_RESULT);
        });

        mTitleBar.setListener(new TitleBar.TitleBarListener() {
            @Override
            public void onLeftClick() {
                onBackPressed();
            }

            @Override
            public void onRight1Click() {

            }

            @Override
            public void onRight2Click() {

            }
        });

        mTimePicker.setAdapter(mAdapter);
        mTimePicker.setLayoutManager(new GridLayoutManager(this, 4));

        mCalendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> mDate = LocalDate.of(year, month + 1, dayOfMonth));
        mDate = LocalDate.now();
    }

    private void setSummary() {
        if (mAdapter.hasSelected()) {
            ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage(getString(R.string.loading));
            pd.setCancelable(false);
            pd.show();

            LocalDateTime[] dates = mAdapter.getBookedHours(DateUtils.stringToDate(mSelectedDate, "yyyy-MM-dd"));
            VehicleBookingScheduler vbs = new VehicleBookingScheduler();
            vbs.setVehicleId(Long.parseLong(mVehicleId));
            vbs.setStartDate(DateUtils.dateTimeToString(dates[0], "yyyy-MM-dd HH:mm:ss", ZoneId.of("Z")));
            vbs.setEndDate(DateUtils.dateTimeToString(dates[1], "yyyy-MM-dd HH:mm:ss", ZoneId.of("Z")));
            NetTask task = new NetTask("v2/vehicle/booking-scheduler", NetTask.JsonRequest.create(vbs), true, new NetworkCallback<NetTask.JsonResponse>() {
                @Override
                public void onSuccess(NetTask.JsonResponse response) {
                    pd.dismiss();
                    showCustomDialog("Vehicle booked successfully!");
                    ++mStep;
                    checkButtons();
                    setLayouts();
                    mAdapter.clear();
                    mSummary.setText(String.format("Booked vehicle from %s to %s.", DateUtils.dateTimeToString(dates[0], "dd/MM/yyyy - HH:mm:ss"),
                            DateUtils.dateTimeToString(dates[1], "dd/MM/yyyy - HH:mm:ss")));
                }

                @Override
                public void onFailure(int code, String errorMsg) {
                    pd.dismiss();
                    showCustomDialogError(errorMsg);
                }
            });
            task.execute();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == QRActivity.QR_RESULT && resultCode == RESULT_OK) {
            mVehicleId = data.getStringExtra("QR_RESULT");
            if (mVehicleId != null) {
                mVehicleId = mVehicleId.replace("vehicle:", "");
                mEtVehicleId.setText(mVehicleId);
            }
        }
    }

    private void checkStep() {
        if (mCalendarView.getDate() > 0) {
            mSelectedDate = DateUtils.dateToString(mDate, "yyyy-MM-dd");
            ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage(getString(R.string.loading));
            pd.setCancelable(false);
            pd.show();

            mVehicleId = mEtVehicleId.getText().toString();

            String zone = ZoneId.systemDefault().getId();
            String timeZone = zone.replace("/", "-");

            NetTask task = new NetTask(NetTask.urlify("v2/vehicle/booking-scheduler/", mVehicleId, mSelectedDate, timeZone), null, true, new NetworkCallback<NetTask.JsonResponse>() {
                @Override
                public void onSuccess(NetTask.JsonResponse response) {
                    pd.dismiss();
                    mLayoutCalendar.setVisibility(View.GONE);
                    mLayoutSchedule.setVisibility(View.VISIBLE);
                    mBooking = response.getResponse(VehicleBooking.class);
                    processBooking();
                    ++mStep;
                    checkButtons();
                    setLayouts();
                }

                @Override
                public void onFailure(int code, String errorMsg) {
                    pd.dismiss();
                    showCustomDialogError(errorMsg);
                }
            });
            task.execute();
        } else {
            showCustomDialogError("Please, select one date to check the booking.");
        }
    }

    private void processBooking() {
        byte[] bookingStatus = mBooking.getBookingStatus();
        LocalDate ld = LocalDate.now();
        LocalTime now = LocalTime.now();

        for (int i1 = 0; i1 < bookingStatus.length; i1++) {
            byte b = bookingStatus[i1];
            BitSet bs = BitSet.valueOf(new byte[] {b});
            for (int i = 0; i < 4; ++i) {
                BookingAdapter.BookingItem bi = new BookingAdapter.BookingItem(i1, i, (byte) (bs.get(i) ? 1 : 0));
                LocalTime lt = LocalTime.of(i1, i * 15);
                if (ld.isAfter(mDate) || (ld.isEqual(mDate) && now.isAfter(lt))) {
                    bi.setBookedState((byte) 3);
                }
                mAdapter.addItem(bi);
            }
        }
        mAdapter.update();
    }

    private void checkButtons() {
        if (mStep == 0) {
            mButtonBack.setEnabled(false);
        } else {
            mButtonBack.setEnabled(true);
        }

        if (mStep < mMaxSteps) {
            mButtonNext.setEnabled(true);
        } else {
            mButtonNext.setEnabled(false);
        }
    }

    private void getViews() {
        mButtonNext = findViewById(R.id.buttonNext);
        mButtonBack = findViewById(R.id.buttonBack);
        mCalendarView = findViewById(R.id.calendarView);
        mEtVehicleId = findViewById(R.id.etVehicleId);
        mQrButton = findViewById(R.id.qrButton);

        mLayoutCarId = findViewById(R.id.layoutCarId);
        mLayoutCalendar = findViewById(R.id.layoutCalendar);
        mLayoutSchedule = findViewById(R.id.layoutSchedule);
        mLayoutConfirm = findViewById(R.id.layoutConfirm);

        mTitleBar = findViewById(R.id.titleBar);
        mTimePicker = findViewById(R.id.timePicker);
        mSummary = findViewById(R.id.summary);

        mAdapter = new BookingAdapter();
    }
}
