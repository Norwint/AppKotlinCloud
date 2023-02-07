package com.otcengineering.white_app.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.tasks.GetImageTask;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.images.ImageUtils;
import com.otc.alice.api.model.Community;
import com.otc.alice.api.model.General;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by cenci7
 */

public class FriendsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface FriendListener {
        void onUserSelected(int position);

        void onUnfriend(int position);
    }

    private Context context;
    private List<FriendInList> friendInLists = new ArrayList<>();
    private FriendListener listener;

    public FriendsAdapter(Context context, FriendListener listener) {
        super();
        this.context = context;
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return friendInLists.size();
    }

    public Community.UserCommunity getItem(int position) {
        if (position >= 0 && position < friendInLists.size()) {
            return friendInLists.get(position).user;
        }
        return null;
    }

    public void setSelected(int position) {
        friendInLists.get(position).selected = !friendInLists.get(position).selected;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_friend, viewGroup, false);
        return new UserHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final UserHolder itemHolder = (UserHolder) holder;
        FriendInList friendInList = friendInLists.get(position);
        final Community.UserCommunity user = friendInList.user;

        getImage(itemHolder, user.getImage());
        itemHolder.txtName.setText(user.getName());

        int userPosition = user.getPosition();
        String privacy = "";
        if (user.getProfileType() == General.ProfileType.PUBLIC) {
            privacy = context.getString(R.string.profile_visible);
        } else if (user.getProfileType() == General.ProfileType.PRIVATE) {
            privacy = context.getString(R.string.profile_not_visible);
        } else if (user.getProfileType() == General.ProfileType.ONLY_FRIENDS) {
            privacy = user.getFriend() ? context.getString(R.string.profile_visible) : context.getString(R.string.profile_not_visible);
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
        itemHolder.btnUnfriend.setSelected(friendInList.selected);
        itemHolder.btnUnfriend.setText(friendInList.selected ? R.string.unfriend : R.string.friend);

        itemHolder.layoutRoot.setOnClickListener(v -> listener.onUserSelected(itemHolder.getAdapterPosition()));

        itemHolder.btnUnfriend.setOnClickListener(v -> showMenuUnfriend(position));
    }

    private void showMenuUnfriend(int position) {
        if (listener != null) {
            listener.onUnfriend(position);
        }
    }

    private void getImage(UserHolder itemHolder, long imageId) {
        if (imageId == 0) {
            showImagePlaceholder(itemHolder);
            return;
        }

        String imageFilePathInCache = ImageUtils.getImageFilePathInCache(context, imageId);
        if (imageFilePathInCache != null) {
            showImage(itemHolder, imageFilePathInCache);
        } else {
            downloadImage(itemHolder, imageId);
        }
    }

    private void downloadImage(UserHolder itemHolder, long imageId) {
        @SuppressLint("StaticFieldLeak")
        GetImageTask getImageTask = new GetImageTask(imageId) {
            @Override
            protected void onPostExecute(String imagePath) {
                super.onPostExecute(imagePath);
                showImage(itemHolder, imagePath);
            }
        };
        if (ConnectionUtils.isOnline(context)) {
            getImageTask.execute(context);
        } else {
            ConnectionUtils.showOfflineToast();
        }
    }

    private void showImage(UserHolder itemHolder, String imagePath) {
        if (imagePath != null) {
            Glide.with(context)
                    .load(ImageUtils.FILE_DIRECTORY + imagePath)
                    .into(itemHolder.imgUser);
        } else {
            showImagePlaceholder(itemHolder);
        }
    }

    private void showImagePlaceholder(UserHolder itemHolder) {
        Glide.with(context)
                .load(R.drawable.user_placeholder)
                .into(itemHolder.imgUser);
    }

    public void clearItems() {
        friendInLists.clear();
    }

    public void addItems(List<Community.UserCommunity> items) {
        for (Community.UserCommunity user : items) {
            this.friendInLists.add(new FriendInList(user));
        }
    }

    public void selectAll() {
        for (FriendInList friendInList : friendInLists) {
            friendInList.selected = true;
        }
    }


    public void unselectAll() {
        for (FriendInList friendInList : friendInLists) {
            friendInList.selected = false;
        }
    }

    public List<Community.UserCommunity> getItemsSelected() {
        List<Community.UserCommunity> friendsSelected = new ArrayList<>();
        for (FriendInList friendInList : friendInLists) {
            if (friendInList.selected) {
                friendsSelected.add(friendInList.user);
            }
        }
        return friendsSelected;
    }

    private class FriendInList {
        private Community.UserCommunity user;
        private boolean selected;

        FriendInList(Community.UserCommunity user) {
            this.user = user;
            this.selected = false;
        }
    }

    protected class UserHolder extends RecyclerView.ViewHolder {
        private FrameLayout layoutSelected;
        private LinearLayout layoutRoot;
        private CircleImageView imgUser;
        private TextView txtName;
        private TextView txtPositionAndPrivacy;
        private ImageView imgIcon;
        private Button btnUnfriend;


        UserHolder(View itemView) {
            super(itemView);
            layoutSelected = itemView.findViewById(R.id.row_friend_layoutSelected);
            layoutRoot = itemView.findViewById(R.id.row_friend_layoutRoot);
            imgUser = itemView.findViewById(R.id.row_friend_imgUser);
            txtName = itemView.findViewById(R.id.row_friend_txtName);
            txtPositionAndPrivacy =  itemView.findViewById(R.id.row_friend_txtPositionAndPrivacy);
            imgIcon = itemView.findViewById(R.id.row_friend_imgIcon);
            btnUnfriend = itemView.findViewById(R.id.row_friend_btnUnfriend);
        }
    }

}