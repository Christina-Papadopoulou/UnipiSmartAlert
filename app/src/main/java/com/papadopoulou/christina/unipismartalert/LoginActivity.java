package com.papadopoulou.christina.unipismartalert;

import android.content.Intent;
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


public class LoginActivity extends AppCompatActivity {

    public static final String USERS = "users";
    protected FirebaseDatabase database;
    public DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText editTextName = findViewById(R.id.editTextName);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        Button buttonSignUp = findViewById(R.id.buttonSingUp);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference(USERS);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String currentLoginUsername = editTextName.getText().toString().toLowerCase().trim();

                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot currentDataSnapshot : dataSnapshot.getChildren()) {

                            String dataBaseUser = currentDataSnapshot.getKey();

                            if (dataBaseUser.equals(currentLoginUsername)) {
                                Toast.makeText(getApplicationContext(), "OK", Toast.LENGTH_SHORT).show();
                                // TODO Start User Activity
                                return;
                            } else {
                                Toast.makeText(getApplicationContext(), "The user does not exists. Please Sing Up", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),SingUpActivity.class);
                startActivity(intent);
            }
        });
    }
}
