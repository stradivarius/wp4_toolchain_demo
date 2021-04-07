package it.unibo.stradivarius.wp4temperatureapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static it.unibo.stradivarius.wp4temperatureapplication.MainViewModel.ArrowheadExecutor;
import static it.unibo.stradivarius.wp4temperatureapplication.MainViewModel.DB_CORRECT;
import static it.unibo.stradivarius.wp4temperatureapplication.MainViewModel.GET_SERVICES;
import static it.unibo.stradivarius.wp4temperatureapplication.MainViewModel.GET_SYSTEMS;
import static it.unibo.stradivarius.wp4temperatureapplication.MainViewModel.MY_SYSTEM_NAME;
import static it.unibo.stradivarius.wp4temperatureapplication.MainViewModel.SERVICE_DB_DEFINITION;
import static it.unibo.stradivarius.wp4temperatureapplication.MainViewModel.SERVICE_DEFINITION;
import static it.unibo.stradivarius.wp4temperatureapplication.MainViewModel.SERVICE_OPT;
import static it.unibo.stradivarius.wp4temperatureapplication.MainViewModel.SERVICE_REGISTRY_URL;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Button btnStart = findViewById(R.id.buttonArrowhead);
        btnStart.setOnClickListener(v -> {
            checkSystemAndDatabaseRegistered();
        });

        Button btnHardWire = findViewById(R.id.buttonArrowhead2);
        btnHardWire.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(),
                    MainActivity.class);
            i.putExtra("address", "https://ahtwp4demo-default-rtdb.firebaseio.com");
            startActivity(i);
        });
    }

    private void checkSystemAndDatabaseRegistered(){
        checkSystemRegistered();
    }

    private void checkSystemRegistered() {
        RequestQueue queue = Volley.newRequestQueue(getApplication().getApplicationContext());
        String url = SERVICE_REGISTRY_URL +
                GET_SYSTEMS +
                SERVICE_OPT;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ArrowheadExecutor.execute(() -> {
                                    try {

                                        /* Search for the system */
                                        int identifier = -1;
                                        JSONObject jObject = new JSONObject(response);
                                        final JSONArray data = jObject.getJSONArray("data");
                                        for (int i = 0; i < data.length(); i++) {
                                            final JSONObject system = data.getJSONObject(i);
                                            String systemName = system.getString("systemName");
                                            if (systemName.equals(MY_SYSTEM_NAME)) {
                                                identifier = system.getInt("id");
                                                Log.i("SYSTEM", "My system ID is " +
                                                        String.valueOf(identifier));
                                                break;
                                            }
                                        }

                                        Log.v("SYSTEM", "After search Sys ID is" +
                                                String.valueOf(identifier));

                                        /* Need to register a new System */
                                        if (identifier == -1) {
                                            registerNewSystem();
                                        } else {
                                            checkDatabase(identifier);
                                        }

                                    } catch (JSONException e) {
                                        Log.e("JSON", "parse failed");
                                    }
                        });
                    }
                },
                new Response.ErrorListener () {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VOLLEY ERROR", error.getMessage());
                    }
                });
        queue.add(stringRequest);
    }

    private void registerNewSystem() {
        RequestQueue queue = Volley.newRequestQueue(getApplication().getApplicationContext());
        String url = SERVICE_REGISTRY_URL +
                GET_SYSTEMS +
                SERVICE_OPT;
        JSONObject postData = new JSONObject();
        try {
            postData.put("address", "127.0.0.1");
            postData.put("port", 0);
            postData.put("systemName", MY_SYSTEM_NAME);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST, url, postData, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        int identifier = response.getInt("id");
                        Log.v("SYSTEM", "Registered new Sys ID " +
                                String.valueOf(identifier));
                        checkDatabase(identifier);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });
            queue.add(jsonObjectRequest);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void checkDatabase (int systemId) {
        /* Volley Request */
        RequestQueue queue = Volley.newRequestQueue(getApplication().getApplicationContext());
        String url = SERVICE_REGISTRY_URL +
                GET_SERVICES +
                SERVICE_DB_DEFINITION +
                SERVICE_OPT;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("VOLLEY", response);

                        /* I'm doing this in background and updating the Live Data accordingly */
                        ArrowheadExecutor.execute(() -> {
                            try {
                                JSONObject jObject = new JSONObject(response);
                                final JSONArray data = jObject.getJSONArray("data");
                                if (data.length() > 0) {
                                    final JSONObject service = data.getJSONObject(0);
                                    final JSONObject provider = service.getJSONObject("provider");
                                    String address = provider.getString("address");
                                    address = "https://ahtwp4demo-default-rtdb.firebaseio.com/";

                                    if (FirebaseDatabase.getInstance(address) != null) {
                                        Intent i = new Intent(getApplicationContext(),
                                                MainActivity.class);
                                        Log.v("SYSTEM", "Passing over Sys ID is" +
                                                String.valueOf(systemId));
                                        i.putExtra("address", address);
                                        i.putExtra("sysId", systemId);
                                        startActivity(i);
                                    } else {
                                        Toast.makeText(SplashActivity.this,
                                                "No database found",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }

                            } catch (JSONException e) {
                                Log.e("JSON", "parse failed");
                            }
                        });
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VOLLEY ERROR", error.getMessage());
            }
        });
        queue.add(stringRequest);
    }
}