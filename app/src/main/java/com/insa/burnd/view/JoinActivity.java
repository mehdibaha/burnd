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
import android.widget.EditText;
import android.widget.ListView;

import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.insa.burnd.R;
import com.insa.burnd.controller.PartyListAdapter;
import com.insa.burnd.models.ApiResponse;
import com.insa.burnd.models.User;
import com.insa.burnd.network.Connection;
import com.insa.burnd.network.SessionController;
import com.insa.burnd.services.GPSTracker;
import com.insa.burnd.utils.BaseActivity;
import com.insa.burnd.utils.Utils;
import com.insa.burnd.view.MainActivity.MainActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import trikita.log.Log;

public class JoinActivity extends BaseActivity implements Connection.ResponseListener {
    private final JoinActivity activity = this;
    private PartyListAdapter adapter;

    @Bind(R.id.parties_listView) ListView partiesListView;
    @Bind(R.id.edittext_join_party_name) EditText etPartyName;
    @Bind(R.id.edittext_join_party_pass) EditText etPartyPass;
    @Bind(R.id.til_join_party_name) TextInputLayout tilPartyName;
    @Bind(R.id.til_join_party_pass) TextInputLayout tilPartyPass;
    @Bind(R.id.toolbar_join) Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        ButterKnife.bind(activity);

        initToolbar();
        etPartyPass.setTypeface(Typeface.DEFAULT);
        setupSession();
    }

    @OnClick(R.id.button_join_join)
    public void joinParty() {
        String partyName = etPartyName.getText().toString();
        String partyPass = etPartyPass.getText().toString();

        if (!TextUtils.isEmpty(partyName) && !TextUtils.isEmpty(partyPass)) {
            Log.d("partyname" + partyName + "partypass" + partyPass);
            new Connection(activity, activity, "joinparty", "Searching for party...").execute(partyName, partyPass);
        }
        else {
            if(TextUtils.isEmpty(partyName))
                tilPartyName.setError("Name can't be empty");
            if(TextUtils.isEmpty(partyPass))
                tilPartyPass.setError("Pass can't be empty");
        }
    }

    @OnItemClick(R.id.parties_listView)
    public void showParties(int position) {
        etPartyName.setText(adapter.getPartyList().get(position).getName());
        partiesListView.setVisibility(View.GONE);
    }

    private void initToolbar() {
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

    // Search party buton
    @OnClick(R.id.button_join_gps)
    public void searchGPS() {
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

            //Utils.showToast(this, mGPSService.getLocationAddress());
            new Connection(activity, activity, "searchparty", "Searching near parties...").execute(""+longitude,""+latitude);
        }

        // make sure you close the gps after using it. Save user's battery power
        mGPSService.closeGPS();
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

    private void setupSession() {
        Session session = new SessionController(activity).getFacebookSession(); // Retrieving fb session
        if (session != null) {
            if (session.isOpened()) {
                final String accessToken = session.getAccessToken();
                com.facebook.Request.newMeRequest(Session.getActiveSession(), new com.facebook.Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        if (user != null) {
                            User myUser = new User.UserBuilder()
                                    .setName(user.getName())
                                    .setAccessToken(accessToken)
                                    .setGender(user.asMap().get("gender").toString())
                                    .setUserId(user.getId())
                                    .build();
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
    public void requestCompleted(ApiResponse ar) {
        String message = ar.getMessage();
        boolean error = ar.isError();
        Log.d(ar.toString());

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
            else if (message.equals("PARTIES_FOUND")) {
                Log.d(ar.getPartyList().toString());
                adapter = new PartyListAdapter(activity, ar.getPartyList());
                partiesListView.setAdapter(adapter);
                partiesListView.setVisibility(View.VISIBLE);
            }
        }
    }
}