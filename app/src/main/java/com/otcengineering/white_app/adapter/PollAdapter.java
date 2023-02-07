package com.otcengineering.white_app.adapter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.otc.alice.api.model.SurveyProto;
import com.otcengineering.white_app.MyApp;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.activities.ConditionDescriptionActivity;
import com.otcengineering.white_app.activities.SurveyActivity;
import com.otcengineering.white_app.components.DialogMultiple;
import com.otcengineering.white_app.utils.DateUtils;

import java.io.Serializable;
import java.util.ArrayList;

public class PollAdapter extends RecyclerView.Adapter<PollAdapter.PollHolder> {
    private ArrayList<SurveyProto.Survey> polls = new ArrayList<>();
    private static Runnable onPollRefreshListener;

    @NonNull
    @Override
    public PollHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PollHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_poll, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PollHolder holder, int position) {
        holder.bind(polls.get(position));
    }

    @Override
    public int getItemCount() {
        return polls.size();
    }

    public void setPolls(ArrayList<SurveyProto.Survey> polls) {
        this.polls.clear();
        this.polls.addAll(polls);
    }

    public void setOnPollRefreshListener(Runnable listener) {
        onPollRefreshListener = listener;
    }

    static class PollHolder extends RecyclerView.ViewHolder {
        private TextView title, date;
        private ImageView image;

        public PollHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            date = itemView.findViewById(R.id.date);
            image = itemView.findViewById(R.id.icon);
        }

        public void bind(SurveyProto.Survey poll) {
            title.setText(poll.getText());
            date.setText(DateUtils.utcStringToLocalString(poll.getLastRegister(), "yyyy-MM-dd HH:mm:ss", "dd/MM/yyyy - HH:mm:ss"));

            if (poll.getLastRegister() == null || poll.getLastRegister().isEmpty()) {
                image.setImageBitmap(BitmapFactory.decodeResource(MyApp.getContext().getResources(), R.drawable.check_fail));
            } else {
                image.setImageBitmap(BitmapFactory.decodeResource(MyApp.getContext().getResources(), R.drawable.vehicle_condition_3));
            }

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(MyApp.getContext(), SurveyActivity.class);
                intent.putExtra("survey", poll);
                Bundle args = new Bundle();
                //args.putSerializable("surveys", (Serializable) poll.);
                intent.putExtra("BUNDLE", args);
                //intent.putExtra("surveys", (Parcelable) allSurveys);
                MyApp.getContext().startActivity(intent);
            });
        }
    }

    public static class Poll {
        public String title;
        public String desc;
        public boolean yesno;
        public String options = null;
        public String response;
        public String date;
    }
}
