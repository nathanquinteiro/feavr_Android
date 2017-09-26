package quinteiro.nathan.feavr.BLE;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

import quinteiro.nathan.feavr.R;
import quinteiro.nathan.feavr.utils.Preferences;


/**
 * Created by Nathan Quinteiro on 05.06.17.
 *
 * Service for managing connection and data communication with a Heart Rate Monitor with
 * Bluetooth Low-Energy.
 *
 * The service allows other classes to easily connect, disconnect and autoconnect to only one
 * HRM BLE devices
 */

public class BluetoothLEService extends Service {
    private final static String TAG = BluetoothLEService.class.getSimpleName();
    private static BluetoothLEService mBLEService;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private BluetoothDevice mBluetoothDevice;
    private String mLastAddress;

    //BLE Devices state
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    //Requests code
    public static final int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_ENABLE_LOCATION = 2;
    public static final int REQUEST_PERMISSION_SETTING = 3;

    //Notification ID
    private static final int FOREGROUND_ID = 4;

    private boolean permissionsEnabled = false;

    //AutoConnect Handler
    Handler reconnectHandler;
    Handler notificationSubscribeHandler;

    private BluetoothGattCharacteristic batteryCharacteristic = null;

    /*Queues allowing to write descriptor, register to notification or read characteristic of BLE
    Device in correct order without overlapping*/
    private Queue<BluetoothGattDescriptor> descriptorWriteQueue = new LinkedList<>();
    private Queue<BluetoothGattCharacteristic> notificationCharacteristicQueue = new LinkedList<>();
    private Queue<BluetoothGattCharacteristic> readCharacteristicQueue = new LinkedList<>();

    //Mutex for queues
    private Object waitMutex = new Object();
    //Indicates wether phone can read characteristic of BLE Device
    private boolean canRead = true;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_HRM_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_HRM_DATA_AVAILABLE";
    public final static String ACTION_BATTERY_LEVEL_AVAILABLE =
            "com.example.bluetooth.le.ACTION_BATTERY_DATA_AVAILABLE";
    public final static String BPM_DATA =
            "com.example.bluetooth.le.BPM_DATA";
    public final static String ENERGY_DATA =
            "com.example.bluetooth.le.RR_DATA";
    public final static String RR_DATA =
            "com.example.bluetooth.le.RR_DATA";
    public final static String BATTERY_LEVEL =
            "com.example.bluetooth.le.BATTERY_LEVEL";


