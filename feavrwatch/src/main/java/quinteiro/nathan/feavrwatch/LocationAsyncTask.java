package quinteiro.nathan.feavrwatch;

import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import java.util.List;

/**
 * Created by degiovan on 08/11/2017.
 */

public class LocationAsyncTask extends AsyncTask<Location,Void,double[]> {

    private MyDatabase db;

    LocationAsyncTask(MyDatabase db){
        this.db = db;
    }


    @Override
    protected double[] doInBackground(Location... locations) {
        SensorData sensorData = new SensorData();
        sensorData.timestamp = System.nanoTime();
        sensorData.type = SensorData.LATITUDE;
        sensorData.value = locations[0].getLatitude();
        //Adding the latitude into the database
        db.sensorDataDao().insertSensorData(sensorData);
        sensorData.type = SensorData.LONGITUDE;
        sensorData.value = locations[0].getLongitude();
        //Adding the longitude into the database
        db.sensorDataDao().insertSensorData(sensorData);
        // Retrieving the last measurement
        List<SensorData> lat_data = db.sensorDataDao().getLastNValues(SensorData.LATITUDE, 1);
        List<SensorData> lon_data =
                db.sensorDataDao().getLastNValues(SensorData.LONGITUDE, 1);
        return new double[]{lon_data.get(0).value, lat_data.get(0).value};
    }

    @Override
    protected void onPostExecute(double[] lon_lat_data) {
        //If you want to use the textViewGPS to check the app without the map remember to pass the view in the constructor as the other parameters
//        textViewGPS = view.findViewById(R.id.textViewGPS);
//        if (textViewGPS != null)
//            textViewGPS.setText("Lat: " + lon_lat_data[0] + "\nLon: " + lon_lat_data[1]);

        //Filling the list with longitude and latitude that we will use for the XYseries
        List<Number> lon_lat = MainActivity.lon_lat;
        XYPlot plot = MainActivity.plot;
        lon_lat.add(lon_lat_data[0]);
        lon_lat.add(lon_lat_data[1]);
        XYSeries series = new SimpleXYSeries(lon_lat,
                SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, "");
        LineAndPointFormatter formatter = new LineAndPointFormatter(Color.RED,
                Color.TRANSPARENT, Color.TRANSPARENT, null);
        plot.clear();
        plot.addSeries(series, formatter);
        plot.redraw();
    }
}
