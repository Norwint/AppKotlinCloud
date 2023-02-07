package com.otcengineering.white_app.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.annimon.stream.Stream;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.otc.alice.api.model.Community;
import com.otc.alice.api.model.General;
import com.otc.alice.api.model.MyTrip;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.activities.RouteDetailsActivity;
import com.otcengineering.white_app.components.CustomDialog;
import com.otcengineering.white_app.interfaces.ShowMenuPostsListener;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.serialization.pojo.RouteItem;
import com.otcengineering.white_app.tasks.GenericTask;
import com.otcengineering.white_app.tasks.GetImageTask;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.DateUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.Utils;
import com.otcengineering.white_app.utils.YoutubeUtils;
import com.otcengineering.white_app.utils.images.ImageUtils;

import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by cenci7
 */

public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    public List<Community.Post> posts = new ArrayList<>();
    private ShowMenuPostsListener listener;
    private boolean isMyUser;

    public PostAdapter(Context context, boolean isMyUser, ShowMenuPostsListener listener) {
        super();
        this.context = context;
        this.isMyUser = isMyUser;
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public Community.Post getItem(int position) {
        return posts.get(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_post, viewGroup, false);
        return new PostHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final PostHolder itemHolder = (PostHolder) holder;
        Community.Post post = posts.get(position);
        String date = post.getDate();
        itemHolder.txtTime.setText(DateUtils.utcStringToLocalString(date, "yyyy-MM-dd HH:mm:ss", "dd/MM/yyyy - HH:mm:ss"));
        itemHolder.img.setImageResource(R.drawable.loading_route);
        if (post.getType() == Community.PostType.ROUTE) {
            itemHolder.img.setImageResource(R.drawable.map_landscape);
        }
        Community.UserCommunity user = post.getUser();
        Glide.with(context).clear(itemHolder.imgUser);
        Long imgId = user.getImage();
        if (imgId > 0) {
            Glide.with(context).load(imgId).into(itemHolder.imgUser);
        } else {
            Glide.with(context).load(R.drawable.user_placeholder_correct).into(itemHolder.imgUser);
        }

        // showUserImage(itemHolder, user);
        itemHolder.txtName.setText(user.getName());
        int userPosition = user.getPosition();
        String privacy = "";
        if (user.getProfileType() == General.ProfileType.PUBLIC) {
            privacy = context.getString(R.string.profile_visible);
        } else if (user.getProfileType() == General.ProfileType.PRIVATE) {
            privacy = context.getString(R.string.profile_not_visible);
        } else if (user.getProfileType() == General.ProfileType.ONLY_FRIENDS) {
            privacy = post.getUser().getFriend() ? context.getString(R.string.profile_visible) : context.getString(R.string.profile_not_visible);
        }
        itemHolder.txtPositionAndPrivacy.setText(String.format(Locale.US, "%d / %s", userPosition, privacy));

        itemHolder.txtLikes.setText(String.valueOf(post.getLikes()));

        itemHolder.layoutUser.setVisibility(isMyUser ? View.VISIBLE : View.GONE);
        itemHolder.layoutRoute.setVisibility(isRoutePost(post) ? View.VISIBLE : View.GONE);
        itemHolder.layoutImageAndPost.setOnClickListener(null);
        itemHolder.btnFav.setVisibility(isRoutePost(post) ? View.VISIBLE : View.GONE);
        itemHolder.btnMenu2.setVisibility(isRoutePost(post) ? View.GONE : View.VISIBLE);
        itemHolder.imgVideo.setVisibility(isVideoPost(post) ? View.VISIBLE : View.GONE);
        itemHolder.layoutMileageEcoSafety.setOnClickListener(null);
        itemHolder.btnMenu.setVisibility(View.VISIBLE);
        //itemHolder.separator.setVisibility(isMyUser ? View.GONE : View.VISIBLE);
        if (isRoutePost(post)) {
            itemHolder.layoutTitle.setVisibility(View.VISIBLE);
            itemHolder.layoutImageAndPost.setVisibility(View.VISIBLE);
            itemHolder.layoutImage.setVisibility(View.VISIBLE);
            itemHolder.layoutMileageEcoSafety.setVisibility(View.GONE);
            MyTrip.Route route = post.getRoute();

            itemHolder.btnFav.getChildAt(0).setBackgroundResource(route.getFavourite() ? R.drawable.my_routes_icons_2 : R.drawable.my_routes_icons_3);
            itemHolder.favourite = route.getFavourite();

            itemHolder.btnFav.setOnClickListener(v ->
            {
                MyTrip.RouteId.Builder rouID = MyTrip.RouteId.newBuilder();
                rouID.setRouteId(route.getId());

                GenericTask gt = new GenericTask(itemHolder.favourite ? Endpoints.ROUTE_UNFAV : Endpoints.ROUTE_FAV, rouID.build(), true, otcResponse -> {
                    if (otcResponse.getStatus() == Shared.OTCStatus.SUCCESS) {
                        itemHolder.favourite = !itemHolder.favourite;
                        itemHolder.btnFav.getChildAt(0).setBackgroundResource(itemHolder.favourite ? R.drawable.my_routes_icons_2 : R.drawable.my_routes_icons_3);
                        int messageRes = itemHolder.favourite ?
                                R.string.add_route_to_favorites_correctly :
                                R.string.remove_route_to_favorites_correctly;
                        Toast.makeText(context, messageRes, Toast.LENGTH_LONG).show();
                    }
                });
                gt.execute();
            });

            if (route.getId() != 0) {
                itemHolder.layoutImageAndPost.setOnClickListener(v -> {
                    ImageUtils.putPost(0, Constants.Extras.ROUTE, new Gson().toJson(new RouteItem(post.getRoute())));
                    Intent intent = new Intent(context, RouteDetailsActivity.class);
                    intent.putExtra("UserID", post.getUser().getUserId());
                    context.startActivity(intent);
                });
                itemHolder.txtTitle.setText(route.getTitle());
                if (post.getMessage().isEmpty()) {
                    itemHolder.txtPost.setText(route.getTitle());
                } else {
                    itemHolder.txtPost.setText(post.getMessage());
                }
                itemHolder.txtDuration.setText(getDurationInMinsFormatted(route.getDuration()));
                itemHolder.txtDistance.setText(String.format(Locale.US, "%.1f km", route.getDistance() / 1000));
                itemHolder.txtConsumption.setText(String.format(Locale.US, "%.1f l", route.getConsumption() / 100.f));
                itemHolder.txtConsumptionAvg.setText(String.format(Locale.US, "%.1f km/l", route.getAvgConsumption()));
                itemHolder.txtDrivingTechnique.setText(String.format(Locale.US, "%.1f", route.getDrivingTechnique() / 10));

                if (ImageUtils.existsImageFileInCache(context, route.getId())) {
                    Utils.runOnBackThread(() -> {
                        Bitmap bmp = ImageUtils.getImageRoute(context, route.getId());
                        Utils.runOnMainThread(() -> itemHolder.img.setImageBitmap(bmp));
                    });
                } else {
                    itemHolder.img.setImageDrawable(context.getDrawable(R.drawable.map_landscape));
                    Utils.getRouteImage(context, route.getId(), route.getGpxFileId(), route.getType(), (bmp, err) -> {
                        if (bmp != null) {
                            Utils.runOnMainThread(() -> {
                                try {
                                    itemHolder.img.setImageBitmap(bmp);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    });
                }
            }
        } else if (isTextPost(post)) {
            itemHolder.layoutTitle.setVisibility(View.GONE);
            itemHolder.layoutImageAndPost.setVisibility(View.VISIBLE);
            itemHolder.layoutImage.setVisibility(View.GONE);
            itemHolder.layoutMileageEcoSafety.setVisibility(View.GONE);

            itemHolder.txtPost.setText(post.getMessage());
        } else if (isImagePost(post) || isVideoPost(post)) {
            itemHolder.layoutTitle.setVisibility(View.GONE);
            itemHolder.layoutImageAndPost.setVisibility(View.VISIBLE);
            itemHolder.layoutImage.setVisibility(View.VISIBLE);
            itemHolder.layoutMileageEcoSafety.setVisibility(View.GONE);

            itemHolder.txtPost.setText(post.getMessage());
            if (isImagePost(post)) {
                showPostImage(itemHolder, post);
            } else { // is video
                String youtubeVideoThumbnailUrl = YoutubeUtils.getYoutubeVideoThumbnail(post.getVideoUrl());
                showImageYoutube(youtubeVideoThumbnailUrl, itemHolder);
            }
        } else if (isMileagePost(post) || isEcoPost(post) || isSafetyPost(post) || isLocationPost(post)) {
            itemHolder.layoutTitle.setVisibility(View.VISIBLE);
            itemHolder.layoutImageAndPost.setVisibility(View.GONE);
            itemHolder.layoutImage.setVisibility(View.GONE);
            itemHolder.layoutMileageEcoSafety.setVisibility(View.VISIBLE);
            itemHolder.btnMenu.setVisibility(View.GONE);
            if (isMileagePost(post)) {
                itemHolder.txtDown.setVisibility(View.VISIBLE);

                itemHolder.txtTitle.setText(R.string.mileage_post_title);
                itemHolder.txtUp.setText(String.format(Locale.US, "%.1f km - %s", post.getDistance(), post.getDuration()));
                itemHolder.txtDown.setText(String.format(Locale.US, "Global: Top %d /Local: Top %d", post.getGlobalPosition(), post.getLocalPosition()));
            } else if (isEcoPost(post)) {
                itemHolder.txtDown.setVisibility(View.VISIBLE);

                itemHolder.txtTitle.setText(R.string.eco_driving_post_title);
                itemHolder.txtUp.setText(String.format(Locale.US, "%.1f l - %.1f km/l - %s", post.getTotalConsumption() / 100, post.getAverageConsumption() * 100, post.getDuration()));
                itemHolder.txtDown.setText(String.format(Locale.US, "Global: Top %d /Local: Top %d", post.getGlobalPosition(), post.getLocalPosition()));
            } else if (isSafetyPost(post)) {
                itemHolder.txtDown.setVisibility(View.GONE);

                itemHolder.txtTitle.setText(R.string.safety_driving_post_title);
                itemHolder.txtUp.setText(String.format(Locale.US, R.string.score + " %.1f - %s", post.getDrivingTechnique(), post.getDuration()));
            }
            if (!post.getMessage().isEmpty() || post.getImage() != 0) {
                itemHolder.layoutImageAndPost.setVisibility(View.VISIBLE);
                if (!post.getMessage().isEmpty()) {
                    itemHolder.txtPost.setText(post.getMessage());
                }
                if (post.getImage() != 0) {
                    itemHolder.layoutImage.setVisibility(View.VISIBLE);
                    itemHolder.img.setVisibility(View.VISIBLE);
                    showPostImage(itemHolder, post);
                }
            }
            itemHolder.txtDate.setText(getDateFormatted(post));
        }

        int pst = MySharedPreferences.createSocial(context).getInteger("post_" + post.getPostId());

        setButtonPressed(itemHolder.btnLike, pst > 0, itemHolder.txtLikes);

        itemHolder.btnLike.setOnClickListener(view -> likePost(itemHolder, post.getPostId()));

        itemHolder.btnMenu.setOnClickListener(view -> {
            if (listener != null) {
                listener.showMenu(post);
            }
        });

        itemHolder.btnMenu2.setOnClickListener(view -> {
            if (listener != null) {
                listener.showMenu(post);
            }
        });

        itemHolder.imgVideo.setOnClickListener(view -> openVideo(post.getVideoUrl()));

        itemHolder.viewPadding.setVisibility(position == getItemCount() - 1 ? View.VISIBLE : View.GONE);
    }

    private void setButtonPressed(final LinearLayout btn, final boolean hasToLikeOrDislikeButton, final TextView tv) {
        final Drawable draw = ContextCompat.getDrawable(context, R.drawable.like_button);
        final int textColor = ContextCompat.getColor(context, hasToLikeOrDislikeButton ? R.color.colorWhite : R.color.colorBlueTrans);
        btn.setBackground(draw);
        btn.setActivated(hasToLikeOrDislikeButton);
        tv.setTextColor(textColor);
    }

    private void showPostImage(PostHolder itemHolder, Community.Post post) {
        String imageFilePathInCache = ImageUtils.getImageFilePathInCache(context, post.getImage());
        if (imageFilePathInCache != null) {
            showImage(imageFilePathInCache, itemHolder);
        } else {
            getImage(itemHolder, post.getImage());
        }
    }

    private String getDurationInMinsFormatted(int durationInMins) {
        if (durationInMins < 0) {
            return "--:-- h";
        }
        int hours = durationInMins / 60;
        String h = String.valueOf(hours);
        int mins = durationInMins % 60;
        String m = String.valueOf(mins);
        if (mins < 10) {
            m = "0" + m;
        }
        return h + ":" + m + " h";
    }

    private void openVideo(String videoUrl) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
        context.startActivity(intent);
    }

    private String getDateFormatted(Community.Post post) {
        LocalDate date = DateUtils.stringToDate(post.getDateRankingStart(), DateUtils.FMT_SRV_DATE);
        if (isMileagePost(post) || isEcoPost(post)) {
            return String.format("%s\n%d", DateUtils.getDayAndMonthFormatted(date), date.getYear());
        } else if (isSafetyPost(post)) {
            LocalDate endOfWeek = date.plus(6, ChronoUnit.DAYS);
            return String.format("%s\n%s\n%d", DateUtils.getDayAndMonthFormatted(date), DateUtils.getDayAndMonthFormatted(endOfWeek), endOfWeek.getYear());
        } else if (isLocationPost(post)) {
            return String.format("%s\n%d", DateUtils.getDayAndMonthFormatted(date), date.getYear());
        }
        return "";
    }

    private Date getEndOfWeek(Date dateStart) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateStart);
        calendar.add(Calendar.DATE, 6);
        return calendar.getTime();
    }

    private boolean isTextPost(Community.Post post) {
        return post.getType() == Community.PostType.TEXT;
    }

    private boolean isLocationPost(Community.Post post) {
        return post.getType() == Community.PostType.LOCATION;
    }

    private boolean isMileagePost(Community.Post post) {
        return post.getType() == Community.PostType.RANKING_MILEAGE;
    }

    private boolean isEcoPost(Community.Post post) {
        return post.getType() == Community.PostType.RANKING_ECO;
    }

    private boolean isSafetyPost(Community.Post post) {
        return post.getType() == Community.PostType.RANKING_SAFETY;
    }

    private boolean isRoutePost(Community.Post post) {
        return post.getType() == Community.PostType.ROUTE;
    }

    private boolean isImagePost(Community.Post post) {
        return post.getType() == Community.PostType.IMAGE;
    }

    private boolean isVideoPost(Community.Post post) {
        return post.getType() == Community.PostType.VIDEO;
    }

    private void dontLikeOrDislikeAgainAPostPlease() {
        Toast.makeText(context, context.getString(R.string.already_liked), Toast.LENGTH_LONG).show();
    }

    @SuppressLint("StaticFieldLeak")
    private void likePost(PostHolder itemHolder, long postId) {
        if (MySharedPreferences.createSocial(context).getBoolean("post_" + postId)) {
            MySharedPreferences.createSocial(context).remove("post_" + postId);
        }
        if (MySharedPreferences.createSocial(context).getInteger("post_" + postId) != 0) {
            dontLikeOrDislikeAgainAPostPlease();
            return;
        }
        MySharedPreferences.createSocial(context).putInteger("post_" + postId, 1);
        if (ConnectionUtils.isOnline(context)) {
            setButtonPressed(itemHolder.btnLike, postId > 0, itemHolder.txtLikes);

            Community.PostId builder = Community.PostId.newBuilder().setPostId(postId).build();
            GenericTask gt = new GenericTask(Endpoints.POST_LIKE, builder, true, otcResponse -> {
                if (otcResponse.getStatus() == Shared.OTCStatus.SUCCESS) {
                    MySharedPreferences.createSocial(context).putInteger("post_" + postId, 1);
                    String likesString = itemHolder.txtLikes.getText().toString();
                    int likes = Integer.parseInt(likesString);
                    itemHolder.txtLikes.setText(String.valueOf(likes + 1));
                } else if (otcResponse.getStatus() == Shared.OTCStatus.ALREADY_LIKED) {
                    dontLikeOrDislikeAgainAPostPlease();
                } else {
                    showCustomDialogError();
                }
            });
            gt.execute();
        } else {
            ConnectionUtils.showOfflineToast();
        }
    }

    private void showCustomDialogError() {
        CustomDialog customDialog = new CustomDialog(context, R.string.error_default, true);
        customDialog.show();
    }

    private void getImage(PostHolder itemHolder, long imageId) {
        if (imageId == 0) {
            showImagePlaceholder(itemHolder.img, false);
            return;
        }
        @SuppressLint("StaticFieldLeak")
        GetImageTask getImageTask = new GetImageTask(imageId) {
            @Override
            protected void onPostExecute(String imagePath) {
                super.onPostExecute(imagePath);
                if (!Utils.isActivityFinish(context)) {
                    showImage(imagePath, itemHolder);
                }
            }
        };
        if (ConnectionUtils.isOnline(context)) {
            getImageTask.execute(context);
        } else {
            ConnectionUtils.showOfflineToast();
        }
    }

    private void showImageYoutube(String path, PostHolder itemHolder) {
        Glide.with(context)
                .load(Uri.parse(path))
                .into(itemHolder.img);
    }

    private void showImage(String path, PostHolder itemHolder) {
        if (!Utils.isActivityFinish(context)) {
            ImageView imageView = itemHolder.img;
            Utils.runOnBackThread(() -> {
                byte[] img = ImageUtils.getImageFromCache(context, path);
                Utils.runOnMainThread(() -> Glide.with(context).load(img).into(imageView));
            });
        }
    }

    private void showImagePlaceholder(ImageView imageView, boolean isUserImage) {
        int placeholder = isUserImage ? R.drawable.user_placeholder_correct : R.drawable.photo_placeholder_landscape;
        Glide.with(context)
                .load(placeholder)
                .into(imageView);
    }

    public void clearItems() {
        posts.clear();
    }

    public void addItems(List<Community.Post> items) {
        for (Community.Post p : items) {
            addItem(p);
        }
    }

    private void addItem(Community.Post item) {
        if (Stream.of(posts).filter(p -> p.getPostId() == item.getPostId()).count() == 0) {
            posts.add(item);
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    protected class PostHolder extends RecyclerView.ViewHolder {
        private TextView txtTime;
        private LinearLayout layoutUser;
        private LinearLayout layoutTitle;
        private LinearLayout layoutImageAndPost;
        private LinearLayout layoutRoute;
        private LinearLayout layoutMileageEcoSafety;
        private FrameLayout layoutImage;
        private ImageView imgUser, img, imgVideo;
        private TextView txtName, txtPositionAndPrivacy, txtTitle, txtPost, txtDuration, txtDistance,
                txtConsumption, txtConsumptionAvg, txtDrivingTechnique, txtDate, txtUp, txtDown, txtLikes;
        private LinearLayout btnLike;
        private FrameLayout btnFav, btnMenu2;
        private ImageView btnMenu;
        private View viewPadding;
        private boolean favourite = false;
        private View separator;

        PostHolder(View itemView) {
            super(itemView);
            txtTime = itemView.findViewById(R.id.row_post_txtTime);
            layoutUser = itemView.findViewById(R.id.row_post_layoutUser);
            layoutTitle = itemView.findViewById(R.id.row_post_layoutTitle);
            layoutImageAndPost = itemView.findViewById(R.id.row_post_layoutImageAndPost);
            layoutImage = itemView.findViewById(R.id.row_post_layoutImage);
            layoutRoute = itemView.findViewById(R.id.row_post_layoutRoute);
            layoutMileageEcoSafety = itemView.findViewById(R.id.row_post_layoutMileageEcoSafety);
            imgUser = itemView.findViewById(R.id.row_post_imgUser);
            img = itemView.findViewById(R.id.row_post_img);
            imgVideo = itemView.findViewById(R.id.row_post_imgVideo);
            txtName = itemView.findViewById(R.id.row_post_txtName);
            txtPositionAndPrivacy = itemView.findViewById(R.id.row_post_txtPositionAndPrivacy);
            txtTitle = itemView.findViewById(R.id.row_post_txtTitle);
            txtPost = itemView.findViewById(R.id.row_post_txtPost);
            txtDuration = itemView.findViewById(R.id.row_post_txtDuration);
            txtDistance = itemView.findViewById(R.id.row_post_txtDistance);
            txtConsumption = itemView.findViewById(R.id.row_post_txtConsumption);
            txtConsumptionAvg = itemView.findViewById(R.id.row_post_txtConsumptionAvg);
            txtDrivingTechnique = itemView.findViewById(R.id.row_post_txtDrivingTechnique);
            txtDate = itemView.findViewById(R.id.row_post_txtDate);
            txtUp = itemView.findViewById(R.id.row_post_txtUp);
            txtDown = itemView.findViewById(R.id.row_post_txtDown);
            txtLikes = itemView.findViewById(R.id.row_post_txtLikes);
            btnLike = itemView.findViewById(R.id.row_post_btnLike);
            btnFav = itemView.findViewById(R.id.row_post_btnFav);
            btnMenu = itemView.findViewById(R.id.row_post_btnMenu);
            btnMenu2 = itemView.findViewById(R.id.row_post_btnMenu2);
            viewPadding = itemView.findViewById(R.id.row_post_viewPadding);
            separator = itemView.findViewById(R.id.separator);
        }
    }
}