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

public class ConnectActivity extends AppCompatActivity {

    private final int BARCODE_READER_REQUEST_CODE = 1;
    private final int BARCODE_GENERATOR_REQUEST_CODE = 2;

    private boolean mConnected = false;

    private Button scan;
    private Button generate;

    private Button testBt;

    private Switch switchMultiPlayer;

    private TextView tvResult;

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
                startActivity(intent);


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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    String text = barcode.displayValue;
                    tvResult.setText(text);


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
                    String myIp = getIPAddress(true);

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
    }

    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
        } // for now eat exceptions
        return "";
    }


}
