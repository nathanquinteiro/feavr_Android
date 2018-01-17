package quinteiro.nathan.feavr.utils;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import java.net.UnknownHostException;

import quinteiro.nathan.feavr.Activities.MainActivity;

/**
 * Created by jeremie on 04.12.17.
 */

public class NetworkMulti {

    private static NetworkMulti _m = null;

    private String myIp = "";
    private String otherIp = "";

    private boolean connectionTested ;
    private boolean ipSetted ;

    private DatagramSocket outSocket = null;
    private InetAddress outLocal = null;


    final private String MSG_POS = "POSITION";
    final private String MSG_BPM = "BPM";
    final private String MSG_EVENT = "EVENT";
    final private int server_port = 12345;


    //PING setup

    private int waitConfirmation = 1000;
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

    //public void validCoTested() {this.connectionTested=true; }

    public void forceSetCoTested(){
        this.connectionTested=true;
    }


    public boolean initConnection(){

        try {
            outSocket = new DatagramSocket();
            outLocal = InetAddress.getByName(otherIp);
        } catch (SocketException e) {
            e.printStackTrace();
            return false;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        }

        this.connectionTested = true;
        return true;


    }

    public void sendConfirmation(){

        if(ipSetted) {
            DatagramSocket s = null;
            InetAddress local = null;
            int server_port = 12345;
            try {
                s = new DatagramSocket();
                local = InetAddress.getByName(otherIp);
            } catch (SocketException | UnknownHostException e) {
                e.printStackTrace();
            }


            // message
            String msg = "OK_IP_RECEIVED";

            int msg_length=msg.length();
            byte[] message = msg.getBytes();

            DatagramPacket p = new DatagramPacket(message, msg_length,local,server_port);
            try {
                assert s != null;
                s.send(p);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean sendMyIp(){

        if(ipSetted){

            DatagramSocket s = null;
            InetAddress local = null;
            int server_port = 12345;
            try {
                s = new DatagramSocket();
                local = InetAddress.getByName(otherIp);
            } catch (SocketException | UnknownHostException e) {
                e.printStackTrace();
            }


            //get my ip
            String myIp = NetworkUtils.getIP4();

            int msg_length=myIp.length();
            byte[] messageIP = myIp.getBytes();

            DatagramPacket p = new DatagramPacket(messageIP, msg_length,local,server_port);
            try {
                assert s != null;
                s.send(p);
            } catch (IOException e) {
                e.printStackTrace();
            }


            // GET ONE MSG CHECK IF  ici

            s.close();

            byte[] message = new byte[500];
            //DatagramPacket p = new DatagramPacket(message, message.length);
            //DatagramSocket s = null;

            p = new DatagramPacket(message, message.length);
            s = null;



            int i = 0;

            try {
                s = new DatagramSocket(server_port);
                s.setSoTimeout(waitConfirmation);
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

            boolean ok = false;
            if(text.equals("OK_IP_RECEIVED")){
                Log.e("TAG_RCV_PING","MSG contains ping");
                //connectionTested=true;
                ok = true;

            } else {
                Log.e("TAG_RCV_PING","MSG not contains ping");
                ok = false;
            }



            s.close();

            return  ok;



        }



        return false;

    }

   /*public boolean testConnection(){

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

    }*/

    /*private class receivePing implements Runnable{
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
    }*/


    /*private class sendPing implements Runnable{
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
    }*/
    public void sendEvent(String msg){

        if(this.isCoTested()){
            String fmsg = MSG_EVENT;
            fmsg+="/"+msg;
            sendMsg(fmsg);
        }

    }

    private void sendMsg(String msg){

        new SendUdpMessage().execute(msg);


    }

    public void sendPositions(float[] p){

        if(this.isCoTested()){

            String msg = MSG_POS;
            for (float pos : p){
                msg+="/"+Float.toString(pos);
            }

            Log.e("COMM","send pos: " + msg);

            this.sendMsg(msg);
        }
    }

    public void sendBpm(int bpm){

        if(this.isCoTested()){

            String msg = MSG_BPM;
            msg+="/"+Integer.toString(bpm);


            Log.e("COMM","send bpm: " + msg);

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

    public void reset(){
        if(outSocket!=null){
            outSocket.close();
        }

        rcvEventThreadActive = false;
        rcvThreadActive = false;

    }

    /*public void initHeadSetMode(){

        this.headSetMode=true;
        // voir connecteur
        // FeavrReceiver
        //
        // Thread get all msg and send it to FeavrReceiver through listener

    }*/




    // old verion not working !!!
    /*
    public void initRcvMsg(final networkMultiListenerNewPosition listPos, final networkMultiListenerNewBPM listBPM){




        if(rcv_T == null){
            rcv_T = new receiveMsg();
            rcv_T.setList(listPos,listBPM);

            Log.e("NW-multi","RunThread");
            rcv_T.run();
            Log.e("NW-multi","Thread started !");


        }


    }*/

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

    private boolean testThreadActive = true;

    public void stopTestThread(){
        testThreadActive=false;
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





    //public void startTestThread(final networkMultiListenerNewBPM lb, final networkMultiListenerNewPosition lp){
    public void startTestThread(final networkMultiListener l){
        //final networkMultiListenerNewBPM list_BPM = lb;
        //final networkMultiListenerNewPosition list_Pos = lp;
        final networkMultiListener listener = l;

        testThreadActive=true;

        final int minX = -4;
        final int minY = -4;

        final int maxX = 59;
        final int maxY = 59;


        if(testThread==null){
            testThread = new Thread(new Runnable() {
                @Override
                public void run() {


                    int xMax = 63;
                    int yMax = 63;

                    int b = 0;


                    //int i = 1;
                    Log.e("TestT","run, active : "+testThreadActive);

                    while (testThreadActive){

//randomNum = minimum + (int)(Math.random() * maximum);

                        int x =0;
                        int y =0;
                        int ll = 56;

                        int randomNumber =0;

                        for(int sector =0; sector<4;sector++){
                            switch (sector) {
                                case 0:
                                    x=0;
                                    y=0;
                                    for(int i = 0;i<ll;i++) {

                                        randomNumber = (int)(Math.random()*2-1);
                                        x=i;
                                        y=randomNumber;
                                        float[] ppos = {x,y};
                                        listener.setBPM(80+(int)(Math.random()*10));
                                        listener.setPosition(ppos);
                                        try {
                                            Thread.sleep(250);
                                        } catch (InterruptedException e) {
                                            //
                                        }


                                    }

                                        break;
                                case 1:

                                    x=56;
                                    y=0;
                                    for(int i = 0;i<ll;i++) {

                                        randomNumber = (int)(Math.random()*2-1);
                                        y=i;
                                        x = 56-randomNumber;
                                        float[] ppos = {x,y};
                                        listener.setBPM(90+(int)(Math.random()*10));
                                        listener.setPosition(ppos);
                                        try {
                                            Thread.sleep(250);
                                        } catch (InterruptedException e) {
                                            //
                                        }


                                    }

                                    break;

                                case 2:

                                    x=56;
                                    y=56;

                                    for(int i = 56;i>1;i--) {

                                        randomNumber = (int)(Math.random()*2-1);
                                        x=i;
                                        y=56-randomNumber;
                                        float[] ppos = {x,y};
                                        listener.setBPM(90+(int)(Math.random()*10));
                                        listener.setPosition(ppos);
                                        try {
                                            Thread.sleep(250);
                                        } catch (InterruptedException e) {
                                            //
                                        }


                                    }

                                    break;
                                case 3:

                                    x=0;
                                    y=56;
                                    for(int i = 56;i>1;i--) {


                                        randomNumber = (int)(Math.random()*2-1);
                                        x=randomNumber;
                                        y=i;
                                        float[] ppos = {x,y};
                                        listener.setBPM(90+(int)(Math.random()*10));
                                        listener.setPosition(ppos);
                                        try {
                                            Thread.sleep(250);
                                        } catch (InterruptedException e) {
                                            //
                                        }


                                    }

                                    break;


                            }
                        }

                        /*try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            //e.printStackTrace();
                        }
                        x+= 10;
                        y+= 10;

                        b+=4;

                        float[] ppos = {x%xMax,y%yMax};

                        listener.setBPM(80+(b%5));
                        listener.setPosition(ppos);*/









                        /*i++;

                        if(i%5000000==0){
                            i=1;
                            Log.e("nwM","moddullo");
                            //list_BPM.getNewBPM(123);
                            //float[] ppos = {1,4};
                            x++;
                            y++;
                            b++;
                            float[] ppos = {x%xMax,y%yMax};

                            listener.setBPM(80+(b%5));
                            listener.setPosition(ppos);


                        }*/


                    }

                }
            });
            testThread.start();
        } else {
            Log.e("TestT","call start but probably already started");
        }
    }



    Thread rcvEventThread;
    boolean rcvEventThreadActive = true;

    public void startRcvEventThread(final networkEventListener l){

        final networkEventListener lIstener = l;
        final String TAG_RCV_EVENT = "RCV_EVENT";

        if(rcvEventThread==null){


            rcvEventThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    rcvEventThreadActive = true;

                    byte[] message = new byte[500];
                    DatagramPacket p = new DatagramPacket(message, message.length);
                    DatagramSocket s = null;


                    try {
                        s = new DatagramSocket(server_port);
                        s.setSoTimeout(500);
                    } catch (SocketException e) {
                        e.printStackTrace();
                        rcvEventThreadActive=false;
                        Log.e(TAG_RCV_EVENT,"-fails init socket or fail set timeout");
                        return;
                    }


                    boolean validMsg ;
                    while (rcvThreadActive) {

                        try {

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

                                if (splitted[0].equals(MSG_EVENT)) {

                                    if (splitted.length == 2) {

                                        //float[] ppos = {Float.parseFloat(splitted[1]), Float.parseFloat(splitted[2])};
                                        //listener.setPosition(ppos);
                                        lIstener.setEvent(splitted[1]);

                                    } else {
                                        Log.e(TAG_RCV_EVENT, "Wrong size msg pos :" + splitted.length);
                                    }


                                }
                            }
                        }
                    }

                    if (s != null) {
                        s.close();
                    }
                }
            });
            rcvEventThread.start();
        }
    }





    Thread rcvMsgThread;
    boolean rcvThreadActive = true;

    public void stopRcvMsgThread(){
        rcvThreadActive = false;
    }

    //public void startRcvMsgThread(final networkMultiListenerNewBPM lb, final networkMultiListenerNewPosition lp){
    public void startRcvThread(final networkMultiListener l){
        //final networkMultiListenerNewBPM list_BPM = lb;
        //final networkMultiListenerNewPosition list_Pos = lp;
        final networkMultiListener listener =l;

        final String TAG_RCV_MSG = "RCV-T";

        if(rcvMsgThread==null) {

            rcvMsgThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    rcvThreadActive = true;

                    byte[] message = new byte[500];
                    DatagramPacket p = new DatagramPacket(message, message.length);
                    DatagramSocket s = null;


                    try {
                        s = new DatagramSocket(server_port);
                        s.setSoTimeout(500);
                    } catch (SocketException e) {
                        e.printStackTrace();
                        rcvThreadActive=false;
                        Log.e(TAG_RCV_MSG,"-fails init socket or fail set timeout");
                        return;
                    }


                    boolean validMsg ;
                    while (rcvThreadActive) {

                        try {

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
                                        listener.setPosition(ppos);

                                    } else {
                                        Log.e(TAG_RCV_MSG, "Wrong size msg pos :" + splitted.length);
                                    }


                                } else if (splitted[0].equals(MSG_BPM)) {

                                    if (splitted.length == 2) {
                                        int b = Integer.parseInt(splitted[1]);
                                        listener.setBPM(b);

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
            });
            rcvMsgThread.start();
        }
    }

    /*
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
    }*/

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


    public interface networkMultiListener{
        void setPosition(float[] p);
        void setBPM(int bpm);
        //void setEvent(String msg);
    }

    public interface networkEventListener{
        void setEvent(String msg);

    }


    class SendUdpMessage extends AsyncTask<String, Void, Boolean> {

        private Exception exception;

        protected Boolean doInBackground(String... params) {
            try {
                String msg = params[0];

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

                return true;
            } catch (Exception e) {
                this.exception = e;

                return null;
            } finally {

            }
        }

        protected void onPostExecute(Void done) {

        }
    }

}
