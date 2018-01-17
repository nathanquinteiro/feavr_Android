package quinteiro.nathan.feavr.Barcode;

import android.nfc.cardemulation.HostApduService;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


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
    private String currentGameKey;
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

    public void pushMessageTest(){
        //mDatabaseReference = database.getReference();
        //mDatabaseReference.setValue("Test-msg","Hello,World!");
    }


    public void startNewGame(){

        inGame = true;


        currentGameKey = ref.child("game").push().getKey();


        /*inGame = true;
        mDatabaseReference = mFirebaseDatabase.getReference();
        currentGameKey = mDatabaseReference.child("std_game").push().getKey();
        currentGameReference = mDatabaseReference.child("std_game").child(currentGameKey).getRef();*/

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














}


/*


// Write a message to the database
FirebaseDatabase database = FirebaseDatabase.getInstance();
DatabaseReference myRef = database.getReference("message");

myRef.setValue("Hello, World!");
 */