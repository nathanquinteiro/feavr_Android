package quinteiro.nathan.feavr.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;


import quinteiro.nathan.feavr.Barcode.BarcodeCaptureActivity;
import quinteiro.nathan.feavr.Barcode.BarcodeGeneratorActivity;
import quinteiro.nathan.feavr.R;


import quinteiro.nathan.feavr.utils.NetworkUtils;

//import static quinteiro.nathan.feavr.utils.NetworkUtils.isValidIP4;

/**
 * Created by jeremie on 14.01.18.
 */

public class MultiPlayerConnectActivity extends AppCompatActivity {

    private final String TAG = "Mult_Co_Activity";

    private final int BARCODE_READER_REQUEST_CODE = 1;
    private final int BARCODE_GENERATOR_REQUEST_CODE = 2;

    private Button btGenQR;
    private Button btScanQR;
    private Button btResetCo;

    public ProgressDialog progress;

    private TextView tvCoState;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_connection_multiplayer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }



        btGenQR = (Button) findViewById(R.id.butt_mult_genQR);

        btScanQR = (Button) findViewById(R.id.butt_mult_scanQR);

        btResetCo = (Button) findViewById(R.id.butt_mult_resetCO);

        tvCoState = (TextView) findViewById(R.id.tv_curr_connection_state_multi);



        btGenQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BarcodeGeneratorActivity.class);
                startActivityForResult(intent, BARCODE_GENERATOR_REQUEST_CODE);
            }
        });

        btScanQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BarcodeCaptureActivity.class);
                startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);
            }
        });

        btResetCo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO implement reset of the connection in connection provider
                Log.e(TAG,"Not yet implemented");
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case BARCODE_READER_REQUEST_CODE:

                if(resultCode == CommonStatusCodes.SUCCESS){
                    if(data != null){


                        tvCoState.setText(R.string.text_current_st_connected);

                        // et c'est tout le reste deletese


                        //Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                        //String ipRcv = barcode.displayValue;





                        /*if(NetworkUtils.isValidIP4(ipRcv)){
                            NetworkMulti.getInstance().setIP(ipRcv);


                            // wait stuff
                            progress = new ProgressDialog(this);
                            progress.setTitle("Wait for response");
                            progress.setMessage("Please wait ...");
                            progress.setCancelable(false);
                            progress.show();
                            progress.setMax(100);

                            boolean connectedSucessfully = NetworkMulti.getInstance().sendMyIp();  // chang sendMyIp and wait for answer ..


                            progress.dismiss();
                            //TODO try to wait some little time to be sure that everything is correct
                            // unwait stuff



                            if(connectedSucessfully){
                                tvCoState.setText(R.string.text_current_st_connected);
                            } else {
                                tvCoState.setText(R.string.text_current_st_connected_fail);
                            }

                        } else {

                            tvCoState.setText(R.string.text_current_st_connection_noIP);

                        }*/


                    } else {
                        Log.e(TAG,"Null data received");
                    }

                } else {
                    Log.e(TAG,"onActivityResult unsuccess");
                    tvCoState.setText(R.string.text_current_st_connected_fail);
                }


                break;

            case BARCODE_GENERATOR_REQUEST_CODE:

                if(resultCode == CommonStatusCodes.SUCCESS){
                    if(data != null){
                        String ip = data.getStringExtra("ip");
                        if(NetworkUtils.isValidIP4(ip)){


                            //NetworkMulti.getInstance().setIP(ip);
                            //NetworkMulti.getInstance().sendConfirmation();




                            tvCoState.setText(R.string.text_current_st_connected);

                        } else {
                            Log.e(TAG,"No valid IP received");
                        }
                    } else {
                        Log.e(TAG,"Null data received");
                    }
                } else {
                    Log.e(TAG,"onActivityResult unsuccess");
                }



                break;

            default:
                Log.e(TAG,"Error: Unknown requestCode for onActivityResult");
                break;

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
