package com.otcengineering.white_app.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.otc.alice.api.model.DashboardAndStatus;
import com.otc.alice.api.model.Shared;
import com.otc.alice.api.model.SurveyProto;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.adapter.PollAdapter;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.tasks.GenericTask;
import com.otcengineering.white_app.tasks.TypedTask;
import com.otcengineering.white_app.utils.interfaces.TypedCallback;

import java.util.ArrayList;
import java.util.Locale;

import javax.annotation.Nonnull;

public class PollFragment extends EventFragment {
    private RecyclerView mRecyclerView;
    private PollAdapter mAdapter;
    private ArrayList<SurveyProto.Survey> mSurveys;

    public PollFragment() {
        super("PollActivity");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_poll, container, false);

        getViews(view);
        setEvents();

        return view;
    }

    private void setEvents() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnPollRefreshListener(this::refreshData);
        mSurveys = new ArrayList<>();
        refreshData();
    }

    private void getPoll(int page) {
        TypedTask<SurveyProto.Surveys> surveysTypedTask = new TypedTask<>(String.format(Locale.US, Endpoints.Surveys.LIST, page), null, true, SurveyProto.Surveys.class, new TypedCallback<SurveyProto.Surveys>() {
            @Override
            public void onSuccess(@Nonnull SurveyProto.Surveys value) {
                mSurveys.addAll(value.getSurveysList());

                if (value.getPages() == value.getPage()) {
                    mAdapter.setPolls(mSurveys);
                    mAdapter.notifyDataSetChanged();
                } else {
                    getPoll(page + 1);
                }
            }

            @Override
            public void onError(@Nonnull Shared.OTCStatus status, @Nullable String message) {

            }
        });
        surveysTypedTask.execute();
    }

    public void refreshData() {
        getPoll(1);
    }

    private PollAdapter.Poll internalCreatePoll(String title, boolean yesno, @Nullable String ops) {
        PollAdapter.Poll poll = new PollAdapter.Poll();
        poll.title = title;
        poll.yesno = yesno;
        poll.options = ops;
        poll.desc = "";
        return poll;
    }

    private void getViews(View view) {
        mRecyclerView = view.findViewById(R.id.conditionTableView);
        mAdapter = new PollAdapter();
    }
}