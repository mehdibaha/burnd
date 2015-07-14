package com.insa.burnd.view;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class LoginActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            // Add the fragment on initial activity setup
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, new LoginFragment()).commit();
        }
    }
}
