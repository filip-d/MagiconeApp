package com.magic_foo.magiconeapp;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener, View.OnTouchListener {

    TextView tvAzimuth;
    TextView tvDistance;
    private final int TIMER_DELAY = 1000;
    private final int MIN_DISTANCE = 100;
    private final int MAX_DISTANCE = 10000000;
    private final double DISTANCE_INC = 1.2;
    private SensorManager mSensorManager;
    private long dialDistance = MIN_DISTANCE;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        tvAzimuth = (TextView)findViewById(R.id.tvAzimuth);
        tvDistance = (TextView)findViewById(R.id.tvDistance);

        resetDial();

    }

    @Override
    protected void onResume() {
        super.onResume();
        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }

/*
    private void initializeTimer() {
        Thread timer = new Thread() {
            public void run () {
                for (;;) {
                    // do stuff in a separate thread
                    try {
                        Thread.sleep(TIMER_DELAY);    // sleep for 5 min
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    timerEvent();
                }
            }
        };
        timer.start();
    }

    protected void timerEvent() {
    }*/

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        Log.d("MusicBoxActivity", "event.source: " + event.getSource() + ", event.action: " + event.getAction());
        if (0 != (event.getSource() & InputDevice.SOURCE_CLASS_POINTER)) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_SCROLL:
                    Log.d("MusicBoxActivity", "event.getAxisValue: " + event.getAxisValue(MotionEvent.AXIS_VSCROLL));
                    if (event.getAxisValue(MotionEvent.AXIS_VSCROLL) < 0.0f)
                        dialUpdated(-1);
                    else
                        dialUpdated(1);
                    return true;
            }
        }
        return super.onGenericMotionEvent(event);
    }

    protected void dialUpdated(int step) {
        if (step > 0 && dialDistance < MAX_DISTANCE / DISTANCE_INC) {
            dialDistance = Math.round(dialDistance * DISTANCE_INC);
        } else if (step < 0 && dialDistance >= MIN_DISTANCE * DISTANCE_INC) {
            dialDistance = Math.round(dialDistance / DISTANCE_INC);
        }
        if (tvDistance!=null) tvDistance.setText(String.valueOf(dialDistance));
    }

    protected void resetDial(){
        dialDistance = MIN_DISTANCE;
        dialUpdated(0);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);
        tvAzimuth.setText("Heading: " + Float.toString(degree) + " degrees");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}
