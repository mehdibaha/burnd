package com.insa.burnd.view.IntroActivity;

import android.content.Intent;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro2;
import com.insa.burnd.utils.Burnd;
import com.insa.burnd.utils.SPManager;
import com.insa.burnd.view.LoginActivity;


public class IntroActivity extends AppIntro2 {
    private final IntroActivity activity = this;

    @Override
    public void init(Bundle bundle) {
        // Adding fragments
        addSlide(new SlideFragment1(), Burnd.getAppContext());
        addSlide(new SlideFragment2(), Burnd.getAppContext());
        addSlide(new SlideFragment3(), Burnd.getAppContext());

        setFadeAnimation();
    }

    @Override
    public void onDonePressed() {
        SPManager.save(activity, "false", "FIRST_USER");
        startActivity(new Intent(activity, LoginActivity.class));
        activity.finish();
    }
}