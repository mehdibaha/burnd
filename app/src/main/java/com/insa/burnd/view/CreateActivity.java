package com.insa.burnd.view;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;

import com.insa.burnd.R;
import com.insa.burnd.models.ApiResponse;
import com.insa.burnd.network.Connection;
import com.insa.burnd.network.SessionController;
import com.insa.burnd.services.GPSTracker;
import com.insa.burnd.utils.BaseActivity;
import com.insa.burnd.utils.Utils;
import com.insa.burnd.view.MainActivity.MainActivity;

import java.text.ParseException;
import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import trikita.log.Log;

public class CreateActivity extends BaseActivity implements Connection.ResponseListener {
    private final CreateActivity activity = this;
    final private int MAX_DURATION_PARTY = 12;
    @Bind(R.id.edittext_create_party_name) EditText etPartyName;
    @Bind(R.id.edittext_create_party_pass) EditText etPartyPass;
    @Bind(R.id.til_create_party_name) TextInputLayout tilPartyName;
    @Bind(R.id.til_create_party_pass) TextInputLayout tilPartyPass;
    @Bind(R.id.edittext_location) EditText etLocation;
    @Bind(R.id.toolbar_create) Toolbar toolbar;
    @Bind(R.id.time) Button buttonTime;
    private double latitude = 0;
    private double longitude = 0;
    private Calendar now ;
    private int hours;
    private int minutes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        ButterKnife.bind(activity);

        initToolbar();
        setButtonToDefaultEndTime();
    }

    @OnClick(R.id.button_create)
    public void createParty() {
        String partyName = etPartyName.getText().toString();
        String partyPass = etPartyPass.getText().toString();
        String partyTime = buttonTime.getText().toString();
        String location = etLocation.getText().toString();

        if (!TextUtils.isEmpty(partyName) && !TextUtils.isEmpty(partyPass)) {
            try {
                if (validTime(partyTime, MAX_DURATION_PARTY))
                    new Connection(activity, activity, "createparty", "Creating party...").execute(partyName, partyPass, partyTime, location, "" + longitude, "" + latitude);
                else
                    Utils.showToast(activity, "Party can't be more than 12 hours from now.");
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            if(TextUtils.isEmpty(partyName))
                tilPartyName.setError("Name can't be empty");
            if(TextUtils.isEmpty(partyPass))
                tilPartyPass.setError("Pass can't be empty");
        }
    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        if(ab!=null) {
            ab.setTitle("Create a party");
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setButtonToDefaultEndTime() {
        now = Calendar.getInstance();
        hours = now.get(Calendar.HOUR_OF_DAY)+8;
        if (hours>24){
            hours=8-(24-now.get(Calendar.HOUR_OF_DAY));
        }
        minutes = now.get(Calendar.MINUTE);
        buttonTime.setText(hours + ":" + minutes);
    }

    @OnClick(R.id.button_location)
    public void GPSLocateParty() {
        GPSTracker mGPSService = new GPSTracker(this);
        mGPSService.getLocation();

        if (!mGPSService.isLocationAvailable()) {
            // Here you can ask the user to try again, using return; for that
            Utils.showToast(this, "Your location is not available, please try again.");
            return;
            // Or you can continue without getting the location, remove the return; above and uncomment the line given below
            // address = "Location not available";
        } else {
            // Getting location co-ordinates
            latitude = mGPSService.getLatitude();
            longitude = mGPSService.getLongitude();
            //Utils.showToast(this, "Latitude:" + latitude + " | Longitude: " + longitude);

            String address = mGPSService.getLocationAddress();
            if(!TextUtils.isEmpty(address)) {
                etLocation.setText(address);
                Utils.showToast(this, address);
            }
        }

        // make sure you close the gps after using it. Save user's battery power
         mGPSService.closeGPS();
    }

    @OnClick(R.id.time)
    public void showTimer() {
        TimePickerDialog mTimePicker = new TimePickerDialog(activity, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                String timeSelected = selectedHour + ":" + selectedMinute;
                buttonTime.setText(timeSelected);
        }}, hours, minutes, true); // 24 hour time view

        mTimePicker.show();
    }

    // Returns if time selected of party is more than a duration from now
    private boolean validTime(String time, int duration) throws ParseException {
        String[] parts = time.split(":");
        int hourSelected = Integer.parseInt(parts[0]);

        now = Calendar.getInstance();// Getting exact current time
        hours = now.get(Calendar.HOUR_OF_DAY);

        int difference = hourSelected - hours;
        if(difference<0) {
            int remaining = 24 + difference;
            return remaining <= duration;
        } else {
            return difference <= duration;
        }
    }

    @Override
    public void requestCompleted(ApiResponse ar) {
        String message = ar.getMessage();
        boolean error = ar.isError();
        Log.d(ar.toString());

        if (!error) {
            Utils.showToast(this, "Party created.");
            startActivity(new Intent(this, MainActivity.class));
            activity.finish();
        } else if (message.equals("PARTY_FAILED")) {
            Utils.showToast(this, "Party creation failed.");
        } else {
            Utils.showToast(this, "Access denied.");
            new SessionController(this).disconnectFB();
            startActivity(new Intent(this, LoginActivity.class));
            activity.finish();
        }
    }
}
