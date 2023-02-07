package com.otcengineering.white_app.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.otc.alice.api.model.General;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.tasks.GetImageTask;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.Utils;
import com.otcengineering.white_app.utils.images.ImageUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cenci7
 */

public class PoiRouteDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<General.POI> poiList = new ArrayList<>();

    public PoiRouteDetailsAdapter(Context context) {
        super();
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return poiList.size();
    }

    public General.POI getItem(int position) {
        return poiList.get(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_poi_route_details, viewGroup, false);
        return new PoiHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final PoiHolder itemHolder = (PoiHolder) holder;
        General.POI poi = poiList.get(position);

        itemHolder.imgThreeDots.setVisibility(position == 0 ? View.GONE : View.VISIBLE);

        itemHolder.txtTitle.setText(poi.getTitle());
        General.PoiType poiType = poi.getType();

        String poiTypeName = Utils.translatePoiType(context, poiType);

        itemHolder.txtType.setText(poiTypeName);

        List<Long> imagesList = poi.getImagesList();
        if (imagesList != null && !imagesList.isEmpty()) {
            itemHolder.layoutImg.setVisibility(View.VISIBLE);
            Long imgId = imagesList.get(0);
            Glide.with(context).load(imgId).into(itemHolder.img);
        } else {
            itemHolder.layoutImg.setVisibility(View.GONE);
        }
    }

    private void getImage(PoiHolder itemHolder, long imageId) {
        if (imageId == 0) {
            return;
        }

        String imageFilePathInCache = ImageUtils.getImageFilePathInCache(context, imageId);
        if (imageFilePathInCache != null) {
            showImage(itemHolder, imageFilePathInCache);
        } else {
            downloadImage(itemHolder, imageId);
        }
    }

    private void downloadImage(PoiHolder itemHolder, long imageId) {
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

    private void showImage(PoiHolder itemHolder, String imagePath) {
        Glide.with(context)
                .load(ImageUtils.FILE_DIRECTORY + imagePath)
                .into(itemHolder.img);
    }

    public void clearItems() {
        poiList.clear();
    }

    public void addItems(List<General.POI> items) {
        if (items != null) {
            poiList.addAll(items);
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    protected class PoiHolder extends RecyclerView.ViewHolder {
        private ImageView imgThreeDots;
        private FrameLayout layoutImg;
        private ImageView img;
        private TextView txtType;
        private TextView txtTitle;


        PoiHolder(View itemView) {
            super(itemView);
            imgThreeDots = itemView.findViewById(R.id.row_poi_route_details_imgThreeDots);
            layoutImg = itemView.findViewById(R.id.row_poi_route_details_layoutImg);
            img = itemView.findViewById(R.id.row_poi_route_details_img);
            txtType = itemView.findViewById(R.id.row_poi_route_details_txtType);
            txtTitle = itemView.findViewById(R.id.row_poi_route_details_txtTitle);
        }
    }

}