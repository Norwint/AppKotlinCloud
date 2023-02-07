package com.otc.alice.test;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.protobuf.InvalidProtocolBufferException;
import com.otc.alice.api.model.Shared;

import java.util.Map;

public class OTCResponseRequest<T> extends Request<T> {
    private final Map<String, String> headers;
    private final Response.Listener<T> listener;

    /**
     * Make a GET request and return a parsed object from OTCResponse.
     *
     * @param url     URL of the request to make
     * @param headers Map of request headers
     */
    public OTCResponseRequest(String url, Map<String, String> headers, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        this.headers = headers;
        this.listener = listener;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            Shared.OTCResponse resp = Shared.OTCResponse.parseFrom(response.data);
            switch (resp.getStatus()) {
                case SUCCESS:
                    return Response.success((T) resp, HttpHeaderParser.parseCacheHeaders(response));
                default:
                    return Response.error(new ParseError());
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            return Response.error(new ParseError(e));
        }
    }
}