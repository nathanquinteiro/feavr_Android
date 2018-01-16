package quinteiro.nathan.feavr.Unity;

import com.unity3d.player.*;
import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;

import quinteiro.nathan.feavr.BLE.BluetoothLEService;
import quinteiro.nathan.feavr.R;
import quinteiro.nathan.feavr.Wear.WearListenerService;

public class UnityPlayerActivity extends Activity
{
	protected UnityPlayer mUnityPlayer; // don't change the name of this variable; referenced from native code

	Handler handler;

	// Setup activity layout
	@Override protected void onCreate (Bundle savedInstanceState)
	{
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);

		getWindow().setFormat(PixelFormat.RGBX_8888); // <--- This makes xperia play happy

		mUnityPlayer = new UnityPlayer(this);
		setContentView(mUnityPlayer);
		mUnityPlayer.requestFocus();


		//Receive HR from BLE
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

		//Receive HR from watch
		registerReceiver(mHeartRateReceiver, new IntentFilter(WearListenerService.ACTION_SEND_HEART_RATE));
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
			Log.d("Received form Watch","BPM: " + bpm);

			if(bpm != -1) {
				Log.e("Received", "BPM: " + bpm);
				FeavrReceiver.setBPM(bpm);
			}
		}
	};


	@Override
	public void onBackPressed() {
		System.out.print("sd");
		super.onBackPressed();
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

	// Quit Unity
	@Override protected void onDestroy ()
	{
		mUnityPlayer.quit();
		unregisterReceiver(mGattUpdateReceiver);
		unregisterReceiver(mHeartRateReceiver);
		super.onDestroy();
	}

	// Pause Unity
	@Override protected void onPause()
	{
		super.onPause();
		mUnityPlayer.pause();
	}

	// Resume Unity
	@Override protected void onResume()
	{
		super.onResume();
		mUnityPlayer.resume();
	}

	// This ensures the layout will be correct.
	@Override public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		mUnityPlayer.configurationChanged(newConfig);
	}

	// Notify Unity of the focus change.
	@Override public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);
		mUnityPlayer.windowFocusChanged(hasFocus);
	}

	// For some reason the multiple keyevent type is not supported by the ndk.
	// Force event injection by overriding dispatchKeyEvent().
	@Override public boolean dispatchKeyEvent(KeyEvent event)
	{
		if (event.getAction() == KeyEvent.ACTION_MULTIPLE)
			return mUnityPlayer.injectEvent(event);
		return super.dispatchKeyEvent(event);
	}

	// Pass any events not handled by (unfocused) views straight to UnityPlayer
	@Override public boolean onKeyUp(int keyCode, KeyEvent event)     { return mUnityPlayer.injectEvent(event); }
	@Override public boolean onKeyDown(int keyCode, KeyEvent event)   {
		if(keyCode == 4) {
			finish();
		}
		return mUnityPlayer.injectEvent(event);
	}
	@Override public boolean onTouchEvent(MotionEvent event)          { return mUnityPlayer.injectEvent(event); }
	/*API12*/ public boolean onGenericMotionEvent(MotionEvent event)  { return mUnityPlayer.injectEvent(event); }
}
