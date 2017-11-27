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

import quinteiro.nathan.feavr.R;
import quinteiro.nathan.feavr.Barcode.*;

public class ConnectActivity extends AppCompatActivity {

    private final int BARCODE_READER_REQUEST_CODE = 1;
    private final int BARCODE_GENERATOR_REQUEST_CODE = 2;

    private boolean mConnected = false;

    private Button scan;
    private Button generate;

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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    String text = barcode.displayValue;
                    tvResult.setText(text);
                }
            }
        }
    }


}
