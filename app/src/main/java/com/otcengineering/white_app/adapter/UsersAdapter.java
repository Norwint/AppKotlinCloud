package com.otcengineering.white_app.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.SpannableString;
import android.text.style.StrikethroughSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.otc.alice.api.model.Community;
import com.otc.alice.api.model.General;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.activities.InviteActivity;
import com.otcengineering.white_app.components.Menu;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.PrefsManager;
import com.otcengineering.white_app.utils.Utils;
import com.otcengineering.white_app.utils.images.ImageUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by cenci7
 */

public class UsersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public void selectAll() {
        for (UserList ul : users) {
            if (ul.user.getFriend()) {
                ul.selected = true;
            }
        }
    }

    public void unselectAll() {
        for (UserList ul : users) {
            ul.selected = false;
        }
    }

    public interface UserSelectedListener {
        void onUserSelected(int position);

        void onUnfriend(int position);
    }

    private class UserList {
        Community.UserCommunity user;
        boolean selected;

        UserList(Community.UserCommunity user, boolean selected) {
            this.user = user;
            this.selected = selected;
        }
    }

    private Context context;
    private List<UserList> users = new ArrayList<>();
    private UserSelectedListener listener;
    private Fragment parent;

    public UsersAdapter(Fragment frag, UserSelectedListener listener) {
        super();
        this.context = frag.getContext();
        this.listener = listener;
        this.parent = frag;
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public Community.UserCommunity getItem(int position) {
        if (position >= 0 && position < users.size()) {
            return users.get(position).user;
        }
        return null;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_friend1, viewGroup, false);
        return new UserHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final UserHolder itemHolder = (UserHolder) holder;
        final Community.UserCommunity user = users.get(position).user;
        long myUserId = PrefsManager.getInstance().getMyUserId(context);
        final UserList friendInList = users.get(position);

        Glide.with(context).clear(itemHolder.imgUser);
        Long usrImage = user.getImage();
        if (usrImage > 0) {
            Glide.with(context).load(usrImage).into(itemHolder.imgUser);
        } else {
            Glide.with(context)
                    .load(R.drawable.user_placeholder_correct)
                    .into(itemHolder.imgUser);
        }
        // getImage(itemHolder, user.getImage());
        itemHolder.txtName.setText(user.getName());

        int userPosition = user.getPosition();
        String privacy = "";
        if (user.getProfileType() == General.ProfileType.PUBLIC) {
            privacy = context.getString(R.string.profile_visible);
            itemHolder.visibility = 1;
        } else if (user.getProfileType() == General.ProfileType.PRIVATE) {
            privacy = context.getString(R.string.profile_not_visible);
            itemHolder.visibility = 0;
        } else if (user.getProfileType() == General.ProfileType.ONLY_FRIENDS) {
            privacy = context.getString(R.string.profile_only_friends);
            itemHolder.visibility = user.getFriend() ? 1 : 0;
        }
        itemHolder.txtPositionAndPrivacy.setText(String.format(Locale.US, "%d / %s", userPosition, privacy));

        if (userPosition == 1) {
            itemHolder.imgIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.my_drive_icons_17));
        } else if (userPosition == 2) {
            itemHolder.imgIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.my_drive_icons_10));
        } else {
            itemHolder.imgIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.my_drive_icons_11));
        }

        itemHolder.layoutSelected.setVisibility(friendInList.selected ? View.VISIBLE : View.GONE);
        itemHolder.btnInvite.setEnabled(true);
        itemHolder.btnInvite.setSelected(false);

        if (!user.getBlocked()) {
            if (user.getFriend()) {
                itemHolder.btnInvite.setSelected(friendInList.selected);
                itemHolder.btnInvite.setText(friendInList.selected ? R.string.unfriend : R.string.friend);
                itemHolder.btnInvite.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
                itemHolder.btnInvite.setBackgroundResource(R.drawable.button_friend_background);
            } else {
                //itemHolder.btnInvite.setVisibility((myUserId == user.getUserId()) ? View.GONE : View.VISIBLE);
                itemHolder.btnInvite.setBackgroundResource(R.drawable.button_invite_background);
                itemHolder.btnInvite.setText(myUserId == user.getUserId() ? R.string.you : R.string.invite);
                itemHolder.btnInvite.setTextColor(ContextCompat.getColor(context, R.color.button_text_color));
                if (MySharedPreferences.createSocial(context).getBoolean("user_" + user.getUserId())) {
                    itemHolder.btnInvite.setEnabled(false);
                    //itemHolder.btnInvite.setTextColor(ContextCompat.getColor(context, R.color.colorTexto));
                    itemHolder.btnInvite.setText(R.string.invited);
                } else if (myUserId == user.getUserId()) {
                    itemHolder.btnInvite.setEnabled(false);
                }
            }
        } else {
            itemHolder.btnInvite.setVisibility(View.VISIBLE);
            itemHolder.btnInvite.setText(context.getString(R.string.blocked));
            itemHolder.btnInvite.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            itemHolder.btnInvite.setBackgroundResource(R.drawable.button_unblock);
            SpannableString ss = new SpannableString(user.getName());
            ss.setSpan(new StrikethroughSpan(), 0, ss.length(), 0);
            itemHolder.txtName.setText(ss);
            itemHolder.visibility = 0;
        }

        itemHolder.layoutRoot.setOnClickListener(v -> {
            if (itemHolder.visibility == 1) {
                listener.onUserSelected(itemHolder.getAdapterPosition());
            }
        });

        itemHolder.btnInvite.setOnClickListener(v -> {
            if (user.getBlocked()) {
                Menu menu = new Menu(context, user.getUserId());
                menu.setOnUnblock(() -> parent.onResume());
                Utils.runOnMainThread(menu::show);
            } else if (!user.getFriend() && !MySharedPreferences.createSocial(context).getBoolean("user_" + user.getUserId())) {
                openInvite(user);
                this.notifyDataSetChanged();
            } else if (user.getFriend()) {
                showMenuUnfriend(position);
            }
        });
    }

    public void setSelected(int position) {
        users.get(position).selected = !users.get(position).selected;
    }

    public List<Community.UserCommunity> getSelected() {
        List<Community.UserCommunity> selected = new ArrayList<>();
        for (UserList ul : users) {
            if (ul.user.getFriend() && ul.selected) {
                selected.add(ul.user);
            }
        }
        return selected;
    }

    private void showMenuUnfriend(int position) {
        if (listener != null) {
            listener.onUnfriend(position);
        }
    }

    private void downloadImage(UserHolder itemHolder, long imageId) {
        if (ConnectionUtils.isOnline(context)) {
            try {
                byte[] img = ApiCaller.getImage(String.format(Locale.US, "%s%d", Endpoints.FILE_GET, imageId), MySharedPreferences.createLogin(context).getString("token"));
                ImageUtils.saveImageFileInCache(context, img, imageId);
                Utils.runOnMainThread(() -> Glide.with(context).load(img).into(itemHolder.imgUser));
            } catch (ApiCaller.OTCException e) {
                e.printStackTrace();
            }
        } else {
            ConnectionUtils.showOfflineToast();
        }
    }

    private static void showImagePlaceholder(Context context, UserHolder itemHolder) {
        Glide.with(context)
                .load(R.drawable.user_placeholder_correct)
                .into(itemHolder.imgUser);
    }

    private void openInvite(Community.UserCommunity user) {
        Intent intent = new Intent(context, InviteActivity.class);
        intent.putExtra(Constants.Extras.USER, user);
        context.startActivity(intent);
    }

    public void clearItems() {
        users.clear();
        notifyDataSetChanged();
    }

    public void addItems(@NonNull List<Community.UserCommunity> items) {
        for (Community.UserCommunity uc : items) {
            users.add(new UserList(uc, false));
        }
    }

    public List<Community.UserCommunity> getItems() {
        List<Community.UserCommunity> list = new ArrayList<>();
        for (UserList ul : users) {
            list.add(ul.user);
        }
        return list;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public class UserHolder extends RecyclerView.ViewHolder {
        private LinearLayout layoutRoot;
        private CircleImageView imgUser;
        private TextView txtPositionAndPrivacy;
        private ImageView imgIcon;
        private Button btnInvite;
        private RelativeLayout layoutSelected;
        private TextView txtName;
        public int visibility = 0;

        UserHolder(View itemView) {
            super(itemView);
            layoutRoot = itemView.findViewById(R.id.row_friend_layoutRoot);
            imgUser = itemView.findViewById(R.id.row_friend_imgUser);
            txtPositionAndPrivacy = itemView.findViewById(R.id.row_friend_txtPositionAndPrivacy);
            imgIcon = itemView.findViewById(R.id.row_friend_imgIcon);
            btnInvite = itemView.findViewById(R.id.row_friend_btnUnfriend);
            layoutSelected = itemView.findViewById(R.id.row_friend_layoutSelected);
            txtName = itemView.findViewById(R.id.row_friend_txtName);
        }
    }
}