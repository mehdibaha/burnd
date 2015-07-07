package com.insa.burnd.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.animation.AlphaAnimation;

import com.insa.burnd.R;
import com.insa.burnd.network.Connection;
import com.insa.burnd.utils.SPManager;
import com.insa.burnd.utils.Utils;
import com.insa.burnd.view.IntroActivity.IntroActivity;
import com.insa.burnd.view.MainActivity.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import trikita.log.Log;

public class SplashActivity extends FragmentActivity implements Connection.ResponseListener {
    private final SplashActivity activity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        boolean firstUser = SPManager.load(activity, "FIRST_USER").equals("");
        Log.d(String.valueOf(firstUser));

        this.fadeInViews(firstUser);
        if (firstUser) { // Boolean can be tricky so we use strings.
            startActivity(new Intent(activity, IntroActivity.class));
            activity.finish();
        }
        else
            new Connection(activity, activity, "checkparty").execute();

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
         Log.d(message);

        if (!error) {
            Utils.showToast(this, "Sending you to party.");
            startActivity(new Intent(this, MainActivity.class));
            activity.finish();
        } else if (message.equals("USER_NOT_IN_PARTY")) {
            Utils.showToast(this, "You're not in a party.");
            startActivity(new Intent(this, JoinActivity.class));
            activity.finish();
        } else {
            Log.d("Sending user to LoginActivity");
            startActivity(new Intent(this, LoginActivity.class));
            activity.finish();
        }
    }
}