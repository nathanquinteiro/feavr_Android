package quinteiro.nathan.feavr.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;


import com.google.vr.sdk.proto.nano.Analytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;

import quinteiro.nathan.feavr.BLE.BLEManager;
import quinteiro.nathan.feavr.BLE.BluetoothLEService;
import quinteiro.nathan.feavr.R;
import quinteiro.nathan.feavr.Unity.FeavrReceiver;
import quinteiro.nathan.feavr.Unity.UnityPlayerActivity;
import quinteiro.nathan.feavr.utils.NetworkMulti;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private BluetoothLEService mBLEService;


    private TextView bpmTextView;

    private Button btStartGame;

    private Button btStartNW;
    private Button btSendBPM;
    private Button btSendPOS;
    private Button btFastStartNW;

    private Button btStopNW;

    private TextView tvRcvBpm;

    private Button btStopTestTH;

    private Button btStartDraw;

    private String bmpLastVal ="none";

    private Switch swDemoMode ;


    final static public String EXTRA_TEST_MODE = "TEST_MODE";

    //private logMsg myLogMsg;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bpmTextView = (TextView) findViewById(R.id.tv_bpm);

        BLEManager.startBluetoothService(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        btStartGame = (Button) findViewById(R.id.btStartGame);
        btStartGame.setOnClickListener(startGameListener);


        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());


        swDemoMode = (Switch) findViewById(R.id.sw_demo_mode);

        btStartNW = (Button) findViewById(R.id.buttonStartNW);
        btSendBPM = (Button) findViewById(R.id.buttonSENDBPM);
        btSendPOS = (Button) findViewById(R.id.buttonSENDPOS);
        btFastStartNW = (Button) findViewById(R.id.fast_STRTNW);

        btStopTestTH = (Button) findViewById(R.id.stopTestTH);

        btStopNW = (Button) findViewById(R.id.buttonStopNW);

        tvRcvBpm = (TextView) findViewById(R.id.tv_bpm);

        tvRcvBpm.setText(bmpLastVal);


        if(NetworkMulti.getInstance().isCoTested()){
            btStartNW.setVisibility(View.VISIBLE);
            btSendBPM.setVisibility(View.VISIBLE);
            btSendPOS.setVisibility(View.VISIBLE);
            btStopNW.setVisibility(View.VISIBLE);
        } else {

            btStartNW.setVisibility(View.INVISIBLE);
            btSendBPM.setVisibility(View.INVISIBLE);
            btSendPOS.setVisibility(View.INVISIBLE);
            btStopNW.setVisibility(View.INVISIBLE);

        }

        btStopNW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NetworkMulti.getInstance().stopRcvMsgThread();
            }
        });

        btStopTestTH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NetworkMulti.getInstance().stopTestThread();
            }
        });

        btFastStartNW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                NetworkMulti.getInstance();
                NetworkMulti.getInstance().setIP("192.168.1.40");
                NetworkMulti.getInstance().forceSetCoTested();

                NetworkMulti.getInstance().startTestThread(new NetworkMulti.networkMultiListener() {
                    @Override
                    public void setPosition(float[] p) {
                        Log.e("MAIN-ACT", "rcv pos : " + p[0]+", "+p[1]);
                        Log.e("MAIN-ACT","invalidate View");




                    }

                    @Override
                    public void setBPM(int bpm) {
                        Log.e("MAIN-ACT", "rcv bmp : " + bpm);

                        bmpLastVal = ""+bpm;
                        //TODO test that :
                        view.postInvalidate();

                    }
                });

                /*NetworkMulti.getInstance().startTestThread(new NetworkMulti.networkMultiListenerNewBPM() {
                    @Override
                    public void getNewBPM(int bpm) {
                        Log.e("MAIN-ACT", "rcv bmp : " + bpm);
                    }
                }, new NetworkMulti.networkMultiListenerNewPosition() {
                    @Override
                    public void getNewPosition(float[] p) {
                        Log.e("MAIN-ACT", "rcv pos : " + p[0]+", "+p[1]);

                    }
                });*/
                Log.e("-","StartNw run Started !!!");


            }
        });


        btStartNW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.e("-","StartNw run");

                NetworkMulti.getInstance().startRcvThread(new NetworkMulti.networkMultiListener() {
                    @Override
                    public void setPosition(float[] p) {
                        Log.e("MAIN-ACT", "rcv pos : " + p[0]+", "+p[1]);

                    }

                    @Override
                    public void setBPM(int bpm) {
                        Log.e("MAIN-ACT", "rcv bmp : " + bpm);





                    }
                });


                Log.e("-","StartNw run Started !!!");


            }
        });

        btSendBPM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetworkMulti.getInstance().sendBpm(83);
            }
        });

        btSendPOS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float[] ppos ={1,1};
                NetworkMulti.getInstance().sendPositions(ppos);
            }
        });

        btStartDraw = (Button) findViewById(R.id.startDrawTab);
        btStartDraw.setOnClickListener(startGameTabListener);



    }


    private View.OnClickListener startGameListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            NetworkMulti.getInstance().startRcvEventThread(new NetworkMulti.networkEventListener() {
                @Override
                public void setEvent(String msg) {

                    FeavrReceiver.setEvent(msg);
                    /*
                    try {
                        JSONObject a = new JSONObject(msg);
                        JSONArray b = a.getJSONArray("event");



                    } catch (JSONException e) {
                        e.printStackTrace();
                    }*/

                }
            });


            Intent intent = new Intent(MainActivity.this, UnityPlayerActivity.class);
            startActivity(intent);
        }
    };

    private View.OnClickListener startGameTabListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Intent intent = new Intent(MainActivity.this, gameTabActivity.class);
            Intent intent = new Intent(getApplicationContext(), gameTabActivity.class);

            intent.putExtra(EXTRA_TEST_MODE,swDemoMode.isChecked());
            startActivity(intent);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        if(NetworkMulti.getInstance().isCoTested()){
            btStartNW.setVisibility(View.VISIBLE);
            btSendBPM.setVisibility(View.VISIBLE);
            btSendPOS.setVisibility(View.VISIBLE);
        }

        //registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mGattUpdateReceiver);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

        //unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            mBLEService.disconnect(false);
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
            if(BLEManager.bluetoothEnabled()) {
                Intent intent = new Intent(this, TechnicalSettingsActivity.class);
                startActivity(intent);
            }
            //If not, request Bluetooth permission
            else {

                BLEManager.startBluetoothService(this);

            }
        }
        if (id == R.id.nav_connect) {
            Intent intent = new Intent(this, ConnectActivity.class);
            startActivity(intent);
        }

        if ( id == R.id.nav_connect_multi_players){
            Intent intent = new Intent(this, MultiPlayerConnectActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void endApp() {
        mBLEService.disconnect(false);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //After user was asked for Bluetooth permission
        if(requestCode == BLEManager.REQUEST_ENABLE_BT) {
            //If user accepted, start Bluetooth service
            if(resultCode == RESULT_OK) {
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
                                BLEManager.startBluetoothService(MainActivity.this);
                            }
                        }).show();
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
                bpmTextView.setText("");
            } else if (BluetoothLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {


                //
                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLEService.ACTION_HRM_DATA_AVAILABLE.equals(action)) {
                int bpm;
                double rrValues[];
                bpm = intent.getIntExtra(BluetoothLEService.HR_DATA, -1);
                if(bpm != -1) {
                    bpmTextView.setText("BPM: " + bpm);
                }

                rrValues = intent.getDoubleArrayExtra(BluetoothLEService.RR_DATA);
                if(rrValues != null) {
                }

            } else if (BluetoothLEService.ACTION_BATTERY_LEVEL_AVAILABLE.equals(action)) {

            }
        }
    };


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
