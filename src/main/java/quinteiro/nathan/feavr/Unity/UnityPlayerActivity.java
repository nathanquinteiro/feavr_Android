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
import quinteiro.nathan.feavr.utils.NetworkMulti;
import quinteiro.nathan.feavr.utils.NetworkUtils;

public class UnityPlayerActivity extends Activity
{
	protected UnityPlayer mUnityPlayer; // don't change the name of this variable; referenced from native code

	Handler handler;

	// Setup activity layout
	@Override protected void onCreate (Bundle savedInstanceState)
	{
		//Receive events from controler and transmit to Unity
		NetworkMulti.getInstance().startRcvEventThread(new NetworkMulti.networkEventListener() {
			@Override
			public void setEvent(String msg) {
				FeavrReceiver.setEvent(msg);
			}
		});

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);

		getWindow().setFormat(PixelFormat.RGBX_8888); // <--- This makes xperia play happy

		mUnityPlayer = new UnityPlayer(this);
		setContentView(mUnityPlayer);
		mUnityPlayer.requestFocus();

	}


	// Quit Unity
	@Override protected void onDestroy ()
	{
		mUnityPlayer.quit();
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
			//Quit the Unity Player in a clean way
			onDestroy();
			//mUnityPlayer.quit();
		}
		return mUnityPlayer.injectEvent(event);
	}
	@Override public boolean onTouchEvent(MotionEvent event)          { return mUnityPlayer.injectEvent(event); }
	/*API12*/ public boolean onGenericMotionEvent(MotionEvent event)  { return mUnityPlayer.injectEvent(event); }
}
