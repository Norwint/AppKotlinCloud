package com.otc.alice.test;

import android.content.Context;

import com.android.volley.Response;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.otc.alice.api.model.Shared.OTCResponse;
import com.otc.alice.api.model.Shared.OTCStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.otcengineering.white_app.network.Endpoints.URL_BASE;

/**
 * @author fbarcons@otcengineering.com
 */
public class HttpCaller {

    private static String TAG = "Volley";
    public static HttpCallerCallback httpCallerCallback;
    public static HttpImagesCallerCallback httpImagesCallerCallback;

    private static Map<String, String> setHeader(String token) {
        Map<String, String> header = new HashMap<>();
        //authorization
        header.put("Authorization", "A1 " + token);
        //protobuf
        header.put("Charset", "UTF-8");
        header.put("Content-Type", "application/x-protobuf");
        header.put("Accept", "application/x-protobuf");

        return header;
    }

    private static Map<String, String> setImageHeader(String token) {
        Map<String, String> header = new HashMap<>();
        //authorization
        header.put("Authorization", "A1 " + token);

        return header;
    }

    public static <T extends Message> T doCall(Context context, String uri, Class<T> response) {
        return doCall(context, uri, "public_access:0", response);
    }

    public static <T extends Message> T doCall(Context context, String uri, String token, Class<T> response) {
        return doCall(context, URL_BASE, uri, token, response);
    }

    public static <T extends Message> T doCall(Context context, String url, String uri, String token, Class<T> response) {
        T returnValue = null;

        /*checkClient();*/

        OTCResponseRequest otcResponseRequest = new OTCResponseRequest(url + uri, setHeader(token),
                (Response.Listener<OTCResponse>) response1 -> {
                    // TODO: Handle ok
                    switch (response1.getStatus()) {
                        case SUCCESS:
                            if (response1.hasData()) {
                                try {
                                    T returnVal = response1.getData().unpack(response);
                                    httpCallerCallback.HttpCallerSuccess(returnVal);
                                } catch (InvalidProtocolBufferException e) {
                                    e.printStackTrace();
                                    httpCallerCallback.HttpCallerError();
                                }
                            }
                            break;
                        default:
                            httpCallerCallback.HttpCallerError();
                            break;
                    }
                },
                error -> {
                    // TODO: Handle error
                    httpCallerCallback.HttpCallerError();
                });
        VolleyController.getInstance(context).addToQueue(otcResponseRequest);

        return returnValue;
    }

    public static byte[] getImage(Context context, String uri, String token) {
        ArrayList<Byte> valRet = new ArrayList<>();
        AtomicBoolean done = new AtomicBoolean(false);

        OTCImagesResponseRequest otcImagesResponseRequest = new OTCImagesResponseRequest(URL_BASE + uri, setImageHeader(token),
                response -> {
                    // TODO handle the response
                    try {
                        if (response != null) {
                            for (byte b : response) {
                                valRet.add(b);
                            }
                        }
                        done.set(true);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        //Log.d("KEY_ERROR", "UNABLE TO DOWNLOAD FILE");
                        e.printStackTrace();
                        //httpImagesCallerCallback.HttpCallerError();
                    }
                }, error -> {
            // TODO handle the
            done.set(true);
            error.printStackTrace();
            //httpImagesCallerCallback.HttpCallerError();
        });
        VolleyController.getInstance(context).addToQueue(otcImagesResponseRequest);
        while (!done.get()) {

        }
        byte[] img = new byte[valRet.size()];
        for (int i = 0; i < valRet.size(); ++i) {
            img[i] = valRet.get(i);
        }
        return img;
    }

    public static class OTCException extends Exception {
        OTCStatus status;

        public OTCStatus getStatus() {
            return status;
        }

        @Override
        public String toString() {
            return getMessage();
        }

        @Override
        public String getMessage() {
            String message = (status != null ? "status=" + status.name() + "\n" : "");
            if (super.getMessage() != null && !super.getMessage().isEmpty()) {
                message += "message=" + super.getMessage();
            }

            return message;
        }

    }

    public interface HttpCallerCallback<T extends Message> {
        void HttpCallerSuccess(T returnVal);

        void HttpCallerError();
    }

    public interface HttpImagesCallerCallback {
        void HttpCallerImageSuccess(Byte[] image);

        void HttpCallerError();
    }
}
