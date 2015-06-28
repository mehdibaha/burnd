package com.insa.burnd.sync;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.insa.burnd.view.CompassActivity;
import com.insa.burnd.view.MainActivity.MainActivity;

public class Notifier {

    public static void launch(Context ctx, String ticker,String title, String text, int d){
        //Le protocole pour créer une notification est récupéré sur developpers.android
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(ctx)
                        .setSmallIcon(d)
                        .setContentTitle(title)
                        .setContentText(text);
        mBuilder.setTicker(ticker);
        Intent resultIntent = new Intent(ctx, CompassActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }
}
