package quinteiro.nathan.feavr.utils;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import java.net.UnknownHostException;

/**
 * Created by jeremie on 04.12.17.
 */

public class NetworkMulti {

    private static NetworkMulti _m = null;

    private String myIp = "";
    private String otherIp = "";

    private boolean connectionTested ;
    private boolean ipSetted ;
    private boolean headSetMode=false;


    private DatagramSocket outSocket = null;
    private InetAddress outLocal = null;


    private receiveMsg rcv_T=null;

    final private String MSG_POS = "POSITION";
    final private String MSG_BPM = "BPM";
    final private int server_port = 12345;


    //PING setup
    private int pingDuration =  5000;
    private int nbPingMsg = 10;

    final private String TAG_SEND="NW_SEND_MSG";



    private NetworkMulti(){

        this.myIp = "";
        this.otherIp="";
        this.connectionTested = false;
        this.ipSetted = false;

    }


    public String [] getIps(){
        return new String[]{myIp,otherIp};
    }

    public boolean isIpSetted(){
        return this.ipSetted;
    }

    public boolean isCoTested(){
        return this.connectionTested;
    }

    public void forceSetCoTested(){
        this.connectionTested=true;
    }



    public boolean testConnection(){

        final  String TAG_TESTCO="TestCO";
        if(this.ipSetted){

            Thread rc = new Thread(new receivePing(),"T_rc");
            Thread sd = new Thread(new sendPing(),"T_sd");

            this.connectionTested=false;


            rc.start();
            sd.start();

            Log.e(TAG_TESTCO,"bothStarted");

            try {
                Log.e(TAG_TESTCO,"JOIN RC thread");
                rc.join();
            } catch (InterruptedException e) {
                Log.e(TAG_TESTCO,"fail join RC thread");
                e.printStackTrace();
            }


            try {
                Log.e(TAG_TESTCO,"JOIN SD thread");
                sd.join();
            } catch (InterruptedException e) {
                Log.e(TAG_TESTCO,"fail join SD thread");
                e.printStackTrace();
            }

            Log.e(TAG_TESTCO,"both joined return");



            if(this.connectionTested){

                try {
                    outSocket = new DatagramSocket();
                    outLocal = InetAddress.getByName(otherIp);
                } catch (SocketException | UnknownHostException e) {
                    this.connectionTested=false;
                    e.printStackTrace();
                }

            }




            return this.connectionTested;


        }

        this.connectionTested = false;
        return false;

    }

    private class receivePing implements Runnable{
        private final String TAG_RCV_PING ="RECV_PING";

        @Override
        public void run() {


            byte[] message = new byte[500];
            DatagramPacket p = new DatagramPacket(message, message.length);
            DatagramSocket s = null;


            int i = 0;

            try {
                s = new DatagramSocket(server_port);
                s.setSoTimeout(pingDuration);
            } catch (SocketException e) {
                e.printStackTrace();
            }


            try {
                s.receive(p);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String text;


            text = new String(message, 0, p.getLength());

            if(text.equals("pingMsg")){
                Log.e("TAG_RCV_PING","MSG contains ping");
                connectionTested=true;

            } else {
                Log.e("TAG_RCV_PING","MSG not contains ping");
            }



            s.close();







        }
    }


    private class sendPing implements Runnable{
        private final String TAG_SD_PING ="SD_PING";
        @Override
        public void run() {


            //ici
            DatagramSocket s = null;
            InetAddress local = null;

            try {
                s = new DatagramSocket();
                //local = InetAddress.getByName("192.168.1.25");
                local = InetAddress.getByName(otherIp);
            } catch (SocketException | UnknownHostException e) {
                e.printStackTrace();
            }


            String pingMsg = "pingMsg";


            int msg_length=pingMsg.length();
            byte[] message = pingMsg.getBytes();

            DatagramPacket p = new DatagramPacket(message, msg_length,local,server_port);

            int nbSend = nbPingMsg;

            while (nbSend > 0){

            try {
                assert s != null;
                s.send(p);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep((long) pingDuration/nbPingMsg);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e(TAG_SD_PING,"unable to sleep 0o");
            }

            nbSend--;
            Log.d(TAG_SD_PING,"SEND :"+nbSend);

            }

        }
    }

    private void sendMsg(String msg){

        int msg_length=msg.length();
        byte[] message = msg.getBytes();


        DatagramPacket p = new DatagramPacket(message, msg_length,outLocal,server_port);


        try {
            outSocket.send(p);
            Log.d(TAG_SEND,msg);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG_SEND,"Unable to send: "+msg);

        }


    }

    public void sendPositions(float[] p){

        if(this.isCoTested()){

            String msg = MSG_POS;
            for (float pos : p){
                msg+="/"+Float.toString(pos);
            }

            this.sendMsg(msg);
        }
    }

    public void sendBpm(int bpm){

        if(this.isCoTested()){

            String msg = MSG_BPM;
            msg+="/"+Integer.toString(bpm);

            this.sendMsg(msg);

        }

    }

