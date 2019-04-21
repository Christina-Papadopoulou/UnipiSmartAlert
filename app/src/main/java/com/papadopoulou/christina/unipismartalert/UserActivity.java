package com.papadopoulou.christina.unipismartalert;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.HashMap;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.SEND_SMS;
import static com.papadopoulou.christina.unipismartalert.LoginActivity.USERS;

public class UserActivity extends AppCompatActivity implements SensorEventListener {
    // FireBase
    protected FirebaseDatabase database;
    public DatabaseReference myRef;

    //Sensor Framework
    private SensorManager sensorManager;

    //Sensor Type
    private Sensor accelerometer;

    // Timer
    private boolean isTimerEnabled;
    private CountDownTimer countDownTimer;

    private MediaPlayer mediaPlayer;

    // Request Code
    private static final int PERMISSION_REQUEST_CODE = 200;

    // Gps Manager
    private LocationManager locationManager;

    // Sms Manager
    private SmsManager smsManager;

    // Public vars
    private Characteristics currentFallingSituation, currentQuakeSituation;
    private String currentDate;
    private String userName;
    private boolean isEarthQuakeDetection;
    private SharedPreferences sharedPref;

    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // Init views
        TextView textViewTimer = findViewById(R.id.textViewTimer);
        Button buttonAbort = findViewById(R.id.buttonAbort);

        // Init Shared Preferences
        sharedPref = getApplicationContext()
                .getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        writeToSharedPref(sharedPref);

        //Init media Player
        mediaPlayer = MediaPlayer.create(getBaseContext(), (R.raw.tick));

        // Init Timer
        countDownTimer = initCountDownTimer(5000, textViewTimer);

        // Register a receiver
        broadcastReceiver  = new MyReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        this.registerReceiver(broadcastReceiver, filter);

        userName = getIntent().getStringExtra("username");
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference(USERS);

        isEarthQuakeDetection = getIntent().getBooleanExtra("readyEarthquake", false);

        // Check Premission
        if (checkPermission()) {
            // Start Listen GPS
            smsManager = SmsManager.getDefault();
        } else {
            // Request premission
            requestPermission();
        }

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        buttonAbort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (countDownTimer == null) {
                    return;
                }
                countDownTimer.cancel();
                isTimerEnabled = false;
                currentFallingSituation.setAlarmAbort(true);
                myRef.child(userName)
                        .child("falls")
                        .child(currentDate)
                        .setValue(currentFallingSituation);
            }
        });

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot currentDataSnapshot : dataSnapshot.getChildren()) {
                    String dataBaseUser = currentDataSnapshot.getKey();

                    for (DataSnapshot snap : currentDataSnapshot.child("quakes").getChildren()) {
                        String quakeKey = snap.getKey();

                        Characteristics characteristics
                                = dataSnapshot.child(dataBaseUser)
                                .child("quakes")
                                .child(quakeKey)
                                .getValue(Characteristics.class);

                        Log.e("JIM", "USer " + dataBaseUser + " Date  " + quakeKey + "   " + String.valueOf(characteristics.isQuakeDetection()));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onSensorChanged(SensorEvent event) {
        isEarthQuakeDetection = sharedPref.getBoolean("readyEarthquake",false);

        int x = Math.round(event.values[0]);
        int y = Math.round(event.values[1]);
        int z = Math.round(event.values[2]);

        //Log.e("JIM"," "  + x  + " " + y + " "+ z);

        if(isEarthQuakeDetection){
            if(x > 2 || y > 2 || z != 10){
//                myRef.child(userName)
//                        .child("quakes")
//                        .child(String.valueOf(new Date()))
//                        .setValue(new Characteristics(true));
            }

        } else if (z == 0 & !isTimerEnabled) {
            isTimerEnabled = true;
            countDownTimer.start();
            currentFallingSituation = new Characteristics(4521, 4564564, false);
            currentDate = String.valueOf(new Date());
            myRef.child(userName)
                    .child("falls")
                    .child(currentDate)
                    .setValue(currentFallingSituation);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // NICE PREMISSION GRADED
                smsManager = SmsManager.getDefault();
                Log.e("JIM", "" + requestCode + "  " + grantResults[0] + "  " + grantResults[1]);
            } else {
                showAlertDialog();
            }
        }
    }


    /**
     * Elegxos Premission an exoun dothei gia na exw prosvasi sto GPS kai SMS
     */
    private boolean checkPermission() {
        int resultGPS = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int resultSMS = ContextCompat.checkSelfPermission(getApplicationContext(), SEND_SMS);

        return resultGPS == PackageManager.PERMISSION_GRANTED &
                resultSMS == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Dialog Box pou mas zitaei na exoume prosvasi sto GPS
     */
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION, SEND_SMS}, PERMISSION_REQUEST_CODE);
    }

    private void showAlertDialog() {
        // Create Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set Message
        builder.setMessage("Please turn on your GPS and allow to Send SMS. The application not working without that premmisions.")
                // Set Title
                .setTitle("Warning");

        // Set Listner for on click OK
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestPermission();
            }
        });

        // Show Dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void writeToSharedPref(SharedPreferences sharedPref) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("mobile1", "6971626722");
        editor.apply();
    }

    private CountDownTimer initCountDownTimer(long millisInFuture, final TextView textView) {
        return new CountDownTimer(millisInFuture, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                textView.setText(String.valueOf(millisUntilFinished / 1000));
                mediaPlayer.start();
            }

            @Override
            public void onFinish() {
                if (isTimerEnabled) {
                    String mobile = getApplicationContext()
                            .getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
                            .getString("mobile1", "no");

                    // PRoblem me to message
                    smsManager.sendTextMessage(mobile, null,
                            getString(R.string.sos_msg) +
                                    getString(R.string.lat) + "11111" +
                                    getString(R.string.lon) + "0000" +
                                    getString(R.string.end_sos_msg), null, null);
                }
                isTimerEnabled = false;
            }
        };
    }
}
