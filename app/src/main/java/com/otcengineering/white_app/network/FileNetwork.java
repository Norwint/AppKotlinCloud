package com.otcengineering.white_app.network;

import com.google.protobuf.ByteString;
import com.otc.alice.api.model.FileProto;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.MyApp;
import com.otcengineering.white_app.interfaces.Callback;
import com.otcengineering.white_app.tasks.GetImageTask;
import com.otcengineering.white_app.tasks.TypedTask;
import com.otcengineering.white_app.utils.images.ImageUtils;
import com.otcengineering.white_app.utils.interfaces.TypedCallback;

import java.io.File;
import java.io.IOException;

public class FileNetwork {
    public static void get(long id, Callback<byte[]> onResponse) {
        GetImageTask git = new GetImageTask(id) {
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                byte[] file = ImageUtils.getImageFromCache(MyApp.getContext(), id);
                onResponse.onSuccess(file);
            }
        };
        git.execute();
    }

    public static void uploadFile(File f, TypedCallback<Shared.OTCResponse> onResponse) {
        try {
            byte[] b = com.otcengineering.white_app.utils.Utils.readFile(f);
            FileProto.UploadFile uf = FileProto.UploadFile.newBuilder().setFileName(f.getName()).setFileData(ByteString.copyFrom(b)).build();
            TypedTask<Shared.OTCResponse> gt = new TypedTask<>(Endpoints.FILE_UPLOAD, uf, true, Shared.OTCResponse.class, onResponse);
            gt.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
