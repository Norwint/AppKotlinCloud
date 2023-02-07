package com.otcengineering.white_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.otc.alice.api.model.General;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cenci7
 */

public class PoiTypeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface PoiListener {
        void onItemSelected(int position);
    }

    private Context context;
    private List<General.PoiType> poiTypeList = new ArrayList<>();
    private PoiListener listener;

    public PoiTypeAdapter(Context context, PoiListener listener) {
        super();
        this.context = context;
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return poiTypeList.size();
    }

    public General.PoiType getItem(int position) {
        return poiTypeList.get(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_poi_type, viewGroup, false);
        return new PoiTypeHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final PoiTypeHolder itemHolder = (PoiTypeHolder) holder;
        General.PoiType poiType = poiTypeList.get(position);

        String poiTypeName = Utils.translatePoiType(context, poiType);

        itemHolder.txtName.setText(poiTypeName);

        itemHolder.layoutRoot.setOnClickListener(view -> {
            if (listener != null) {
                listener.onItemSelected(itemHolder.getAdapterPosition());
            }
        });
    }

    public void clearItems() {
        poiTypeList.clear();
    }

    public void addItems(List<General.PoiType> items) {
        poiTypeList.addAll(items);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    protected class PoiTypeHolder extends RecyclerView.ViewHolder {
        private LinearLayout layoutRoot;
        private TextView txtName;

        PoiTypeHolder(View itemView) {
            super(itemView);
            layoutRoot = itemView.findViewById(R.id.row_poi_type_layoutRoot);
            txtName = itemView.findViewById(R.id.row_poi_type_txtName);
        }
    }

}