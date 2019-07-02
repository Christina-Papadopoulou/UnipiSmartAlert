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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.SEND_SMS;
import static com.papadopoulou.christina.unipismartalert.LoginActivity.USERS;

public class UserActivity extends AppCompatActivity implements SensorEventListener, LocationListener {
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

    // TTS
    private MyTts tts;

    // Public vars
    private Characteristics currentFallingSituation, tempCharacteristics;
    private String currentDate = null;
    private String userName;
    private boolean isChargedOn;
    private SharedPreferences sharedPref;
    private int count = 0, countSos = 0, countAbort = 0;
    private String strLat, strLong;
    private BroadcastReceiver broadcastReceiver;
    private Button buttonAbort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // Init views
        TextView textViewTimer = findViewById(R.id.textViewTimer);
        buttonAbort = findViewById(R.id.buttonAbort);
        Button buttonSos = findViewById(R.id.buttonSos);
        buttonAbort.setEnabled(false);

        // Init Shared Preferences to store data that persist across user sessions even if app is killed
        sharedPref = getApplicationContext()
                .getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        // Write to shared Prefs
        writeToSharedPref(sharedPref);

        //Init media Player
        mediaPlayer = MediaPlayer.create(getBaseContext(), (R.raw.tick));

        // Init Timer
        countDownTimer = initCountDownTimer(3000, textViewTimer);

        //Init Sensor
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // TTS
        tts = new MyTts(getApplicationContext());

        // Init Db
        userName = getIntent().getStringExtra("username");
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference(USERS);

        isChargedOn = getIntent().getBooleanExtra("readyEarthquake", false);

        // Check Premission
        if (checkPermission()) {
            // Start Listen GPS
            smsManager = SmsManager.getDefault();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        } else {
            // Request premission
            requestPermission();
        }

        buttonAbort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (countDownTimer == null) {
                    return;
                }

                countDownTimer.cancel();
                isTimerEnabled = false;
                currentFallingSituation.setFallAborted(true);
                myRef.child(userName)
                        .child("falls")
                        .child(currentDate)
                        .setValue(currentFallingSituation);

                countAbort++;
            }
        });


        buttonSos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tts.speak("I need Help");
                sendSms();

                countSos++;
                Toast.makeText(getApplicationContext(),
                        getString(R.string.sos_msg) +
                                getString(R.string.lat) +
                                strLat + getString(R.string.lon) +
                                strLong +
                                getString(R.string.end_sos_msg),
                        Toast.LENGTH_SHORT).show();
                myRef.child(userName).child("countsos").setValue(countSos);
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        // Register a receiver
        broadcastReceiver = new MyReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(broadcastReceiver, filter);

        // Read Database
        myRef.addValueEventListener(valueEventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Remove listener because when i change my database the event listener called in All Activity
        myRef.removeEventListener(valueEventListener);
        sensorManager.unregisterListener(this);

        // Remove broadcast receiver
        if(broadcastReceiver == null || countDownTimer == null) return;

        unregisterReceiver(broadcastReceiver);

        countDownTimer.cancel();
        //Close the Text to Speech Library
        if(tts != null) {
            tts.stop();
        }
    }

    //==============================================================================================
    // READ DATABASE
    //==============================================================================================
    ArrayList<String> dateQuakes = new ArrayList<>();

    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            if (isChargedOn) {

                if (currentDate == null) {
                    return;
                }

                for (DataSnapshot currentDataSnapshot : dataSnapshot.getChildren()) {
                    String user = currentDataSnapshot.getKey();
                    String dateQuake = (String) dataSnapshot.child(user).child("quakes").getValue();
                    dateQuakes.add(dateQuake);
                }

                int duplicates = getDupCount(dateQuakes);

                if (duplicates >= 2) {
                    Toast.makeText(getApplicationContext(), getString(R.string.earth), Toast.LENGTH_SHORT).show();
                }
            }else{
                tempCharacteristics = dataSnapshot.child(userName).getValue(Characteristics.class);
                countSos = tempCharacteristics.getCountSos();
                countAbort = tempCharacteristics.getCountAlarmAbort();
            }
        }

        @Override
        public void onCancelled (@NonNull DatabaseError databaseError) {

        }
    };


    public int getDupCount(ArrayList<String> l) {
        int cnt = 0;
        HashSet<String> h = new HashSet<>(l);

        for (String token : h) {
            if (Collections.frequency(l, token) > 1)
                cnt++;
        }

        return cnt;
    }

    //==============================================================================================
    // ACCELEROMETER LISTENER
    //==============================================================================================
    long pastTime = 0;

    @SuppressLint("MissingPermission")
    @Override
    public void onSensorChanged(SensorEvent event) {
        isChargedOn = sharedPref.getBoolean("readyEarthquake", false);

        int x = Math.round(event.values[0]);
        int y = Math.round(event.values[1]);
        int z = Math.round(event.values[2]);

        long currentTime = System.currentTimeMillis();

        // When the mobile is on charge
        if (isChargedOn) {
            // Every 1000millisecond
            if (currentTime - pastTime > 1000) {
                pastTime = System.currentTimeMillis();

                // Check if my mobile was shaked
                if (x >= 2 || x < 0 || y > 1 || y < -1) {
                    //Take the date of the quake
                    currentDate = String.valueOf(new Date());
                    //Add it on firebase
                    myRef.child(userName)
                            .child("quakes")
                            .setValue(currentDate);
                }
            }
        // If z == 0 and timer is not running we have fall
        } else if (z == 0 & !isTimerEnabled) {
            isTimerEnabled = true;
            buttonAbort.setEnabled(true);
            countDownTimer.start();
            currentFallingSituation = new Characteristics(Double.parseDouble(strLat), Double.parseDouble(strLong), false);

            if (currentDate == null) return;
            currentDate = String.valueOf(new Date());

            // Add a branch on currentDate in firebase
            // Example jim -> falls-> Date -> Characteristics
            myRef.child(userName)
                    .child("falls")
                    .child(currentDate)
                    .setValue(currentFallingSituation);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //==============================================================================================
    // GPS LISTENER
    //==============================================================================================
    @Override
    public void onLocationChanged(Location location) {
        strLat = Location.convert(location.getLatitude(), Location.FORMAT_DEGREES);
        strLong = Location.convert(location.getLongitude(), Location.FORMAT_DEGREES);

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    //==============================================================================================
    // PUBLIC FUNCTIONS
    //==============================================================================================
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // NICE PREMISSION GRADED
                smsManager = SmsManager.getDefault();
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
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
        builder.setMessage(getString(R.string.alertDialog))
                // Set Title
                .setTitle("Warning");

        // Set Listener for on click OK
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
        editor.putString("mobile1", "6944051295");
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
                    sendSms();
                }
                isTimerEnabled = false;
            }
        };
    }

    private void sendSms() {
        String mobile = getApplicationContext()
                .getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
                .getString("mobile1", "no");

        ArrayList<String> textSms = new ArrayList<>();
        textSms.add(getString(R.string.sos_msg));
        textSms.add(getString(R.string.lat));
        textSms.add(strLat);
        textSms.add(getString(R.string.lon));
        textSms.add(strLong);
        textSms.add(getString(R.string.end_sos_msg));

        smsManager.sendMultipartTextMessage(mobile, null, textSms, null, null);
    }

}
