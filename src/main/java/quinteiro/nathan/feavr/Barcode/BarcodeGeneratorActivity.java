package quinteiro.nathan.feavr.Barcode;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

import quinteiro.nathan.feavr.R;
import quinteiro.nathan.feavr.utils.NetworkMulti;
import quinteiro.nathan.feavr.utils.NetworkUtils;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

/**
 * Created by jeremie on 27.11.17.
 */

public class BarcodeGeneratorActivity extends Activity {

    public final static int QRcodeWidth = 200 ;

    public Bitmap bitmap;
    public ImageView imageViewBarcode;
    public TextView twBarcode;
    public TextView twIPRec;

    public ProgressBar pBar;



    public ProgressDialog progress;

    private  getIpAsync myGetIpTaks;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode_generator);

        //imageViewBarcode  = (ImageView) findViewById(R.id.ivBarCode);
        twBarcode = (TextView) findViewById(R.id.twBarCode);

        twIPRec = (TextView) findViewById(R.id.twIPRec);


        //String ipAddress = getIPAddress(true);
        String ipAddress = NetworkUtils.getIP4();

        //pBar = (ProgressBar) findViewById(R.id.progressBarQRCode);
        //pBar.setIndeterminate(true);


        if(ipAddress.isEmpty()){
            Log.e("e","empytx");
        } else  {
            Log.e("e","no empty");
        }

        Log.e("IP :",ipAddress);


        twBarcode.setText("My IP : "+ipAddress);



        //TODO Save state

        /*try {
            bitmap = TextToImageEncode(ipAddress);
            //imageViewBarcode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }*/

        progress = new ProgressDialog(this);


        progress.setTitle("Generating QRCode");
        progress.setMessage("Please Wait ......");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();

        progress.setMax(100);

        Log.e("-","-onCreate-");


        new generateQRCode().execute(ipAddress);

        myGetIpTaks = new getIpAsync();
        myGetIpTaks.execute();
        //new getIpAsync().execute();


    }

    @Override
    protected void onPause() {

        Log.d("IpTask","onPause");
        myGetIpTaks.cancel(true);

        super.onPause();
    }



    private class generateQRCode extends AsyncTask<String, Integer, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... strings) {

            String Value = strings[0];

            BitMatrix bitMatrix = null;
            try {
                bitMatrix = new MultiFormatWriter().encode(
                        Value,
                        //BarcodeFormat.DATA_MATRIX.QR_CODE,
                        BarcodeFormat.QR_CODE,
                        QRcodeWidth, QRcodeWidth, null
                );

            } catch (IllegalArgumentException Illegalargumentexception) {

                return null;
            } catch (WriterException e) {
                e.printStackTrace();
            }
            int bitMatrixWidth = bitMatrix.getWidth();

            int bitMatrixHeight = bitMatrix.getHeight();

            int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

            for (int y = 0; y < bitMatrixHeight; y++) {
                int offset = y * bitMatrixWidth;

                publishProgress((int) ((y/ (float) bitMatrixHeight)*100));

                for (int x = 0; x < bitMatrixWidth; x++) {

                    pixels[offset + x] = bitMatrix.get(x, y) ?
                            getResources().getColor(R.color.QRCodeBlackColor):getResources().getColor(R.color.QRCodeWhiteColor);
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

            //bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight);
            bitmap.setPixels(pixels, 0, QRcodeWidth, 0, 0, bitMatrixWidth, bitMatrixHeight);
            return bitmap;


        }

        @Override
        protected void onProgressUpdate(Integer... p) {


        }


        @Override
        protected void onPostExecute(Bitmap s){

            imageViewBarcode  = (ImageView) findViewById(R.id.ivBarCode);
            imageViewBarcode.setImageBitmap(s);
            imageViewBarcode.setVisibility(View.VISIBLE);

            pBar = (ProgressBar) findViewById(R.id.progressBarQRCode);
            pBar.setVisibility(View.INVISIBLE);

            progress.dismiss();


        }
    }

    private class getIpAsync extends AsyncTask<Void, Void, String> {

        private final String TAG_IP ="GET_IP";
        @Override
        protected String doInBackground(Void... voids) {

            boolean timeout = true;

            int server_port = 12345;
            byte[] message = new byte[500];
            DatagramPacket p = new DatagramPacket(message, message.length);
            DatagramSocket s = null;

            Log.e(TAG_IP, "Listen task");
            int i = 0;

            try {
                s = new DatagramSocket(server_port);
                s.setSoTimeout(1000);
            } catch (SocketException e) {
                e.printStackTrace();
            }

            while (timeout && !isCancelled()) {

                timeout = false;


                try {

                    Log.e(TAG_IP,"after new");

                    s.receive(p);
                } catch (SocketTimeoutException e) {
                    Log.e(TAG_IP, "socket timeout exception");
                    //e.printStackTrace();
                    i = 1;

                    timeout = true;


                } catch (SocketException e) {
                    //Log.e(TAG_IP, "socket exception");
                    timeout = true;
                    i = 2;

                } catch (IOException e) {
                    Log.e(TAG_IP, "IO exception");
                    e.printStackTrace();
                    timeout = true;
                    i = 3;
                }

            }
            Log.e(TAG_IP, "" + i);
            String text;

            s.close();

            if (!timeout && !isCancelled()){

                text = new String(message, 0, p.getLength());


                if(NetworkUtils.isValidIP4(text)){

                    NetworkMulti.getInstance().setIP(text);
                    NetworkMulti.getInstance().sendConfirmation();

                    NetworkMulti.getInstance().initConnection();





                }




        } else {
                text="-";
            }


            return text;



        }

        @Override
        protected void onPostExecute(String s){


            if(NetworkUtils.isValidIP4(s)){

                Intent returnIntent = new Intent();
                returnIntent.putExtra("ip",s);
                setResult(CommonStatusCodes.SUCCESS,returnIntent);
                finish();

            } else {

                Log.e(TAG_IP,"Wrong IP received :"+s);

                TextView tw = (TextView) findViewById(R.id.twIPRec);
                tw.setText("Invalid ip received  : "+s);

            }
        }
    }
}
