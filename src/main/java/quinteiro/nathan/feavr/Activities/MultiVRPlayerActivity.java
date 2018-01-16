package quinteiro.nathan.feavr.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import quinteiro.nathan.feavr.BLE.BLEManager;
import quinteiro.nathan.feavr.BLE.BluetoothLEService;
import quinteiro.nathan.feavr.Barcode.BarcodeCaptureActivity;
import quinteiro.nathan.feavr.Barcode.BarcodeGeneratorActivity;
import quinteiro.nathan.feavr.R;
import quinteiro.nathan.feavr.Unity.UnityPlayerActivity;

public class MultiVRPlayerActivity extends AppCompatActivity {

    private static final int BARCODE_READER_REQUEST_CODE = 5;
    private BluetoothLEService mBLEService;

    private boolean locationEnabledChecked = false;

    Button btBLE;
    Button btStart;
    Button btClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_vr_player);

        BLEManager.startBluetoothService(this);


        btBLE = (Button) findViewById(R.id.btBLE);
        btBLE.setOnClickListener(BLESettingsListener);
        btStart = (Button) findViewById(R.id.btLaunch);
        btStart.setOnClickListener(startGameListener);
        btClose = (Button) findViewById(R.id.btClose);
        btClose.setOnClickListener(closeListener);

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    private View.OnClickListener BLESettingsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            locationEnabledChecked = false;
            launchTechnicalSettings();
        }
    };

    private View.OnClickListener startGameListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Intent intent = new Intent(MainActivity.this, gameTabActivity.class);
            Intent intent = new Intent(MultiVRPlayerActivity.this, BarcodeCaptureActivity.class);
            startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);
        }
    };

    private View.OnClickListener closeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mBLEService != null) {
                mBLEService.disconnect(false);
            }
            finish();
        }
    };

    private void launchTechnicalSettings() {
        if(BLEManager.bluetoothEnabled() && locationEnabledChecked) {
            Intent intent = new Intent(this, TechnicalSettingsActivity.class);
            startActivity(intent);
        }
        //If not, request Bluetooth permission
        else {
            if(!BLEManager.bluetoothEnabled()) {
                BLEManager.startBluetoothService(this, BLEManager.REQUEST_ENABLE_BT_FOR_TECHNICAL);
            }
            else {
                createLocationRequest();
            }
        }
    }


    //Location must be activated for BLE to work on Android > 6.0
    protected void createLocationRequest() {
        GoogleApiClient googleApiClient = null;
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .build();
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result.getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            locationEnabledChecked = true;
                            launchTechnicalSettings();
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the user
                            // a dialog.

                            locationEnabledChecked = false;
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(
                                        MultiVRPlayerActivity.this, 1000);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            });
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                Intent intent = new Intent(MultiVRPlayerActivity.this, UnityPlayerActivity.class);
                startActivity(intent);
            }
        }
        //After user was asked for Bluetooth permission
        if (requestCode == BLEManager.REQUEST_ENABLE_BT) {
            //If user accepted, start Bluetooth service
            if (resultCode == RESULT_OK) {
                BLEManager.startBluetoothService(this);
            }
            //If user refused, display a dialog to explain why Bluetooth is required
            else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);

                dialog.setTitle(getString(R.string.bluetooth_required))
                        .setInverseBackgroundForced(true)
                        //.setIcon(R.drawable.ic_info_black_24dp)
                        .setMessage(getString(R.string.request_bluetooth_text))

                        .setNegativeButton(getString(R.string.dont_allow), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface, int i) {
                                dialoginterface.dismiss();
                            }
                        })
                        .setPositiveButton(getString(R.string.retry), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface, int i) {
                                dialoginterface.dismiss();
                                BLEManager.startBluetoothService(MultiVRPlayerActivity.this);
                            }
                        }).show();
            }
        }
        if (requestCode == BLEManager.REQUEST_LOCATION) {
            switch (resultCode) {
                case RESULT_OK: {
                    locationEnabledChecked = true;
                    launchTechnicalSettings();
                    break;
                }
                case RESULT_CANCELED: {
                    // The user was asked to change settings, but chose not to
                    break;
                }
            }
        }
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(mBLEService == null) {
                mBLEService = BluetoothLEService.getBLEService();
            }
            if (BluetoothLEService.ACTION_GATT_CONNECTED.equals(action)) {
                //updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
                //updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                //bpmTextView.setText("");
            } else if (BluetoothLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {


                //
                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLEService.ACTION_HRM_DATA_AVAILABLE.equals(action)) {
                int bpm;
                double rrValues[];
                bpm = intent.getIntExtra(BluetoothLEService.HR_DATA, -1);
                if(bpm != -1) {
                    //bpmTextView.setText("BPM: " + bpm);
                }

                rrValues = intent.getDoubleArrayExtra(BluetoothLEService.RR_DATA);
                if(rrValues != null) {
                }

            } else if (BluetoothLEService.ACTION_BATTERY_LEVEL_AVAILABLE.equals(action)) {

            }
        }
    };

    @Override
    public void onBackPressed() {
        if(mBLEService != null) {
            mBLEService.disconnect(false);
        }

        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mGattUpdateReceiver);
        super.onDestroy();
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
}
