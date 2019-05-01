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

import java.util.ArrayList;

import static com.papadopoulou.christina.unipismartalert.LoginActivity.USERS;

public class SingUpActivity extends AppCompatActivity {
    private DatabaseReference myRef;
    private final ArrayList<String> dataBaseUsers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_up);

        final EditText editTextUsername = findViewById(R.id.editTextUsername);
        Button buttonRegister = findViewById(R.id.buttonRegister);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference(USERS);

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Take the input of the user and make it to string, to lowercases and without spaces
                String currenUserName = editTextUsername.getText().toString().trim().toLowerCase();
                boolean userExist = false;
                // Username "" is not acceptable
                if(currenUserName.equals("")){ return; }

                //Read all users from Array List dataBaseUsers
                for (String user: dataBaseUsers) {
                    if(user.equals(currenUserName)){
                        userExist = true;
                    }
                }

                if(!userExist) {
                    Characteristics characteristics = new Characteristics(0, 0);
                    myRef.child(currenUserName).setValue(characteristics);

                    Toast.makeText(getApplicationContext(), getString(R.string.register_success), Toast.LENGTH_SHORT).show();
                    //Destroy Activity
                    finish();
                }else{
                    //If username exists, inform user that "The user exists. Enter another username"
                    Toast.makeText(getApplicationContext(), getString(R.string.register_error), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        myRef.addValueEventListener(valueEventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //When Activity is onStop I remove valueEventListener otherwise it will be called everywhere in my app
        //when i would have a change in a user
        myRef.removeEventListener(valueEventListener);
    }

    ValueEventListener valueEventListener = new ValueEventListener() {
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
    };

}
