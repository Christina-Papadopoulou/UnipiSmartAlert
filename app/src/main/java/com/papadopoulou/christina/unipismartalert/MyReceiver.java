package com.papadopoulou.christina.unipismartalert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

public class MyReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPref = context
                .getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
            Toast.makeText(context, "Power just connected. Ready to detect EarthQuake !!!", Toast.LENGTH_SHORT).show();
            editor.putBoolean("readyEarthquake", true);
        } else if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
            Toast.makeText(context, "Power just disconnected. EarthQuake detection ended !!!", Toast.LENGTH_SHORT).show();
            editor.putBoolean("readyEarthquake", false);
        }

        editor.apply();
    }
}
