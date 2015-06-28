package com.insa.burnd.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

/* Common practice class to handle various methods (conversions, string operations...) */
public class Utils {

    private static final String TAG = "BURND-Utils" ;

    public Utils() {

    }

    public static float pxToDp(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static int dpToPx(final Context context, final float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    public static void showToast(final Context context, String msg) {
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    public static boolean isInternetAvailable(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        // if network is NOT available networkInfo will be null, or check if we are connected
        if(networkInfo != null)
            Log.d(TAG, "User connection state : " + String.valueOf(networkInfo.isConnected()));
        else
            Log.d(TAG, "Null network");
        return networkInfo != null && networkInfo.isConnected();
    }

    /* Concatenates json arrays at a specific index */
    public static JSONArray concatArray(JSONArray arr1, JSONArray arr2, int mergeIndex)
            throws JSONException {
        JSONArray result = new JSONArray();
        for (int i = 0; i < arr1.length(); i++) {
            result.put(arr1.get(i));
        }
        for (int i = mergeIndex; i < arr2.length(); i++) {
            result.put(arr2.get(i));
        }
        return result;
    }

    public static String makeReadable(String s, int maxChars) {
        return s.substring(0,maxChars) + "..." +s.substring(s.length()-maxChars,s.length());
    }

}
