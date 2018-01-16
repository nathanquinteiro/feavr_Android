package quinteiro.nathan.feavrwatch;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends WearableActivity implements SensorEventListener, LocationListener {

    private static final String TAG = "MainActivity";
    private TextView textViewGPS;
    private TextView textViewHR;
    static int heartRate;
    static List<Number> lon_lat = new ArrayList<>();
    private ProgressBar progressBar1, progressBar2;
    private boolean storeData;
    private FloatingActionButton btnRecord;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set the heart image
        ImageView imageView = findViewById(R.id.heartImage);
        imageView.setImageDrawable(getDrawable(R.drawable.heart));

//         Enables Always-on
        setAmbientEnabled();

        //Button state and HR TextView set up
        storeData = false;
        btnRecord = findViewById(R.id.buttonRecord);
        btnRecord.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
        btnRecord.setImageResource(android.R.drawable.ic_media_play);
        textViewHR = findViewById(R.id.textViewHR);
        textViewHR.setText("0");

        //Progress bars set up
        progressBar1 = findViewById(R.id.progressBar1);
        progressBar2 = findViewById(R.id.progressBar2);
        progressBar1.setProgress(0);
        progressBar2.setProgress(0);

        //Asking for permission to access HR sensor
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M //M is for Marshmallow
                && checkSelfPermission("android.permission.BODY_SENSORS") ==
                PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{"android.permission.BODY_SENSORS"}, 0);
        }


//        //Registering listener for location - You can use this or the previous one - The FusedLocationProvider handles the indoor/outdoor location by itself
//        LocationManager locationManager = (LocationManager) getSystemService(MainActivity.LOCATION_SERVICE);
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
//        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);


    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        //Heart rate in and from DB only when the state of the button is "true" (or set to "play")
        if (storeData) {
            //We create a specific class to handle the AsyncTask in order to not have possible memory leaks
            heartRate = (int) sensorEvent.values[0];

            Intent intent = new Intent(MainActivity.this, WearListenerService.class);
            intent.setAction(WearListenerService.ACTION_SEND_HEART_RATE);
            intent.putExtra(WearListenerService.DATAMAP_INT_HEART_RATE, heartRate);
            startService(intent);

            if (textViewHR != null) {
                textViewHR.setText(String.valueOf(heartRate));
                int hrProgressBar = (int) (0.2 * heartRate);
                progressBar1.setProgress(hrProgressBar);
                progressBar2.setProgress(hrProgressBar);


            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //You could implement some smart way to check the accuracy and alert the user about it
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }


    public void recordData(View view) {
        SensorManager sensorManager = (SensorManager) getSystemService(MainActivity.SENSOR_SERVICE);
        if (sensorManager != null) {
            Sensor hrSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
            storeData = !storeData;
            if (!storeData) {
                sensorManager.unregisterListener(this, hrSensor);
                btnRecord.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                btnRecord.setImageResource(android.R.drawable.ic_media_play);
                textViewHR.setText("0");
                progressBar1.setProgress(0);
                progressBar2.setProgress(0);
            } else {
                //Registering listener for HR sensor
                sensorManager.registerListener(this, hrSensor, SensorManager.SENSOR_DELAY_UI);
                btnRecord.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                btnRecord.setImageResource(android.R.drawable.ic_media_pause);


                //Asking for permission to access fine location
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        && checkSelfPermission("android.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_DENIED) {
                    requestPermissions(new String[]{"android.permission.ACCESS_FINE_LOCATION"}, 0);
                }

                //Asking for permission to access coarse location
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        && checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == PackageManager.PERMISSION_DENIED) {
                    requestPermissions(new String[]{"android.permission.ACCESS_COARSE_LOCATION"}, 0);
                }
            }
        }
    }




}
