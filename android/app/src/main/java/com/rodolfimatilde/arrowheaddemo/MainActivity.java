package com.rodolfimatilde.arrowheaddemo;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

//TODO fare grafico diviso per giorni
//TODO perchè non fa n volte il centrale l'orchestrator?

public class MainActivity extends AppCompatActivity {

    //SharedPreferences variables
    public static final String PREF_NAME = "arrowheadDemo";

    /*##########################################
    ###               GENERAL                ###
    ############################################*/
    public static final String MY_IP = "MY_IP";
    public static final String MY_PORT = "MY_PORT";
    public static final String MY_SYSTEM_NAME = "MY_SYSTEM_NAME";

    public static final String MY_SYSTEM_ID = "MY_SYSTEM_ID";

    public static final String CHOREOGRAPHER_ID = "CHOREOGRAPHER_ID";
    public static final String CHOREOGRAPHER_PLAN_ID = "CHOREOGRAPHER_PLAN_ID";

    public static final String ARROWHEAD_ADDRESS = "ARROWHEAD_ADDRESS";

    /*##########################################
    ###               DATABASE               ###
    ############################################*/
    public static final String DATABASE_IP_ADDRESS = "DATABASE_IP_ADDRESS";
    public static final String DATABASE_PORT = "DATABASE_PORT";
    public static final String DATABASE_FILL = "DATABASE_FILL";
    public static final String DATABASE_FILL_ID = "DATABASE_FILL_ID";
    public static final String DATABASE_GET_DATA = "DATABASE_GET_DATA";
    public static final String DATABASE_GET_DATA_ID = "DATABASE_GET_DATA_ID";
    public static final String DATABASE_DELETE_DATA = "DATABASE_DELETE_DATA";
    public static final String DATABASE_DELETE_DATA_ID = "DATABASE_DELETE_DATA_ID";
    public static final String DATABASE_SET_CONFIG = "DATABASE_SET_CONFIG";
    public static final String DATABASE_SET_CONFIG_ID = "DATABASE_SET_CONFIG_ID";
    public static final String DATABASE_SYSTEM_NAME = "DATABASE_SYSTEM_NAME";
    public static final String DATABASE_SYSTEM_ID = "DATABASE_SYSTEM_ID";

    public static final String DATABASE_INTERVAL = "DATABASE_INTERVAL";
    public static final String DATABASE_INTERVAL_SUGGESTION = "DATABASE_INTERVAL_SUGGESTION";

    /*##########################################
    ###                SENSOR                ###
    ############################################*/
    public static final String SENSOR_IP_ADDRESS = "SENSOR_IP_ADDRESS";
    public static final String SENSOR_PORT = "SENSOR_PORT";
    public static final String SENSOR_GET_DATA = "SENSOR_GET_DATA";
    public static final String SENSOR_GET_DATA_ID = "SENSOR_GET_DATA_ID";
    public static final String SENSOR_SET_CONFIG = "SENSOR_SET_CONFIG";
    public static final String SENSOR_SET_CONFIG_ID = "SENSOR_SET_CONFIG_ID";
    public static final String SENSOR_START_DATA = "SENSOR_START_DATA";
    public static final String SENSOR_START_DATA_ID = "SENSOR_START_DATA_ID";
    public static final String SENSOR_STOP_DATA = "SENSOR_STOP_DATA";
    public static final String SENSOR_STOP_DATA_ID = "SENSOR_STOP_DATA_ID";
    public static final String SENSOR_SYSTEM_NAME = "SENSOR_SYSTEM_NAME";
    public static final String SENSOR_SYSTEM_ID = "SENSOR_SYSTEM_ID";

    public static final String SENSOR_SAMPLING_TIME = "SENSOR_SAMPLING_TIME";
    public static final String SENSOR_BUFFER_LENGTH = "SENSOR_BUFFER_LENGTH";
    public static final String SENSOR_SAMPLING_TIME_SUGGESTION = "SENSOR_SAMPLING_TIME_SUGGESTION";
    public static final String SENSOR_BUFFER_LENGTH_SUGGESTION = "SENSOR_BUFFER_LENGTH_SUGGESTION";

    private RadioButton lrb0, lrb1, lrb2;
    private TextView load_error;

    private SharedPreferences pref;

