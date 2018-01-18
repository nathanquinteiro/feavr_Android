package quinteiro.nathan.feavr.Database;

import android.util.Log;
import android.util.SparseIntArray;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 *
 *  DataProvider for the database
 *
 *  use singleton pattern for convenience
 *
 *
 */

public class DataProvider {

    private static DataProvider _dp = null ;

    private FirebaseDatabase database;
    private DatabaseReference ref;
    //private DatabaseReference currentGameReference;


    private Boolean inGame = false;
    private String currentGameKey = null;
    private Calendar date ;


    private DataProvider(){
        database = FirebaseDatabase.getInstance();
        ref = database.getReference();

    }

    public static DataProvider getInstance(){
        if(_dp == null){
            _dp = new DataProvider();
        }
        return _dp;
    }


    public String getCurrentGameKey(){
        return currentGameKey;
    }

    public String startNewGame(){

        inGame = true;

        currentGameKey = ref.child("game").push().getKey();

        return currentGameKey;

    }




    public void pushPosGame(float x, float z){
        if(inGame){

            date = Calendar.getInstance();

            HashMap<String, Object> ev = new HashMap<>();
            ev.put("x",String.valueOf(x));
            ev.put("z",String.valueOf(z));
            ev.put("ts",date.getTimeInMillis());

            HashMap<String, Object> entry = new HashMap<>();


            String posKey = ref.child("game").child(currentGameKey).child("POS").push().getKey();

            entry.put(posKey,ev);

            ref.child("game").child(currentGameKey).child("POS").updateChildren(entry);


        } else {

        }

    }

    public void pushBPMGame(int bpm){
        if(inGame){

            date = Calendar.getInstance();

            HashMap<String, Object> ev = new HashMap<>();
            ev.put("value",bpm);

            ev.put("ts",date.getTimeInMillis());

            HashMap<String, Object> entry = new HashMap<>();



            String bpmKey =  ref.child("game").child(currentGameKey).child("BPM").push().getKey();

            entry.put(bpmKey,ev);


            ref.child("game").child(currentGameKey).child("BPM").updateChildren(entry);

        } else {

        }

    }

    public void pushEventGame(String event){
        if(inGame){

            date = Calendar.getInstance();
            HashMap<String, Object> ev = new HashMap<>();
            ev.put("value",event);
            ev.put("ts",date.getTimeInMillis());

            HashMap<String, Object> entry = new HashMap<>();

            String evKey = ref.child("game").child(currentGameKey).child("EVENT").push().getKey();

            entry.put(evKey,ev);

            ref.child("game").child(currentGameKey).child("EVENT").updateChildren(entry);

        } else {

        }
    }


    public void endCurrentGame(){
        inGame= false;
        //currentGameKey=null;
    }


    public void getBPMOfGame(String gameReference, final dataProviderListenerBPM listener){



        DatabaseReference dbRef = database.getReference("game/"+gameReference+"/BPM");
        Log.e("DataProvider","ask bpm for gameReference:"+gameReference);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //listener.resultBPM();
                Map<String, Object> bpmMap = (Map<String, Object>) dataSnapshot.getValue();

                if(bpmMap!= null) {

                    HashMap<Long, Long> hmap = new HashMap<>();

                    HashMap<String, Long> current;

                    Log.e("DP", "size" + bpmMap.size());

                    //bpmMap.entrySet()
                    for (Map.Entry<String, Object> entry : bpmMap.entrySet()) {

                        current = (HashMap<String, Long>) entry.getValue();

                        hmap.put(current.get("ts"), current.get("value"));
                    }

                    Map<Long, Long> map = new TreeMap<Long, Long>(hmap);

                    listener.resultBPM(map);
                } else {
                    listener.resultBPM(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.resultCancelled();
            }
        });
    }

    public interface dataProviderListenerBPM{
        void resultBPM(Map<Long,Long> a);
        void resultCancelled();
    }
}