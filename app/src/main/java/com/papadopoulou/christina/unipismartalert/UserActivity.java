package com.papadopoulou.christina.unipismartalert;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class UserActivity extends AppCompatActivity implements SensorEventListener {
    //Sensor Framework
    private SensorManager sensorManager;
    //Sensor Type
    private Sensor accelerometer;

    private TextView textViewTimer;
    private boolean flagtimer;
    private CountDownTimer countDownTimer;

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        textViewTimer = findViewById(R.id.textViewTimer);
        Button buttonAbort = findViewById(R.id.buttonAbort);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        //Init media Player
        mediaPlayer = MediaPlayer.create(getBaseContext(), (R.raw.tick));

        countDownTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                textViewTimer.setText(String.valueOf(millisUntilFinished / 1000));
                mediaPlayer.start();
            }

            @Override
            public void onFinish() {
            }
        };

        buttonAbort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownTimer.cancel();
                flagtimer = false;

                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage("6971626722",null,
                        "Hello from Unipi class",null,null);

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int z = Math.round(event.values[2]);

        if (z == 0 & !flagtimer) {
            flagtimer = true;
            countDownTimer.start();
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
