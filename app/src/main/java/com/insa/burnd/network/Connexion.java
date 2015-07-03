package com.insa.burnd.network;

import android.app.ProgressDialog;
import android.content.Context;
import android.text.TextUtils;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.insa.burnd.utils.SPManager;
import com.insa.burnd.utils.Utils;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

import trikita.log.Log;

/* Class used to make ALL network requests to the API */
public class Connexion {
    private ResponseListener rListener;
    private Context ctx;
    private ProgressDialog dialog;
    private String loadingMessage;
    private String url = "http://burnd.cles-facil.fr/index.php/";

    // Différentes surcharges (dialog personnalisé ou non)
    public Connexion(Context ctx, ResponseListener rListener, String apiFunction) {
        this.ctx = ctx;
        this.rListener = rListener;
        this.url = url + apiFunction + "/"; // Important: The slash after the apiFunction
        this.loadingMessage = "Loading...";
    }

    // Surcharge du constructeur
    public Connexion(Context ctx, ResponseListener rListener, String apiFunction, String loadingMessage) {
        this(ctx, rListener, apiFunction); // Classe.
        this.loadingMessage = loadingMessage;
        this.dialog = new ProgressDialog(ctx);
    }

    public void execute(final String... arg0) {
        if(!Utils.isInternetAvailable(ctx)) {
            Utils.showToast(ctx, "Internet is not available.");
            Log.d("No internet.");
        }
        else {
            if (dialog != null) {
                dialog.setMessage(loadingMessage);
                dialog.show();
            }

            // String request
            StringRequest stringRequest = new StringRequest(Request.Method.POST,
                    url, new com.android.volley.Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    if (dialog != null) dialog.dismiss();
                    try {
                        rListener.requestCompleted(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new com.android.volley.Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    if (dialog != null) dialog.dismiss();
                    Log.e("Connexion", error.toString());
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    for (int i = 0; i < arg0.length; i++)
                        params.put("req_" + i, arg0[i]); // Convention de nommage pour la REST API : "req_i"
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("Content-Type", "application/x-www-form-urlencoded");

                    String userId = SPManager.load(ctx, "USER_ID");
                    String accessToken = SPManager.load(ctx, "ACCESS_TOKEN");

                    if(!TextUtils.isEmpty(userId) && !TextUtils.isEmpty(accessToken)) {
                        Log.d(userId + "|" + Utils.makeReadable(accessToken, 5));
                        params.put("user_id", userId);
                        params.put("access_token", accessToken);
                    }

                    return params;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance().addToRequestQueue(stringRequest);
        }
    }

    public interface ResponseListener {
        void requestCompleted(String response) throws JSONException;
    }
}