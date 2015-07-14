package com.insa.burnd.utils;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.insa.burnd.network.VolleySingleton;

import butterknife.ButterKnife;
import trikita.log.Log;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    // Cancels request if context == null
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(getClass().toString());
        VolleySingleton.getInstance().getRequestQueue().cancelAll(getClass().toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }
}
