package com.otcengineering.white_app.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
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
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.tasks.GetImageTask;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.DateUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.images.ImageUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by cenci7
 */

public class InvitationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface RefreshListListener {
        void refresh();
    }

    private Context context;
    private List<Community.FriendRequest> invitations = new ArrayList<>();
    private RefreshListListener listener;

    public InvitationAdapter(Context context, RefreshListListener listener) {
        super();
        this.context = context;
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return invitations.size();
    }

    public Community.FriendRequest getItem(int position) {
        return invitations.get(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_invitation, viewGroup, false);
        return new InvitationHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final InvitationHolder itemHolder = (InvitationHolder) holder;
        Community.FriendRequest friendRequest = invitations.get(position);

        getImage(itemHolder, friendRequest.getImage());

        String date = friendRequest.getDate();
        String localDate = DateUtils.utcStringToLocalString(date, "yyyy-MM-dd HH:mm:ss", "dd/MM/yyyy - HH:mm:ss");
        itemHolder.txtTime.setText(localDate);

        //itemHolder.txtTime.setText(friendRequest.getDate());
        itemHolder.txtName.setText(friendRequest.getName());
        int userPosition = friendRequest.getPosition();
        String privacy = "";
        if (friendRequest.getProfileType() == General.ProfileType.PUBLIC) {
            privacy = context.getString(R.string.profile_visible);
        } else if (friendRequest.getProfileType() == General.ProfileType.PRIVATE) {
            privacy = context.getString(R.string.profile_not_visible);
        }
        itemHolder.txtPositionAndPrivacy.setText(String.format(Locale.US, "%d / %s", userPosition, privacy));

        itemHolder.txtMessage.setText(friendRequest.getMessage());

        itemHolder.btnDiscard.setOnClickListener(view -> answerInvitation(friendRequest.getInvitationId(), Community.AcceptType.DISCARD));

        itemHolder.btnMakeAfriend.setOnClickListener(view -> answerInvitation(friendRequest.getInvitationId(), Community.AcceptType.ACCEPT));
    }

    private void getImage(InvitationHolder itemHolder, long imageId) {
        if (imageId == 0) {
            showImagePlaceholder(itemHolder.imgUser);
            return;
        }

        String imageFilePathInCache = ImageUtils.getImageFilePathInCache(context, imageId);
        if (imageFilePathInCache != null) {
            showImage(itemHolder, imageFilePathInCache);
        } else {
            downloadImage(itemHolder, imageId);
        }
    }

    private void downloadImage(InvitationHolder itemHolder, long imageId) {
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

    private void showImage(InvitationHolder itemHolder, String imagePath) {
        if (imagePath != null) {
            Glide.with(context)
                    .load(ImageUtils.getImageFromCache(context, imagePath))
                    .into(itemHolder.imgUser);
        } else {
            showImagePlaceholder(itemHolder.imgUser);
        }
    }

    private void showImagePlaceholder(ImageView imageView) {
        Glide.with(context)
                .load(R.drawable.user_placeholder)
                .into(imageView);
    }

    private void answerInvitation(long requestId, Community.AcceptType acceptType) {
        AcceptOrDiscardInvitationTask acceptOrDiscardInvitationTask = new AcceptOrDiscardInvitationTask(requestId, acceptType);
        acceptOrDiscardInvitationTask.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class AcceptOrDiscardInvitationTask extends AsyncTask<Void, Void, Boolean> {

        private long requestId;
        private Community.AcceptType acceptType;

        AcceptOrDiscardInvitationTask(long requestId, Community.AcceptType acceptType) {
            this.requestId = requestId;
            this.acceptType = acceptType;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                MySharedPreferences msp = MySharedPreferences.createLogin(context);

                Community.AnswerRequest.Builder builder = Community.AnswerRequest.newBuilder();
                builder.setRequestId(requestId);
                builder.setAccept(acceptType);

                Shared.OTCResponse response = ApiCaller.doCall(Endpoints.ANSWER_REQUEST, msp.getBytes("token"), builder.build(), Shared.OTCResponse.class);
                return response != null && response.getStatus() == Shared.OTCStatus.SUCCESS;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (listener != null) {
                listener.refresh();
            }
        }
    }

    public void clearItems() {
        invitations.clear();
    }

    public void addItems(List<Community.FriendRequest> items) {
        invitations.addAll(items);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    protected class InvitationHolder extends RecyclerView.ViewHolder {
        private TextView txtTime;
        private ImageView imgUser;
        private TextView txtName, txtPositionAndPrivacy, txtMessage;
        private LinearLayout btnDiscard, btnMakeAfriend;

        InvitationHolder(View itemView) {
            super(itemView);
            txtTime = itemView.findViewById(R.id.row_invitation_txtTime);
            imgUser = itemView.findViewById(R.id.row_invitation_imgUser);
            txtName = itemView.findViewById(R.id.row_invitation_txtName);
            txtPositionAndPrivacy = itemView.findViewById(R.id.row_invitation_txtPositionAndPrivacy);
            txtMessage = itemView.findViewById(R.id.row_invitation_txtMessage);
            btnDiscard = itemView.findViewById(R.id.row_invitation_btnDiscard);
            btnMakeAfriend = itemView.findViewById(R.id.row_invitation_btnMakeAfriend);
        }
    }

}