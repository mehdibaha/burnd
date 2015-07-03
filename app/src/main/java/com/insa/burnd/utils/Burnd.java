package com.insa.burnd.utils;

import android.app.Application;
import android.content.Context;

// Application Class. Common practice in Android.
// Used in this project to deal with all connections in the app, for instance.

public class Burnd extends Application {
    private static Burnd instance;
    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        this.setAppContext(getApplicationContext());
    }

    public static Burnd getInstance() {
        return instance;
    }

    public static Context getAppContext() {
        return appContext;
    }

    private void setAppContext(Context ctx) {
        appContext = ctx;
    }
}