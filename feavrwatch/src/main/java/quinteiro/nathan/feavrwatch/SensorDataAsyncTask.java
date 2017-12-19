package quinteiro.nathan.feavrwatch;

import android.os.AsyncTask;

import java.util.List;

import quinteiro.nathan.feavrwatch.SensorData;

/**
 * Created by degiovan on 08/11/2017.
 */

public class SensorDataAsyncTask extends AsyncTask<Float,Void,SensorData>{

    private MyDatabase db;

    SensorDataAsyncTask(MyDatabase db){
        this.db = db;
    }

    @Override
    protected SensorData doInBackground(Float... values) {
        SensorData sensorData = new SensorData();
        sensorData.timestamp = System.nanoTime();
        sensorData.type = SensorData.HEART_RATE;
        sensorData.value = values[0];
        db.sensorDataDao().insertSensorData(sensorData);
        List<SensorData> heartRateData = db.sensorDataDao().getLastNValues(SensorData.HEART_RATE, 1);
        return heartRateData.get(0);
    }

    @Override
    protected void onPostExecute(SensorData sensorData) {
        MainActivity.heartRate = (int) sensorData.value;
    }
}
