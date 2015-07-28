package com.insa.burnd.view;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;

import com.insa.burnd.R;
import com.insa.burnd.models.ApiResponse;
import com.insa.burnd.network.Connection;
import com.insa.burnd.services.GPSTracker;
import com.insa.burnd.sync.SyncAdapter;
import com.insa.burnd.utils.RedView;
import com.insa.burnd.view.MainActivity.MainActivity;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import trikita.log.Log;


public class CompassActivity extends Activity implements SensorEventListener, Connection.ResponseListener{
    private SensorManager sM;
    private Sensor mS;
    private Sensor aS;
    private float[] accState;
    private float[] magState;
    private float[] rot;
    private volatile float[] result = new float[3];
    private volatile double myLat = 0;
    private volatile double myLon = 0;
    private volatile double yourLat = 0;
    private volatile double yourLon = 0;
    private volatile float[] distance = new float[3];
    private static final long COMPASS_FREQ = 8000;
    private static final long MEET_DURATION = 7*60000;
    private Context ctx = this;
    private Connection.ResponseListener rListener = this;
    private Bundle compBundle;
    private volatile boolean running = true;
    private volatile Thread gpsThread;
    private static volatile CompassActivity instance;

    @Bind(R.id.redView) volatile RedView rv;
    @Bind(R.id.button_start_stop) Button ssButton;
    @Bind(R.id.timer) TextView timerText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        float[] distance = new float[3];
        Location.distanceBetween(39.819708, -97.634262, 39.819728, -97.633425, distance);
        Log.d("distance : " + distance[0]);
        Log.d("bearing : " + distance[1]);
        super.onCreate(savedInstanceState);
        instance = this;
        compBundle = new Bundle();
        compBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        compBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        compBundle.putString("reqID", "gpsSync");
        setContentView(R.layout.activity_compass);
        ButterKnife.bind(this);

        sM = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mS = sM.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        aS = sM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accState = new float[3];
        magState = new float[3];
        rot = new float[9];
        new Connection(this,this, "activatematch").execute();
        //Cette thread appelle régulièrement le gpsSync durant toute la durée du match actif pour mettre à jour la position.
        gpsThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(running){
                    ContentResolver.requestSync(MainActivity.getAccount(), MainActivity.AUTHORITY, compBundle);
                    try{
                        Thread.sleep(COMPASS_FREQ);
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        });
        gpsThread.start();
        //Cette timertask permet de tout interrompre après une durée MEET_DURATION déterminée.
        new CountDownTimer(MEET_DURATION, 1000) {

            public void onTick(long millisUntilFinished) {
                long secs = (millisUntilFinished / 1000) % 60;
                long minutes = ((millisUntilFinished / 1000) - secs)/60;
                if(secs < 10){
                    timerText.setText("0"+minutes + ":0" + secs);
                }else{
                    timerText.setText("0"+minutes + ":" + secs);
                }
            }

            public void onFinish() {
                timerText.setText("Time !");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(rv.getSearch()){
                            rv.startStopSearch();
                        }
                    }
                });
                myLat = 0;
                myLon = 0;
                yourLat = 0;
                yourLon = 0;
                running = false;
                gpsThread.interrupt();
                gpsThread = null;
                instance = null;
                SyncAdapter.killMatch();
                new Connection(ctx, rListener, "killmatch").execute();
            }
        }.start();
    }

    @OnClick(R.id.button_start_stop)
    public void startStopButton(){
        rv.startStopSearch();
        if(rv.getSearch()){
            ssButton.setText("Stop !");
        }else{
            ssButton.setText("Start !");
        }
    }

    public static CompassActivity getInstance(){
        return instance;
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if(event.sensor == aS){
            System.arraycopy(event.values, 0, accState, 0, 3);
        }
        if(event.sensor == mS){
            System.arraycopy(event.values, 0, magState, 0, 3);
        }
        SensorManager.getRotationMatrix(rot, null, accState, magState);
        SensorManager.getOrientation(rot, result);
        //On calcule la distance entre les deux personnes, ainsi que la direction qui est stockée dans direction[1].
        //Location.distanceBetween(myLat, myLon, yourLat, yourLon, distance);
        rv.updateBearing(result[0]);
        rv.updateDistance(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sM.registerListener(this, mS, SensorManager.SENSOR_DELAY_NORMAL);
        sM.registerListener(this, aS, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sM.unregisterListener(this);
    }

    //Ceci est la fonction appelée par le SyncAdapter pour mettre à jour les positions.
    public double[] updateLocation(){
        double[] d = new double[2];
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GPSTracker gps = new GPSTracker(ctx);
                gps.getLocation();
                if(!gps.isLocationAvailable()) {
                } else {
                    myLat = gps.getLocation().getLatitude();
                    myLon = gps.getLocation().getLongitude();
                }
                gps.closeGPS();
            }
        });
        d[0] = myLat;
        d[1] = myLon;
        yourLat = 45.780774;
        yourLon = 4.868868;
        float declination = (float) Math.toRadians(new GeomagneticField((float)myLat, (float)myLon, 0, System.currentTimeMillis()).getDeclination());
        if(!(myLat != 0 && myLon != 0 && yourLat != 0 && yourLon != 0)){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(rv.getSearch()){
                        rv.startStopSearch();
                    }
                }
            });
        }
        return d;
    }

    public void updateYou(double lat, double lon){
        yourLat = lat;
        yourLon = lon;
    }

    @Override
    public void requestCompleted(ApiResponse ar) {
        String message = ar.getMessage();
        if(message.equals("killmatch")){
            startActivity(new Intent(this, MainActivity.class));
            this.finish();
        }
    }
}

