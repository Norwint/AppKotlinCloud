package com.otcengineering.white_app.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.otc.alice.api.model.MyTrip;
import com.otc.alice.api.model.ProfileAndSettings;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.tasks.TypedTask;
import com.otcengineering.white_app.utils.DateUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.Utils;
import com.otcengineering.white_app.utils.interfaces.TypedCallback;

import java.util.ArrayList;
import java.util.Locale;

import javax.annotation.Nonnull;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<NotificationDummy> notifications;
    private OnItemClickListener listener;
    public boolean deleteMode = false;

    public NotificationAdapter() {
        notifications = new ArrayList<>();
    }

    public void addNotification(NotificationDummy dummy) {
        notifications.add(dummy);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void addNotifications(ArrayList<NotificationDummy> dummies) {
        notifications.addAll(dummies);
        notifyDataSetChanged();
    }

    public void clear() {
        notifications.clear();
    }

    public void update(ArrayList<NotificationDummy> list) {
        notifications.clear();
        addNotifications(list);
    }

    public NotificationDummy make(String title, String message, String timestamp) {
        return new NotificationDummy(title, message, timestamp);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_notification, parent, false);
        return new NotificationHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NotificationDummy dummy = notifications.get(position);
        ((NotificationHolder)holder).setContent(dummy);
        ((NotificationHolder) holder).bind(listener);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    class NotificationHolder extends RecyclerView.ViewHolder {
        private TextView titleTV, descriptionTV, timestampTV;
        private ImageView imageView;
        private ConstraintLayout layout;
        private View deleteNotification;
        private ImageView imgDeleteNotification;
        private Context ctx;
        private NotificationDummy dummy;

        NotificationHolder(View itemView) {
            super(itemView);

            titleTV = itemView.findViewById(R.id.title);
            descriptionTV = itemView.findViewById(R.id.description);
            timestampTV = itemView.findViewById(R.id.timestamp);
            imageView = itemView.findViewById(R.id.imageView);
            layout = itemView.findViewById(R.id.linearLayout);
            deleteNotification = itemView.findViewById(R.id.deleteNotification);
            imgDeleteNotification = itemView.findViewById(R.id.imgDeleteNotification);

            ctx = itemView.getContext();
        }

        private void bind(final OnItemClickListener listener) {
            itemView.setOnClickListener(v -> listener.onItemClick(this.dummy));
        }

        public void setContent(final NotificationDummy dummy) {
            this.dummy = dummy;
            titleTV.setText(getTitle(dummy.title));
            descriptionTV.setText(getSubTitle(dummy.title, dummy.message));
            if (dummy.title.equals("ROUTE")) {
                if (MySharedPreferences.createDefault(ctx).contains(String.format(Locale.US, "route_desc_%s", dummy.message))) {
                    String tmp = MySharedPreferences.createDefault(ctx).getString(String.format(Locale.US, "route_desc_%s", dummy.message));
                    descriptionTV.setText(Utils.translateRouteTextNotification(tmp));
                } else {
                    MyTrip.RouteId routeId = MyTrip.RouteId.newBuilder().setRouteId(Long.parseLong(dummy.message)).build();
                    TypedTask<MyTrip.Route> gt = new TypedTask<>(Endpoints.GET_ROUTE, routeId, true, MyTrip.Route.class, new TypedCallback<MyTrip.Route>() {
                        @Override
                        public void onSuccess(@Nonnull @NonNull MyTrip.Route value) {
                            String txt = Utils.getRouteTextNotification(value);
                            descriptionTV.setText(Utils.translateRouteTextNotification(txt));
                            MySharedPreferences.createDefault(ctx).putString(String.format(Locale.US, "route_desc_%d", value.getId()), txt);
                        }

                        @Override
                        public void onError(@NonNull Shared.OTCStatus status, String str) {

                        }
                    });
                    gt.execute();
                }
            }
            timestampTV.setText(formatTimestamp(dummy.timestamp));

            layout.setAlpha(dummy.wasChecked ? 0.5f : 1.0f);

            if (dummy.img != null) {
                imageView.setImageBitmap(dummy.img);
                if (dummy.imgId == R.drawable.icon || (dummy.type.ordinal() >= 1 && dummy.type.ordinal() <= 4)) {
                    ImageViewCompat.setImageTintList(imageView, null);
                } else {
                    ImageViewCompat.setImageTintList(imageView, ContextCompat.getColorStateList(ctx, R.color.quantum_black_100));
                }
            }
            imgDeleteNotification.setImageDrawable(dummy.selected ? ContextCompat.getDrawable(ctx, R.drawable.check_white) : ContextCompat.getDrawable(ctx, R.drawable.my_routes_icons_27));
            Drawable background = ctx.getDrawable(dummy.selected ? R.drawable.circle_red_background : R.drawable.circle_background);
            deleteNotification.setBackground(background);

            if (deleteMode) {
                imageView.setVisibility(View.GONE);
                deleteNotification.setVisibility(View.VISIBLE);
            } else {
                imageView.setVisibility(View.VISIBLE);
                deleteNotification.setVisibility(View.GONE);
            }

            deleteNotification.setOnClickListener(v -> {
                dummy.selected = !dummy.selected;
                change();
            });
        }

        private void change() {
            imgDeleteNotification.setImageDrawable(dummy.selected ? ContextCompat.getDrawable(ctx, R.drawable.check_white) : ContextCompat.getDrawable(ctx, R.drawable.my_routes_icons_27));
            Drawable background = ctx.getDrawable(dummy.selected ? R.drawable.circle_red_background : R.drawable.circle_background);
            deleteNotification.setBackground(background);
        }

        private String formatTimestamp(final String ts) {
            return DateUtils.utcStringToLocalString(ts.substring(0, ts.indexOf(".")), DateUtils.FMT_SRV_DATETIME, DateUtils.FMT_DATETIME_2);
        }

        private String getTitle(@Nonnull final String msg) {
            switch (msg) {
                case "NEW_FRIEND_REQUEST": return ctx.getString(R.string.friendship_invitation);
                case "NEW_FRIEND_POST": return ctx.getString(R.string.friends_posts);
                case "NEW_CONNECTECH_POST": return ctx.getString(R.string.connectech_post);
                case "NEW_CONNECTECH_MESSAGE": return ctx.getString(R.string.connectech_private_message);
                case "NEW_DEALER_POST": return ctx.getString(R.string.dealer_post);
                case "NEW_DEALER_MESSAGE": return ctx.getString(R.string.dealer_private_message);
                case "FW_UPDATED": return ctx.getString(R.string.FW_UPDATED);
                case "FW_ERROR": return ctx.getString(R.string.FW_ERROR);
                case "FW_DELETING_IMAGE": return ctx.getString(R.string.FW_DELETING_IMAGE);
                case "FW_SENDING_NEW_FW": return ctx.getString(R.string.FW_SENDING_NEW_FW);
                case "FW_RESTART_DONGLE": return ctx.getString(R.string.FW_RESTART_DONGLE);
                case "VEHICLE_STATUS_DOOR": return ctx.getString(R.string.doors);
                case "VEHICLE_STATUS_ENGINE": return ctx.getString(R.string.engine);
                case "GEOFENCING": return "Geofencing";
                case "FW_UPDATE_ASK": return ctx.getString(R.string.FW_UPDATE_ASK);
                case "USER_EXPIRED_IN_X_DAYS": return ctx.getString(R.string.months_no_connect);
                case "USER_UNLINK_MOBILE": return String.format(ctx.getString(R.string.new_phone_txt), MySharedPreferences.createLogin(ctx).getString("Nick"));
                case "ROUTE": return ctx.getString(R.string.thank_you_for_driving);
                case "USER_UNVALIDATED_EMAIL_ADDRESS": /*return "The email address is not validated.";*/ return ctx.getString(R.string.user_email_not_validated);
                case "VEHICLE_IN_SERVICE_TIMING": return ctx.getString(R.string.periodic_maintenance_now);
                case "USER_ALL_SURVEYS_DONE": return ctx.getString(R.string.user_all_surveys_done_title);
                default: {
                    if (msg.startsWith("FW_UPDATE_ASK_")) {
                        return ctx.getString(R.string.update_dongle);
                    } else if (msg.startsWith("BADGE")) {
                        return ctx.getString(R.string.new_badge);
                    } else if (msg.startsWith("VEHICLE_CONDITION")) {
                        String what = msg.replace("VEHICLE_CONDITION_", "");
                        return Utils.translateVehicleCondition(what);
                    }
                    return "";
                }
            }
        }

        private String parseDate(final String inputDate) {
            return DateUtils.utcStringToLocalString(inputDate, "yyyyMMddHHmmss", DateUtils.FMT_DATETIME_2);
        }

        private String getSubTitle(@Nonnull final String msg, @Nullable final String extras) {
            switch (msg) {
                case "NEW_FRIEND_REQUEST": return ctx.getResources().getString(R.string.NEW_FRIEND_REQUEST);
                case "NEW_FRIEND_POST": return ctx.getResources().getString(R.string.NEW_FRIEND_POST);
                case "NEW_CONNECTECH_POST": return ctx.getResources().getString(R.string.NEW_CONNECTECH_POST);
                case "NEW_CONNECTECH_MESSAGE": return ctx.getResources().getString(R.string.NEW_CONNECTECH_MESSAGE);
                case "NEW_DEALER_POST": return ctx.getResources().getString(R.string.NEW_DEALER_POST);
                case "NEW_DEALER_MESSAGE": return ctx.getResources().getString(R.string.NEW_DEALER_MESSAGE);
                case "FRIEND_SHARING_LOCATION": return ctx.getResources().getString(R.string.FRIEND_SHARING_LOCATION);
                case "FW_UPDATED": return ctx.getResources().getString(R.string.FW_UPDATED);
                case "FW_ERROR": return ctx.getResources().getString(R.string.FW_ERROR);
                case "FW_DELETING_IMAGE": return ctx.getResources().getString(R.string.FW_DELETING_IMAGE);
                case "FW_SENDING_NEW_FW": return ctx.getResources().getString(R.string.FW_SENDING_NEW_FW);
                case "FW_RESTART_DONGLE": return ctx.getResources().getString(R.string.FW_RESTART_DONGLE);
                case "USER_UNLINK_MOBILE": return ctx.getString(R.string.new_phone_subtitle);
                case "VEHICLE_STATUS_DOOR": {
                    String how = extras.split("_")[0].toLowerCase();
                    String when = extras.split("_")[1];
                    return String.format("%s %s at %s", ctx.getResources().getString(R.string.VEHICLE_STATUS_DOOR), Utils.translateDoorState(how), parseDate(when));
                }
                case "VEHICLE_STATUS_ENGINE": {
                    String how = extras.split("_")[0].toLowerCase();
                    String when = extras.split("_")[1];
                    return String.format("%s %s at %s", ctx.getResources().getString(R.string.VEHICLE_STATUS_ENGINE), how, parseDate(when));
                }
                case "GEOFENCING": {
                    if (extras != null) {
                        String date = parseDate(extras);
                        return ctx.getString(R.string.you_vehicle_is_out, date);
                    } else {
                        return "Your vehicle is outside the Geofencing Area";
                    }
                }
                case "USER_EXPIRED_IN_X_DAYS": {
                    if (extras.equals("0")) {
                        return ctx.getString(R.string.days_0);
                    } else {
                        String str = ctx.getString(R.string.days_n);
                        return String.format(str, extras);
                    }
                }
                case "FW_UPDATE_ASK": return ctx.getString(R.string.update_dongle);
                case "USER_UNVALIDATED_EMAIL_ADDRESS": {
                    String submsg = ctx.getString(R.string.unactivated_email);
                    return String.format(submsg, extras.replace("Unactivated email address: ", ""));
                }
                case "VEHICLE_IN_SERVICE_TIMING": return ctx.getString(R.string.its_time_for_maintenance);
                case "USER_ALL_SURVEYS_DONE": return ctx.getString(R.string.user_all_surveys_done_desc);
                default: {
                    if (msg.startsWith("FW_UPDATE_ASK_")) {
                        return String.format(ctx.getResources().getString(R.string.update_dongle_), extras);
                    } else if (msg.startsWith("BADGE")) {
                        return String.format(ctx.getResources().getString(R.string.won_badge), msg.replace("_", " ").replace("BADGE ", ""));
                    } else if (msg.startsWith("VEHICLE_CONDITION")) {
                        String what = msg.replace("VEHICLE_CONDITION_", "");
                        String date = parseDate(extras);
                        return ctx.getString(R.string.the_indicator_is_on, Utils.translateVehicleCondition(what), date);
                    }
                    return "";
                }
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(NotificationDummy item);
    }

    public class NotificationDummy {
        public String title, message, timestamp;
        public Bitmap img;
        public boolean wasChecked = false;
        public boolean selected = false;
        public long id;
        public int imgId;
        public ProfileAndSettings.NotificationType type;

        private NotificationDummy(String title, String message, String timestamp) {
            this.title = title;
            this.message = message;
            this.timestamp = timestamp;
        }
    }
}

