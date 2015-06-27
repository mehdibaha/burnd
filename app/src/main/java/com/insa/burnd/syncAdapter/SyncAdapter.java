package com.insa.burnd.syncAdapter;
//Les autres classes de ce package sont récupérées sur le tutoriel android.

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.insa.burnd.R;
import com.insa.burnd.network.Connexion;
import com.insa.burnd.view.CompassActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter implements Connexion.ResponseListener{
    // Global variables
    // Define a variable to contain a content resolver instance
    ContentResolver mContentResolver;
    private static boolean checkedMatch = false;
    private double[] pos = new double[2];
    /**
     * Set up the sync adapter
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();
    }

    /**
     * Set up the sync adapter. This form of the
     * constructor maintains compatibility with Android 3.0
     * and later platform versions
     */
    public SyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();
    }

   //Ici, on execute les syncs.
    @Override
    public void onPerformSync(
            Account account,
            Bundle extras,
            String authority,
            ContentProviderClient provider,
            SyncResult syncResult) {

        if(extras.getString("reqID") != null){

            switch(extras.getString("reqID")){
                case "matchSync" :{
                    matchSync();
                }
                case "gpsSync" :{
                    gpsSync();
                }
            }
        }
    }

    private void matchSync(){
        new Connexion(getContext(), this, "checkmatch").execute();
        Log.d("check","sync");
    }

    private void gpsSync(){
        new Connexion(getContext(), this, "updatelocation").execute(Double.toString(pos[0]),Double.toString(pos[1]));
        CompassActivity ca = CompassActivity.getInstance();
        if(ca != null){
           pos = ca.updateLocation();
        }
        Log.d("gps", "sync");
    }

    public static void killMatch(){
        checkedMatch = false;
    }

    //Les 2 fonctions suivantes ont été ajoutées afin de permettre de gérer les syncs, trouver un sync par exemple.
    public static boolean checkSyncs(Account acc, String auth, String reqID){
        for(int i =0; i<ContentResolver.getPeriodicSyncs(acc,auth).size();i++){
            if(ContentResolver.getPeriodicSyncs(acc,auth).get(i).extras.getString("reqID") != null){
                if(ContentResolver.getPeriodicSyncs(acc,auth).get(i).extras.getString("reqID").equals(reqID)){
                    return true;
                }
            }
        }
        return false;
    }

    public static Bundle getBundle(Account acc, String auth, String reqID){
        for(int i =0; i<ContentResolver.getPeriodicSyncs(acc,auth).size();i++){
            if(ContentResolver.getPeriodicSyncs(acc,auth).get(i).extras.getString("reqID") != null){
                if(ContentResolver.getPeriodicSyncs(acc,auth).get(i).extras.getString("reqID").equals(reqID)){
                    return ContentResolver.getPeriodicSyncs(acc,auth).get(i).extras;
                }
            }
        }
        return new Bundle();
    }

    @Override
    public void requestCompleted(String response) throws JSONException {
        JSONObject json = new JSONObject(response);
        String id = json.optString("id");
        Log.d("checkID", id);
        switch(id){
            //We discriminate against the reponse id (sync type) to get the appropriate info (gps, match, etc...)
            case "updatelocation":{
                Log.d("check", "inUpdateLocation");
                JSONArray jArray = json.optJSONArray("location");
                CompassActivity ca = CompassActivity.getInstance();
                if(ca != null){
                    //ca.updateYou(Double.parseDouble(jArray.getString(0)), Double.parseDouble(jArray.getString(1)));
                }
            }
            case "checkmatch":{
                Log.d("check", "in");
                String message = json.optString("match");
                if(!checkedMatch && message.equals("Match!")){
                    Notifier.launch(getContext(), "Explicit: Match!", "You have a match!" , "If you want to meet your match, click here." , R.mipmap.ic_launcher);
                    checkedMatch = true;
                }
            }
        }
    }
}