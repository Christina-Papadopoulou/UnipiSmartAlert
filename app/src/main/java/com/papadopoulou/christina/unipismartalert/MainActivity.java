package com.papadopoulou.christina.unipismartalert;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    //
    protected FirebaseDatabase database;
    private DatabaseReference myRef;
    //Sensor Framework
    private SensorManager sensorManager;
    //Sensor Type
    private Sensor accelerometer;

    private EditText editTextName;
    protected Button buttonSingUp;
    private TextView textViewTimer;
    private boolean flagtimer;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextName = findViewById(R.id.editTextName);
        buttonSingUp = findViewById(R.id.buttonSingUp);
        textViewTimer = findViewById(R.id.textViewTimer);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("message");

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        final HashMap<String, String> hashMap = new HashMap<>();

        buttonSingUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hashMap.put("LONG", "100");
                hashMap.put("LAT", "30");
                myRef.child("xristos").setValue(hashMap);

                hashMap.put("LONG", "200");
                hashMap.put("LAT", "20");
                myRef.child("makis").setValue(hashMap);
            }
        });


        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot currentDataSnapshot : dataSnapshot.getChildren()) {
                    Log.e("Get Data", currentDataSnapshot.getKey());

                    String usr = currentDataSnapshot.getKey();

                    for (DataSnapshot makisSnap : dataSnapshot.child(usr).getChildren()) {

                        String val = makisSnap.getValue(String.class);
                        if (val.equals("200")) {

                            Log.e("JIM", "to vrika " + currentDataSnapshot.getKey());
                        }
                        Log.e("Get Data", " " + makisSnap.getValue());
                    }

                }
            }


            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("XRISTINA", "Failed to read value.", error.toException());
            }

        });

        mediaPlayer = MediaPlayer.create(getBaseContext(), (R.raw.tick));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int x = Math.round(event.values[0]);
        int y = Math.round(event.values[1]);
        int z = Math.round(event.values[2]);

        if (z == 0 & !flagtimer) {
            flagtimer = true;
            new CountDownTimer(30000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    Log.e("ONTICK", "seconds remaining: " + millisUntilFinished / 1000);
                    textViewTimer.setText(String.valueOf(millisUntilFinished / 1000));
                    mediaPlayer.start();
                }

                @Override
                public void onFinish() {
                    flagtimer = false;
                }
            }.start();

            Toast.makeText(this, "PTOSI EGINE", Toast.LENGTH_SHORT).show();
            Log.e("XRISTINA", "Eleutheri ptosi");
        }


        //Log.e("XRISTINA", "X " + x + "  Y " + y + "  Z " + z);

    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
