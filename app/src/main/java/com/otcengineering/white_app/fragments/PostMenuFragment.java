package com.otcengineering.white_app.fragments;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.otc.alice.api.model.Community;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.serialization.pojo.OemDealerItem;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;

/**
 * Created by Luis on 22/01/2018.
 */

public class PostMenuFragment extends BaseFragment {

    private TextView btnSendReport, btnCancel;

    private Object post;

    public void setPostSelected(Object post) {
        this.post = post;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_menu_post, container, false);
        retrieveViews(v);
        setEvents();
        return v;
    }

    private void retrieveViews(View v) {
        btnSendReport = v.findViewById(R.id.menu_post_btnSendReport);
        btnCancel = v.findViewById(R.id.menu_post_btnCancel);
    }

    private void setEvents() {
        btnSendReport.setOnClickListener(view -> sendReport());

        btnCancel.setOnClickListener(view -> closeMenu());
    }

    private void closeMenu() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        }
    }

    private void sendReport() {
        if (ConnectionUtils.isOnline(getContext())) {
            SendReportTask sendReportTask = new SendReportTask();
            sendReportTask.execute();
        } else {
            ConnectionUtils.showOfflineToast();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class SendReportTask extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... params) {
            try {
                MySharedPreferences msp = MySharedPreferences.createLogin(getActivity());

                if (post instanceof OemDealerItem) {
                    Community.PostId.Builder builder = Community.PostId.newBuilder();
                    builder.setPostId(((OemDealerItem) post).getId());

                    Shared.OTCResponse response = ApiCaller.doCall(Endpoints.DEALER_REPORT, msp.getBytes("token"), builder.build(), Shared.OTCResponse.class);
                    return response.getStatusValue();
                } else if (post instanceof Community.Post) {
                    Community.PostReport.Builder builder = Community.PostReport.newBuilder();
                    builder.setPostId(((Community.Post) post).getPostId());

                    Shared.OTCResponse response = ApiCaller.doCall(Endpoints.REPORT_POST, msp.getBytes("token"), builder.build(), Shared.OTCResponse.class);
                    return response.getStatusValue();
                } else {
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result != null && result == Shared.OTCStatus.SUCCESS_VALUE) {
                showCustomDialog(R.string.send_report_correctly);
            } else {
                showCustomDialogError();
            }
            closeMenu();
        }
    }
}
