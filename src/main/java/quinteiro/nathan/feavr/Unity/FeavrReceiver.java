package quinteiro.nathan.feavr.Unity;

import android.util.Log;

import java.util.Vector;

/**
 * Created by nathan on 20.09.17.
 */

public class FeavrReceiver {
    public static int bpm = 0;
    public static float[] position = {0,0};

    public static int getBPM() {
        Log.e("UNITY","getVal called");
        return bpm;
    }

    public static void setBPM(int newBPM) {
        bpm = newBPM;
        Log.e("Android", "setVal called with value: " + newBPM);
        // Send BPM through network
    }

    public static float[] getPosition() {
        Log.e("Android", "getPosition called");
        return position;
    }

    public static void setPosition(float x, float z) {
        Log.e("UNITY","setVal called with values: " + x + " " + z);
        position[0] = x;
        position[1] = z;
        // Send position through network
    }


}
