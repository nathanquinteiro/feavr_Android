package quinteiro.nathan.feavrwatch;

import android.os.AsyncTask;

import java.util.List;

/**
 * Created by degiovan on 08/11/2017.
 */

public class SensorDataAsyncTask extends AsyncTask<Float,Void,SensorData>{

    @Override
    protected SensorData doInBackground(Float... values) {
        SensorData sensorData = new SensorData();
        sensorData.timestamp = System.nanoTime();
        sensorData.type = SensorData.HEART_RATE;
        sensorData.value = values[0];
        return sensorData;
    }

    @Override
    protected void onPostExecute(SensorData sensorData) {
        MainActivity.heartRate = (int) sensorData.value;
    }
}
