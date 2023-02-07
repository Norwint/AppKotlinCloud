package com.otcengineering.white_app.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.otc.alice.api.model.Wallet;
import com.otcengineering.white_app.BuildConfig;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.activities.PdfActivity;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.PrefsManager;
import com.otcengineering.white_app.utils.images.ImageUtils;

import java.io.File;


public class ManualFragment extends BaseFragment {

    private Button btnDownload;
    private TextView txtNewVersion;

    private long pdfFileId;
    private boolean forceDownload;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_manual, container, false);
        retrieveViews(v);
        setEvents();
        getManualVersion();
        return v;
    }

    private void retrieveViews(View v) {
        btnDownload = v.findViewById(R.id.manual_btnDownload);
        txtNewVersion = v.findViewById(R.id.manual_txtNewVersion);
    }

    private void setEvents() {
        btnDownload.setOnClickListener(view -> openPdf());
    }

    private void getManualVersion() {
        if (ConnectionUtils.isOnline(getContext())) {
            GetManualVersionTask getManualVersionTask = new GetManualVersionTask();
            getManualVersionTask.execute();
        } else {
            ConnectionUtils.showOfflineToast();
        }
    }

    private void openPdf() {
        Intent intent = new Intent(getActivity(), PdfActivity.class);
        intent.putExtra(Constants.Extras.PDF, pdfFileId);
        intent.putExtra(Constants.Extras.PDF_FORCE_DOWNLOAD, forceDownload);
        startActivity(intent);
    }

    @SuppressLint("StaticFieldLeak")
    public class GetManualVersionTask extends AsyncTask<Object, Void, Wallet.Manual> {
        private ProgressDialog progressDialog = new ProgressDialog(getActivity());

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.show();
        }

        @Override
        protected Wallet.Manual doInBackground(Object... params) {
            try {
                MySharedPreferences msp = MySharedPreferences.createLogin(getActivity());

                return ApiCaller.doCall(Endpoints.MANUAL, msp.getBytes("token"), null, Wallet.Manual.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Wallet.Manual response) {
            super.onPostExecute(response);
            progressDialog.dismiss();
            if (response != null) {
                pdfFileId = response.getFile();
                try {
                    String versionString = response.getVersion();
                    int currentManualVersion = PrefsManager.getInstance().getManualVersion(getContext());
                    int serverManualVersion = convertVersionToInt(versionString);
                    File manual = ImageUtils.getManualFileName(getActivity());
                    boolean manualDoesntExist = manual == null || !manual.exists();
                    if (serverManualVersion > currentManualVersion || manualDoesntExist) {
                        forceDownload = true;
                        txtNewVersion.setVisibility(View.VISIBLE);
                        PrefsManager.getInstance().saveManualVersion(serverManualVersion, getContext());
                    } else {
                        txtNewVersion.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private int convertVersionToInt(String version) {
        version = version.replace(".", "");
        return Integer.parseInt(version);
    }

}
