package com.insa.burnd.network;

import android.content.Context;
import android.util.Log;

import com.facebook.Session;
import com.insa.burnd.utils.SPManager;

/* Handles Facebook Sessions in general */
public class SessionController {
    private Context ctx;
    private static String TAG = "BURND-SessionController";

    public SessionController(Context ctx) {
        this.ctx = ctx;
    }

    // Gestion de la déconnexion
    public Session getFacebookSession() {
        Session session = Session.getActiveSession();
        if (session == null) {
            session = Session.openActiveSessionFromCache(ctx);
        }

        return session;
    }

    public void disconnectFB() {
        final Session session = getFacebookSession();

        if (session != null) {
            session.closeAndClearTokenInformation();
            SPManager.clear(ctx);
            SPManager.save(ctx, "false", "FIRST_USER");
            Log.d(TAG, "Disconnected.");
        }
    }

}
