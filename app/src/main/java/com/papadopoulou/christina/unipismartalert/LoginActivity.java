package com.papadopoulou.christina.unipismartalert;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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


public class LoginActivity extends AppCompatActivity {

    public static final String USERS = "users";
    protected FirebaseDatabase database;
    public DatabaseReference myRef;
    final ArrayList<String> dataBaseLoginUsers = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText editTextName = findViewById(R.id.editTextName);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        Button buttonSignUp = findViewById(R.id.buttonSingUp);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference(USERS);

        // Read database
        myRef.addValueEventListener(valueEventListener);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String currentLoginUsername = editTextName.getText().toString().toLowerCase().trim();
                boolean  userExist = false;

                if(currentLoginUsername.equals("")){ return; }


                // Check if user exists
                for (String loginUser: dataBaseLoginUsers) {
                    if (loginUser.equals(currentLoginUsername)) {
                        userExist = true;
                        break;
                    }
                }

                if(userExist){
                    // Start User Activity
                    Intent intent = new Intent(getApplicationContext(), UserActivity.class);
                    intent.putExtra("username", currentLoginUsername);
                    startActivity(intent);
                    Toast.makeText(getApplicationContext(), getString(R.string.login_succes), Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.login_error), Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Start sign up Activity
                Intent intent = new Intent(getApplicationContext(),SingUpActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Remove listener because when i change my database the event listener called in All Activity
        myRef.removeEventListener(valueEventListener);
    }

    ValueEventListener valueEventListener =  new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            for (DataSnapshot currentDataSnapshot : dataSnapshot.getChildren()) {
                //Add the users name in array list
                String dataBaseUser = currentDataSnapshot.getKey();
                dataBaseLoginUsers.add(dataBaseUser);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
}
