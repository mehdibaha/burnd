package com.insa.burnd.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.animation.AlphaAnimation;

import com.insa.burnd.R;
import com.insa.burnd.network.Connexion;
import com.insa.burnd.utils.SPManager;
import com.insa.burnd.utils.Utils;
import com.insa.burnd.view.MainActivity.MainActivity;
import com.insa.burnd.view.TutorialActivity.TutorialActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class SplashActivity extends FragmentActivity implements Connexion.ResponseListener {
    private final SplashActivity activity = this;
    private static String TAG = "BURND-SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        boolean firstUser = SPManager.load(activity, "FIRST_USER").equals("");
        Log.d(TAG, String.valueOf(firstUser));

        this.fadeInViews(firstUser);
        if (firstUser) { // Boolean can be tricky so we use strings.
            startActivity(new Intent(activity, TutorialActivity.class));
            activity.finish();
        }
        else
            new Connexion(activity, activity, "checkparty").execute();

    }

    private void fadeInViews(boolean firstUser) {
        int duration = firstUser ? 2500 : 1000;

        AlphaAnimation fadeIn = new AlphaAnimation(0.0f , 1.0f );
        fadeIn.setDuration(duration);
        fadeIn.setFillAfter(true);
        findViewById(R.id.progress_wheel).startAnimation(fadeIn);
        findViewById(R.id.textView5).startAnimation(fadeIn);
        findViewById(R.id.logo_image).startAnimation(fadeIn);
    }

    @Override
    public void requestCompleted(String response) throws JSONException {
        JSONObject json = new JSONObject(response);
        String message = json.getString("message");
        boolean error = json.getBoolean("error");

        //Logger.json(response);
         Log.d(TAG, message);

        if (!error) {
            Utils.showToast(this, "Sending you to party.");
            startActivity(new Intent(this, MainActivity.class));
            activity.finish();
        } else if (message.equals("USER_NOT_IN_PARTY")) {
            Utils.showToast(this, "You're not in a party.");
            startActivity(new Intent(this, JoinActivity.class));
            activity.finish();
        } else {
            Log.d("Splash", "Sending user to LoginActivity");
            startActivity(new Intent(this, LoginActivity.class));
            activity.finish();
        }
    }
}