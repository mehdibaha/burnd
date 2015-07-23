package com.insa.burnd.network;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import trikita.log.Log;

/**
 * Volley GET request which parses JSON server response into Java object.
 */
public class GsonRequest<T> extends Request<T> {
    private final Gson gson;
    private final Class<T> responseClass;
    private final Map<String, String> headers;
    private final Map<String, String> params;
    private final Listener<T> listener;

    /**
     * Make a GET request and return a parsed object from JSON.
     *
     * @param url URL of the request to make
     * @param headers Map of request headers
     */
    public GsonRequest(String url, Class<T> responseClass, Map<String, String> headers,
                       Map<String, String> params, Listener<T> listener, ErrorListener errorListener) {
        super(Method.POST, url, errorListener);
        this.setRetryPolicy(getCustomRetryPolicy());
        this.responseClass = responseClass;
        this.gson = new GsonBuilder().create();
        this.headers = headers;
        this.params = params;
        this.listener = listener;
    }

    private RetryPolicy getCustomRetryPolicy() {
        return new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 3, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Log.d(headers.toString());
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    public Map<String, String> getParams() throws AuthFailureError {
        Log.d(params.toString());
        return params != null ? params : super.getParams();
    }

    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(
                    response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            return Response.success(
                    gson.fromJson(json, responseClass),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        }
    }
}