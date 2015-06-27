package com.insa.burnd.view.MainActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.support.v4.preference.PreferenceFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.insa.burnd.R;
import com.insa.burnd.network.Connexion;
import com.insa.burnd.network.SessionController;
import com.insa.burnd.syncAdapter.SyncAdapter;
import com.insa.burnd.utils.SPManager;
import com.insa.burnd.utils.Utils;
import com.insa.burnd.view.JoinActivity;
import com.insa.burnd.view.LoginActivity;
import com.insa.burnd.view.SplashActivity;

import org.json.JSONException;
import org.json.JSONObject;


public class SettingsFragment extends PreferenceFragment implements Connexion.ResponseListener, Preference.OnPreferenceChangeListener {
    private Activity mActivity;
    private final SettingsFragment fragment = this;
    private static String TAG = "BURND-SettingsFragment";

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        findPreference("you").setOnPreferenceChangeListener(this);
        findPreference("them").setOnPreferenceChangeListener(this);
        if(!(SPManager.load(mActivity, "date").equals("0"))){
            Preference p = findPreference("date");
            p.setSummary(SPManager.load(mActivity, "date"));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        ListView listView = (ListView) v.findViewById(android.R.id.list);
        listView.setPadding(0, Utils.dpToPx(mActivity, 75), 0, 0);

        return v;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue){
        switch(preference.getKey()){
            case "you": {
                if (newValue.equals("1")) {
                    new Connexion(mActivity, this, "insertsex", "Updating sex preference...").execute("m");
                } else if (newValue.equals("2")) {
                    new Connexion(mActivity, this, "insertsex", "Updating sex preference...").execute("f");
                }
            }
            case "them":{
                if(newValue.equals("1")){
                    new Connexion(mActivity, this, "insertsexpreferance", "Updating sex preference...").execute("m");
                }else if(newValue.equals("2")) {
                    new Connexion(mActivity, this, "insertsexpreferance", "Updating sex preference...").execute("f");
                }
            }
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        switch (preference.getKey()) {
            case "meeting": {
                //Turn on matchChecking if meeting is activated.
                if(SPManager.loadBoolean(mActivity, "meeting") && !SyncAdapter.checkSyncs(MainActivity.getAccount(), MainActivity.AUTHORITY, "matchSync")) {
                    Bundle b1 = new Bundle();
                    b1.putString("reqID", "matchSync");
                    ContentResolver.addPeriodicSync(
                            MainActivity.getAccount(),
                            MainActivity.AUTHORITY,
                            b1,
                            1200);
                }
                if(!SPManager.loadBoolean(mActivity, "meeting") && SyncAdapter.checkSyncs(MainActivity.getAccount(), MainActivity.AUTHORITY, "matchSync")){
                    ContentResolver.removePeriodicSync(MainActivity.getAccount(), MainActivity.AUTHORITY, SyncAdapter.getBundle(MainActivity.getAccount(), MainActivity.AUTHORITY, "matchSync"));
                    Log.d("matchSync","Removed");
                }
            }
            break;
            case "first_user": {
                SPManager.remove(mActivity, "FIRST_USER");
                Utils.showToast(mActivity, "You became a first user.");
                Log.d(TAG, "first_user");
            }
            break;
            case "leave_party": {
                Utils.showToast(mActivity, "You left party.");
                new Connexion(mActivity, fragment, "leaveparty","Leaving Party...").execute();
                Log.d(TAG, "leave_party");
            }
            break;
            case "logout": {
                Utils.showToast(mActivity, "You logged out.");
                new SessionController(mActivity).disconnectFB();
                startActivity(new Intent(mActivity, LoginActivity.class));
                Log.d(TAG, "logout");
            }
            case "clear_cache": {
                Utils.showToast(mActivity, "Cache cleared.");
                SPManager.remove(mActivity, "LAST_POST_ID");
            }
            break;
        }
        return false;
    }

    @Override
    public void requestCompleted(String response) throws JSONException {
        JSONObject json = new JSONObject(response);
        String message = json.getString("message");
        boolean error = json.getBoolean("error");

        Log.d(TAG, response);
        Log.d(TAG, message);

        if (!error) {
            if(message.equals("SEX_PREF_INSERTED") || message.equals("SEX_INSERTED")){
                Utils.showToast(mActivity, "Sex Updated");
            } else {
                clearCache();
                Utils.showToast(mActivity, "Leaving party.");
                startActivity(new Intent(mActivity, JoinActivity.class));
                mActivity.finish();
            }
        } else if (message.equals("SESSION_NOT_DELETED")) {
            Utils.showToast(mActivity, "Leaving party failed.");
        } else {
            Utils.showToast(mActivity, "Access denied.");
            startActivity(new Intent(mActivity, SplashActivity.class));
            mActivity.finish();
        }
    }

    public void clearCache() {
        SPManager.remove(mActivity, "LAST_POST_ID");
    }
}
