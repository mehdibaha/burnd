package com.insa.burnd.network;

import android.app.ProgressDialog;
import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.insa.burnd.models.ApiResponse;
import com.insa.burnd.utils.SPManager;
import com.insa.burnd.utils.Utils;

import java.util.HashMap;
import java.util.Map;

import trikita.log.Log;

/* Class used to make ALL network requests to the API */
public class Connection {
    private Context ctx;
    private ResponseListener rListener;
    private Map<String, String> headers;
    private Map<String, String> params;
    private ProgressDialog dialog;
    private String url = "http://burnd.cles-facil.fr/index.php/";

    // Différentes surcharges (dialog personnalisé ou non)
    public Connection(Context ctx, ResponseListener rListener, String apiFunction) {
        this.ctx = ctx;
        this.rListener = rListener;
        this.url = url + apiFunction + "/"; // The last slash is mandatory
        this.setHeaders();
    }

    public Connection(Context ctx, ResponseListener rListener, String apiFunction, String loadingMessage) {
        this(ctx, rListener, apiFunction);
        setCustomMessage(loadingMessage);
    }

    public void execute(final String... arg0) {
        if(!Utils.isInternetAvailable(ctx)) {
            Utils.showToast(ctx, "Internet is not available.");
            Log.d("No internet.");
        }
        else {
            if (dialog != null) dialog.show();
            this.setParams(arg0);
            makeRequest();
        }
    }

    private void makeRequest() {
        // Main request to retrive generic object from network call
        GsonRequest<ApiResponse> gsonRequest = new GsonRequest<>(url,
                ApiResponse.class, headers, params, new Response.Listener<ApiResponse>() {
            @Override
            public void onResponse(ApiResponse response) {
                if (dialog != null) dialog.dismiss();
                if (rListener != null) rListener.requestCompleted(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (dialog != null) dialog.dismiss();
                Log.e(error.toString());
            }
        });

        // Setting tag to request (to cancel later for example)
        gsonRequest.setTag(ctx.getClass().toString());
        VolleySingleton.getInstance().addToRequestQueue(gsonRequest);
    }

    private void setParams(String[] arg0) {
        Map<String, String> params = new HashMap<>(arg0.length);
        for (int i = 0; i < arg0.length; i++)
            params.put("req_" + i, arg0[i]); // Convention de nommage pour la REST API : "req_i"
        this.params = params;
    }

    private void setHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        String userId = SPManager.load(ctx, "USER_ID");
        String accessToken = SPManager.load(ctx, "ACCESS_TOKEN");

        if(!TextUtils.isEmpty(userId) && !TextUtils.isEmpty(accessToken)) {
            headers.put("user_id", userId);
            headers.put("access_token", accessToken);
        }
        this.headers = headers;
    }

    /* Message disabled by default if no loading message */
    private void setCustomMessage(String loadingMessage) {
        this.dialog = new ProgressDialog(ctx);
        this.dialog.setMessage(loadingMessage);
    }

    public interface ResponseListener {
        void requestCompleted(ApiResponse response);
    }
}