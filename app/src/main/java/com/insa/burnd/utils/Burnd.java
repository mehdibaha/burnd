package com.insa.burnd.utils;

import android.app.Application;
import android.content.Context;

// Application Class. Common practice in Android.
// Used in this project to deal with all connections in the app, for instance.

public class Burnd extends Application {
    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();

        this.setAppContext(getApplicationContext());
    }

    public static Context getAppContext() {
        return appContext;
    }

    private void setAppContext(Context ctx) {
        appContext = ctx;
    }
}