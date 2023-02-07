package com.otcengineering.white_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.otc.alice.api.model.Community;
import com.otc.alice.api.model.General;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.network.utils.GlideApp;
import com.otcengineering.white_app.serialization.pojo.UserInRankingItem;
import com.otcengineering.white_app.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by cenci7
 */

public class RankingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface RankingListener {
        void onUserInRankingItemSelected(int position);
    }

    private Context context;
    private List<UserInRankingItem> userInRankingItems = new ArrayList<>();
    private RankingListener listener;

    public RankingAdapter(Context context, RankingListener listener) {
        super();
        this.context = context;
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return userInRankingItems.size();
    }

    public UserInRankingItem getItem(int position) {
        if (position >= 0 && position < userInRankingItems.size()) {
            return userInRankingItems.get(position);
        }
        return null;
    }

    public Community.UserCommunity getUserCommunity(int position) {
        if (position >= 0 && position < userInRankingItems.size()) {
            UserInRankingItem userInRankingItem = userInRankingItems.get(position);
            return createUserCommunity(userInRankingItem);
        }
        return null;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_ranking, viewGroup, false);
        return new UserInRankingHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final UserInRankingHolder itemHolder = (UserInRankingHolder) holder;
        final UserInRankingItem user = userInRankingItems.get(position);

        itemHolder.usrId = user.getId();
        Glide.with(context).clear(itemHolder.imgUser);
        showImagePlaceholder(itemHolder);
        Long imageId = Utils.tryParseLong(user.getImage());
        if (imageId > 0) {
            GlideApp.with(context).load(imageId).into(itemHolder.imgUser);
        }
        // Utils.runOnBackground(() -> retrieveImage(itemHolder, user));
        itemHolder.txtName.setText(user.getName());

        int userPosition = user.getPosition();
        String privacy = "";
        if (user.getProfileType() == General.ProfileType.PUBLIC) {
            privacy = context.getString(R.string.profile_visible);
        } else if (user.getProfileType() == General.ProfileType.PRIVATE) {
            privacy = context.getString(R.string.profile_not_visible);
        } else if (user.getProfileType() == General.ProfileType.ONLY_FRIENDS) {
            privacy = user.isFriend() ? context.getString(R.string.profile_visible) : context.getString(R.string.profile_not_visible);
        }
        itemHolder.txtPositionAndPrivacy.setText(String.format(Locale.US, "%d / %s", userPosition, privacy));

        int resource;
        if (userPosition == 1) {
            resource = R.drawable.my_drive_icons_17;
        } else if (userPosition == 2) {
            resource = R.drawable.my_drive_icons_10;
        } else {
            resource = R.drawable.my_drive_icons_11;
        }
        Glide.with(context).load(resource).fitCenter().into(itemHolder.imgIcon);

        itemHolder.layoutRoot.setOnClickListener(v -> listener.onUserInRankingItemSelected(itemHolder.getAdapterPosition()));
    }

    private void showImagePlaceholder(UserInRankingHolder itemHolder) {
        Utils.runOnMainThread(() -> Glide.with(context)
                .load(R.drawable.user_placeholder_correct)
                .fitCenter()
                .into(itemHolder.imgUser));
    }

    private Community.UserCommunity createUserCommunity(UserInRankingItem user) {
        Community.UserCommunity.Builder builder = Community.UserCommunity.newBuilder();
        builder.setUserId(user.getId());
        builder.setFriend(user.isFriend());
        builder.setName(user.getName());
        try {
            builder.setImage(Long.parseLong(user.getImage()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        builder.setPosition(user.getPosition());
        builder.setProfileType(user.getProfileType());
        return builder.build();
    }

    public void clearItems() {
        userInRankingItems.clear();
    }

    public void addItems(List<UserInRankingItem> items) {
        userInRankingItems.addAll(items);
        Collections.sort(userInRankingItems, (item1, item2) -> {
            Integer number1 = item1.getNumber();
            Integer number2 = item2.getNumber();
            return number1.compareTo(number2);
        });
    }

    public List<UserInRankingItem> getItems() {
        return userInRankingItems;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    protected class UserInRankingHolder extends RecyclerView.ViewHolder {
        private LinearLayout layoutRoot;
        private CircleImageView imgUser;
        private TextView txtName;
        private TextView txtPositionAndPrivacy;
        private ImageView imgIcon;
        private int usrId;

        UserInRankingHolder(View itemView) {
            super(itemView);
            layoutRoot = itemView.findViewById(R.id.row_ranking_layoutRoot);
            imgUser = itemView.findViewById(R.id.row_ranking_imgUser);
            txtName = itemView.findViewById(R.id.row_ranking_txtName);
            txtPositionAndPrivacy = itemView.findViewById(R.id.row_ranking_txtPositionAndPrivacy);
            imgIcon = itemView.findViewById(R.id.row_ranking_imgIcon);
        }
    }

}