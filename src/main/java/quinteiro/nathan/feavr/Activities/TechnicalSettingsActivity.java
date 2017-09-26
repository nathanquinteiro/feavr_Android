package quinteiro.nathan.feavr.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import quinteiro.nathan.feavr.BLE.BluetoothLEService;
import quinteiro.nathan.feavr.R;
import quinteiro.nathan.feavr.UI.BatteryProgressView;

public class TechnicalSettingsActivity extends AppCompatActivity {
    private TextView deviceTextView;
    private TextView bpmTextView;
    private Button scanButton;
    private BatteryProgressView batteryView;

    private List<BluetoothDevice> devicesList = new ArrayList<>();

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBLEScan;

    private BluetoothLEService mBLEService;


    private DeviceListViewAdapter deviceListViewAdapter;
    ListView deviceListView;


    private boolean mConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_technical_settings);
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Button closeButton = (Button) findViewById(R.id.btClose);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        scanButton = (Button) findViewById(R.id.btScan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scan();
            }
        });

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        deviceListViewAdapter = new DeviceListViewAdapter();

        mBLEService = BluetoothLEService.getBLEService();
        mBLEService.checkPermissions(this);

        bpmTextView = (TextView) findViewById(R.id.tv_bpm);
        deviceTextView = (TextView) findViewById(R.id.tv_device);

        batteryView = (BatteryProgressView) findViewById(R.id.batteryView);
        batteryView.setProgress(0);

        deviceConnection(mBLEService.isConnected());
    }

    private void deviceConnection(boolean connected) {
        if(connected){
            mConnected = true;
            scanButton.setText(getString(R.string.disconnect));
            String deviceName = mBLEService.getConnectedDeviceName();
            bpmTextView.setText("");
            if(deviceName != null) {
                String deviceText = getString(R.string.connected_to) + deviceName;
                deviceTextView.setText(deviceText);
            }
            else {
                deviceTextView.setText("");
            }
        }
        else {
            mConnected = false;
            scanButton.setText(getString(R.string.scan));
            batteryView.setProgress(0);
            batteryView.setVisibility(View.INVISIBLE);
            bpmTextView.setText("");
            deviceTextView.setText(getString(R.string.not_connected));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BluetoothLEService.REQUEST_ENABLE_BT);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(mGattUpdateReceiver);

        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            scanLeDevice(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothLEService.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //Bluetooth not enabled.
                finish();
                return;
            }
        }
        if(requestCode == BluetoothLEService.REQUEST_PERMISSION_SETTING) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                finish();
                return;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
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
            if (BluetoothLEService.ACTION_GATT_CONNECTED.equals(action)) {
                deviceConnection(true);
            } else if (BluetoothLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
               deviceConnection(false);
            } else if (BluetoothLEService.ACTION_HRM_DATA_AVAILABLE.equals(action)) {
                int bpm = intent.getIntExtra(BluetoothLEService.BPM_DATA, -1);
                if(bpm != -1) {
                    String bpmText = getString(R.string.bpm) + bpm;
                    bpmTextView.setText(bpmText);
                }
            } else if(BluetoothLEService.ACTION_BATTERY_LEVEL_AVAILABLE.equals(action)) {
                int batteryLevel = intent.getIntExtra(BluetoothLEService.BATTERY_LEVEL, -1);
                if(batteryLevel != -1) {
                    batteryView = (BatteryProgressView) findViewById(R.id.batteryView);
                    batteryView.setProgress(batteryLevel);
                    batteryView.setVisibility(View.VISIBLE);
                }
            }
        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == BluetoothLEService.REQUEST_ENABLE_LOCATION) {
            mBLEService.permissionsResult(requestCode, permissions, grantResults, this);
        }
    }


    public class DeviceListViewAdapter extends BaseAdapter implements ListAdapter {

        @Override
        public int getCount() {
            return devicesList.size();
        }

        @Override
        public Object getItem(int i) {
            return devicesList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(view == null) {
                //inflate view because it is null.
                view = LayoutInflater.from(getApplicationContext()).inflate(getResources()
                        .getLayout(R.layout.dialog_device_layout), (ListView) findViewById(R.id.lvDevices));
            }

            TextView name = (TextView) view.findViewById(R.id.bluetooth_device_name);
            TextView address = (TextView) view.findViewById(R.id.bluetooth_device_address);

            name.setText(devicesList.get(i).getName());
            address.setText(devicesList.get(i).getAddress());

            return view;
        }
    }


    private void scanLeDevice(final boolean enable) {
        if(mBLEScan != null) {
            if (enable) {

                ParcelUuid uuid = ParcelUuid.fromString(BluetoothLEService.UUID_HEART_RATE_SERVICE.toString());
                ScanFilter scanFilter = new ScanFilter.Builder().setServiceUuid(uuid).build();
                List<ScanFilter> scanFilters = new ArrayList<>();
                scanFilters.add(scanFilter);

                ScanSettings scanSettings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();

                mBLEScan.startScan(scanFilters, scanSettings, mScanCallback);

            } else {
                mBLEScan.stopScan(mScanCallback);
            }
        }
    }


    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            BluetoothDevice btDevice = result.getDevice();
            String name = btDevice.getName();
            if(name != null) {
                if(!devicesList.contains(btDevice)) {
                    devicesList.add(btDevice);
                    deviceListViewAdapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {


        }

        @Override
        public void onScanFailed(int errorCode) {

        }
    };

    public void connectToDevice(BluetoothDevice device) {
        if(mBLEService != null) {
            mBLEService.connect(device);
        }
    }

    public void scan() {
        if(mConnected) {
            mBLEService.disconnect(true);
        }
        else {
            if(mBLEService.permissionsEnabled(this)) {
                mBLEScan = mBluetoothAdapter.getBluetoothLeScanner();
                devicesList.clear();
                scanLeDevice(true);
                final Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.ble_devices_dialog);
                dialog.setTitle(getString(R.string.scan_dialog_title));
                dialog.setCancelable(true);


                deviceListView = (ListView) dialog.findViewById(R.id.lvDevices);
                deviceListView.setAdapter(deviceListViewAdapter);
                deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        BluetoothDevice device = (BluetoothDevice) adapterView.getItemAtPosition(i);
                        connectToDevice(device);
                        mBLEScan.stopScan(mScanCallback);
                        dialog.dismiss();
                    }
                });

                //setListViewHeightBasedOnChildren(deviceListView);

                //set up button
                Button button = (Button) dialog.findViewById(R.id.btCancel);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mBLEScan.stopScan(mScanCallback);
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }


        }
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
