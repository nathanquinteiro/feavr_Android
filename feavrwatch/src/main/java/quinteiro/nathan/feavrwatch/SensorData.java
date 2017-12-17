package quinteiro.nathan.feavrwatch;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by degiovan on 07/11/2017.
 */

//Annotation for Room library to recognize the entity
@Entity
public class SensorData {
    //Different types of sensors
    public final static int HEART_RATE = 0;
    public final static int LATITUDE = 1;
    public final static int LONGITUDE = 2;

    @PrimaryKey(autoGenerate = true)
    public int uid;
    @ColumnInfo
    public long timestamp;
    @ColumnInfo
    public int type;
    @ColumnInfo
    public double value;

}
