package it.unibo.stradivarius.wp4temperatureapplication;

import android.accounts.NetworkErrorException;
import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainViewModel extends AndroidViewModel {

    static final String CONFIGURATION_TAG = "config";
    static final String TEMPERATURE_TAG = "Temperature";
    static final String SERVICE_REGISTRY_URL = "http://137.204.57.93:8443/";
    static final String GET_SERVICES = "serviceregistry/mgmt/servicedef/";
    static final String GET_SYSTEMS = "serviceregistry/mgmt/systems/";
    static final String SERVICE_DEFINITION = "wp4demo-configuration";
    static final String SERVICE_DB_DEFINITION = "wp4demo-persister-db";
    static final String SERVICE_OPT = "?direction=ASC&sort_field=id";
    static final String MY_SYSTEM_NAME = "wp4_monitor";
    static final int BUFFER_LENGTH = 50;
    private static final int N_THREADS = 4;

    public static final int DB_CORRECT = 0;
    public static final int DB_LOADING = 1;
    public static final int DB_INCORRECT = 2;

    public int systemId = -1;

    static final ExecutorService ArrowheadExecutor =
            Executors.newFixedThreadPool(N_THREADS);

    private FirebaseDatabase mDatabase;
    private MutableLiveData<TemperatureDataPoint> tempPoint;
    private MutableLiveData<Integer> configuration;
    private MutableLiveData<String> serviceAddress;

    public MainViewModel(@NonNull Application application) {
        super(application);

        //mDatabase = FirebaseDatabase.getInstance();
        //checkDatabase();
    }

    public void instantiateDb(String address) {
        mDatabase = FirebaseDatabase.getInstance(address);
    }

    public LiveData<Integer> getConfiguration () {
        if (configuration == null) {
            configuration = new MutableLiveData<>();
            mDatabase.getReference(CONFIGURATION_TAG)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            configuration.postValue(snapshot.getValue(Integer.class));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("DATABASE ERROR", "Configuration failure");
                        }
                    });
        }
        return configuration;
    }

    public LiveData<TemperatureDataPoint> getDataPoint () {
        if (tempPoint == null) {
            tempPoint = new MutableLiveData<>();
            mDatabase.getReference(TEMPERATURE_TAG)
                    .addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot,
                                                 @Nullable String previousChildName) {
                            tempPoint.postValue(snapshot.getValue(TemperatureDataPoint.class));
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot snapshot,
                                                   @Nullable String previousChildName) { /* pass */ }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot snapshot) { /* pass */ }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot snapshot,
                                                 @Nullable String previousChildName) { /* pass */ }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("DATABASE ERROR", "Temperature failure");
                        }
                    });
        }
        return tempPoint;
    }

    public LiveData<String> getServiceAddress () {
        if (serviceAddress == null ) {
            serviceAddress = new MutableLiveData<>();
        }
        return serviceAddress;
    }

//    public LiveData<String> getDatabaseAddress () {
//        if (databaseAddress == null ) {
//            databaseAddress = new MutableLiveData<>();
//        }
//        return databaseAddress;
//    }

    public void  UpdateServiceRegistryAddress() {
        /* Volley Request */
        RequestQueue queue = Volley.newRequestQueue(getApplication().getApplicationContext());
        String url = SERVICE_REGISTRY_URL +
                GET_SERVICES +
                SERVICE_DEFINITION +
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
                                    int port = provider.getInt("port");
                                    String serviceUri = service.getString("serviceUri");
                                    String completeURL = "http://" +
                                            address + ":" + String.valueOf(port) + "/" + serviceUri;
                                    serviceAddress.postValue(completeURL);
                                }
                            } catch (JSONException e) {
                                serviceAddress.postValue("Parsing error");
                            }
                        });
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VOLLEY ERROR", error.getMessage());
                        serviceAddress.postValue("Network error");
                    }
        });
        queue.add(stringRequest);
    }

    public void pushConfiguration(int samplingTime) throws NetworkErrorException {
        RequestQueue queue = Volley.newRequestQueue(getApplication().getApplicationContext());
        String URL = getServiceAddress().getValue();
        if (URL == null) {
            throw new NetworkErrorException("URL is empty");
        }

        /* Set up the payload */
        JSONObject postData = new JSONObject();
        try {
            postData.put("sampling_time", samplingTime);
            postData.put("buffer_length", BUFFER_LENGTH);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, URL, postData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("CONFIG", "Success! " + response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("CONFIG", "Failure!");
            }
        });
        queue.add(jsonObjectRequest);
    }


}
