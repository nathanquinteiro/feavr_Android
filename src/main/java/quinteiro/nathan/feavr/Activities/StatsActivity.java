package quinteiro.nathan.feavr.Activities;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

import quinteiro.nathan.feavr.R;
import quinteiro.nathan.feavr.utils.NetworkMulti;
import quinteiro.nathan.feavr.utils.Preferences;

public class StatsActivity extends AppCompatActivity {

    NetworkMulti.networkMultiListener listener;

    LinearLayout ll;

    private final static int NB_HR_ON_GRAPH = 20;

    private GraphView hrGraph;
    private LineGraphSeries<DataPoint> hrSerie;
    private ArrayList<Double> hrData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game_tab);
        ll = (LinearLayout) findViewById(R.id.gameLayout);

        hrGraph = (GraphView) findViewById(R.id.graph);

        // set manual X bounds
        hrGraph.getViewport().setYAxisBoundsManual(true);
        hrGraph.getViewport().setMinY(0);
        hrGraph.getViewport().setMaxY(200);

        hrGraph.getViewport().setXAxisBoundsManual(true);
        hrGraph.getViewport().setMinX(0);
        hrGraph.getViewport().setMaxX(20);

        for (int i = 0; i < NB_HR_ON_GRAPH; i++) {
            hrData.add(0.);
        }
        hrSerie = new LineGraphSeries<>(listToDataPoint(hrData));
        hrGraph.addSeries(hrSerie);

        //setContentView();
    }

    public DataPoint[] listToDataPoint(ArrayList<Double> list) {
        DataPoint dataPoint[] = new DataPoint[list.size()];
        for(int i = 0; i < dataPoint.length; i++) {
            dataPoint[i] = new DataPoint(i, list.get(i));
        }
        return dataPoint;
    }

    public void appendNewHR(double newHR) {
        hrData.remove(0);
        hrData.add(newHR);
        hrSerie.resetData(listToDataPoint(hrData));
    }
}
