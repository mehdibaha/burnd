package com.insa.burnd.network;

import android.content.Context;

import com.facebook.Session;
import com.insa.burnd.utils.SPManager;

import trikita.log.Log;

/* Handles Facebook Sessions in general */
public class SessionController {
    private Context ctx;

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
            Log.d("Disconnected.");
        }
    }

}
