package com.papadopoulou.christina.unipismartalert;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    protected FirebaseDatabase database;
    private DatabaseReference myRef;
    private EditText editTextName;
    protected Button buttonSingUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextName = findViewById(R.id.editTextName);
        buttonSingUp = findViewById(R.id.buttonSingUp);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("message");

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
                    Log.e("Get Data",  currentDataSnapshot.getKey());

                    String usr = currentDataSnapshot.getKey();

                    for(DataSnapshot makisSnap : dataSnapshot.child(usr).getChildren()){

                        String val = makisSnap.getValue(String.class);
                        if(val.equals("200")){

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
    }
}
