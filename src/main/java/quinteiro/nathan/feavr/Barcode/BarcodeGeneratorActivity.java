package quinteiro.nathan.feavr.Barcode;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

import quinteiro.nathan.feavr.R;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

/**
 * Created by jeremie on 27.11.17.
 */

public class BarcodeGeneratorActivity extends Activity {

    public final static int QRcodeWidth = 500 ;

    public Bitmap bitmap;
    public ImageView imageViewBarcode;
    public TextView twBarcode;
    public TextView twIPRec;
    public String ip = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode_generator);

        imageViewBarcode  = (ImageView) findViewById(R.id.ivBarCode);
        twBarcode = (TextView) findViewById(R.id.twBarCode);

        twIPRec = (TextView) findViewById(R.id.twIPRec);


        String ipAddress = getIPAddress(true);



        twBarcode.setText("My IP : "+ipAddress);

        //TODO Use a spinner (generate the qrcode take sometimes...)

        //TODO Save state

        try {
            bitmap = TextToImageEncode(ipAddress);
            imageViewBarcode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }



        class getIpAsync extends AsyncTask<Void, Void, String> {

            @Override
            protected String doInBackground(Void... voids) {

                int server_port = 12345;
                byte[] message = new byte[500];
                DatagramPacket p = new DatagramPacket(message, message.length);
                DatagramSocket s = null;
                try {
                    s = new DatagramSocket(server_port);
                    s.receive(p);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String text = new String(message, 0, p.getLength());


                return text;



            }

            @Override
            protected void onPostExecute(String s){
                if (R.id.twIPRec!=1){

                    TextView tw = (TextView) findViewById(R.id.twIPRec);
                    tw.setText("Other IP : "+s);


                }
            }
        }

        new getIpAsync().execute();
        //getIpAsync a = new getIpAsync();
        //a.doInBackground();




/*
        Runnable getIP = new Runnable() {




            @Override
            public void run() {
                //client side : http://www.helloandroid.com/tutorials/simple-udp-communication-example
                int server_port = 12345;
                byte[] message = new byte[500];
                DatagramPacket p = new DatagramPacket(message, message.length);
                DatagramSocket s = null;
                try {
                    s = new DatagramSocket(server_port);
                    s.receive(p);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String text = new String(message, 0, p.getLength());
                Log.e("Udp",text);
                //ip=text;
                //ip = text;

                TextView tw = (TextView) findViewById(R.id.twIPRec);
                tw.setText("Other IP : "+text);




                //twIPRec.setText("Other IP : "+text);

            }
        };

        new Thread(getIP).start();
*/



        /*twIPRec.post(new Runnable() {
            @Override
            public void run() {

                int server_port = 12345;
                byte[] message = new byte[500];
                DatagramPacket p = new DatagramPacket(message, message.length);
                DatagramSocket s = null;
                try {
                    s = new DatagramSocket(server_port);
                    s.receive(p);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String text = new String(message, 0, p.getLength());
                Log.e("Udp",text);
                twIPRec.setText("Other IP : "+ text);

            }
        });*/



    }



    Bitmap TextToImageEncode(String Value) throws WriterException {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(
                    Value,
                    //BarcodeFormat.DATA_MATRIX.QR_CODE,
                    BarcodeFormat.QR_CODE,
                    QRcodeWidth, QRcodeWidth, null
            );

        } catch (IllegalArgumentException Illegalargumentexception) {

            return null;
        }
        int bitMatrixWidth = bitMatrix.getWidth();

        int bitMatrixHeight = bitMatrix.getHeight();

        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;

            for (int x = 0; x < bitMatrixWidth; x++) {

                pixels[offset + x] = bitMatrix.get(x, y) ?
                        getResources().getColor(R.color.QRCodeBlackColor):getResources().getColor(R.color.QRCodeWhiteColor);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
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
