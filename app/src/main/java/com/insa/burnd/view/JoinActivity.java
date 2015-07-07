package com.insa.burnd.view;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.insa.burnd.R;
import com.insa.burnd.controller.PartyAdapter;
import com.insa.burnd.models.User;
import com.insa.burnd.network.Connection;
import com.insa.burnd.network.SessionController;
import com.insa.burnd.services.GPSTracker;
import com.insa.burnd.utils.BaseActivity;
import com.insa.burnd.utils.Utils;
import com.insa.burnd.view.MainActivity.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import trikita.log.Log;

public class JoinActivity extends BaseActivity implements Connection.ResponseListener {
    private EditText etPartyName;
    private EditText etPartyPass;
    private TextInputLayout tilPartyName;
    private TextInputLayout tilPartyPass;
    private ListView partiesListView;

    private PartyAdapter adapter;
    private final JoinActivity activity = this;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        initToolbar();

        // Join Party Form
        etPartyName = (EditText) findViewById(R.id.edittext_join_party_name);
        etPartyPass = (EditText) findViewById(R.id.edittext_join_party_pass);
        etPartyPass.setTypeface(Typeface.DEFAULT);
        tilPartyName = (TextInputLayout) findViewById(R.id.til_join_party_name);
        tilPartyPass = (TextInputLayout) findViewById(R.id.til_join_party_pass);

        Button button1 = (Button) findViewById(R.id.button_join_join);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String partyName = etPartyName.getText().toString();
                String partyPass = etPartyPass.getText().toString();

                if (!TextUtils.isEmpty(partyName) && !TextUtils.isEmpty(partyPass)) {
                    new Connection(activity, activity, "joinparty", "Loading...").execute(partyName, partyPass);
                }
                else {
                    if(TextUtils.isEmpty(partyName))
                        tilPartyName.setError("Name can't be empty");
                    if(TextUtils.isEmpty(partyPass))
                        tilPartyPass.setError("Pass can't be empty");
                }
            }
        });

        partiesListView = (ListView) findViewById(R.id.parties_listView);
        partiesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                etPartyName.setText(adapter.getPartiesList().get(position));
                partiesListView.setVisibility(View.GONE);
            }
        });

        Button buttonGPS = (Button) findViewById(R.id.button_join_gps);
        buttonGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchGPS();
            }
        });

        setupSession();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_join);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        if(ab!=null)
            ab.setTitle("Join a party");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_join, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            Utils.showToast(this, "You logged out.");
            new SessionController(this).disconnectFB();
            startActivity(new Intent(this, LoginActivity.class));
            return true;
        }
        else if(id == R.id.action_create_party) {
            startActivity(new Intent(activity, CreateActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Search party buton
    private void searchGPS() {
        GPSTracker mGPSService = new GPSTracker(this);
        mGPSService.getLocation();

        if (!mGPSService.isLocationAvailable()) {
            // Here you can ask the user to try again, using return; for that
            Utils.showToast(this, "Your location is not available, please try again.");
            return;
            // Or you can uncomment the line // address = "Location not available" and continue.
        } else {

            // Getting location co-ordinates
            double latitude = mGPSService.getLatitude();
            double longitude = mGPSService.getLongitude();
            float accuracy = mGPSService.getAccuracy();
            Log.d("Latitude:" + latitude + " | Longitude: " + longitude + " | Accuracy: " + accuracy);

            //Utils.showToast(this, mGPSService.getLocationAddress());
            new Connection(activity, activity, "searchparty", "Searching...").execute(""+longitude,""+latitude);
        }

        // make sure you close the gps after using it. Save user's battery power
        mGPSService.closeGPS();
    }



    private void setupSession() {
        Session session = new SessionController(activity).getFacebookSession(); // Retrieving fb session
        if (session != null) {
            if (session.isOpened()) {
                final String accessToken = session.getAccessToken();
                com.facebook.Request.newMeRequest(Session.getActiveSession(), new com.facebook.Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        if (user != null) {
                            User myUser = new User(user, accessToken);
                            Log.d(myUser.toString());
                            if(!TextUtils.isEmpty(user.getName())) {
                                Utils.showToast(activity, "Welcome " + user.getName());
                            }
                            myUser.saveToMemory(activity);
                            new Connection(activity, activity, "createuser").execute(myUser.getName(), myUser.getGender());
                        }
                    }
                }).executeAsync();
            } else {
                Log.e("CLOSED_SESSION");
            }
        } else {
            Log.e("NULL_SESSION");
        }
    }

    @Override
    public void requestCompleted(String response) throws JSONException {
        JSONObject json = new JSONObject(response);
        String message = json.getString("message");
        boolean error = json.getBoolean("error");

        Log.d(response);
        Log.d(message);
        if (error) {
            if (message.equals("PARTY_NOT_FOUND")) {
                Utils.showToast(this, "Party not found.");
            }
            else if (message.equals("USER_FAILED")){
                Utils.showToast(this, "Access denied.");
                new SessionController(this).disconnectFB();
                startActivity(new Intent(this, LoginActivity.class));
                activity.finish();
            }

        } else {
            if (message.equals("USER_ALREADY_EXISTS")) {
                Log.d("User already exists.");
            }
            else if (message.equals("PARTY_FOUND")) {
                Utils.showToast(this, "Sending you to party.");
                startActivity(new Intent(this, MainActivity.class));
                activity.finish();
            }
            else if (message.equals("Search")) {
                String parties = json.getString("PartiesFound");
                Log.d("Party Search" + parties);

                ArrayList<String> partylist = new ArrayList<>();
                ArrayList<String> partylistLocation = new ArrayList<>();

                try {
                    // Locate the NodeList name
                    JSONArray jsonarray = json.getJSONArray("PartiesFound");
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject json2 = jsonarray.getJSONObject(i);

                        partylist.add(json2.optString("party"));
                        partylistLocation.add(json2.optString("location"));
                    }
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }

                adapter = new PartyAdapter(activity, partylist, partylistLocation);
                partiesListView.setAdapter(adapter);
                partiesListView.setVisibility(View.VISIBLE);

            }
        }
    }
}