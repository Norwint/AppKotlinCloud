package com.otc.alice.test;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.util.Map;

public class OTCImagesResponseRequest extends Request<byte[]> {
    private Map<String, String> headers;
    private final Response.Listener<byte[]> listener;

    public Map<String, String> responseHeaders;

    public OTCImagesResponseRequest(String url, Map<String, String> headers, Response.Listener<byte[]> listener, Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        setShouldCache(false);
        this.headers = headers;
        this.listener = listener;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    protected void deliverResponse(byte[] response) {
        listener.onResponse(response);
    }

    @Override
    protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
        //Initialise local responseHeaders map with response headers received
        responseHeaders = response.headers;

        //Pass the response data here
        return Response.success(response.data, HttpHeaderParser.parseCacheHeaders(response));
    }
}