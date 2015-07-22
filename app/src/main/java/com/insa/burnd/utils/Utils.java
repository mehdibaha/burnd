package com.insa.burnd.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.insa.burnd.R;

import trikita.log.Log;

/* Common practice class to handle various methods (conversions, string operations...) */
public class Utils {

    private Utils() {}

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
            Log.v("Is internet ? : " + String.valueOf(networkInfo.isConnected()));
        else
            Log.e("Null network");
        return networkInfo != null && networkInfo.isConnected();
    }

    public static String makeReadable(String s, int maxChars) {
        return s.substring(0,maxChars) + "..." +s.substring(s.length()-maxChars,s.length());
    }

    public static int getToolbarHeight(Context context) {
        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                new int[]{R.attr.actionBarSize});
        int toolbarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        return toolbarHeight;
    }
}
