package com.otcengineering.white_app.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.otcengineering.white_app.utils.Utils;
import com.otcengineering.white_app.utils.images.ImageUtils;

/**
 * Created by cenci7
 */

public class GetImageTask extends AsyncTask<Context, Object, String> {

    private static final String TAG = "GetImageTask";

    private long imageId;

    public GetImageTask(long imageId) {
        this.imageId = imageId;
    }

    @Override
    protected String doInBackground(Context... params) {
        try {
            Context context = params[0];
            byte[] imageBytes = Utils.downloadImageSync(context, imageId);
            ImageUtils.saveImageFileInCache(context, imageBytes, imageId);

            return ImageUtils.getImageFilePathInCache(context, imageId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
