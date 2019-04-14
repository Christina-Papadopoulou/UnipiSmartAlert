package com.papadopoulou.christina.unipismartalert;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class SingUpActivity extends AppCompatActivity {
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_up);

        final EditText editTextUsername = findViewById(R.id.editTextUsername);
        Button buttonRegister = findViewById(R.id.buttonRegister);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference(LoginActivity.USERS);

        final HashMap<String, Characteristics> usersMap = new HashMap<>();

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String username = editTextUsername.getText().toString().trim().toLowerCase();

                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot currentDataSnapshot : dataSnapshot.getChildren()) {
                            String dataBaseUser = currentDataSnapshot.getKey();
                            //Log.e("Get Data",  currentDataSnapshot.getKey());
                            if (dataBaseUser.equals(username)) {
                                Toast.makeText(getApplicationContext(), "The user exists. Enter another username", Toast.LENGTH_SHORT).show();
                            } else {
                                User user = new User(editTextUsername.getText().toString().trim().toLowerCase());
                                Characteristics characteristics = new Characteristics();
                                usersMap.put(user.getUsername(), characteristics);
                                myRef.setValue(usersMap);

                                //TODO call back to Main Activity
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }
}
