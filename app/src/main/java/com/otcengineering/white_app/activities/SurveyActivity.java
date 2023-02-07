package com.otcengineering.white_app.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.InvalidProtocolBufferException;
import com.otc.alice.api.model.Community;
import com.otc.alice.api.model.DashboardAndStatus;
import com.otc.alice.api.model.General;
import com.otc.alice.api.model.Shared;
import com.otc.alice.api.model.SurveyProto;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.adapter.SurveyAdapter;
import com.otcengineering.white_app.components.TitleBar;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.tasks.GenericTask;
import com.otcengineering.white_app.utils.DateUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.PrefsManager;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SurveyActivity extends BaseActivity {
    private TextView title, description, date, text, yes, no;

    private CardView box_date, box_yes, box_no, box_text, box_num_persons;

    private RecyclerView surveysRecycler;

    private SurveyAdapter adapter;

    EditText edText, numPersons;

    private SurveyProto.Survey survey;
    private List<SurveyProto.Survey> surveys;

    private SurveyProto.SurveyAnswer ansSelected;

    public SurveyActivity() {
        super("SurveyActivity");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_survey);

        getExtras();
        getViews();
        setData(this.survey);
        setEvents();
    }


    private void getExtras() {
        // Get Extras
        Bundle extras = getIntent().getExtras();
        this.survey = (SurveyProto.Survey) extras.get("survey");
        Bundle args = extras.getBundle("BUNDLE");
        assert args != null;
        this.surveys = (List<SurveyProto.Survey>) args.getSerializable("surveys");
        //this.surveys = extras.getParcelable("surveys");
    }

    private void setData(SurveyProto.Survey survey) {
        General.SignalMode sm = General.SignalMode.WORKING;
        String descr = "";
        edText.setText("");

        switch (sm) {
            case DISABLED:
                descr = getResources().getString(R.string.not_available);
                break;
            case WORKING:
                descr = "";
                break;
            case PROBLEM:
                descr = getResources().getString(R.string.service_required);
                break;
        }

        //text.setText(desc);
        title.setText(survey.getText().toUpperCase());
        description.setText(descr);
        if (descr.isEmpty()) {
            description.setVisibility(View.GONE);
        }
//        Glide.with(this).load(icon).into(img);
//        Glide.with(this).load(ConditionAdapter.getResourceForSignalMode(sm)).into(condition);
        text.setText("");
        String s = DateUtils.dateToString(LocalDate.now(), "dd/MM/yyyy");
        switch ((int) survey.getFormType()) {
            case 0:
                box_yes.setVisibility(View.GONE);
                box_no.setVisibility(View.GONE);
                box_date.setVisibility(View.GONE);
                box_text.setVisibility(View.GONE);
                box_num_persons.setVisibility(View.GONE);
                surveysRecycler.setVisibility(View.VISIBLE);
                //survey.getAnswers(0).getNextIdsList();
                if (MySharedPreferences.createSurvey(this).contains(String.valueOf(survey.getId()))) {
                    Gson gson = new Gson();
                    String jsonText = MySharedPreferences.createSurvey(this).getString(String.valueOf(survey.getId()));
                    adapter.setList(gson.fromJson(jsonText, new TypeToken<ArrayList<SurveyAdapter.Survey>>() {
                    }.getType()));
                    if (survey.getSubSurveysList().size() > adapter.getSurveys().size()) {
                        List<SurveyProto.SubSurvey> newSurveys = new ArrayList<>();
                        for (SurveyProto.SubSurvey subSurvey : this.survey.getSubSurveysList()) {
                            if (!adapter.containsSurvey(subSurvey.getId()))
                                newSurveys.add(subSurvey);
                        }
                        getSurveys(newSurveys);
                    }
                    updateSurveys();
                } else {
                    getSurveys();
                }

                surveysRecycler.setAdapter(adapter);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
                surveysRecycler.setLayoutManager(layoutManager);
                break;
            case 1:
                box_yes.setVisibility(View.VISIBLE);
                box_no.setVisibility(View.VISIBLE);
                box_date.setVisibility(View.GONE);
                box_text.setVisibility(View.GONE);
                box_num_persons.setVisibility(View.GONE);
                surveysRecycler.setVisibility(View.GONE);
                break;
            case 2:
                box_yes.setVisibility(View.VISIBLE);
                box_no.setVisibility(View.VISIBLE);
                if (ansSelected != null && survey.getAnswersList().indexOf(this.ansSelected) == 0) {
                    box_date.setVisibility(View.VISIBLE);
                }
                box_text.setVisibility(View.GONE);
                box_num_persons.setVisibility(View.GONE);
                surveysRecycler.setVisibility(View.GONE);
                break;
            case 3:
                box_yes.setVisibility(View.VISIBLE);
                box_no.setVisibility(View.VISIBLE);
                box_date.setVisibility(View.GONE);
                box_text.setVisibility(View.VISIBLE);
                box_num_persons.setVisibility(View.GONE);
                surveysRecycler.setVisibility(View.GONE);
                break;
            case 4:
                box_yes.setVisibility(View.GONE);
                box_no.setVisibility(View.GONE);
                box_date.setVisibility(View.GONE);
                box_text.setVisibility(View.VISIBLE);
                box_num_persons.setVisibility(View.VISIBLE);
                surveysRecycler.setVisibility(View.GONE);
                break;
            case 5:
                box_yes.setVisibility(View.VISIBLE);
                box_no.setVisibility(View.VISIBLE);
                box_date.setVisibility(View.VISIBLE);
                box_text.setVisibility(View.VISIBLE);
                box_num_persons.setVisibility(View.GONE);
                surveysRecycler.setVisibility(View.GONE);
                break;
        }

        if (survey.getFormType() != 0)
            if (MySharedPreferences.createSurvey(this).contains(String.valueOf(survey.getId()))) {
                byte[] arr = MySharedPreferences.createSurvey(this).getBytes(String.valueOf(survey.getId()));
                SurveyProto.SurveyAnswer resp;
                try {
                    resp = SurveyProto.SurveyAnswer.parseFrom(arr);
                    if (survey.getAnswersList().indexOf(resp) == 0) {
                        box_yes.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        yes.setTextColor(Color.WHITE);
                        box_no.setCardBackgroundColor(Color.WHITE);
                        no.setTextColor(Color.BLACK);
                        ansSelected = survey.getAnswers(0);
                        setAdvice(survey.getAnswers(0));
                        if (survey.getFormType() == 2) box_date.setVisibility(View.VISIBLE); //lapa
                    } else {
                        box_no.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        no.setTextColor(Color.WHITE);
                        box_yes.setCardBackgroundColor(Color.WHITE);
                        yes.setTextColor(Color.BLACK);
                        ansSelected = survey.getAnswers(1);
                        setAdvice(survey.getAnswers(1));
                        box_date.setVisibility(View.INVISIBLE); //lapa
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

        this.date.setText(s);

        /*box_no.setCardBackgroundColor(Color.WHITE);
        no.setTextColor(Color.BLACK);
        box_yes.setCardBackgroundColor(Color.WHITE);
        yes.setTextColor(Color.BLACK);*/
    }

    private void getViews() {
        adapter = new SurveyAdapter(this);

        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        date = findViewById(R.id.data);
        text = findViewById(R.id.text);

        box_date = findViewById(R.id.box_date);
        box_yes = findViewById(R.id.option_yes);
        box_no = findViewById(R.id.option_no);
        box_text = findViewById(R.id.box_text);
        box_num_persons = findViewById(R.id.box_num_persons);
        yes = findViewById(R.id.txt_yes);
        no = findViewById(R.id.txt_no);

        box_date.setVisibility(View.GONE);// lapa
        edText = findViewById(R.id.edit_text_text);

        surveysRecycler = findViewById(R.id.recyclerSurvey);

        TitleBar titleBar = findViewById(R.id.conddesc_titleBar);

        titleBar.setListener(new TitleBar.TitleBarListener() {
            @Override
            public void onLeftClick() {
                finish();
            }

            @Override
            public void onRight1Click() {
            }

            @Override
            public void onRight2Click() {
            }
        });
    }

    private void setEvents() {
        box_no.setOnClickListener(v -> {
            box_no.setCardBackgroundColor(getColor(R.color.colorPrimary));
            no.setTextColor(Color.WHITE);
            box_yes.setCardBackgroundColor(Color.WHITE);
            yes.setTextColor(Color.BLACK);
            ansSelected = survey.getAnswers(1);
            setAdvice(survey.getAnswers(1));
            box_date.setVisibility(View.INVISIBLE);
        });

        box_yes.setOnClickListener(v -> {
            box_yes.setCardBackgroundColor(getColor(R.color.colorPrimary));
            yes.setTextColor(Color.WHITE);
            box_no.setCardBackgroundColor(Color.WHITE);
            no.setTextColor(Color.BLACK);
            ansSelected = survey.getAnswers(0);
            setAdvice(survey.getAnswers(0));
            if (survey.getFormType() == 2) box_date.setVisibility(View.VISIBLE);
        });
        setDatePicker(date, box_date);


    }

    private void setAdvice(SurveyProto.SurveyAnswer answer) {
        String advice = "";
        for (SurveyProto.SurveyNotification surveyNotification : answer.getNotificationsList()) {
            advice = advice.concat(surveyNotification.getText());
            advice = advice.concat("\n");
        }

        text.setText(advice);

    }

    private void setDatePicker(TextView et, CardView box) {
        DatePickerDialog.OnDateSetListener onDateSetListener =
                (view, year, month, dayOfMonth) -> et.setText(String.format(Locale.US, "%02d/%02d/%04d", dayOfMonth, month + 1, year));
        box.setOnClickListener(v -> {
            LocalDate date = LocalDate.now();
            new DatePickerDialog(this, AlertDialog.THEME_HOLO_DARK, onDateSetListener, date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth()).show();
        });
    }

    private void getSurveys() {
        for (SurveyProto.SubSurvey subSurvey : this.survey.getSubSurveysList()) {
            GenericTask getDashboard = new GenericTask(String.format(Locale.getDefault(), "%s/%d", Endpoints.Surveys.ID, subSurvey.getId()), null, true, otcResponse -> {
                if (otcResponse.getStatus() == Shared.OTCStatus.SUCCESS) {
                    SurveyProto.Survey response = otcResponse.getData().unpack(SurveyProto.Survey.class);
                    adapter.addSurvey(response);
                    adapter.notifyDataSetChanged();
                }
            });
            getDashboard.execute();
        }
    }

    private void getSurveys(List<SurveyProto.SubSurvey> subSurveys) {
        for (SurveyProto.SubSurvey subSurvey : subSurveys) {
            GenericTask getDashboard = new GenericTask(String.format(Locale.getDefault(), "%s/%d", Endpoints.Surveys.ID, subSurvey.getId()), null, true, otcResponse -> {
                if (otcResponse.getStatus() == Shared.OTCStatus.SUCCESS) {
                    SurveyProto.Survey response = otcResponse.getData().unpack(SurveyProto.Survey.class);
                    adapter.addSurvey(response);
                    adapter.notifyDataSetChanged();
                }
            });
            getDashboard.execute();
        }
    }

    private void answerSurvey() {
        SurveyProto.UserSurveyAnswer.Builder request = SurveyProto.UserSurveyAnswer.newBuilder();
        request.setSurveyId(survey.getId());

        switch ((int) survey.getFormType()) {
            case 0:
                sendFormType0();
                break;
            case 1:
                if (ansSelected != null) {
                    request.setSurveyId(survey.getId());
                    request.setSurveyAnswerId(ansSelected.getId());
                    sendSurvey(request);
                } else {
                    showError("", getResources().getString(R.string.survey_not_responeded));
                }
                break;
            case 2:// SI + DATE / NO
                if (ansSelected != null) {
                    request.setSurveyId(survey.getId());
                    request.setSurveyAnswerId(ansSelected.getId());
                    request.setValue(this.date.getText().toString());
                    sendSurvey(request);
                } else {
                    showError("", getResources().getString(R.string.survey_not_responeded));
                }
                break;
            case 3:// SI + TEXT / NO
                if (ansSelected != null) {
                    request.setSurveyId(survey.getId());
                    request.setSurveyAnswerId(ansSelected.getId());
                    request.setValue(this.edText.getText().toString());
                    sendSurvey(request);
                } else {
                    showError("", getResources().getString(R.string.survey_not_responeded));
                }
                break;
            case 4:// SI + NÚMERO | EDATS / NO
                if (!this.numPersons.getText().toString().equals("")) {
                    if (Integer.parseInt(this.numPersons.getText().toString()) > 0 && !this.text.getText().toString().equals("") || Integer.parseInt(this.numPersons.getText().toString()) == 0) {
                        request.setSurveyId(survey.getId());
                        request.setSurveyAnswerId(Integer.parseInt(this.numPersons.getText().toString()) > 0 ? survey.getAnswers(1).getId() : survey.getAnswers(1).getId());
                        request.setValue(String.format(Locale.getDefault(), "%s|%s", this.numPersons.getText().toString(), this.text.getText().toString()));
                        sendSurvey(request);
                    } else {
                        showError("", getResources().getString(R.string.survey_not_responeded));
                    }
//                    isLoop = true;
                } else {
                    showError("", getResources().getString(R.string.survey_not_responeded));
                }
                break;
            case 5:// SI + DATE | TEXT / NO
                if (ansSelected != null) {
                    request.setSurveyId(survey.getId());
                    request.setSurveyAnswerId(ansSelected.getId());
                    request.setValue(String.format(Locale.getDefault(), "%s|%s", this.date.getText().toString(), this.edText.getText().toString()));
                    sendSurvey(request);
                } else {
                    showError("", getResources().getString(R.string.survey_not_responeded));
                }
                break;
        }
        boolean isLoop = false;
        if (isLoop) {
            /*GenericTask answerSurvey = new GenericTask(Endpoints.Surveys.SURVEY_ANSWER_LOOP, request.build(), true, otcResponse -> {
                if (otcResponse.getStatus() == Shared.OTCStatus.SUCCESS) {
                    //DashboardAndStatus.SurveysResponse resp = otcResponse.getData().unpack(DashboardAndStatus.SurveysResponse.class);
                    boolean surveyFound = false;
                    if (ansSelected.getNextSurveyId() == 0) {
                        onBackPressed();
                    } else {
                        getNextSurvey(ansSelected.getNextSurveyId());
                    }
                }
            });
            answerSurvey.execute();*/
        }
    }

    private void sendFormType0() {
        boolean surveyNoResponse = false;
        for (SurveyAdapter.Survey s : adapter.getSurveys()) {
            if (s.answerSelected == null && s.surveyResponse.getFormType() != 4)
                surveyNoResponse = true;
            if (s.surveyResponse.getFormType() == 4) {
                if (!s.numPersons.equals("")) {
                    if ((Integer.parseInt(s.numPersons) <= 0 || s.text.equals("")) && Integer.parseInt(s.numPersons) != 0) {
                        surveyNoResponse = true;
                    } else {
                        s.answerSelected = Integer.parseInt(s.numPersons) > 0 ? s.surveyResponse.getAnswers(0) : s.surveyResponse.getAnswers(1);
                    }

                } else {
                    surveyNoResponse = true;
                }
            }
        }

        if (!surveyNoResponse) {
            for (SurveyAdapter.Survey s : adapter.getSurveys()) {
                SurveyProto.UserSurveyAnswer.Builder request = SurveyProto.UserSurveyAnswer.newBuilder();
                switch ((int) s.surveyResponse.getFormType()) {
                    case 1:// SI / NO
                        request.setSurveyId(s.surveyResponse.getId());
                        request.setSurveyAnswerId(s.answerSelected.getId());
                        break;
                    case 2:// SI + DATE / NO
                        request.setSurveyId(s.surveyResponse.getId());
                        request.setSurveyAnswerId(s.answerSelected.getId());
                        request.setValue(DateUtils.dateToString(DateUtils.stringToDate(s.date, "dd/MM/yyyy"), "yyyy-MM-dd"));
                        break;
                    case 3:// SI + TEXT / NO
                        request.setSurveyId(s.surveyResponse.getId());
                        request.setSurveyAnswerId(s.answerSelected.getId());
                        request.setValue(s.text);
                        break;
                    case 4:// SI + NÚMERO | EDATS / NO
                        if (!s.numPersons.equals("")) {
                            if (Integer.parseInt(s.numPersons) > 0 && !s.text.equals("") || Integer.parseInt(s.numPersons) == 0) {
                                request.setSurveyId(s.surveyResponse.getId());
                                request.setSurveyAnswerId(s.answerSelected.getId());
                                request.setValue(String.format(Locale.getDefault(), "%s|%s", s.numPersons, s.numPersons));
                                sendSurvey(request);
                            } else {
                                showError("", getResources().getString(R.string.survey_not_responeded));
                            }
//                    isLoop = true;
                        } else {
                            showError("", getResources().getString(R.string.survey_not_responeded));
                        }
                        //isLoop = true;
                        break;
                    case 5:// SI + DATE | TEXT / NO
                        request.setSurveyId(s.surveyResponse.getId());
                        request.setSurveyAnswerId(s.answerSelected.getId());
                        request.setValue(String.format(Locale.getDefault(), "%s|%s", s.date, s.text));
                        break;
                }
            }
            Gson gson = new Gson();
            String jsonText = gson.toJson(adapter.getSurveys());
            MySharedPreferences.createSurvey(this).putString(String.valueOf(survey.getId()), jsonText);
            adapter.clearList();
            if (survey.getAnswers(0).getNextSurveyId() == 0) {
                onBackPressed();
            } else {
                getNextSurvey(survey.getAnswers(0).getNextSurveyId());
            }
        }
    }

    private void sendSurvey(SurveyProto.UserSurveyAnswer.Builder builder) {
        GenericTask answerSurvey = new GenericTask(Endpoints.Surveys.USER_ANSWER, builder.build(), true, otcResponse -> {
            if (otcResponse.getStatus() == Shared.OTCStatus.SUCCESS) {
                MySharedPreferences.createSurvey(this).putBytes(String.valueOf(survey.getId()), ansSelected.toByteArray());
                if (ansSelected.getNextSurveyId() == 0) {
                    onBackPressed();
                } else {
                    getNextSurvey(ansSelected.getNextSurveyId());
                }
            }
        });
        answerSurvey.execute();
    }

    private void getNextSurvey(long surveyId) {
        GenericTask getDashboard = new GenericTask(String.format(Locale.getDefault(), "%s/%d", Endpoints.Surveys.ID, surveyId), null, true, otcResponse -> {
            if (otcResponse.getStatus() == Shared.OTCStatus.SUCCESS) {
                this.survey = otcResponse.getData().unpack(SurveyProto.Survey.class);
                setData(this.survey);
            }
            if (otcResponse.getStatus() == Shared.OTCStatus.INVALID_ITEM) onBackPressed();
        });
        getDashboard.execute();
    }

    private void updateSurveys() {
        ArrayList<SurveyAdapter.Survey> list = adapter.getSurveys();
        for (SurveyAdapter.Survey s:list ) {
            for (SurveyProto.Survey survey:surveys ) {
                if (survey.getId() == s.surveyResponse.getId()) {
                    s.surveyResponse = survey;
                }
            }
        }
        adapter.setList(list);
        adapter.notifyDataSetChanged();
    }

    public void callToDealer(View sender) {
        answerSurvey();
    }

    private void showError(String title, String message) {
        new AlertDialog.Builder(SurveyActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.close), (dialog, which) -> {
                    // Whatever...
                }).show();

    }
}
