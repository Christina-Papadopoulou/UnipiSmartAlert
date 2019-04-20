package com.papadopoulou.christina.unipismartalert;

import android.content.Intent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText editTextName = findViewById(R.id.editTextName);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        Button buttonSignUp = findViewById(R.id.buttonSingUp);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference(USERS);

        final ArrayList<String> dataBaseLoginUsers = new ArrayList<>();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {// PIthanoata signle value listenre
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot currentDataSnapshot : dataSnapshot.getChildren()) {
                    String dataBaseUser = currentDataSnapshot.getKey();
                    dataBaseLoginUsers.add(dataBaseUser);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String currentLoginUsername = editTextName.getText().toString().toLowerCase().trim();

                if(currentLoginUsername.equals("")){ return; }

                for (String loginUser: dataBaseLoginUsers) {
                    if (loginUser.equals(currentLoginUsername)) {
                        Toast.makeText(getApplicationContext(), "Login Success", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(getApplicationContext(), UserActivity.class);
                        intent.putExtra("username", currentLoginUsername);
                        startActivity(intent);

                        break;
                    } else {
                        Toast.makeText(getApplicationContext(), "The user does not exists. Please Sing Up", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),SingUpActivity.class);
                startActivityForResult(intent, 200);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == 200){
            //TODO start User Activity
            Log.e("JIM", "onActivityRes");
        }
    }
}
