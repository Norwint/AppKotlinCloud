package com.otcengineering.white_app.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.activities.BadgeActivity;
import com.otcengineering.white_app.serialization.models.Badge;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.utils.BadgeUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;

import java.util.List;

public class BadgeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<Badge> badgeList;

    private ImageView badgeImage;

    public BadgeAdapter(Context mContext, List<Badge> badgeList) {
        this.mContext = mContext;
        this.badgeList = badgeList;
    }

    public void setBadgeList(List<Badge> badgeList) {
        this.badgeList = badgeList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_badge, parent, false);
        return new BadgeHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final Badge badge = badgeList.get(position);
        BadgeHolder badgeHolder = (BadgeHolder) holder;
        badgeHolder.badgeName.setText(badge.getName());
        badgeHolder.badgeObjective.setText(badge.getSubtitle());

        badgeHolder.badgeLayout.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, BadgeActivity.class);
            intent.putExtra("badge_name", badge.getName());
            intent.putExtra("badge_image", badge.getImage());
            intent.putExtra("badge_objective", badge.getSubtitle());
            intent.putExtra("badge_state", badge.getState());
            intent.putExtra("badge_description", badge.getObjective());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        });

        badgeImage = badgeHolder.badgeImage;
        badgeImage.setImageResource(BadgeUtils.getImageId(badge.getName()));

        if (badge.getState() == 0) {
            badgeHolder.badgeLayout.setAlpha(0.5f);
            badgeHolder.badgeParent.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.colorGrayBadge)));
        } else {
            badgeHolder.badgeLayout.setAlpha(1f);
            badgeHolder.badgeParent.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, android.R.color.white)));
        }
    }

    class getImage extends AsyncTask<Object, Object, byte[]> {
        Bitmap decodedImage;
        byte[] imageBytes;

        @Override
        protected byte[] doInBackground(Object... params) {
            try {
                MySharedPreferences msp = MySharedPreferences.createLogin(mContext);
                imageBytes = ApiCaller.getImage(Endpoints.FILE_GET + params[0], msp.getString("token"));
                return imageBytes;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(byte[] image) {
            // If the image is null, this function cras
            if (image == null) {
                return;
            }

            decodedImage = BitmapFactory.decodeByteArray(image, 0, image.length);

            String path = MediaStore.Images.Media.insertImage(mContext.getContentResolver(), decodedImage, "Title", null);
            Glide.with(mContext).load(Uri.parse(path)).into(badgeImage);
        }
    }

    @Override
    public int getItemCount() {
        return badgeList.size();
    }

    public boolean isBig(int position) {
        ////Log.d("CardsAdapter", "isBig");
        return badgeList.get(position).getSize() == 2;
    }

    protected class BadgeHolder extends RecyclerView.ViewHolder {

        private ImageView badgeImage;
        private TextView badgeName, badgeObjective;
        private ConstraintLayout badgeLayout;
        private View badgeParent;

        public BadgeHolder(View itemView) {
            super(itemView);
            this.badgeImage = itemView.findViewById(R.id.badgeImage);
            this.badgeName = itemView.findViewById(R.id.badgeName);
            this.badgeObjective = itemView.findViewById(R.id.badgeObjective);
            this.badgeLayout = itemView.findViewById(R.id.badgeLayout);
            badgeParent = itemView.findViewById(R.id.badgeParent);
        }
    }


}