    private static final String Action_dbGetDataURL = "arrowheadDemo.dbGetDataURL";
    private static final String Action_dbDeleteDataURL = "arrowheadDemo.dbDeleteDataURL";
    private static final String Action_dbSetConfigURL = "arrowheadDemo.dbSetConfigURL";
    private static final String Action_sensorSetConfigURL = "arrowheadDemo.sensorSetConfigURL";
    private static final String Action_choreographer = "arrowheadDemo.choreographer";
    private static final String Action_management = "arrowheadDemo.management";
    public static final String Action_start_management = "arrowheadDemo.start.management";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Action_dbGetDataURL);
        filter.addAction(Action_dbDeleteDataURL);
        filter.addAction(Action_dbSetConfigURL);
        filter.addAction(Action_sensorSetConfigURL);
        filter.addAction(Action_choreographer);
        filter.addAction(Action_management);
        filter.addAction(Action_start_management);
        this.registerReceiver(new Receiver(), filter);

        addRadioButtonListener();

        addSharedPreferencesValue();

        ImageView settings = findViewById(R.id.settings);
        settings.setOnClickListener(thisView -> {
            Intent i = new Intent();
            i.setClass(this, SettingsActivity.class);
            startActivity(i);
        });

        Button deleteButton = findViewById(R.id.deleteData);
        deleteButton.setOnClickListener(thisView -> new AlertDialog.Builder(this)
                .setMessage("Sure to delete all data in database?")
                .setPositiveButton("Yes", (dialog, which) -> startDeleteDatabaseData())
                .setNegativeButton("No", null)
                .show());

        Button startChoreographer = findViewById(R.id.startChoreographer);
        startChoreographer.setOnClickListener(thisView -> startChoreographer());

        lrb0.setChecked(true);
        startCommunicationForDbData(0);
    }

    @Override
    protected void onDestroy() {
        String ipAddress = pref.getString(MY_IP, "");
        int port = pref.getInt(MY_PORT, 0);
        String system_name = pref.getString(MY_SYSTEM_NAME, "");
        Arrowhead.delete_System(getApplicationContext(), ipAddress, port, system_name);

        super.onDestroy();
    }

    private void addSharedPreferencesValue(){
        //set preferences
        pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        if(!pref.contains("initialized")){
            String ip = "192.168.178.141";

            SharedPreferences.Editor editor = pref.edit();

            editor.putBoolean("initialized", true);

            editor.putInt(MY_PORT, 5000);
            editor.putString(MY_SYSTEM_NAME, "mySystem");
            editor.putString(ARROWHEAD_ADDRESS, ip);

            editor.putString(DATABASE_IP_ADDRESS, ip);
            editor.putInt(DATABASE_PORT, 5001);
            editor.putString(DATABASE_FILL, "database-fill");
            editor.putString(DATABASE_GET_DATA, "database-getData");
            editor.putString(DATABASE_DELETE_DATA, "database-deleteData");
            editor.putString(DATABASE_SET_CONFIG, "database-setConfig");
            editor.putString(DATABASE_SYSTEM_NAME, "databaseSystem");

            editor.putInt(DATABASE_INTERVAL, 10);
            editor.putInt(DATABASE_INTERVAL_SUGGESTION, 10);

            editor.putString(SENSOR_IP_ADDRESS, ip);
            editor.putInt(SENSOR_PORT, 5000);
            editor.putString(SENSOR_GET_DATA, "batterySensor-getData");
            editor.putString(SENSOR_SET_CONFIG, "batterySensor-setConfig");
            editor.putString(SENSOR_START_DATA, "batterySensor-startData");
            editor.putString(SENSOR_STOP_DATA, "batterySensor-stopData");
            editor.putString(SENSOR_SYSTEM_NAME, "batterySensorSystem");

            editor.putInt(SENSOR_SAMPLING_TIME, 2);
            editor.putInt(SENSOR_BUFFER_LENGTH, 100);
            editor.putInt(SENSOR_SAMPLING_TIME_SUGGESTION, 2);
            editor.putInt(SENSOR_BUFFER_LENGTH_SUGGESTION, 100);

            editor.apply();
            System.out.println("Shared Preferences initialized");
        }

        updateSettings(true);
    }

    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case Action_dbGetDataURL:
                    dbGetDataURLReceiver(intent);
                    break;
                case Action_dbDeleteDataURL:
                    dbDeleteDataURLReceiver(context, intent);
                    break;
                case Action_dbSetConfigURL:
                    dbSetConfigURLReceiver(context, intent);
                    break;
                case Action_sensorSetConfigURL:
                    sensorSetConfigURLReceiver(context, intent);
                    break;
                case Action_choreographer:
                    startChoreographerReceiver(intent);
                    break;
                case Action_management:
                    managementReceiver(context, intent);
                    break;
                case Action_start_management:
                    updateSettings(intent.getBooleanExtra("saveManagement", false));
                    break;
            }
        }
    }

    private void updateSettings(boolean saveManagement){
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String ipAddress =  Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(MY_IP, ""+ipAddress);
        editor.apply();
        System.out.println("My IP: "+ipAddress);

        int port = pref.getInt(MY_PORT, 0);
        String system_name = pref.getString(MY_SYSTEM_NAME, "");
        Arrowhead.register_System(getApplicationContext(), ipAddress, port, system_name);

        startUpdateIntervalDatabase();

        startUpdateConfigSensor();

        if (saveManagement) {
            startUpdateManagement();
        }
    }



    /*##########################################
    ###          LOAD DATA IN GRAPH          ###
    ############################################*/

    private void addRadioButtonListener() {
        lrb0 = findViewById(R.id.zero);
        lrb1 = findViewById(R.id.one);
        lrb2 = findViewById(R.id.two);
        load_error = findViewById(R.id.load_error);

        lrb0.setOnClickListener(onClickLevelRadioButton);
        lrb1.setOnClickListener(onClickLevelRadioButton);
        lrb2.setOnClickListener(onClickLevelRadioButton);
    }

    private final View.OnClickListener onClickLevelRadioButton = v -> setRadioButtonChecked();

    private void setRadioButtonChecked(){
        if (lrb0.isChecked()) {
            startCommunicationForDbData(0);
        }
        if (lrb1.isChecked()) {
            startCommunicationForDbData(1);
        }
        if (lrb2.isChecked()) {
            startCommunicationForDbData(2);
        }
    }

    private void startCommunicationForDbData(int level){
        GraphView graph = findViewById(R.id.graph);
        graph.removeAllSeries();
        switch (level) {
            case 0:
                String database_ipAddress = pref.getString(DATABASE_IP_ADDRESS, "");
                int database_port = pref.getInt(DATABASE_PORT, 0);
                String database_getData = pref.getString(DATABASE_GET_DATA, "");
                String address;
                if (!Objects.equals(database_ipAddress, "") && database_port!=0 && !Objects.equals(database_getData, "")){
                    address = database_ipAddress+":"+database_port+"/"+database_getData;
                }
                else{
                    address = "error";
                }
                Intent intent = new Intent();
                intent.putExtra("URL", address);
                intent.setAction(Action_dbGetDataURL);
                intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                sendBroadcast(intent);
                break;
            case 1:
                String name = pref.getString(DATABASE_GET_DATA, "");
                Arrowhead.find_Service_SR(this, getApplicationContext(), Action_dbGetDataURL, name);
                break;
            case 2:
                String ipaddress = pref.getString(MY_IP, "");
                int port = pref.getInt(MY_PORT, 0);
                String system_name = pref.getString(MY_SYSTEM_NAME, "");
                String db_getData = pref.getString(DATABASE_GET_DATA, "");
                Arrowhead.find_Service_Orchestrator(this, getApplicationContext(), Action_dbGetDataURL, ipaddress, port, system_name, db_getData);
                break;
        }
    }

    private void dbGetDataURLReceiver(Intent intent) {
        String URL = intent.getStringExtra("URL");
        if (Objects.equals(URL, "error") || URL==null){
            load_error.setVisibility(View.VISIBLE);
        }
        else{
            load_error.setVisibility(View.INVISIBLE);
            loadData ld = new loadData();
            ld.execute(URL);
        }
    }


    private class loadData extends AsyncTask<String, Void, String> {

        private ProgressBar waiting;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            load_error.setVisibility(View.INVISIBLE);
            waiting = findViewById(R.id.progressBar_cyclic);
            waiting.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            String urlWebService = params[0];
            HttpURLConnection urlConnection = null;
            try {
                System.out.println("URL database: "+urlWebService);
                URL url = new URL("http://"+urlWebService);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                    if (urlConnection.getResponseCode() == 200){
                        InputStream inputStream = urlConnection.getInputStream();
                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        return bufferedReader.readLine();
                    }
                    else{
                        return "error";
                    }

            } catch (IOException e) {
                e.printStackTrace();
                return "error";
            } finally {
                if (urlConnection!=null){
                    urlConnection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            waiting.setVisibility(View.INVISIBLE);
            if (Objects.equals(s, "error") || s==null){
                load_error.setVisibility(View.VISIBLE);
            }
            else{
                load_error.setVisibility(View.INVISIBLE);
                loadIntoGraph(s);
            }
        }
    }

    private void loadIntoGraph(String json) {
        try{
            GraphView graph = findViewById(R.id.graph);

            graph.setTitle("Battery");
            graph.onDataChanged(false, false);

            graph.getViewport().setScrollable(true); // enables horizontal scrolling
            graph.getViewport().setScrollableY(true); // enables vertical scrolling
            graph.getViewport().setScalable(true);

            graph.getViewport().setYAxisBoundsManual(true);
            graph.getViewport().setMinY(0);
            graph.getViewport().setMaxY(100);

            JSONArray jsonArray = new JSONArray(json);

            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMinX(0);
            graph.getViewport().setMaxX(jsonArray.length());

            DataPoint[] dataPoints = new DataPoint[jsonArray.length()];
            double value, lastValue = 0;
            String lastTimestamp = "", startTimestamp = "";

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray obj = jsonArray.optJSONArray(i);
                value = obj.getDouble(2);
                dataPoints[i] = new DataPoint(i, value);
                if (value == lastValue){
                    lastTimestamp = obj.getString(0);
                }
                else{
                    lastValue = value;
                    startTimestamp = obj.getString(0);
                    lastTimestamp = obj.getString(0);
                }
            }

            long last = 130000, start=0;
            if (!lastTimestamp.equals("") && !startTimestamp.equals("")){
                last = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(lastTimestamp)).getTime();
                start = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startTimestamp)).getTime();
            }

            System.out.println("last "+last);
            System.out.println("start"+start);
            System.out.println("differences in seconds "+((last - start) / 2000));

            int newTime = Math.min((int)((last - start) / 2000), 60);
            int newInterval = newTime*5;
            int newLenght = newTime*50;
            SharedPreferences.Editor editor = pref.edit();
            editor.putInt(DATABASE_INTERVAL_SUGGESTION, newInterval);
            editor.putInt(SENSOR_SAMPLING_TIME_SUGGESTION, newTime);
            editor.putInt(SENSOR_BUFFER_LENGTH_SUGGESTION, newLenght);
            editor.apply();

            LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);
            series.setDrawDataPoints(true);
            graph.addSeries(series);

            TextView last_update = findViewById(R.id.last_update_label);
            Date currentTime = Calendar.getInstance().getTime();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            last_update.setText(format.format(currentTime));
        }
        catch (JSONException | ParseException e) {
            e.printStackTrace();
        }
    }



    /*##########################################
    ###          START CHOREOGRAPHER         ###
    ############################################*/

    private void startChoreographer(){
        EditText iterationNumber = findViewById(R.id.iterationNumber);
        if (iterationNumber.getText().toString().trim().isEmpty()) {
            iterationNumber.setError("Required");
            iterationNumber.requestFocus();
        } else {
            int repetitions = Integer.parseInt(iterationNumber.getText().toString());
            if (repetitions<=0) {
                iterationNumber.setError("Value too high");
                iterationNumber.requestFocus();
            } else {
                String database_fill = pref.getString(DATABASE_FILL, "");
                String sensor_start = pref.getString(SENSOR_START_DATA, "");
                String sensor_stop = pref.getString(SENSOR_STOP_DATA, "");

                Arrowhead.save_management_Choreographer(this, getApplicationContext(), Action_choreographer, repetitions, sensor_start, database_fill, sensor_stop);
            }
        }
    }

    private void startChoreographerReceiver(Intent intent) {
        String result = intent.getStringExtra("result");
        if (Objects.equals(result, "error") || result==null){
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("ERROR")
                    .setMessage("Error with update of managementChoreographer")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            System.out.println("Error with update of managementChoreographer");
        }
        else{
            //Toast.makeText(_activity, "managementChoreographer update with success", Toast.LENGTH_LONG).show();
            System.out.println("managementChoreographer update with success");
            Choreographer c = new Choreographer();
            c.execute();
        }
    }

    private class Choreographer extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            try {
                String arrowhead_address = pref.getString(ARROWHEAD_ADDRESS, "");
                int id = pref.getInt(CHOREOGRAPHER_PLAN_ID, 0);
                String query= "[ { \"id\": "+id+" }]";
                URL url = new URL("http://"+arrowhead_address+":8457/choreographer/mgmt/session/start");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");

                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json") ;
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                BufferedOutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
                writer.write(query);
                writer.flush();
                writer.close();
                out.close();

                urlConnection.connect();
                if (urlConnection.getResponseCode() == 201){
                    return "OK";
                }
                else{
                    return "error";
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "error";
            } finally {
                if (urlConnection!=null){
                    urlConnection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (Objects.equals(s, "error") || s==null){
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("ERROR")
                        .setMessage("Error with start Choreographer")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                System.out.println("Error with start Choreographer");
            }
            else{
                Toast.makeText(MainActivity.this, "Start Choreographer with success", Toast.LENGTH_LONG).show();
                System.out.println("Start Choreographer with success");
                setRadioButtonChecked();
            }
        }
    }



    /*##########################################
    ###         DELETE DATABASE DATA         ###
    ############################################*/

    private void startDeleteDatabaseData(){
        if (lrb0.isChecked()) {
            String database_ipAddress = pref.getString(DATABASE_IP_ADDRESS, "");
            int database_port = pref.getInt(DATABASE_PORT, 0);
            String database_deleteData = pref.getString(DATABASE_DELETE_DATA, "");
            String address;
            if (!Objects.equals(database_ipAddress, "") && database_port!=0 && !Objects.equals(database_deleteData, "")){
                address = database_ipAddress+":"+database_port+"/"+database_deleteData;
            }
            else{
                address = "error";
            }

            Intent intent = new Intent();
            intent.putExtra("URL", address);
            intent.setAction(Action_dbDeleteDataURL);
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            sendBroadcast(intent);
        }
        if (lrb1.isChecked()) {
            String name = pref.getString(DATABASE_DELETE_DATA, "");
            Arrowhead.find_Service_SR(this, getApplicationContext(), Action_dbDeleteDataURL, name);
        }
        if (lrb2.isChecked()) {
            String ipaddress = pref.getString(MY_IP, "");
            int port = pref.getInt(MY_PORT, 0);
            String system_name = pref.getString(MY_SYSTEM_NAME, "");
            String db_deleteData = pref.getString(DATABASE_DELETE_DATA, "");
            Arrowhead.find_Service_Orchestrator(this, getApplicationContext(), Action_dbDeleteDataURL, ipaddress, port, system_name, db_deleteData);
        }
    }

    private void dbDeleteDataURLReceiver(Context context, Intent intent) {
        String URL = intent.getStringExtra("URL");
        if (Objects.equals(URL, "error") || URL==null){
            new AlertDialog.Builder(context)
                    .setTitle("ERROR")
                    .setMessage("Error with delete of Database Data")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            System.out.println("Error with delete of Database Data #1");
        }
        else{
            deleteDatabaseData ddd = new deleteDatabaseData();
            ddd.execute(URL);
        }
    }

    private class deleteDatabaseData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String urlWebService = params[0];
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL("http://"+urlWebService);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.connect();

                if (urlConnection.getResponseCode() == 200){
                    return "OK";
                }
                else{
                    return "error";
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "error";
            } finally {
                if (urlConnection!=null){
                    urlConnection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (Objects.equals(s, "error") || s==null){
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("ERROR")
                        .setMessage("Error with delete of Database Data")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                System.out.println("Error with delete of Database Data #2");
            }
            else{
                Toast.makeText(MainActivity.this, "Database Data delete with success", Toast.LENGTH_LONG).show();
                System.out.println("Database Data delete with success");
                setRadioButtonChecked();
            }
        }
    }



    /*##########################################
    ###       UPDATE DATABASE INTERVAL       ###
    ############################################*/

    private void startUpdateIntervalDatabase(){
        if (lrb0.isChecked()) {
            String database_ipAddress = pref.getString(DATABASE_IP_ADDRESS, "");
            int database_port = pref.getInt(DATABASE_PORT, 0);
            String database_setConfig = pref.getString(DATABASE_SET_CONFIG, "");
            String address;
            if (!Objects.equals(database_ipAddress, "") && database_port!=0 && !Objects.equals(database_setConfig, "")){
                address = database_ipAddress+":"+database_port+"/"+database_setConfig;
            }
            else{
                address = "error";
            }

            Intent intent = new Intent();
            intent.putExtra("URL", address);
            intent.setAction(Action_dbSetConfigURL);
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            sendBroadcast(intent);
        }
        if (lrb1.isChecked()) {
            String name = pref.getString(DATABASE_SET_CONFIG, "");
            Arrowhead.find_Service_SR(this, getApplicationContext(), Action_dbSetConfigURL, name);
        }
        if (lrb2.isChecked()) {
            String ipaddress = pref.getString(MY_IP, "");
            int port = pref.getInt(MY_PORT, 0);
            String system_name = pref.getString(MY_SYSTEM_NAME, "");
            String db_setConfig = pref.getString(DATABASE_SET_CONFIG, "");
            Arrowhead.find_Service_Orchestrator(this, getApplicationContext(), Action_dbSetConfigURL, ipaddress, port, system_name, db_setConfig);
        }
    }

    private void dbSetConfigURLReceiver(Context context, Intent intent) {
        String URL = intent.getStringExtra("URL");
        if (Objects.equals(URL, "error") || URL==null){
            new AlertDialog.Builder(context)
                    .setTitle("ERROR")
                    .setMessage("Error with update of Database Interval")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            System.out.println("Error with update of Database Interval #1");
        }
        else{
            updateIntervalDatabase ld = new updateIntervalDatabase();
            ld.execute(URL);
        }
    }

    private class updateIntervalDatabase extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String urlWebService = params[0];
            HttpURLConnection urlConnection = null;
            try {
                int interval = pref.getInt(DATABASE_INTERVAL, 10);
                String query = "{\"interval\": "+interval+"}";

                URL url = new URL("http://"+urlWebService);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");

                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json") ;
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                BufferedOutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
                writer.write(query);
                writer.flush();
                writer.close();
                out.close();

                urlConnection.connect();

                if (urlConnection.getResponseCode() == 200){
                    InputStream inputStream = urlConnection.getInputStream();
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String response = bufferedReader.readLine();

                    if (response.contains("Config Update")) {
                        return "OK";
                    }
                    else{
                        return "error";
                    }
                }
                else{
                    return "error";
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "error";
            } finally {
                if (urlConnection!=null){
                    urlConnection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (Objects.equals(s, "error") || s==null){
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("ERROR")
                        .setMessage("Error with update of Database Interval")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                System.out.println("Error with update of Database Interval #2");
            }
            else{
                Toast.makeText(MainActivity.this, "Database Interval update with success", Toast.LENGTH_LONG).show();
                System.out.println("Database Interval update with success");
            }
        }
    }



    /*##########################################
    ###         UPDATE SENSOR CONFIG         ###
    ############################################*/

    private void startUpdateConfigSensor(){
        if (lrb0.isChecked()) {
            String sensor_ipAddress = pref.getString(SENSOR_IP_ADDRESS, "");
            int sensor_port = pref.getInt(SENSOR_PORT, 0);
            String sensor_setConfig = pref.getString(SENSOR_SET_CONFIG, "");
            String address;
            if (!Objects.equals(sensor_ipAddress, "") && sensor_port!=0 && !Objects.equals(sensor_setConfig, "")){
                address = sensor_ipAddress+":"+sensor_port+"/"+sensor_setConfig;
            }
            else{
                address = "error";
            }

            Intent intent = new Intent();
            intent.putExtra("URL", address);
            intent.setAction(Action_sensorSetConfigURL);
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            sendBroadcast(intent);
        }
        if (lrb1.isChecked()) {
            String name = pref.getString(SENSOR_SET_CONFIG, "");
            Arrowhead.find_Service_SR(this, getApplicationContext(), Action_sensorSetConfigURL, name);
        }
        if (lrb2.isChecked()) {
            String ipaddress = pref.getString(MY_IP, "");
            int port = pref.getInt(MY_PORT, 0);
            String system_name = pref.getString(MY_SYSTEM_NAME, "");
            String sensor_setConfig = pref.getString(SENSOR_SET_CONFIG, "");
            Arrowhead.find_Service_Orchestrator(this, getApplicationContext(), Action_sensorSetConfigURL, ipaddress, port, system_name, sensor_setConfig);
        }
    }

    private void sensorSetConfigURLReceiver(Context context, Intent intent) {
        String URL = intent.getStringExtra("URL");
        if (Objects.equals(URL, "error") || URL==null){
            new AlertDialog.Builder(context)
                    .setTitle("ERROR!")
                    .setMessage("Error with update of Sensor Configuration")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            System.out.println("Error with update of Sensor Configuration #1");
        }
        else{
            updateConfigSensor ld = new updateConfigSensor();
            ld.execute(URL);
        }
    }

    private class updateConfigSensor extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String urlWebService = params[0];
            HttpURLConnection urlConnection = null;
            try {
                int buffer_length = pref.getInt(SENSOR_BUFFER_LENGTH, 100);
                int sampling_time = pref.getInt(SENSOR_SAMPLING_TIME, 2);
                String query = "{\"sampling_time\": "+sampling_time+", \"buffer_length\": "+buffer_length+"}";

                URL url = new URL("http://"+urlWebService);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");

                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json") ;
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                BufferedOutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
                writer.write(query);
                writer.flush();
                writer.close();
                out.close();

                urlConnection.connect();

                if (urlConnection.getResponseCode() == 200){
                    InputStream inputStream = urlConnection.getInputStream();
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String response = bufferedReader.readLine();

                    if (response.contains("Config Update")) {
                        return "OK";
                    }
                    else{
                        return "error";
                    }
                }
                else{
                    return "error";
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "error";
            } finally {
                if (urlConnection!=null){
                    urlConnection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (Objects.equals(s, "error") || s==null){
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("ERROR")
                        .setMessage("Error with update of Sensor Configuration")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                System.out.println("Error with update of Sensor Configuration #2");
            }
            else{
                Toast.makeText(MainActivity.this, "Sensor Configuration update with success", Toast.LENGTH_LONG).show();
                System.out.println("Sensor Configuration update with success");
            }
        }
    }



    /*##########################################
    ###    UPDATE MANAGEMENT ORCHESTRATOR    ###
    ############################################*/

    private void startUpdateManagement(){
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(MainActivity.SENSOR_SYSTEM_ID, 0);
        editor.putInt(MainActivity.CHOREOGRAPHER_ID, 0);
        editor.putInt(MainActivity.SENSOR_GET_DATA_ID, 0);
        editor.putInt(MainActivity.SENSOR_SET_CONFIG_ID, 0);
        editor.putInt(MainActivity.SENSOR_START_DATA_ID, 0);
        editor.putInt(MainActivity.SENSOR_STOP_DATA_ID, 0);
        editor.putInt(MainActivity.DATABASE_SYSTEM_ID, 0);
        editor.putInt(MainActivity.DATABASE_GET_DATA_ID, 0);
        editor.putInt(MainActivity.DATABASE_DELETE_DATA_ID, 0);
        editor.putInt(MainActivity.DATABASE_SET_CONFIG_ID, 0);
        editor.putInt(MainActivity.DATABASE_FILL_ID, 0);
        editor.apply();

        Arrowhead.find_Service_SR(this, getApplicationContext(), Action_management, "choreographer-service");
        Arrowhead.find_Service_SR(this, getApplicationContext(), Action_management, pref.getString(SENSOR_GET_DATA, ""));
        Arrowhead.find_Service_SR(this, getApplicationContext(), Action_management, pref.getString(SENSOR_SET_CONFIG, ""));
        Arrowhead.find_Service_SR(this, getApplicationContext(), Action_management, pref.getString(SENSOR_START_DATA, ""));
        Arrowhead.find_Service_SR(this, getApplicationContext(), Action_management, pref.getString(SENSOR_STOP_DATA, ""));
        Arrowhead.find_Service_SR(this, getApplicationContext(), Action_management, pref.getString(DATABASE_GET_DATA, ""));
        Arrowhead.find_Service_SR(this, getApplicationContext(), Action_management, pref.getString(DATABASE_DELETE_DATA, ""));
        Arrowhead.find_Service_SR(this, getApplicationContext(), Action_management, pref.getString(DATABASE_SET_CONFIG, ""));
        Arrowhead.find_Service_SR(this, getApplicationContext(), Action_management, pref.getString(DATABASE_FILL, ""));
    }

    private void managementReceiver(Context context, Intent intent) {
        String URL = intent.getStringExtra("URL");
        if (Objects.equals(URL, "error") || URL==null){
            new AlertDialog.Builder(context)
                    .setTitle("ERROR!")
                    .setMessage("Error with update of Management - "+intent.getStringExtra("NAME"))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            System.out.println("Error with update of Management - "+intent.getStringExtra("NAME"));
        }
        else{
            String name = intent.getStringExtra("NAME");
            int system_id = intent.getIntExtra("SYSTEM_ID", 0);
            int service_id = intent.getIntExtra("SERVICE_ID", 0);
            SharedPreferences.Editor editor = pref.edit();
            if (Objects.equals(name, "choreographer-service")){
                editor.putInt(MainActivity.CHOREOGRAPHER_ID, system_id);
            }
            if (Objects.equals(name, pref.getString(SENSOR_GET_DATA, ""))){
                editor.putInt(MainActivity.SENSOR_SYSTEM_ID, system_id);
                editor.putInt(MainActivity.SENSOR_GET_DATA_ID, service_id);
            }
            if (Objects.equals(name, pref.getString(SENSOR_SET_CONFIG, ""))){
                editor.putInt(MainActivity.SENSOR_SYSTEM_ID, system_id);
                editor.putInt(MainActivity.SENSOR_SET_CONFIG_ID, service_id);
            }
            if (Objects.equals(name, pref.getString(SENSOR_START_DATA, ""))){
                editor.putInt(MainActivity.SENSOR_SYSTEM_ID, system_id);
                editor.putInt(MainActivity.SENSOR_START_DATA_ID, service_id);
            }
            if (Objects.equals(name, pref.getString(SENSOR_STOP_DATA, ""))){
                editor.putInt(MainActivity.SENSOR_SYSTEM_ID, system_id);
                editor.putInt(MainActivity.SENSOR_STOP_DATA_ID, service_id);
            }
            if (Objects.equals(name, pref.getString(DATABASE_GET_DATA, ""))){
                editor.putInt(MainActivity.DATABASE_SYSTEM_ID, system_id);
                editor.putInt(MainActivity.DATABASE_GET_DATA_ID, service_id);
            }
            if (Objects.equals(name, pref.getString(DATABASE_DELETE_DATA, ""))){
                editor.putInt(MainActivity.DATABASE_SYSTEM_ID, system_id);
                editor.putInt(MainActivity.DATABASE_DELETE_DATA_ID, service_id);
            }
            if (Objects.equals(name, pref.getString(DATABASE_SET_CONFIG, ""))){
                editor.putInt(MainActivity.DATABASE_SYSTEM_ID, system_id);
                editor.putInt(MainActivity.DATABASE_SET_CONFIG_ID, service_id);
            }
            if (Objects.equals(name, pref.getString(DATABASE_FILL, ""))){
                editor.putInt(MainActivity.DATABASE_SYSTEM_ID, system_id);
                editor.putInt(MainActivity.DATABASE_FILL_ID, service_id);
            }
            editor.apply();

            if ((pref.getInt(CHOREOGRAPHER_ID, 0)!=0) && (pref.getInt(SENSOR_SYSTEM_ID, 0)!=0) && (pref.getInt(SENSOR_GET_DATA_ID, 0)!=0) && (pref.getInt(SENSOR_SET_CONFIG_ID, 0)!=0) && (pref.getInt(SENSOR_START_DATA_ID, 0)!=0) && (pref.getInt(SENSOR_STOP_DATA_ID, 0)!=0) && (pref.getInt(DATABASE_SYSTEM_ID, 0)!=0) && (pref.getInt(DATABASE_GET_DATA_ID, 0)!=0) && (pref.getInt(DATABASE_DELETE_DATA_ID, 0)!=0) && (pref.getInt(DATABASE_SET_CONFIG_ID, 0)!=0) && (pref.getInt(DATABASE_FILL_ID, 0)!=0)){
                updateManagement();
            }
        }
    }

    private void updateManagement(){
        int my_system_id=pref.getInt(MY_SYSTEM_ID, 0);
        int choreographer_id=pref.getInt(CHOREOGRAPHER_ID, 0);

        int db_system_id=pref.getInt(DATABASE_SYSTEM_ID, 0);
        String db_system_name=pref.getString(DATABASE_SYSTEM_NAME, "");
        String db_ip=pref.getString(DATABASE_IP_ADDRESS, "");
        int db_port=pref.getInt(DATABASE_PORT, 0);
        int db_getData_id=pref.getInt(DATABASE_GET_DATA_ID, 0);
        String db_getData_name=pref.getString(DATABASE_GET_DATA, "");
        int db_deleteData_id=pref.getInt(DATABASE_DELETE_DATA_ID, 0);
        String db_deleteData_name=pref.getString(DATABASE_DELETE_DATA, "");
        int db_setConfig_id=pref.getInt(DATABASE_SET_CONFIG_ID, 0);
        String db_setConfig_name=pref.getString(DATABASE_SET_CONFIG, "");
        int db_fill_id=pref.getInt(DATABASE_FILL_ID, 0);
        String db_fill_name=pref.getString(DATABASE_FILL, "");

        int sensor_system_id=pref.getInt(SENSOR_SYSTEM_ID, 0);
        String sensor_system_name=pref.getString(SENSOR_SYSTEM_NAME, "");
        String sensor_ip=pref.getString(SENSOR_IP_ADDRESS, "");
        int sensor_port=pref.getInt(SENSOR_PORT, 0);
        int sensor_getData_id=pref.getInt(SENSOR_GET_DATA_ID, 0);
        String sensor_getData_name=pref.getString(SENSOR_GET_DATA, "");
        int sensor_setConfig_id=pref.getInt(SENSOR_SET_CONFIG_ID, 0);
        String sensor_setConfig_name=pref.getString(SENSOR_SET_CONFIG, "");
        int sensor_startData_id=pref.getInt(SENSOR_START_DATA_ID, 0);
        String sensor_startData_name=pref.getString(SENSOR_START_DATA, "");
        int sensor_stopData_id=pref.getInt(SENSOR_STOP_DATA_ID, 0);
        String sensor_stopData_name=pref.getString(SENSOR_STOP_DATA, "");

        //Authorization
        Arrowhead.save_management_Authorization(this, getApplicationContext(), db_system_id, sensor_system_id, sensor_getData_id, sensor_getData_name);
        System.out.println("my_system_id: " + my_system_id);
        if (my_system_id>0){
            Arrowhead.save_management_Authorization(this, getApplicationContext(), my_system_id, sensor_system_id, sensor_setConfig_id, sensor_setConfig_name);
            Arrowhead.save_management_Authorization(this, getApplicationContext(), choreographer_id, sensor_system_id, sensor_startData_id, sensor_startData_name);
            Arrowhead.save_management_Authorization(this, getApplicationContext(), choreographer_id, sensor_system_id, sensor_stopData_id, sensor_stopData_name);
            Arrowhead.save_management_Authorization(this, getApplicationContext(), my_system_id, db_system_id, db_getData_id, db_getData_name);
            Arrowhead.save_management_Authorization(this, getApplicationContext(), my_system_id, db_system_id, db_deleteData_id, db_deleteData_name);
            Arrowhead.save_management_Authorization(this, getApplicationContext(), my_system_id, db_system_id, db_setConfig_id, db_setConfig_name);
            Arrowhead.save_management_Authorization(this, getApplicationContext(), choreographer_id, db_system_id, db_fill_id, db_fill_name);
        }
        else{
            System.out.println("my_system_id not initialised");
        }

        //Orchestrator
        Arrowhead.save_management_Orchestrator(this, getApplicationContext(), db_system_id, sensor_system_name, sensor_ip, sensor_port, sensor_getData_name);
        if (my_system_id>0){
            Arrowhead.save_management_Orchestrator(this, getApplicationContext(), my_system_id, sensor_system_name, sensor_ip, sensor_port, sensor_setConfig_name);
            Arrowhead.save_management_Orchestrator(this, getApplicationContext(), choreographer_id, sensor_system_name, sensor_ip, sensor_port, sensor_startData_name);
            Arrowhead.save_management_Orchestrator(this, getApplicationContext(), choreographer_id, sensor_system_name, sensor_ip, sensor_port, sensor_stopData_name);
            Arrowhead.save_management_Orchestrator(this, getApplicationContext(), my_system_id, db_system_name, db_ip, db_port, db_getData_name);
            Arrowhead.save_management_Orchestrator(this, getApplicationContext(), my_system_id, db_system_name, db_ip, db_port, db_deleteData_name);
            Arrowhead.save_management_Orchestrator(this, getApplicationContext(), my_system_id, db_system_name, db_ip, db_port, db_setConfig_name);
            Arrowhead.save_management_Orchestrator(this, getApplicationContext(), choreographer_id, db_system_name, db_ip, db_port, db_fill_name);
        }
    }
}