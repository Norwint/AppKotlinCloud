package com.otcengineering.white_app.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.components.TitleBar;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.images.ImageUtils;
import com.github.barteksc.pdfviewer.PDFView;
import com.otcengineering.white_app.network.utils.ApiCaller;

import java.io.File;

public class PdfActivity extends BaseActivity {

    private TitleBar titleBar;
    private PDFView pdfView;

    private long pdfFileId;
    private boolean forceDownload;

    public PdfActivity() {
        super("PdfActivity");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);
        retrieveViews();
        retrieveExtras();
        setEvents();
        downloadPdfIfNecessary();
    }

    private void retrieveViews() {
        titleBar = findViewById(R.id.pdf_titleBar);
        pdfView = findViewById(R.id.pdf_pdfView);
    }

    private void retrieveExtras() {
        if (getIntent() != null && getIntent().getExtras() != null) {
            pdfFileId = getIntent().getExtras().getLong(Constants.Extras.PDF, 0);
            forceDownload = getIntent().getExtras().getBoolean(Constants.Extras.PDF_FORCE_DOWNLOAD, true);
        }
    }

    private void setEvents() {
        titleBar.setListener(new TitleBar.TitleBarListener() {
            @Override
            public void onLeftClick() {
                onBackPressed();
            }

            @Override
            public void onRight1Click() {

            }

            @Override
            public void onRight2Click() {

            }
        });
    }

    private void downloadPdfIfNecessary() {
        File manual = ImageUtils.getManualFileName(this);
        boolean manualDoesntExist = manual == null || !manual.exists();
        if ((pdfFileId != 0 && forceDownload) || manualDoesntExist) {
            GetPdfTask getPdfTask = new GetPdfTask();
            getPdfTask.execute();
        } else {
            showPdf();
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class GetPdfTask extends AsyncTask<Context, Object, byte[]> {
        private ProgressDialog progressDialog = new ProgressDialog(PdfActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.show();
        }

        @Override
        protected byte[] doInBackground(Context... params) {
            try {
                MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());
                String url = Endpoints.FILE_GET + pdfFileId;

                return ApiCaller.getImage(url, msp.getString("token"));
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            progressDialog.dismiss();
            if (bytes != null) {
                ImageUtils.savePdfFile(PdfActivity.this, bytes);
                showPdf();
            } else {
                Toast.makeText(PdfActivity.this, R.string.error_default, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showPdf() {
        File manual = ImageUtils.getManualFileName(this);
        pdfView.fromFile(manual)
                .onError(t -> showErrorAndClose())
                .load();
    }

    private void showErrorAndClose() {
        if (!com.otcengineering.white_app.utils.Utils.isActivityFinish(this)) {
            Toast.makeText(this, R.string.error_default, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

}