    public void setIP(String oIp){
        this.myIp = NetworkUtils.getIP4();
        this.otherIp = oIp;

        this.ipSetted = true;
    }


    public static  NetworkMulti getInstance(){
        if(_m == null){
            _m = new NetworkMulti();
        }

        return _m;
    }

    /*public void initHeadSetMode(){

        this.headSetMode=true;
        // voir connecteur
        // FeavrReceiver
        //
        // Thread get all msg and send it to FeavrReceiver through listener

    }*/



    //Stuff with listener

    public void initRcvMsg(final networkMultiListenerNewPosition listPos, final networkMultiListenerNewBPM listBPM){


        // this is a thread


        //check

        //Thread rcv_T = new Thread(new receiveMsg());

        if(rcv_T == null){
            rcv_T = new receiveMsg();
            rcv_T.setList(listPos,listBPM);

            Log.e("NW-multi","RunThread");
            rcv_T.run();
            Log.e("NW-multi","Thread started !");


        }


    }

    /*public void stopRcvMsg(){
        if(rcv_T!= null){
            rcv_T.stopRCV();


            try {
                rcv_T.join();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            rcv_T=null;
        }
    }*/

    Thread testThread;

    private boolean active = true;

    public void stopTestThread(){
        active=false;
        if(testThread!= null){
            try {
                Log.e("testT","before join");
                testThread.join();
                Log.e("testT","after join");
                testThread=null;
            } catch (InterruptedException e) {

                e.printStackTrace();
            }
        } else {
            Log.e("testT","call stop but thread is null");
        }
    }

