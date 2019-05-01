package com.papadopoulou.christina.unipismartalert;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;


public class LoginActivity extends AppCompatActivity {

    public static final String USERS = "users";
    protected FirebaseDatabase database;
    private DatabaseReference myRef;
    private final ArrayList<String> dataBaseLoginUsers = new ArrayList<>();

    private SharedPreferences sharedPref;
    private String language;
    private Button buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read Languages from shared prefs
        sharedPref = getApplicationContext()
                .getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        language = getApplicationContext()
                .getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
                .getString("language", "en");

        // Apply Language
        Locale locale  = new Locale(language);
        Configuration config = getBaseContext().getResources().getConfiguration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        // Render view
        setContentView(R.layout.activity_login);

        // Find Views
        final EditText editTextName = findViewById(R.id.editTextName);
        buttonLogin = findViewById(R.id.buttonLogin);
        Button buttonSignUp = findViewById(R.id.buttonSingUp);
        Switch switchLanguage = findViewById(R.id.switchLanguage);

        buttonLogin.setEnabled(false);

        // Init switch
        if(language.equals("en")){
            switchLanguage.setChecked(false);
        }else{
            switchLanguage.setChecked(true);
        }

        // Read database
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference(USERS);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String currentLoginUsername = editTextName.getText().toString().toLowerCase().trim();
                boolean  userExist = false;

                if(currentLoginUsername.equals("")){ return; }

                for (String loginUser: dataBaseLoginUsers) {
                    if (loginUser.equals(currentLoginUsername)) {
                        userExist = true;
                        break;
                    }
                }

                if(userExist){
                    // Start User Activity
                    Intent intent = new Intent(getApplicationContext(), UserActivity.class);
                    // Send string data from LoginActivity to UserActivity
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


        switchLanguage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPref.edit();

                if(isChecked){
                    editor.putString("language", "el");
                }else{
                    editor.putString("language", "en");
                }

                editor.apply();

                // Restart Activity
                Intent intent = getIntent();
                finish();
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

    @Override
    protected void onResume() {
        super.onResume();
        myRef.addValueEventListener(valueEventListener);
    }

    ValueEventListener valueEventListener =  new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            for (DataSnapshot currentDataSnapshot : dataSnapshot.getChildren()) {
                //Add the users name in array list
                String dataBaseUser = currentDataSnapshot.getKey();
                dataBaseLoginUsers.add(dataBaseUser);
            }
            buttonLogin.setEnabled(true);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
}
