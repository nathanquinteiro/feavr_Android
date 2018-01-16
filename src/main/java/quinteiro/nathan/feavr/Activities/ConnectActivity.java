package quinteiro.nathan.feavr.Activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

import quinteiro.nathan.feavr.R;
import quinteiro.nathan.feavr.Barcode.*;
import quinteiro.nathan.feavr.utils.NetworkMulti;
import quinteiro.nathan.feavr.utils.NetworkUtils;

public class ConnectActivity extends AppCompatActivity {

    private final int BARCODE_READER_REQUEST_CODE = 1;
    private final int BARCODE_GENERATOR_REQUEST_CODE = 2;

    private boolean mConnected = false;

    private String otherIP = "";
    private boolean ipExchanged = false;
    private  boolean connectionTested = false;

    private Button scan;
    private Button generate;

    private Button testBt;

    private Button testConnectionBt;

    private Switch switchMultiPlayer;

    private TextView tvResult;

    private TextView tvCoState;


    final private String TAG_CA ="ConnectActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_connection);
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvResult = (TextView) findViewById(R.id.tvScanResult);

        switchMultiPlayer = (Switch) findViewById(R.id.swMultiPlayer);
        switchMultiPlayer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    generate.setVisibility(View.VISIBLE);
                    scan.setVisibility(View.VISIBLE);

                } else{
                    generate.setVisibility(View.INVISIBLE);
                    scan.setVisibility(View.INVISIBLE);
                }
            }
        });



        scan = (Button) findViewById(R.id.btScan);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), BarcodeCaptureActivity.class);
                startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);

            }
        });

        generate = (Button) findViewById(R.id.btGenerate);
        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BarcodeGeneratorActivity.class);
                //startActivity(intent);
                startActivityForResult(intent, BARCODE_GENERATOR_REQUEST_CODE);


            }
        });

        testBt = (Button) findViewById(R.id.button2);
        testBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("BtTest","----pushed");

                String messageStr="Hello";
                DatagramSocket s = null;
                InetAddress local = null;
                int server_port = 12345;
                try {
                    s = new DatagramSocket();
                    local = InetAddress.getByName("192.168.1.25");
                } catch (SocketException | UnknownHostException e) {
                    e.printStackTrace();
                }

                int msg_length=messageStr.length();
                byte[] message = messageStr.getBytes();

                DatagramPacket p = new DatagramPacket(message, msg_length,local,server_port);
                try {
                    assert s != null;
                    s.send(p);
                } catch (IOException e) {
                    e.printStackTrace();
                }






            }
        });



        tvCoState = (TextView) findViewById(R.id.tvConnectionState);

        testConnectionBt = (Button) findViewById(R.id.btTestCo);

        if(NetworkMulti.getInstance().isIpSetted()){
            testConnectionBt.setVisibility(View.VISIBLE);
        }
        if(NetworkMulti.getInstance().isCoTested()){
            tvCoState.setText(R.string.tvConnectionStateCO);
        } else {
            tvCoState.setText(R.string.tvConnectionState);
        }

        testConnectionBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(NetworkMulti.getInstance().isIpSetted()){

                    Log.e(TAG_CA,"testCO ");

                    //connectionTested = NetworkMulti.getInstance().testConnection();

                    if(NetworkMulti.getInstance().isCoTested()){

                        tvCoState.setText(R.string.tvConnectionStateCO);
                    } else {
                        tvCoState.setText("FAILS!");
                    }




                } else {
                    Log.e(TAG_CA,"testCO but ip not setted");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    String text = barcode.displayValue;
                    tvResult.setText(text);
                    otherIP=text;
                    ipExchanged=true;
                    testConnectionBt.setVisibility(View.VISIBLE);

                    if(NetworkUtils.isValidIP4(text)){
                        NetworkMulti.getInstance().setIP(text);

                    }



                    //ici
                    DatagramSocket s = null;
                    InetAddress local = null;
                    int server_port = 12345;
                    try {
                        s = new DatagramSocket();
                        //local = InetAddress.getByName("192.168.1.25");
                        local = InetAddress.getByName(text);
                    } catch (SocketException | UnknownHostException e) {
                        e.printStackTrace();
                    }


                    //get my ip
                    String myIp = NetworkUtils.getIP4();

                    int msg_length=myIp.length();
                    byte[] message = myIp.getBytes();

                    DatagramPacket p = new DatagramPacket(message, msg_length,local,server_port);
                    try {
                        assert s != null;
                        s.send(p);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }
            }
        }

        if (requestCode == BARCODE_GENERATOR_REQUEST_CODE){
            if(resultCode == CommonStatusCodes.SUCCESS){

                // get other ip from data ...


                if(data != null) {
                    String ip = data.getStringExtra("ip");
                    Log.e("----",ip);
                    tvResult.setText(ip);
                    otherIP = ip;
                    ipExchanged=true;


                    NetworkMulti.getInstance().setIP(ip);
                    testConnectionBt.setVisibility(View.VISIBLE);



                } else {
                    Log.e("----","data null");

                }
            }

        }
    }
}
