package quinteiro.nathan.feavr.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


import com.google.android.gms.common.api.CommonStatusCodes;

import quinteiro.nathan.feavr.BLE.BluetoothLEService;
import quinteiro.nathan.feavr.Barcode.BarcodeGeneratorActivity;
import quinteiro.nathan.feavr.Barcode.DataProvider;
import quinteiro.nathan.feavr.R;
import quinteiro.nathan.feavr.Unity.FeavrReceiver;
import quinteiro.nathan.feavr.Unity.UnityPlayerActivity;
import quinteiro.nathan.feavr.Wear.WearListenerService;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Button btSingle;
    private Button btVR;
    private Button btControl;
    private Button btTest;


    private final int BARCODE_GENERATOR_CODE = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.app_bar_main);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

            drawer.setDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);
        }

        btSingle = (Button) findViewById(R.id.btSingle);
        btSingle.setOnClickListener(startSingleListener);

        btVR = (Button) findViewById(R.id.btVR);
        btVR.setOnClickListener(startVRListener);

        btControl = (Button) findViewById(R.id.btControl);
        btControl.setOnClickListener(startControlListener);

        btTest = (Button) findViewById(R.id.btTestDB);
        btTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataProvider.getInstance().pushMessageTest();
            }
        });


        //Receive HR from Watch
        registerReceiver(mHeartRateReceiver, new IntentFilter(WearListenerService.ACTION_SEND_HEART_RATE));

        //Receive HR from BLE
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());


    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLEService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLEService.ACTION_HRM_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLEService.ACTION_BATTERY_LEVEL_AVAILABLE);
        return intentFilter;
    }

    private BroadcastReceiver mHeartRateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int bpm = intent.getIntExtra(WearListenerService.INT_HEART_RATE,-1);
            Log.d("Received from Watch","BPM: " + bpm);

            if(bpm != -1) {
                Log.d("Received from Watch","BPM: " + bpm);
                FeavrReceiver.setBPM(bpm);
            }
        }
    };

    @Override protected void onDestroy ()
    {
        unregisterReceiver(mGattUpdateReceiver);
        unregisterReceiver(mHeartRateReceiver);
        super.onDestroy();
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLEService.ACTION_GATT_CONNECTED.equals(action)) {
                //updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
                //updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
            } else if (BluetoothLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {


                //
                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLEService.ACTION_HRM_DATA_AVAILABLE.equals(action)) {
                int bpm;
                double rrValues[];
                bpm = intent.getIntExtra(BluetoothLEService.HR_DATA, -1);
                if(bpm != -1) {
                    Log.e("Received", "BPM: " + bpm);
                    FeavrReceiver.setBPM(bpm);
                }

                rrValues = intent.getDoubleArrayExtra(BluetoothLEService.RR_DATA);
                if(rrValues != null) {
                    for(int i = 0; i < rrValues.length; i++) {
                        Log.e("Received", "RR: " + rrValues[i]);
                    }
                }

            } else if (BluetoothLEService.ACTION_BATTERY_LEVEL_AVAILABLE.equals(action)) {



            }
        }
    };


    private View.OnClickListener startSingleListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Intent intent = new Intent(MainActivity.this, UnityPlayerActivity.class);
            startActivity(intent);
        }
    };


    private View.OnClickListener startVRListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(MainActivity.this, MultiVRPlayerActivity.class);
            startActivity(intent);
        }
    };


    private View.OnClickListener startControlListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(MainActivity.this, BarcodeGeneratorActivity.class);
            startActivityForResult(intent, BARCODE_GENERATOR_CODE);
        }
    };


    @Override
    protected void onResume() {
        super.onResume();


        //registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }



    @Override
    protected void onPause() {
        super.onPause();

        //unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer != null) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }
        else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        //Launch technical settings if Bluetooth is enabled
        if (id == R.id.nav_technical_settings) {

        }
        if (id == R.id.nav_connect) {
            /*
            Intent intent = new Intent(this, ConnectActivity.class);
            startActivity(intent);
            */
        }

        if ( id == R.id.nav_connect_multi_players){
            /*Intent intent = new Intent(this, MultiPlayerConnectActivity.class);
            startActivity(intent);*/
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BARCODE_GENERATOR_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                Intent intent = new Intent(MainActivity.this, gameTabActivity.class);
                startActivity(intent);
            }
        }
    }

}
