package com.insa.burnd.sync;
//Les autres classes de ce package sont récupérées sur le tutoriel android.

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import com.insa.burnd.R;
import com.insa.burnd.models.ApiResponse;
import com.insa.burnd.network.Connection;
import com.insa.burnd.view.CompassActivity;

import org.json.JSONArray;

import trikita.log.Log;

/**
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter implements Connection.ResponseListener{
    // Global variables
    // Define a variable to contain a content resolver instance
    ContentResolver mContentResolver;
    private static boolean checkedMatch = false;
    private Context ctx;
    private double[] pos = new double[2];
    /**
     * Set up the sync adapter
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        ctx = context;
        mContentResolver = context.getContentResolver();
    }


    public SyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        ctx = context;
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

        String reqID = extras.getString("reqID");
        if(reqID != null) {
            switch(reqID) {
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
        new Connection(getContext(), this, "checkmatches").execute();
        Log.d("syncMatch");
    }

    private void gpsSync(){
        double[] pos = new double[2];
        CompassActivity ca = CompassActivity.getInstance();
        if(ca != null){
           pos = ca.getLocation();
        }
        new Connection(getContext(), this, "updatelocation").execute(Double.toString(pos[0]),Double.toString(pos[1]));
        Log.d("syncGPS");
    }

    public static void killMatch(){
        checkedMatch = false;
    }

    public static void setCheckedMatch(){
        checkedMatch = true;
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
    public void requestCompleted(ApiResponse sr) {
        Log.d(sr);
        /*if(!sr.isError()) {
            if (sr.getLocation() != null) {
                CompassActivity ca = CompassActivity.getInstance();
                if (ca != null) {
                    ca.setLocation(Double.parseDouble(sr.getLocation()[0]), Double.parseDouble(sr.getLocation()[0]));
                }
            }else if (sr.getMessage().equals("MATCH")) {
                if(!checkedMatch){
                    Notifier.launch(ctx, "Match !", "Match found !", "Click to find your match now !", R.mipmap.ic_launcher);
                }
            }
        }*/
    }
}