    public final static UUID UUID_HEART_RATE_SERVICE =
            UUID.fromString(SampleGATTAttributes.HEART_RATE_SERVICE);


    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGATTAttributes.HEART_RATE_MEASUREMENT);


    public final static UUID UUID_BATTERY_MEASUREMENT =
            UUID.fromString(SampleGATTAttributes.BATTERY_SERVICE);


    public final static UUID UUID_BATTERY_LEVEL =
            UUID.fromString(SampleGATTAttributes.BATTERY_LEVEL);

    private Integer mHeartRate = null;

    // Implements callback methods for GATT events
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);


            synchronized(waitMutex) {
                //pop the descriptor that we just finishing writing from the queue
                descriptorWriteQueue.remove();

                //Device can now perform read on the device
                canRead = true;

                //if there is another descriptor to write, do it, otherwise subscribe to
                //characteristic notification or read characteristic
                if(descriptorWriteQueue.size() > 0) {
                    mBluetoothGatt.writeDescriptor(descriptorWriteQueue.element());
                    canRead = false;
                }
                else if(notificationCharacteristicQueue.size() > 0) {
                    mBluetoothGatt.setCharacteristicNotification(notificationCharacteristicQueue.element(), true);
                    canRead = false;
                } else if(readCharacteristicQueue.size() > 0) {
                    mBluetoothGatt.readCharacteristic(notificationCharacteristicQueue.element());
                    canRead = false;
                }
            }

        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            //Only care about the BLE Device user want to connect to
            if(gatt.getDevice() != mBluetoothDevice) {
                return;
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {

                //Display notification on the phone to let user know the device is connected and
                //obtain high priority for this service
                startForeground(FOREGROUND_ID, buildForegroundNotification(gatt.getDevice().getName()));

                //Save this device address in non-volatile memory to reconnect automatically next time
                mLastAddress = gatt.getDevice().getAddress();
                Preferences.saveLastDevice(mLastAddress, getApplicationContext());

                //Broadcast the message that the GATT is connected
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(ACTION_GATT_CONNECTED);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                disconnect(false);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //Iterate through all services of the BLE device and subscribe to notification on
                //the Heart Rate Measurement and read the Battery Level
                for(BluetoothGattService service :  mBLEService.getSupportedGattServices()) {
                    if(service.getUuid().equals(BluetoothLEService.UUID_HEART_RATE_SERVICE)) {
                        for(BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                            if(characteristic.getUuid().equals(BluetoothLEService.UUID_HEART_RATE_MEASUREMENT)) {
                                //mBLEService.setCharacteristicNotification(characteristic, true);
                                setNotificationOn(characteristic);
                            }
                        }
                    }
                    if(service.getUuid().equals(BluetoothLEService.UUID_BATTERY_MEASUREMENT)) {
                        for(BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                            if(characteristic.getUuid().equals(BluetoothLEService.UUID_BATTERY_LEVEL)) {
                                batteryCharacteristic = characteristic;
                                getBatteryLevel();
                            }
                        }
                    }
                }

                //Broadcast the services discovery
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            //Broadcast the characteristic read if successful
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(characteristic);
            }

            //Remove the read characteristic from the queue and write descriptor/subscribe to
            //notification/read characteristic if there are in queues
            synchronized (waitMutex) {
                canRead = true;
                readCharacteristicQueue.remove();
                if(descriptorWriteQueue.size() > 0) {
                    mBluetoothGatt.writeDescriptor(descriptorWriteQueue.element());
                    canRead = false;
                }
                else if (notificationCharacteristicQueue.size() > 0) {
                    mBluetoothGatt.setCharacteristicNotification(notificationCharacteristicQueue.element(), true);
                    canRead = false;
                    notificationCharacteristicQueue.remove();
                } else if (readCharacteristicQueue.size() > 0) {
                    mBluetoothGatt.readCharacteristic(readCharacteristicQueue.element());
                    canRead = false;
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            //Broadcast the characteristic
            broadcastUpdate(characteristic);

            //Remove the notification from the queue and do other read/write if there are in the
            //queues
            synchronized (waitMutex) {
                if(notificationCharacteristicQueue.size() > 0) {
                    notificationCharacteristicQueue.remove();
                }
                canRead = true;
                if(descriptorWriteQueue.size() > 0) {
                    mBluetoothGatt.writeDescriptor(descriptorWriteQueue.element());
                    canRead = false;
                }
                else if (notificationCharacteristicQueue.size() > 0) {
                    mBluetoothGatt.setCharacteristicNotification(notificationCharacteristicQueue.element(), true);
                    canRead = false;
                    notificationCharacteristicQueue.remove();
                } else if (readCharacteristicQueue.size() > 0) {
                    mBluetoothGatt.readCharacteristic(readCharacteristicQueue.element());
                    canRead = false;
                }
            }
        }
    };



    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final BluetoothGattCharacteristic characteristic) {
        if (characteristic.getUuid().equals(UUID_HEART_RATE_MEASUREMENT)) {
            final Intent intent = new Intent(ACTION_HRM_DATA_AVAILABLE);
            extractHeartBeatValues(intent, characteristic);
            sendBroadcast(intent);
        }

        if (characteristic.getUuid().equals(UUID_BATTERY_LEVEL)) {
            final Intent intent = new Intent(ACTION_BATTERY_LEVEL_AVAILABLE);
            extractBatteryLevel(intent, characteristic);
            sendBroadcast(intent);
        }
    }

    private static void extractBatteryLevel(Intent intent, BluetoothGattCharacteristic characteristic) {
        int batteryLevel = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        intent.putExtra(BATTERY_LEVEL, batteryLevel);
    }

    private static void extractHeartBeatValues(Intent intent, BluetoothGattCharacteristic characteristic) {
        int flag = characteristic.getProperties();
        int format = -1;
        int energy = -1;
        int offset = 1;
        int rr_count = 0;

        if ((flag & 0x01) != 0) {
            format = BluetoothGattCharacteristic.FORMAT_UINT16;
            offset = 3;
        } else {
            format = BluetoothGattCharacteristic.FORMAT_UINT8;
            offset = 2;
        }

        final int heartRate = characteristic.getIntValue(format, 1);
        Log.d(TAG, String.format("Received heart rate: %d", heartRate));
        intent.putExtra(BPM_DATA, heartRate);

        //Check if calories info are present
        if ((flag & 0x08) != 0) {
            energy = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
            offset += 2;
            Log.d(TAG, "Received energy: {}"+ energy);
            intent.putExtra(ENERGY_DATA, energy);
        }
        //Check if RR are prsent
        if ((flag & 0x16) != 0){
            rr_count = ((characteristic.getValue()).length - offset) / 2;
            if (rr_count > 0) {
                double[] mRr_values = new double[rr_count];
                //Read all RR values and converts them in seconds
                for (int i = 0; i < rr_count; i++) {
                    int rr_value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
                    mRr_values[i] = rr_value / 1024.0f;
                    offset += 2;
                    Log.d(TAG, "Received RR: " + mRr_values[i]);
                }
                intent.putExtra(RR_DATA, mRr_values);
            }
        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        initialize();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initialize();
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(bluetoothStateReceiver);
        super.onDestroy();
    }

    public boolean permissionsEnabled(Activity activity) {
        boolean enabled = false;
        checkPermissions(activity);
        if(permissionsEnabled) {
            if(mBluetoothAdapter.isEnabled()) {
                enabled = true;
            }
            else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBtIntent, BluetoothLEService.REQUEST_ENABLE_BT);
            }
        }
        return enabled;
    }

    public void permissionsResult(int requestCode, String[] permissions, int[] grantResults, final Activity activity) {
        if (requestCode == BluetoothLEService.REQUEST_ENABLE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionsEnabled = true;
            } else {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {

                    AlertDialog.Builder dialog = new AlertDialog.Builder(activity);

                    dialog.setTitle(getString(R.string.permission_denied))
                            .setInverseBackgroundForced(true)
                            //.setIcon(R.drawable.ic_info_black_24dp)
                            .setMessage(getString(R.string.request_permissions_text))

                            .setNegativeButton(getString(R.string.dont_allow), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialoginterface, int i) {
                                    dialoginterface.dismiss();
                                    activity.finish();
                                }
                            })
                            .setPositiveButton(getString(R.string.retry), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialoginterface, int i) {
                                    dialoginterface.dismiss();
                                    mBLEService.checkPermissions(activity);
                                }
                            }).show();

                } else {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(activity);

                    dialog.setTitle(getString(R.string.permission_denied))
                            .setInverseBackgroundForced(true)
                            //.setIcon(R.drawable.ic_info_black_24dp)
                            .setMessage(getString(R.string.request_permissions_settings_text))

                            .setNegativeButton(getString(R.string.dont_allow), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialoginterface, int i) {
                                    dialoginterface.dismiss();
                                    activity.finish();
                                }
                            })
                            .setPositiveButton(getString(R.string.open_settings), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialoginterface, int i) {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    activity.startActivityForResult(intent, BluetoothLEService.REQUEST_PERMISSION_SETTING);
                                }
                            }).show();
                }
            }
        }
    }

    public String getConnectedDeviceName() {
        return mBluetoothDevice.getName();
    }

    public boolean isConnected() {
        return mConnectionState == STATE_CONNECTED;
    }

    public void getBatteryLevel() {
        if(batteryCharacteristic != null) {
            readCharacteristic(batteryCharacteristic);
        }
    }

    public class LocalBinder extends Binder {
        BluetoothLEService getService() {
            return BluetoothLEService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        notificationSubscribeHandler = new Handler();
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        mBLEService = this;

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothStateReceiver, filter);

        connectToLastDevice();

        return true;
    }

    public void checkPermissions(Activity activity) {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            permissionsEnabled = false;

            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_ENABLE_LOCATION);
        }
        else {
            permissionsEnabled = true;
        }
    }

    public static BluetoothLEService getBLEService() {
        return mBLEService;
    }

    Runnable reconnectRunnable = new Runnable() {
        @Override
        public void run() {
            if(mConnectionState == STATE_DISCONNECTED && mLastAddress != null && mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
                final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mLastAddress);
                connect(device);
            }

            reconnectHandler.postDelayed(this, 2000);
        }
    };

    public boolean connectToLastDevice() {
        mLastAddress = Preferences.getLastDevice(getApplicationContext());
        if(mLastAddress == null) {
            return false;
        }

        reconnectHandler = new Handler();
        reconnectHandler.post(reconnectRunnable);

        return true;
    }



    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param device The destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final BluetoothDevice device) {
        if (mBluetoothAdapter == null || device == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (device == mBluetoothDevice && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        if(mConnectionState == STATE_CONNECTED) {
            disconnect(false);
        }

        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDevice = device;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect(boolean forget) {
        //Remove notification
        synchronized(waitMutex) {
            descriptorWriteQueue.clear();
            notificationCharacteristicQueue.clear();
            readCharacteristicQueue.clear();
            canRead = true;
        }

        stopForeground(true);
        //Broadcast disconnection to listener
        mConnectionState = STATE_DISCONNECTED;
        Log.i(TAG, "Disconnected from GATT server.");
        broadcastUpdate(ACTION_GATT_DISCONNECTED);
        close();

        if(forget) {
            Preferences.forgetLastDevice(getApplicationContext());
            mLastAddress = null;
        }

        reconnectHandler.removeCallbacks(reconnectRunnable);
        batteryCharacteristic = null;
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.disconnect();
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        canRead = true;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        synchronized (waitMutex) {
            readCharacteristicQueue.add(characteristic);
            if (descriptorWriteQueue.size() == 0 && notificationCharacteristicQueue.size() == 0 &&
                    readCharacteristicQueue.size() == 1 && canRead) {

                mBluetoothGatt.readCharacteristic(characteristic);
                canRead = false;
            }
            else {
            }
        }
    }


    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }


        synchronized (waitMutex) {
        // This is specific to Heart Rate Measurement.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGATTAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

                descriptorWriteQueue.add(descriptor);
                if (descriptorWriteQueue.size() == 1 && canRead) {
                    mBluetoothGatt.writeDescriptor(descriptor);
                    canRead = false;
                }

                notificationCharacteristicQueue.add(characteristic);
            }
        }

        return;
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }


    //Create a notification on the phone indicating the user that a BLE device is connected
    private Notification buildForegroundNotification(String deviceName) {
        NotificationCompat.Builder b=new NotificationCompat.Builder(this);

        b.setOngoing(true)
                .setContentTitle(getString(R.string.ble_notification_title))
                .setContentText(deviceName)
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .setTicker(getString(R.string.ble_notification_title));
        Notification n = b.build();

        return(n);
    }

    public void setNotificationOn(final BluetoothGattCharacteristic characteristic) {

        Runnable r = new Runnable() {
            @Override
            public void run() {
                mBLEService.setCharacteristicNotification(characteristic, true);
                /*if(batteryCharacteristic != null) {
                    mBLEService.readCharacteristic(batteryCharacteristic);
                }*/
            }
        };


        notificationSubscribeHandler.postDelayed(r, 100);


    }

    private final BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        disconnect(false);
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        break;
                    case BluetoothAdapter.STATE_ON:
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }
    };

}