package com.insa.burnd.view;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;

import com.insa.burnd.R;
import com.insa.burnd.network.Connexion;
import com.insa.burnd.network.SessionController;
import com.insa.burnd.services.GPSTracker;
import com.insa.burnd.utils.BaseActivity;
import com.insa.burnd.utils.Utils;
import com.insa.burnd.view.MainActivity.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Calendar;

public class CreateActivity extends BaseActivity implements Connexion.ResponseListener {
    private final CreateActivity activity = this;
    private static String TAG = "BURND-CreateActivity";
    private Toolbar toolbar;

    private EditText etPartyName;
    private EditText etPartyPass;
    private TextInputLayout tilPartyName;
    private TextInputLayout tilPartyPass;
    private Button buttonTime;
    private EditText etLocation;

    private double latitude = 0;
    private double longitude = 0;

    private Calendar now;
    private int hours;
    private int minutes;

    final private int MAX_DURATION_PARTY = 12;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        initToolbar();
        etPartyName = (EditText) findViewById(R.id.edittext_create_party_name);
        etPartyPass = (EditText) findViewById(R.id.edittext_create_party_pass);
        etPartyPass.setTypeface(Typeface.DEFAULT);
        etLocation = (EditText) findViewById(R.id.edittext_location);
        tilPartyName = (TextInputLayout) findViewById(R.id.til_create_party_name);
        tilPartyPass = (TextInputLayout) findViewById(R.id.til_create_party_pass);
        buttonTime = (Button) findViewById(R.id.time);

        setButtonToNow();

        Button buttonCreate = (Button) findViewById(R.id.button_create);
        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String partyName = etPartyName.getText().toString();
                String partyPass = etPartyPass.getText().toString();
                String partyTime = buttonTime.getText().toString();
                String location = etLocation.getText().toString();

                if (!TextUtils.isEmpty(partyName) && !TextUtils.isEmpty(partyPass)) {
                    try {
                        if (validTime(partyTime, MAX_DURATION_PARTY))
                            new Connexion(activity, activity, "createparty").execute(partyName, partyPass, partyTime, location, "" + longitude, "" + latitude);
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
        });

        buttonTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimer();
            }
        });

        Button buttonLocaton = (Button) findViewById(R.id.button_location);
        buttonLocaton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GPSLocateParty();
            }
        });
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar_create);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        if(ab!=null) {
            ab.setTitle("Create a party");
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setButtonToNow() {
        now = Calendar.getInstance();
        hours = now.get(Calendar.HOUR_OF_DAY);
        minutes = now.get(Calendar.MINUTE);
        buttonTime.setText(hours + ":" + minutes);
    }

    private void GPSLocateParty() {
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

    private void showTimer() {
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
    public void requestCompleted(String response) throws JSONException {
        JSONObject json = new JSONObject(response);
        String message = json.getString("message");
        boolean error = json.getBoolean("error");

        Log.d(TAG, message);

        if (!error) {
            Utils.showToast(this, "Party created.");
            startActivity(new Intent(this, MainActivity.class));
            activity.finish();
        } else if (message.equals("PARTY_FAILED")) {
            Utils.showToast(this, "Post failed.");
        } else {
            Utils.showToast(this, "Access denied.");
            new SessionController(this).disconnectFB();
            startActivity(new Intent(this, LoginActivity.class));
            activity.finish();
        }
    }
}
