package com.magic_foo.magiconeapp;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends Activity implements SensorEventListener, View.OnTouchListener {

    TextView tvAzimuth;
    TextView tvDistance;
    TextView tvApiResponse;
    private final int TIMER_DELAY = 1000;
    private final int MIN_DISTANCE = 100;
    private final int MAX_DISTANCE = 10000000;
    private final double DISTANCE_INC = 1.2;
    private final int MAX_VOLUME = 100;
    private final int AZIMUTH_TOLERANCE = 10;
    
    
    private SensorManager mSensorManager;
    private MediaPlayer noisePlayer;
    private String nowPlayingSound = "";

    private int azimuth = 0;
    private int dialDistance = MIN_DISTANCE;

    private int lastCheckedAzimuth = 0;
    private int lastCheckedDistance = 0;

    private PlaceMap placeMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        noisePlayer = new MediaPlayer();


        tvAzimuth = (TextView)findViewById(R.id.tvAzimuth);
        tvDistance = (TextView)findViewById(R.id.tvDistance);
        tvApiResponse = (TextView)findViewById(R.id.tvApiResponse);

        resetDial();
        initializeTimer();

        try {
            placeMap = new PlaceMap(getJsonFromFile("london"));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

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


    private void initializeTimer() {
        Thread timer = new Thread() {
            public void run () {
                for (;;) {
                    // do stuff in a separate thread
                    try {
                        Thread.sleep(TIMER_DELAY);
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
        Log.d("MainAct", "Azimuth: " + azimuth + ", distance: " + dialDistance);
        resolveSound(azimuth, dialDistance);
    }

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
            dialDistance = (int) Math.round(dialDistance * DISTANCE_INC);
        } else if (step < 0 && dialDistance >= MIN_DISTANCE * DISTANCE_INC) {
            dialDistance = (int) Math.round(dialDistance / DISTANCE_INC);
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
        azimuth = Math.round(degree);
        tvAzimuth.setText("Heading: " + Float.toString(degree) + " degrees");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    protected void playSoundFile(String filename) {
        //playSound(getResources().getIdentifier(filename, "raw", getPackageName()));
        if (nowPlayingSound.equals(filename)) return;
        nowPlayingSound = filename;
        noisePlayer.reset();
        try {
            noisePlayer.setDataSource(this, Uri.parse("android.resource://" + getPackageName() + "/raw/" + filename));
            noisePlayer.prepare();
            noisePlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void setStreamVolume(int volume) {
        if (volume<0) volume =0;
        if (volume>MAX_VOLUME) volume = MAX_VOLUME;
        float log1=(float)(Math.log(MAX_VOLUME-volume)/Math.log(MAX_VOLUME));
        Log.d("MainAct", "Setting stream volume to " + volume + "(" + log1 + ")");
        noisePlayer.setVolume(1 - log1, 1 - log1);

    }


    protected void resolveSound(int azimuth, int distance) {

        int levelSize = (MAX_DISTANCE - MIN_DISTANCE) / placeMap.getSize();

        int mappedLevel = distance / levelSize;
        int mappedDirection = azimuth / 45;

        final Place currentPlace = placeMap.getLevel(mappedLevel).getItem(mappedDirection);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvApiResponse.setText(currentPlace.getDescription());
            }
        });

        playSoundFile(currentPlace.getSound());


    }

    protected void resolveSoundLive(int azimuth, long distance) {

        if (lastCheckedDistance == distance &&
                lastCheckedAzimuth < azimuth + AZIMUTH_TOLERANCE &&
                lastCheckedAzimuth > azimuth - AZIMUTH_TOLERANCE) {
            Log.d("MainAct", "Skipping resolving azimuth (" + azimuth + ") close enough to last checked ("+lastCheckedAzimuth+")");
            return;
        }

        OkHttpClient client = new OkHttpClient();

        String requestUrl = "http://magicone.fooropa.com/resolve?distance"+distance+"&azimuth="+azimuth;

        Request request = new Request.Builder()
                .url(requestUrl)
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
            final String responseString = response.body().string();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvApiResponse.setText(responseString);

                }
            });
            try {
                JSONObject jsonResponse =new JSONObject(responseString);
                String soundFile = jsonResponse.getString("test");
                if (!soundFile.equals("")) {
                    playSoundFile(soundFile);
                } else {
                    noisePlayer.pause();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public JSONObject getJsonFromFile(String fileName)
            throws JSONException, IOException  {

        String json;
        FileInputStream fis;

        InputStream is = getResources().openRawResource(R.raw.london);

//        fis = this.openFileInput(R.raw.london);
        int size = is.available();

        byte[] buffer = new byte[size];
        //noinspection ResultOfMethodCallIgnored
        is.read(buffer);
        is.close();
        json = new String(buffer, "UTF-8");

        return new JSONObject(json);


    }
}
