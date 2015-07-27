package com.insa.burnd.view.MainActivity;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.insa.burnd.R;
import com.insa.burnd.controller.OnSwipeTouchListener;
import com.insa.burnd.models.ApiResponse;
import com.insa.burnd.models.MeetingResponse;
import com.insa.burnd.network.Connection;
import com.insa.burnd.network.VolleySingleton;
import com.insa.burnd.utils.BaseFragment;
import com.insa.burnd.utils.Utils;
import com.insa.burnd.view.CompassActivity;
import com.insa.burnd.view.JoinActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import trikita.log.Log;


public class MeetingFragment extends BaseFragment implements Connection.ResponseListener {
    private final MeetingFragment fragment = this;
    private ImageLoader imageLoader;
    private int numMessages = 0;

    @Bind(R.id.profilePic) NetworkImageView photo;
    @Bind(R.id.textView) TextView tView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Connection(mActivity, fragment, "getprofile").execute();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_meeting, container, false);
        ButterKnife.bind(fragment, v);

        if (imageLoader == null)
            imageLoader = VolleySingleton.getInstance().getImageLoader();

        tView.setText("Loading...");
        return v;
    }

    @OnClick(R.id.textView)
    public void clickLoading() {
        startActivity(new Intent(mActivity, CompassActivity.class));
    }

    @Override
    public void requestCompleted(ApiResponse sr) {
        String message = sr.getMessage();
        boolean error = sr.isError();
        MeetingResponse mr = sr.getMeetingResponse();
        Log.d(sr.toString());

        if (!error) {
            int age = mr.getAge();
            final String id = mr.getId();
            String match = mr.getMatch();
            String name = mr.getName();

            if(match.equals("MATCH")) displayNotification();
            tView.setText(name + " | " + age);
            photo.setEnabled(true);
            photo.setImageUrl("https://graph.facebook.com/" + id + "/picture?type=large", imageLoader);
            photo.setOnTouchListener(new OnSwipeTouchListener(mActivity) {
                public void onSwipeTop() {
                    new Connection(mActivity, fragment, "swipeup", "Like").execute(id);
                }

                public void onSwipeBottom() {
                    new Connection(mActivity, fragment, "swipedown", "Nope").execute(id);
                }
            });
        } else if(mr.isStop()) {
                photo.setImageUrl("http://burnd.cles-facil.fr/uploads/seen_everyone.png", imageLoader);
                tView.setText("You have seen everyone!");
                photo.setEnabled(false);
        }
    }

    public void displayNotification() {
        // Invoking the default notification service
        NotificationCompat.Builder  mBuilder = new NotificationCompat.Builder(mActivity);
        mBuilder.setContentTitle("You have a match!");
        mBuilder.setContentText("If you want to meet your match, click here.");
        mBuilder.setTicker("Explicit: Match!");
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);

        // Increase notification number every time a new notification arrives
        mBuilder.setNumber(++numMessages);

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(mActivity, CompassActivity.class);
        int notificationId = 111;
        resultIntent.putExtra("notificationId", notificationId);

        //This ensures that navigating backward from the Activity leads out of the app to Home page
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mActivity);

        // Adds the back stack for the Intent
        stackBuilder.addParentStack(CompassActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_ONE_SHOT //can only be used once
                );
        // start the activity when the user clicks the notification text
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager myNotificationManager = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);

        // pass the Notification object to the system
        myNotificationManager.notify(notificationId, mBuilder.build());
    }
}
