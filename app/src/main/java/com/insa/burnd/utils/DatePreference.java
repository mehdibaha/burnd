package com.insa.burnd.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;

import com.insa.burnd.R;
import com.insa.burnd.models.ApiResponse;
import com.insa.burnd.network.Connection;
import com.insa.burnd.view.MainActivity.MainActivity;
import com.insa.burnd.view.SplashActivity;

import java.util.Calendar;

/* Class to handle Dates settings */
public class DatePreference extends DialogPreference implements Connection.ResponseListener {
    private String curVal;
    private String defVal;
    private DatePicker dp;
    private Context ctx;
    private static String TAG = "BURND-DatePreference";

    public DatePreference(Context ctx, AttributeSet atts) {
        super(ctx, atts);
        this.ctx=ctx;
        setDialogLayoutResource(R.layout.dp_layout);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        setDialogIcon(null);
        Calendar cal = Calendar.getInstance();
        defVal = Integer.toString(cal.get(Calendar.DAY_OF_MONTH)) + "-" + Integer.toString(cal.get(Calendar.MONTH)) + "-" + Integer.toString(cal.get(Calendar.YEAR));
    }

    @Override
    protected void onBindDialogView(@NonNull View v) {
        super.onBindDialogView(v);
        dp = (DatePicker) v.findViewById(R.id.dp);
        if (Build.VERSION.SDK_INT >= 11) {
            dp.setCalendarViewShown(false);
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            curVal = this.getPersistedString(defVal);
        } else {
            curVal = defVal;
            persistString(curVal);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            String persVal = SPManager.load(ctx, "date");
            updateVal();
            if (!persVal.equals(curVal)) { // Only change if birthday is different
                persistString(curVal);
                new Connection(ctx, this, "insertage", "Updating age...").execute(SPManager.load(ctx, "date"));
            }
        }
    }

    private void updateVal(){
        String day = Integer.toString(dp.getDayOfMonth());
        if(day.length() < 2){
            day = "0"+day;
        }
        String month = Integer.toString(dp.getMonth()+1);
        if(month.length() < 2){
            month = "0"+month;
        }
        String year = Integer.toString(dp.getYear());
        curVal = day+"-"+month+"-"+year;

        Preference p = findPreferenceInHierarchy("date");
        p.setSummary(curVal);
    }

    @Override
    public void requestCompleted(ApiResponse sr) {
        String message = sr.getMessage();
        boolean error = sr.isError();

        if (!error)
                Utils.showToast(ctx, "Age updated.");
        else if (message.equals("AGE_NOT_INSERTED"))
            Utils.showToast(ctx, "Age failed to update.");
        else {
            Utils.showToast(ctx, "Access denied.");
            ctx.startActivity(new Intent(ctx, SplashActivity.class));
            ((MainActivity) ctx).finish();
        }
    }
}
