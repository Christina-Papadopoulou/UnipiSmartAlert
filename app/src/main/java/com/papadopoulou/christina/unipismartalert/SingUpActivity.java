package com.papadopoulou.christina.unipismartalert;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
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

        final ArrayList<String> dataBaseUsers = new ArrayList<>();

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot currentDataSnapshot : dataSnapshot.getChildren()) {
                    String user = currentDataSnapshot.getKey();
                    dataBaseUsers.add(user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currenUserName = editTextUsername.getText().toString().trim().toLowerCase();

                // TODO show message if the editext is empty

                for (String user: dataBaseUsers) {
                    if(user.equals(currenUserName)){
                        Toast.makeText(getApplicationContext(), "The user exists. Enter another username", Toast.LENGTH_SHORT).show();
                    }else{
                        User newUser = new User(editTextUsername.getText().toString().trim().toLowerCase());
                        Characteristics characteristics = new Characteristics();
                        myRef.child(newUser.getUsername()).setValue(characteristics);

                        Toast.makeText(getApplicationContext(), "The user created successful !!!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(getApplication(), LoginActivity.class); // In case you want to pass data to main activity just set putExtra
                        setResult(RESULT_OK);
                        finish();
                    }
                }
            }
        });
    }
}
