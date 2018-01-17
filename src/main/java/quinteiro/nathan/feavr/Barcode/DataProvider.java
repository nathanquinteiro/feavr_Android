package quinteiro.nathan.feavr.Barcode;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


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

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;


    private DataProvider(){
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();

    }

    public static DataProvider getInstance(){
        if(_dp == null){
            _dp = new DataProvider();
        }
        return _dp;
    }

    public void pushMessageTest(){
        mDatabaseReference = mFirebaseDatabase.getReference("message");
        mDatabaseReference.setValue("Test-msg","Hello,World!");
    }








}


/*


// Write a message to the database
FirebaseDatabase database = FirebaseDatabase.getInstance();
DatabaseReference myRef = database.getReference("message");

myRef.setValue("Hello, World!");
 */