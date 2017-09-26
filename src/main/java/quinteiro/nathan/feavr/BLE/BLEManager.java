package quinteiro.nathan.feavr.BLE;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;

/**
 * Created by nathan on 03.06.17.
 */

public class BLEManager {

    public final static int REQUEST_ENABLE_BT = 1;
    private static BLEManager mBLEManager;
    private static BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mBLEService;

    private BLEManager() {
    }

    public static boolean startBluetoothService(Activity activity) {
        if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            // Initializes Bluetooth adapter.
            final BluetoothManager bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();

            // Ensures Bluetooth is available on the device and it is enabled. If not,
            // displays a dialog requesting user permission to enable Bluetooth.
            if (!bluetoothEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        if(bluetoothEnabled()) {
            Intent BLEServiceIntent = new Intent(activity, BluetoothLEService.class);
            activity.startService(BLEServiceIntent);
            return true;
        }

        return false;
    }

    public static boolean bluetoothEnabled() {
        return !(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled());
    }

    public static void connectToDevice(Activity activity) {

    }
}
