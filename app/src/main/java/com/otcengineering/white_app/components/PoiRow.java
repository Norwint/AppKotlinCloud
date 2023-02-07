package com.otcengineering.white_app.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.otc.alice.api.model.General;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.adapter.PoiImageAdapter;
import com.otcengineering.white_app.serialization.pojo.PoiWrapper;
import com.otcengineering.white_app.utils.Utils;
import com.otcengineering.white_app.utils.images.ImageUtils;

import java.util.List;

/**
 * Created by cenci7
 */

public class PoiRow extends FrameLayout {

    public interface PoiRowListener {
        void onDelete(int position);

        void onTypeChanged(int position);

        void onImageAdded(int position);

        void onImageDeleted(int position);
    }

    private Button btnDelete;
    private EditText editTitle;
    private TextView txtType;
    private ImageView btnAddImage;
    private RecyclerView recyclerImages;

    private PoiImageAdapter adapter;

    private Context context;
    private PoiWrapper poi;
    private PoiRowListener listener;

    private int position;


    public PoiRow(@NonNull Context context, PoiWrapper poi, int position, PoiRowListener listener) {
        super(context);
        this.context = context;
        this.poi = poi;
        this.position = position;
        this.listener = listener;
        retrieveViews();
        setEvents();
        configureAdapter();
        showPoiInfo();
    }

    private void retrieveViews() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (inflater != null) {
            View view = inflater.inflate(R.layout.row_poi_save_route, this, true);
            btnDelete = view.findViewById(R.id.row_poi_save_route_btnDelete);
            editTitle = view.findViewById(R.id.row_poi_save_route_editPoiTitle);
            txtType = view.findViewById(R.id.row_poi_save_route_txtPoiType);
            btnAddImage = view.findViewById(R.id.row_poi_save_route_btnAddImage);
            recyclerImages = view.findViewById(R.id.row_poi_save_route_recyclerImages);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setEvents() {
        btnDelete.setOnClickListener(view -> {
            if (isLocalPoi(poi.getPoi())) {
                actionsAfterDeletePoi(position);
                String poiImages = ImageUtils.getPoiImageName(poi.getInternalID(), context);
                ImageUtils.deleteImageFile(context, poiImages);
            } else {
                actionsAfterDeletePoi(position);
            }
        });

        editTitle.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (editTitle.getRight() - editTitle.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    editTitle.setText("");
                    return true;
                }
            }
            return false;
        });
        editTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                poi.setPoi(poi.getPoi().toBuilder().setTitle(s.toString()).build());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        txtType.setOnClickListener(view -> {
            if (listener != null) {
                listener.onTypeChanged(position);
            }
        });

        btnAddImage.setOnClickListener(view -> {
            if (listener != null) {
                listener.onImageAdded(position);
            }
        });
    }

    private boolean isLocalPoi(General.POI poi) {
        return poi.getPoiId() == 0;
    }

    private void actionsAfterDeletePoi(int position) {
        if (listener != null) {
            listener.onDelete(position);
        }
    }

    private void configureAdapter() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        recyclerImages.setLayoutManager(layoutManager);
        adapter = new PoiImageAdapter(context, () -> {
            btnAddImage.setVisibility(View.VISIBLE);
            if (listener != null) {
                listener.onImageDeleted(position);
            }
        });
        recyclerImages.setAdapter(adapter);
    }

    private void showPoiInfo() {
        editTitle.setText(poi.getPoi().getTitle());
        General.PoiType poiType = poi.getPoi().getType();
        txtType.setText(Utils.translatePoiType(context, poiType));

        if (poi.getPoi().getImagesCount() > 0) {
            addPoiImagesToRecycler(String.valueOf(poi.getPoi().getImages(0)));
            btnAddImage.setVisibility(View.GONE);
        } else { // check for local images
            showLocalImages();
        }
    }

    private void showLocalImages() {
        //List<String> poiImages = ImageUtils.getPoiImages(position + 1, context);
        String poiImage = ImageUtils.getPoiImageName(poi.getInternalID(), context);
        if (ImageUtils.poiImageExists(poi.getInternalID(), context)) {
            addPoiImagesToRecycler(poiImage);
            btnAddImage.setVisibility(View.GONE);
        } else {
            btnAddImage.setVisibility(View.VISIBLE);
        }
    }

    private void addPoiImagesToRecycler(String images) {
        adapter.clearItems();
        adapter.addItem(images);
        adapter.notifyDataSetChanged();
    }

    @NonNull
    public General.POI.Builder createPoiBuilderWithCurrentValues() {
        General.POI.Builder builder = General.POI.newBuilder();
        builder.setPoiId(poi.getPoi().getPoiId());
        builder.setLatitude(poi.getPoi().getLatitude());
        builder.setLongitude(poi.getPoi().getLongitude());
        builder.setRouId(poi.getPoi().getRouId());
        String title = editTitle.getText().toString();
        builder.setTitle(title);
        builder.setType(poi.getPoi().getType());
        List<Long> imagesList = poi.getPoi().getImagesList();
        builder.addAllImages(imagesList);
        return builder;
    }

    public void updateImages() {
        showLocalImages();
    }
}
