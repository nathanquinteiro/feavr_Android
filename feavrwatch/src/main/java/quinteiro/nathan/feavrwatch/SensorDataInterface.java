package quinteiro.nathan.feavrwatch;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Created by degiovan on 07/11/2017.
 */

//Annotation for Room library to recognize the Dao
@Dao
public interface SensorDataInterface {
    //Implementation of the queries to use to read and write the database

    @Query("SELECT * FROM SensorData WHERE type = :sensortype ORDER BY timestamp DESC LIMIT :N")
    List<SensorData> getLastNValues(int sensortype, int N);

    @Insert
    long insertSensorData(SensorData sensorData);

    @Query("DELETE FROM SensorData")
    void deleteAll();
}
