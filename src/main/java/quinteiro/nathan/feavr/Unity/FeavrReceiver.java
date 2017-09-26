package quinteiro.nathan.feavr.Unity;

import android.util.Log;

/**
 * Created by nathan on 20.09.17.
 */

public class FeavrReceiver {
    public static int bpm = 0;

    public static int getVal() {
        Log.e("UNITY","getVal called");
        return bpm;
    }

    public static void setVal(int newBPM) {
        bpm = newBPM;
        Log.e("Android", "setVal called with value: " + newBPM);
    }
}
