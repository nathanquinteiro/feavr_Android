package quinteiro.nathan.feavr.Unity;

import android.util.Log;

import java.util.Vector;

import quinteiro.nathan.feavr.utils.NetworkMulti;

/**
 * Created by nathan on 20.09.17.
 */

public class FeavrReceiver {
    public static int bpm = 0;
    public static float[] position = {0,0};
    public static String event = null;

    public static String getEvent() {
        return event;
    }

    public static void setEvent(String event) {
        FeavrReceiver.event = event;
    }

    //Call by unity to get the BPM
    public static int getBPM() {
        Log.e("UNITY","getVal called");
        return bpm;
    }

    // Call by UnityPlayerActivity to set the new BPM value
    public static void setBPM(int newBPM) {
        bpm = newBPM;
        Log.e("Android", "setVal called with value: " + newBPM);
        // Send BPM through network
        if(NetworkMulti.getInstance().isCoTested()){
            NetworkMulti.getInstance().sendBpm(newBPM);
        }
    }


    public static float[] getPosition() {
        Log.e("Android", "getPosition called");
        return position;
    }


    // Call by unity to give the position ...
    public static void setPosition(float x, float z) {
        Log.e("UNITY","setVal called with values: " + x + " " + z);
        position[0] = x;
        position[1] = z;
        // Send position through network

        if(NetworkMulti.getInstance().isCoTested()){

            NetworkMulti.getInstance().sendPositions(position);

        }

    }


}
