package com.otcengineering.white_app.adapter;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.otc.alice.api.model.DashboardAndStatus;
import com.otc.alice.api.model.SurveyProto;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.utils.DateUtils;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Locale;

public class SurveyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context mContext;
    ArrayList<Survey> m_surveys = new ArrayList<>();

    public SurveyAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public SurveyAdapter(Context mContext, ArrayList<Survey> m_surveys) {
        this.mContext = mContext;
        this.m_surveys = m_surveys;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_booking, parent, false);
        return new SurveyHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Survey survey = m_surveys.get(position);
        SurveyHolder sh = (SurveyHolder) holder;
        setData(sh, survey);
        setEvents(sh, survey);
        if (position % 2 == 0) {
            holder.itemView.setBackgroundColor(0x00000000);
        } else {
            holder.itemView.setBackgroundColor(mContext.getColor(R.color.quantum_grey200));
        }
    }

    private void setData(SurveyHolder holder, Survey survey) {

        switch ((int) survey.surveyResponse.getFormType()) {
            case 1:
                holder.box_yes.setVisibility(View.VISIBLE);
                holder.box_no.setVisibility(View.VISIBLE);
                holder.box_date.setVisibility(View.GONE);
                holder.box_text.setVisibility(View.GONE);
                holder.box_num_persons.setVisibility(View.GONE);
                break;
            case 2:
                holder.box_yes.setVisibility(View.VISIBLE);
                holder.box_no.setVisibility(View.VISIBLE);
                if (survey.answerSelected != null && survey.surveyResponse.getAnswersList().indexOf(survey.answerSelected) == 0) {
                    holder.box_date.setVisibility(View.VISIBLE);
                }
                holder.box_text.setVisibility(View.GONE);
                holder.box_num_persons.setVisibility(View.GONE);
                break;
            case 3:
                holder.box_yes.setVisibility(View.VISIBLE);
                holder.box_no.setVisibility(View.VISIBLE);
                holder.box_date.setVisibility(View.GONE);
                holder.box_text.setVisibility(View.VISIBLE);
                holder.box_num_persons.setVisibility(View.GONE);
                break;
            case 4:
                holder.box_yes.setVisibility(View.GONE);
                holder.box_no.setVisibility(View.GONE);
                holder.box_date.setVisibility(View.GONE);
                holder.box_text.setVisibility(View.VISIBLE);
                holder.box_num_persons.setVisibility(View.VISIBLE);
                break;
            case 5:
                holder.box_yes.setVisibility(View.VISIBLE);
                holder.box_no.setVisibility(View.VISIBLE);
                holder.box_date.setVisibility(View.VISIBLE);
                holder.box_text.setVisibility(View.VISIBLE);
                holder.box_num_persons.setVisibility(View.GONE);
                break;
        }
        holder.question.setText(survey.surveyResponse.getText());
        if(survey.answerSelected != null) {
            if (survey.surveyResponse.getAnswersList().indexOf(survey.answerSelected) == 0) {
                holder.box_yes.setCardBackgroundColor(mContext.getResources().getColor(R.color.colorPrimary));
                holder.yes.setTextColor(Color.WHITE);
                holder.box_no.setCardBackgroundColor(Color.WHITE);
                holder.no.setTextColor(Color.BLACK);
                holder.box_date.setVisibility(View.VISIBLE);
            } else {
                holder.box_no.setCardBackgroundColor(mContext.getResources().getColor(R.color.colorPrimary));
                holder.no.setTextColor(Color.WHITE);
                holder.box_yes.setCardBackgroundColor(Color.WHITE);
                holder.yes.setTextColor(Color.BLACK);
                holder.box_date.setVisibility(View.GONE);
            }

        } else {
            holder.box_no.setCardBackgroundColor(Color.WHITE);
            holder.no.setTextColor(Color.BLACK);
            holder.box_yes.setCardBackgroundColor(Color.WHITE);
            holder.yes.setTextColor(Color.BLACK);
            holder.box_date.setVisibility(View.GONE);
        }

        holder.date.setText(survey.date);
    }

    private void setEvents(SurveyHolder holder, Survey survey) {
        holder.box_no.setOnClickListener(v -> {
            holder.box_no.setCardBackgroundColor(mContext.getResources().getColor(R.color.colorPrimary));
            holder.no.setTextColor(Color.WHITE);
            holder.box_yes.setCardBackgroundColor(Color.WHITE);
            holder.yes.setTextColor(Color.BLACK);
            survey.answerSelected = survey.surveyResponse.getAnswers(1);
            holder.box_date.setVisibility(View.GONE);
        });

        holder.box_yes.setOnClickListener(v -> {
            holder.box_yes.setCardBackgroundColor(mContext.getResources().getColor(R.color.colorPrimary));
            holder.yes.setTextColor(Color.WHITE);
            holder.box_no.setCardBackgroundColor(Color.WHITE);
            holder.no.setTextColor(Color.BLACK);
            survey.answerSelected = survey.surveyResponse.getAnswers(0);
            if (survey.surveyResponse.getFormType() == 2) holder.box_date.setVisibility(View.VISIBLE);
        });

        setDatePicker(holder.date, holder.box_date, survey);

        holder.numPersons.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                survey.numPersons = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        holder.text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                survey.text = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setDatePicker(TextView et, CardView box, Survey survey) {
        DatePickerDialog.OnDateSetListener onDateSetListener =
                (view, year, month, dayOfMonth) -> {
                    String format = String.format(Locale.US, "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    et.setText(format);
                    survey.date = format;
                };

        box.setOnClickListener(v -> {
            LocalDate date = LocalDate.now();
            new DatePickerDialog(mContext, AlertDialog.THEME_HOLO_DARK, onDateSetListener, date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth()).show();
        });
    }

    @Override
    public int getItemCount() {
        return m_surveys.size();
    }

    public void addSurvey(SurveyProto.Survey survey) {
        this.m_surveys.add(new Survey(survey));
    }

    public boolean containsSurvey(long surveyId) {
        for (Survey survey: m_surveys) {
            if (survey.surveyResponse.getId() == surveyId) return true;
        }
        return false;
    }

    public void clearList() {
        this.m_surveys.clear();
    }

    public ArrayList<Survey> getSurveys() {
        return m_surveys;
    }

    public void setList(ArrayList<Survey> list) {
        m_surveys = list;
    }

    public class Survey {
        public SurveyProto.Survey surveyResponse;

        public SurveyProto.SurveyAnswer answerSelected;

        public String date = DateUtils.dateToString(LocalDate.now(), "dd/MM/yyyy");

        public String numPersons = "";

        public String text = "";
        Survey(SurveyProto.Survey surveyResponse) {
            this.surveyResponse = surveyResponse;
        }
    }

    protected class SurveyHolder extends RecyclerView.ViewHolder {

        CardView box_date, box_yes, box_no, box_text, box_num_persons;
        TextView no, yes, date, question, text, numPersons;

        public SurveyHolder(View itemView) {
            super(itemView);
            box_date = itemView.findViewById(R.id.box_date);
            box_yes = itemView.findViewById(R.id.option_yes);
            box_no = itemView.findViewById(R.id.option_no);
            box_text = itemView.findViewById(R.id.box_text);
            box_num_persons = itemView.findViewById(R.id.box_num_persons);
            //question = itemView.findViewById(R.id.txt_question);
            yes = itemView.findViewById(R.id.txt_yes);
            no = itemView.findViewById(R.id.txt_no);
            date = itemView.findViewById(R.id.data);
            text = itemView.findViewById(R.id.edit_text_text);
            numPersons = itemView.findViewById(R.id.edit_text_num);
        }
    }
}
