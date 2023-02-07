package com.otcengineering.white_app.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.tasks.DeleteImageTask;
import com.otcengineering.white_app.tasks.GetImageTask;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.images.ImageUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cenci7
 */

public class PoiImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public interface PoiImageListener {
        void onDelete();
    }

    private Context context;
    private List<String> images = new ArrayList<>();
    private PoiImageListener listener;


    public PoiImageAdapter(Context context, PoiImageListener listener) {
        super();
        this.context = context;
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public String getItem(int position) {
        return images.get(position);
    }

    public List<String> getItems() {
        return images;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_poi_image, viewGroup, false);
        return new PoiImageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final PoiImageHolder itemHolder = (PoiImageHolder) holder;
        String imagePath = images.get(position);

        if (imageIsFromServer(imagePath)) {
            Long imgId = Long.valueOf(imagePath);
            Glide.with(context).load(imgId).into(itemHolder.image);
        } else {
            showImage(itemHolder, imagePath);
        }

        itemHolder.btnDelete.setOnClickListener(view -> deleteItem(position, imagePath));
    }

    private boolean imageIsFromServer(String string) {
        try {
            Long.parseLong(string);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private void getImage(PoiImageHolder itemHolder, long imageId) {
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

    private void downloadImage(PoiImageHolder itemHolder, long imageId) {
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

    private void showImage(PoiImageHolder itemHolder, String imagePath) {
        Glide.with(context).load(BitmapFactory.decodeFile(imagePath)).into(itemHolder.image);
    }

    private void showImagePlaceholder(PoiImageHolder itemHolder) {
        Glide.with(context)
                .load(R.drawable.photo_placeholder_landscape)
                .into(itemHolder.image);
    }

    private void deleteItem(int position, String imagePath) {
        if (imageIsFromServer(imagePath)) {
            deleteImage(position, imagePath);
        } else {
            ImageUtils.deleteImageFile(context, imagePath);
            actionsAfterDeleteImage(position);
        }
    }

    private void deleteImage(int position, String imageId) {
        @SuppressLint("StaticFieldLeak")
        DeleteImageTask deleteImageTask = new DeleteImageTask(Long.valueOf(imageId)) {
            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                if (result) {
                    actionsAfterDeleteImage(position);
                }
            }
        };
        if (ConnectionUtils.isOnline(context)) {
            deleteImageTask.execute(context);
        } else {
            ConnectionUtils.showOfflineToast();
        }
    }

    private void actionsAfterDeleteImage(int position) {
        images.remove(position);
        notifyDataSetChanged();
        if (listener != null) {
            listener.onDelete();
        }
    }

    public void clearItems() {
        images.clear();
    }

    public void addItems(List<String> items) {
        images.addAll(items);
    }

    public void addItem(String item) {
        images.add(item);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    protected class PoiImageHolder extends RecyclerView.ViewHolder {
        private ImageView image;
        private ImageView btnDelete;


        PoiImageHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.item_poi_image_img);
            btnDelete = itemView.findViewById(R.id.item_poi_image_btnDelete);
        }
    }
}