package com.otcengineering.white_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.otcengineering.white_app.R;
import com.otcengineering.white_app.serialization.pojo.CustomKeySet;

import java.util.ArrayList;

public class ProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<CustomKeySet> m_valuesMap;
    public ProfileAdapter() {
        m_valuesMap = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_values_adapter, parent, false);

        return new ProfileHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CustomKeySet cks = m_valuesMap.get(position);
        if (position % 2 == 1) {
            holder.itemView.setBackgroundResource(R.color.my_profile_odd);
        } else {
            holder.itemView.setBackgroundResource(R.color.my_profile_even);
        }

        ProfileHolder pHolder = (ProfileHolder)holder;

        pHolder.setValues(cks.getKey(), cks.getValue());
    }

    public void addKeySet(String key, String value) {
        CustomKeySet cks = new CustomKeySet(key, value);
        m_valuesMap.add(cks);
    }

    public void clear() {
        m_valuesMap.clear();
    }

    @Override
    public int getItemCount() {
        return m_valuesMap.size();
    }

    public class ProfileHolder extends RecyclerView.ViewHolder {
        private TextView title, description;

        ProfileHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.txtTitle);
            description = itemView.findViewById(R.id.txtValue);
        }

        void setValues(String title, String description) {
            this.title.setText(title);
            this.description.setText(description);
        }
    }
}
