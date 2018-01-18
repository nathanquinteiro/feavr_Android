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
import android.widget.TextView;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import quinteiro.nathan.feavr.Database.DataProvider;
import quinteiro.nathan.feavr.R;
import quinteiro.nathan.feavr.utils.NetworkMulti;
import quinteiro.nathan.feavr.utils.Preferences;

public class StatsActivity extends AppCompatActivity {

    private GraphView hrGraph;
    private LineGraphSeries<DataPoint> hrSerie;
    private ArrayList<Double> hrData = new ArrayList<>();

    TextView tvText;
    Button btClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_stats);

        hrGraph = (GraphView) findViewById(R.id.graph);

        tvText = (TextView) findViewById(R.id.tvStatText);
        btClose = (Button) findViewById(R.id.btClose);
        btClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //Get reference of last game run on the phone
        String lastGameRef = Preferences.getLastGameReference(getApplicationContext());
        //Get the data from the Firebase DataBase and display the graph of the heartbeat
        if(lastGameRef != null) {
            DataProvider.getInstance().getBPMOfGame(lastGameRef, new DataProvider.dataProviderListenerBPM() {
                @Override
                public void resultBPM(Map<Long, Long> data) {
                    if(data!=null) {

                        DataPoint dataPoint[] = new DataPoint[data.size()];
                        int i = 0;
                        long first = (long) data.keySet().toArray()[0];
                        for (Long timestamp : data.keySet()) {
                            dataPoint[i] = new DataPoint((timestamp - first) / 1000.0, data.get(timestamp));
                            i++;
                        }

                        hrSerie = new LineGraphSeries<>(dataPoint);
                        hrGraph.addSeries(hrSerie);
                        hrGraph.setVisibility(View.VISIBLE);
                    } else {
                        tvText.setText(R.string.text_noBPMRecorded);
                    }
                }
                @Override
                public void resultCancelled() {
                    tvText.setText(R.string.text_unableReacheDB);
                }
            });
        }
        else {
            tvText.setText(R.string.text_no_previous_game);
        }
    }
}
