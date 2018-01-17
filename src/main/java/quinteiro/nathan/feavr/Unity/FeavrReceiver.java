package quinteiro.nathan.feavr.Unity;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Vector;

import quinteiro.nathan.feavr.Database.DataProvider;
import quinteiro.nathan.feavr.utils.NetworkMulti;

/**
 * Created by nathan on 20.09.17.
 */

public class FeavrReceiver {
    public static int bpm = 0;
    public static float[] position = {0,0};
    public static String event = null;

    private static boolean saveGameInit = false;

    private static int posCounter = 0;

    public static String getEvent() {
        return event;
    }

    public static void setEvent(String event) {

        Log.e("FVRRECEIVER","New event : "+event);
        FeavrReceiver.event = event;

        if(saveGameInit) {
            DataProvider.getInstance().pushEventGame(event);
        }
    }

    //Call by unity to get the BPM
    public static int getBPM() {
        Log.e("UNITY","getVal called");
        return bpm;
    }

    // Call by UnityPlayerActivity to set the new BPM value
    public static void setBPM(int newBPM) {
        bpm = newBPM;
        Log.e("Android", "setBPM called with value: " + newBPM);

        if(saveGameInit) {
            DataProvider.getInstance().pushBPMGame(newBPM);
        }

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
        //Reduce the network transmission by discarding some position update
        posCounter++;
        if(posCounter % 5 == 0) {
            Log.e("UNITY","setPosition called with values: " + x + " " + z);
            position[0] = x;
            position[1] = z;
            // Send position through network

            if(posCounter % 50 == 0) {
                if (saveGameInit) {
                    DataProvider.getInstance().pushPosGame(x, z);
                }
                posCounter = 0;
            }

            if(NetworkMulti.getInstance().isCoTested()){

                NetworkMulti.getInstance().sendPositions(position);

            }
        }
    }

    public static void initSaveGame() {
        DataProvider.getInstance().startNewGame();
        saveGameInit = true;
    }
}