package com.rodolfimatilde.arrowheaddemo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        insertSharedPreferencesValue();

        ImageView settings = findViewById(R.id.settings);
        settings.setOnClickListener(thisView -> save());
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Sure to go back and lose all change?")
                .setPositiveButton("Yes", (dialog, which) -> finish())
                .setNegativeButton("No", null)
                .show();
    }

    private void insertSharedPreferencesValue(){
        SharedPreferences pref = getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_PRIVATE);

        EditText mySystemName = findViewById(R.id.mySystemName);
        mySystemName.setText(pref.getString(MainActivity.MY_SYSTEM_NAME, ""));

        EditText myPort = findViewById(R.id.myPort);
        myPort.setText(String.valueOf(pref.getInt(MainActivity.MY_PORT, 0)));

        EditText arrowheadAddress = findViewById(R.id.arrowheadAddress);
        arrowheadAddress.setText(pref.getString(MainActivity.ARROWHEAD_ADDRESS, ""));

        EditText dbSystemName = findViewById(R.id.dbSystemName);
        dbSystemName.setText(pref.getString(MainActivity.DATABASE_SYSTEM_NAME, ""));

        EditText dbAddress = findViewById(R.id.dbAddress);
        dbAddress.setText(pref.getString(MainActivity.DATABASE_IP_ADDRESS, ""));

        EditText dbPort = findViewById(R.id.dbPort);
        dbPort.setText(String.valueOf(pref.getInt(MainActivity.DATABASE_PORT, 0)));

        EditText dbGetData = findViewById(R.id.dbGetData);
        dbGetData.setText(pref.getString(MainActivity.DATABASE_GET_DATA, ""));

        EditText dbSetConfig = findViewById(R.id.dbSetConfig);
        dbSetConfig.setText(pref.getString(MainActivity.DATABASE_SET_CONFIG, ""));

        EditText dbFill = findViewById(R.id.dbFill);
        dbFill.setText(pref.getString(MainActivity.DATABASE_FILL, ""));

        EditText dbInterval = findViewById(R.id.dbInterval);
        dbInterval.setText(String.valueOf(pref.getInt(MainActivity.DATABASE_INTERVAL, 0)));

        TextView dbIntervalSuggestion = findViewById(R.id.dbInterval_suggestion);
        dbIntervalSuggestion.setText("Suggestion: " +(pref.getInt(MainActivity.DATABASE_INTERVAL_SUGGESTION, 0)));

        EditText sensorSystemName = findViewById(R.id.sensorSystemName);
        sensorSystemName.setText(pref.getString(MainActivity.SENSOR_SYSTEM_NAME, ""));

        EditText sensorAddress = findViewById(R.id.sensorAddress);
        sensorAddress.setText(pref.getString(MainActivity.SENSOR_IP_ADDRESS, ""));

        EditText sensorPort = findViewById(R.id.sensorPort);
        sensorPort.setText(String.valueOf(pref.getInt(MainActivity.SENSOR_PORT, 0)));

        EditText sensorGetData = findViewById(R.id.sensorGetData);
        sensorGetData.setText(pref.getString(MainActivity.SENSOR_GET_DATA, ""));

        EditText sensorSetConfig = findViewById(R.id.sensorSetConfig);
        sensorSetConfig.setText(pref.getString(MainActivity.SENSOR_SET_CONFIG, ""));

        EditText sensorStart = findViewById(R.id.sensorStart);
        sensorStart.setText(pref.getString(MainActivity.SENSOR_START_DATA, ""));

        EditText sensorStop = findViewById(R.id.sensorStop);
        sensorStop.setText(pref.getString(MainActivity.SENSOR_STOP_DATA, ""));

        EditText sensorSamplingTime = findViewById(R.id.sensorSamplingTime);
        sensorSamplingTime.setText(String.valueOf(pref.getInt(MainActivity.SENSOR_SAMPLING_TIME, 0)));

        TextView sensorSamplingTimeSuggestion = findViewById(R.id.sensorSamplingTime_suggestion);
        sensorSamplingTimeSuggestion.setText("Suggestion: " +(pref.getInt(MainActivity.SENSOR_SAMPLING_TIME_SUGGESTION, 0)));

        EditText sensorBufferLength = findViewById(R.id.sensorBufferLength);
        sensorBufferLength.setText(String.valueOf(pref.getInt(MainActivity.SENSOR_BUFFER_LENGTH, 0)));

        TextView sensorBufferLengthSuggestion = findViewById(R.id.sensorBufferLength_suggestion);
        sensorBufferLengthSuggestion.setText("Suggestion: " +(pref.getInt(MainActivity.SENSOR_BUFFER_LENGTH_SUGGESTION, 0)));
    }

    private void save(){
        SharedPreferences pref = getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        EditText mySystemName = findViewById(R.id.mySystemName);
        if (mySystemName.getText().toString().trim().isEmpty()) {
            mySystemName.setError("Required");
            mySystemName.requestFocus();
            return;
        } else {
            editor.putString(MainActivity.MY_SYSTEM_NAME, mySystemName.getText().toString());
        }

        EditText myPort = findViewById(R.id.myPort);
        if (myPort.getText().toString().trim().isEmpty()) {
            myPort.setError("Required");
            myPort.requestFocus();
            return;
        } else {
            editor.putInt(MainActivity.MY_PORT, Integer.parseInt(myPort.getText().toString()));
        }

        EditText arrowheadAddress = findViewById(R.id.arrowheadAddress);
        if (arrowheadAddress.getText().toString().trim().isEmpty()) {
            arrowheadAddress.setError("Required");
            arrowheadAddress.requestFocus();
            return;
        } else {
            editor.putString(MainActivity.ARROWHEAD_ADDRESS, arrowheadAddress.getText().toString());
        }

        EditText dbSystemName = findViewById(R.id.dbSystemName);
        if (dbSystemName.getText().toString().trim().isEmpty()) {
            dbSystemName.setError("Required");
            dbSystemName.requestFocus();
            return;
        } else {
            editor.putString(MainActivity.DATABASE_SYSTEM_NAME, dbSystemName.getText().toString());
        }

        EditText dbAddress = findViewById(R.id.dbAddress);
        if (dbAddress.getText().toString().trim().isEmpty()) {
            dbAddress.setError("Required");
            dbAddress.requestFocus();
            return;
        } else {
            editor.putString(MainActivity.DATABASE_IP_ADDRESS, dbAddress.getText().toString());
        }

        EditText dbPort = findViewById(R.id.dbPort);
        if (dbPort.getText().toString().trim().isEmpty()) {
            dbPort.setError("Required");
            dbPort.requestFocus();
            return;
        } else {
            editor.putInt(MainActivity.DATABASE_PORT, Integer.parseInt(dbPort.getText().toString()));
        }

        EditText dbGetData = findViewById(R.id.dbGetData);
        if (dbGetData.getText().toString().trim().isEmpty()) {
            dbGetData.setError("Required");
            dbGetData.requestFocus();
            return;
        } else {
            editor.putString(MainActivity.DATABASE_GET_DATA, dbGetData.getText().toString());
        }

        EditText dbSetConfig = findViewById(R.id.dbSetConfig);
        if (dbSetConfig.getText().toString().trim().isEmpty()) {
            dbSetConfig.setError("Required");
            dbSetConfig.requestFocus();
            return;
        } else {
            editor.putString(MainActivity.DATABASE_SET_CONFIG, dbSetConfig.getText().toString());
        }

        EditText dbFill = findViewById(R.id.dbFill);
        if (dbFill.getText().toString().trim().isEmpty()) {
            dbFill.setError("Required");
            dbFill.requestFocus();
            return;
        } else {
            editor.putString(MainActivity.DATABASE_FILL, dbFill.getText().toString());
        }

        EditText dbInterval = findViewById(R.id.dbInterval);
        if (dbInterval.getText().toString().trim().isEmpty()) {
            dbInterval.setError("Required");
            dbInterval.requestFocus();
            return;
        } else {
            editor.putInt(MainActivity.DATABASE_INTERVAL, Integer.parseInt(dbInterval.getText().toString()));
        }

        EditText sensorSystemName = findViewById(R.id.sensorSystemName);
        if (sensorSystemName.getText().toString().trim().isEmpty()) {
            sensorSystemName.setError("Required");
            sensorSystemName.requestFocus();
            return;
        } else {
            editor.putString(MainActivity.SENSOR_SYSTEM_NAME, sensorSystemName.getText().toString());
        }

        EditText sensorAddress = findViewById(R.id.sensorAddress);
        if (sensorAddress.getText().toString().trim().isEmpty()) {
            sensorAddress.setError("Required");
            sensorAddress.requestFocus();
            return;
        } else {
            editor.putString(MainActivity.SENSOR_IP_ADDRESS, sensorAddress.getText().toString());
        }

        EditText sensorPort = findViewById(R.id.sensorPort);
        if (sensorPort.getText().toString().trim().isEmpty()) {
            sensorPort.setError("Required");
            sensorPort.requestFocus();
            return;
        } else {
            editor.putInt(MainActivity.SENSOR_PORT, Integer.parseInt(sensorPort.getText().toString()));
        }

        EditText sensorGetData = findViewById(R.id.sensorGetData);
        if (sensorGetData.getText().toString().trim().isEmpty()) {
            sensorGetData.setError("Required");
            sensorGetData.requestFocus();
            return;
        } else {
            editor.putString(MainActivity.SENSOR_GET_DATA, sensorGetData.getText().toString());
        }

        EditText sensorSetConfig = findViewById(R.id.sensorSetConfig);
        if (sensorSetConfig.getText().toString().trim().isEmpty()) {
            sensorSetConfig.setError("Required");
            sensorSetConfig.requestFocus();
            return;
        } else {
            editor.putString(MainActivity.SENSOR_SET_CONFIG, sensorSetConfig.getText().toString());
        }

        EditText sensorStart = findViewById(R.id.sensorStart);
        if (sensorStart.getText().toString().trim().isEmpty()) {
            sensorStart.setError("Required");
            sensorStart.requestFocus();
            return;
        } else {
            editor.putString(MainActivity.SENSOR_START_DATA, sensorStart.getText().toString());
        }

        EditText sensorStop = findViewById(R.id.sensorStop);
        if (sensorStop.getText().toString().trim().isEmpty()) {
            sensorStop.setError("Required");
            sensorStop.requestFocus();
            return;
        } else {
            editor.putString(MainActivity.SENSOR_STOP_DATA, sensorStop.getText().toString());
        }

        EditText sensorSamplingTime = findViewById(R.id.sensorSamplingTime);
        if (sensorSamplingTime.getText().toString().trim().isEmpty()) {
            sensorSamplingTime.setError("Required");
            sensorSamplingTime.requestFocus();
            return;
        } else {
            editor.putInt(MainActivity.SENSOR_SAMPLING_TIME, Integer.parseInt(sensorSamplingTime.getText().toString()));
        }

        EditText sensorBufferLength = findViewById(R.id.sensorBufferLength);
        if (sensorBufferLength.getText().toString().trim().isEmpty()) {
            sensorBufferLength.setError("Required");
            sensorBufferLength.requestFocus();
            return;
        } else {
            editor.putInt(MainActivity.SENSOR_BUFFER_LENGTH, Integer.parseInt(sensorBufferLength.getText().toString()));
        }

        int dbi = Integer.parseInt(dbInterval.getText().toString());
        int sst = Integer.parseInt(sensorSamplingTime.getText().toString());
        int sbl = Integer.parseInt(sensorBufferLength.getText().toString());

        if (dbi>sst*sbl){
            dbInterval.setError("Value too high");
            dbInterval.requestFocus();
            return;
        }

        editor.apply();

        new AlertDialog.Builder(this)
                .setMessage("You want change also the Arrowhead Management Setting?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Intent intent = new Intent();
                    intent.setAction(MainActivity.Action_start_management);
                    intent.putExtra("saveManagement", true);
                    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    this.sendBroadcast(intent);

                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    Intent intent = new Intent();
                    intent.setAction(MainActivity.Action_start_management);
                    intent.putExtra("saveManagement", false);
                    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    this.sendBroadcast(intent);

                    finish();
                })
                .show();


    }

}