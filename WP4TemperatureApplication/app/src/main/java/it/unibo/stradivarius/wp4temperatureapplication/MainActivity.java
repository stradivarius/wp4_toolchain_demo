package it.unibo.stradivarius.wp4temperatureapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.accounts.NetworkErrorException;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    static final int PERMISSION_CODE = 14;
    static final int BUFSIZE = 20;
    static final int TIME_WINDOW = BUFSIZE * 1000;

    private MainViewModel mViewModel;
    private EditText configuration;
    private TextView serviceAddress;
    private TextView lastUpdate;

    private Button pushConfig;
    private Button RefreshServiceRegistry;

    private GraphView graph;
    LineGraphSeries<DataPoint> series;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        String databaseAddress = getIntent().getExtras().getString("address");
        int systemId = getIntent().getExtras().getInt("sysId");
        Log.v("SYSTEM ID", String.valueOf(systemId));

        mViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(
                this.getApplication()).create(MainViewModel.class);
        mViewModel.systemId = systemId;
        mViewModel.instantiateDb(databaseAddress);

        lastUpdate = findViewById(R.id.last_update_label);
        configuration = findViewById(R.id.suggConfValue);

        graph = findViewById(R.id.graph);
        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0.0);
        graph.getViewport().setMaxX(0.0);
        series = new LineGraphSeries<>(new DataPoint[] {
//                new DataPoint(0, 1),
//                Check for old values from ViewModel?
        });
        series.setDrawDataPoints(true);
        series.setThickness(8);
        series.setDataPointsRadius(10);
        graph.addSeries(series);

        /* Pushing configuration */
        pushConfig = findViewById(R.id.button_push_config);
        pushConfig.setOnClickListener(v -> {
            try {
                mViewModel.pushConfiguration(Integer.parseInt(configuration.getText().toString()));
                Toast.makeText(this, "Configure success!", Toast.LENGTH_SHORT).show();
            } catch (NetworkErrorException e) {
                Toast.makeText(this, "Configure failed!", Toast.LENGTH_SHORT).show();
            }
        });

        /* Refreshing the Service Registry here */
        serviceAddress = findViewById(R.id.ServiceRegistryLabel);
        RefreshServiceRegistry = findViewById(R.id.ServiceRegistryButton);
        RefreshServiceRegistry.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions();
            } else {
                mViewModel.UpdateServiceRegistryAddress();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        mViewModel.getConfiguration().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if (integer != null ) {
                    configuration.setText(integer.toString());
                }
            }
        });

        mViewModel.getServiceAddress().observe(this, s -> {
            if (s != null) {
                serviceAddress.setText(s);
            }
        });

        mViewModel.getDataPoint().observe(this, new Observer<TemperatureDataPoint>() {
            @Override
            public void onChanged(TemperatureDataPoint tp) {
                try {
                    long timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                            .parse(tp.getTimestamp()).getTime();
                    series.appendData(new DataPoint(timestamp, tp.getValue()),
                            false, BUFSIZE);
                    graph.getViewport().setMinX(timestamp - TIME_WINDOW );
                    graph.getViewport().setMaxX(timestamp);
                    lastUpdate.setText(new Date(timestamp).toString());
                } catch (ParseException e) {
                    e.printStackTrace(); // TODO
                }
            }
        });

    }

    void requestPermissions(){
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.INTERNET},
                PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                /* Do nothing */
            }
        }
    }
}