    public void startTestThread(final networkMultiListenerNewBPM lb, final networkMultiListenerNewPosition lp){

        final networkMultiListenerNewBPM list_BPM = lb;
        final networkMultiListenerNewPosition list_Pos = lp;

        active=true;

        if(testThread==null){
            testThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    int i = 1;
                    Log.e("TestT","run, active : "+active);

                    while (active){

                        i++;

                        if(i%50000000==0){
                            i=1;
                            Log.e("nwM","moddullo");
                            list_BPM.getNewBPM(123);
                            float[] ppos = {1,4};
                            list_Pos.getNewPosition(ppos);
                        }


                    }

                }
            });
            testThread.start();
        } else {
            Log.e("TestT","call start but probably already started");
        }
    }

    Thread rcvMsgThread;

    public void startRcvMsgThread(final networkMultiListenerNewBPM lb, final networkMultiListenerNewPosition lp){

        final networkMultiListenerNewBPM list_BPM = lb;
        final networkMultiListenerNewPosition list_Pos = lp;

        final String TAG_RCV_MSG = "newTh";

        if(rcvMsgThread==null) {

            rcvMsgThread = new Thread(new Runnable() {
                @Override
                public void run() {
////////////////////
///
/// //////////////


                    byte[] message = new byte[500];
                    DatagramPacket p = new DatagramPacket(message, message.length);
                    DatagramSocket s = null;

                    boolean active = true;
                    int i = 0;

                    try {
                        s = new DatagramSocket(server_port);
                        s.setSoTimeout(1000);
                    } catch (SocketException e) {
                        e.printStackTrace();
                        active=false;
                        Log.e(TAG_RCV_MSG,"-fails init socket or fail set timout");
                        return;
                    }


                    boolean validMsg = true;
                    while (active) {

                        try {
                            //quand meme mettre timout pour désactivé après si non on s'en sort jamais ....

                            s.receive(p);
                            validMsg = true;
                        } catch (IOException e) {
                            //e.printStackTrace();
                            //Log.e(TAG_RCV_MSG,"-IOException receive msg");

                            validMsg = false;

                        }

                        if(validMsg){

                            String text;
                            text = new String(message, 0, p.getLength());


                            String[] splitted = text.split("/");

                            if (splitted.length != 0) {

                                if (splitted[0].equals(MSG_POS)) {

                                    if (splitted.length == 3) {

                                        float[] ppos = {Float.parseFloat(splitted[1]), Float.parseFloat(splitted[2])};
                                        list_Pos.getNewPosition(ppos);

                                    } else {
                                        Log.e(TAG_RCV_MSG, "Wrong size msg pos :" + splitted.length);
                                    }


                                } else if (splitted[0].equals(MSG_BPM)) {

                                    if (splitted.length == 2) {
                                        int b = Integer.parseInt(splitted[1]);
                                        list_BPM.getNewBPM(b);

                                    } else {
                                        Log.e(TAG_RCV_MSG, "Wrong size msg bmp :" + splitted.length);
                                    }

                                }

                            }
                        }


                    }

                    if (s != null) {
                        s.close();
                    }





                    //////////////////////
                    /*int i = 1;

                    while (true) {

                        i++;

                        if (i % 100000000 == 0) {
                            i = 1;
                            Log.e("nwM", "moddulloFast");
                            list_BPM.getNewBPM(123);
                            float[] ppos = {1, 4};
                            list_Pos.getNewPosition(ppos);
                        }


                    }*/

                }
            });
            rcvMsgThread.start();
        }
    }

    private class receiveMsg implements Runnable{

        private final String TAG_RCV_MSG ="RECV_MSG";

        private networkMultiListenerNewPosition listPos = null;
        private networkMultiListenerNewBPM listBPM = null;

        private boolean  active = true;


        public void setList(final networkMultiListenerNewPosition lp, final networkMultiListenerNewBPM lb){
            listPos=lp;
            listBPM=lb;

        }

        @Override
        public void run() {

            byte[] message = new byte[500];
            DatagramPacket p = new DatagramPacket(message, message.length);
            DatagramSocket s = null;


            int i = 0;

            try {
                s = new DatagramSocket(server_port);
                s.setSoTimeout(1000);
            } catch (SocketException e) {
                e.printStackTrace();
                active=false;
                Log.e(TAG_RCV_MSG,"-fails init socket or fail set timout");
                return;
            }


            boolean validMsg = true;
            while (active) {

                try {
                    //quand meme mettre timout pour désactivé après si non on s'en sort jamais ....

                    s.receive(p);
                    validMsg = true;
                } catch (IOException e) {
                    //e.printStackTrace();
                    //Log.e(TAG_RCV_MSG,"-IOException receive msg");

                    validMsg = false;

                }

                if(validMsg){

                    String text;
                    text = new String(message, 0, p.getLength());


                    String[] splitted = text.split("/");

                    if (splitted.length != 0) {

                        if (splitted[0].equals(MSG_POS)) {

                            if (splitted.length == 3) {

                                float[] ppos = {Float.parseFloat(splitted[1]), Float.parseFloat(splitted[2])};
                                listPos.getNewPosition(ppos);

                            } else {
                                Log.e(TAG_RCV_MSG, "Wrong size msg pos :" + splitted.length);
                            }


                        } else if (splitted[0].equals(MSG_BPM)) {

                            if (splitted.length == 2) {
                                int b = Integer.parseInt(splitted[1]);
                                listBPM.getNewBPM(b);

                            } else {
                                Log.e(TAG_RCV_MSG, "Wrong size msg bmp :" + splitted.length);
                            }

                        }

                    }
                }


            }

            if (s != null) {
                s.close();
            }


        }
    }

    /*private class receiveMsg extends Thread{
        private final String TAG_RCV_MSG ="RECV_MSG";

        private networkMultiListenerNewPosition listPos = null;
        private networkMultiListenerNewBPM listBPM = null;



        public void stopRCV(){
            active=false;
        }

        //faire qqch avec synchronised pour stopper le thread
        private boolean  active = true;

        public void setList(final networkMultiListenerNewPosition lp, final networkMultiListenerNewBPM lb){
            listPos=lp;
            listBPM=lb;

        }

        @Override
        public void run() {



            byte[] message = new byte[500];
            DatagramPacket p = new DatagramPacket(message, message.length);
            DatagramSocket s = null;


            int i = 0;

            try {
                s = new DatagramSocket(server_port);
            } catch (SocketException e) {
                e.printStackTrace();
                active=false;
                Log.e(TAG_RCV_MSG,"-fails init socket");
            }

            try {
                s.setSoTimeout(1000);
            } catch (SocketException e) {
                e.printStackTrace();
                Log.e(TAG_RCV_MSG,"-fails set Timout socket");
            }

            boolean validMsg = true;
            while (active) {

                try {
                    //quand meme mettre timout pour désactivé après si non on s'en sort jamais ....

                    s.receive(p);
                    validMsg = true;
                } catch (IOException e) {
                    //e.printStackTrace();
                    //Log.e(TAG_RCV_MSG,"-IOException receive msg");

                    validMsg = false;

                }

                if(validMsg){

                    String text;
                    text = new String(message, 0, p.getLength());


                    String[] splitted = text.split("/");

                    if (splitted.length != 0) {

                        if (splitted[0].equals(MSG_POS)) {

                            if (splitted.length == 3) {

                                float[] ppos = {Float.parseFloat(splitted[1]), Float.parseFloat(splitted[2])};
                                listPos.getNewPosition(ppos);

                            } else {
                                Log.e(TAG_RCV_MSG, "Wrong size msg pos :" + splitted.length);
                            }


                        } else if (splitted[0].equals(MSG_BPM)) {

                            if (splitted.length == 2) {
                                int b = Integer.parseInt(splitted[1]);
                                listBPM.getNewBPM(b);

                            } else {
                                Log.e(TAG_RCV_MSG, "Wrong size msg bmp :" + splitted.length);
                            }

                        }

                    }
            }


            }

            if (s != null) {
                s.close();
            }


        }
    }*/



    public interface networkMultiListenerNewPosition{
        void getNewPosition(float[] p);
    }

    public interface networkMultiListenerNewBPM{
        void getNewBPM(int bpm);
    }




}
