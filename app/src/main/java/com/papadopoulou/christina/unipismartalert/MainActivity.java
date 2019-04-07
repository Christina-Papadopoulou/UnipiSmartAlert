package com.papadopoulou.christina.unipismartalert;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;

    FirebaseDatabase database;
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
    double prevTime = 0;
    int  tempX = 0;
    int  tempY = 0;
    int  tempZ = 0;
    @Override
    public void onSensorChanged(SensorEvent event) {
        double curTime = System.currentTimeMillis();


            prevTime = curTime;
            int x = Math.round(event.values[0]);
            int y = Math.round(event.values[1]);
            int z = Math.round(event.values[2]);


            if(x == tempX & y == tempY & z != tempZ){
                Log.e("XRISTINA", "PTOSI");
            }

            tempX = x;
            tempY = y;
            tempZ = z;



            Log.e("XRISTINA", "X " + x + "  Y " + y + "  Z " + z);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
