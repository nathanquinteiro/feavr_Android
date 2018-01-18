package quinteiro.nathan.feavr.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

import quinteiro.nathan.feavr.Barcode.BarcodeGeneratorActivity;
import quinteiro.nathan.feavr.utils.NetworkMulti;

import quinteiro.nathan.feavr.R;

public class gameTabActivity extends AppCompatActivity {

    DemoView dm ;

    NetworkMulti.networkMultiListener listener;

    LinearLayout ll;

    Random random ;


    private final static int NB_HR_ON_GRAPH = 20;

    private GraphView hrGraph;
    private LineGraphSeries<DataPoint> hrSerie;
    private ArrayList<Double> hrData = new ArrayList<>();

    boolean lightOn = true;

    private final int nbLamps = 15;

    private Switch  swLamps[];
    private boolean lampsState [] ;//= new boolean[nbLamps];


    Button btLight;

    public gameTabActivity() {
    }

    @Override
    protected void onDestroy() {
        Log.e("----","onDestroyCalled");
        super.onDestroy();
        NetworkMulti.getInstance().reset();

        //unregisterReceiver(mGattUpdateReceiver);
    }


    //aaaaa

    //private View

    private CompoundButton.OnCheckedChangeListener lampsControlListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            Log.e("---","id : "+buttonView.getId());
            int lampsID = 0;
            switch (buttonView.getId()){
                case  R.id.SwLamp1:
                    lampsID = 0;
                    break;
                case  R.id.SwLamp2:
                    lampsID = 1;
                    break;
                case  R.id.SwLamp3:
                    lampsID = 2;
                    break;
                case  R.id.SwLamp4:
                    lampsID = 3;
                    break;
                case  R.id.SwLamp5:
                    lampsID = 4;
                    break;
                case  R.id.SwLamp6:
                    lampsID = 5;
                    break;
                case  R.id.SwLamp7:
                    lampsID = 6;
                    break;
                case  R.id.SwLamp8:
                    lampsID = 7;
                    break;
                case  R.id.SwLamp9:
                    lampsID = 8;
                    break;
                case  R.id.SwLamp10:
                    lampsID = 9;
                    break;
                case  R.id.SwLamp11:
                    lampsID = 10;
                    break;
                case  R.id.SwLamp12:
                    lampsID = 11;
                    break;
                case  R.id.SwLamp13:
                    lampsID = 12;
                    break;
                case  R.id.SwLamp14:
                    lampsID = 13;
                    break;
                case  R.id.SwLamp15:
                    lampsID = 14;
                    break;

            }

            lampsState[lampsID]=isChecked;

            if(NetworkMulti.getInstance().isCoTested()){

                JSONObject a = new JSONObject();
                JSONArray b = null;

                try {
                    b = new JSONArray(lampsState);
                } catch (JSONException e) {
                    //e.printStackTrace();
                }
                try {
                    a.put("lamps",b);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                NetworkMulti.getInstance().sendEvent(a.toString());

            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_game_tab);





        setContentView(R.layout.activity_game_tab);


        lampsState = new boolean[nbLamps];
        for(int i = 0; i< lampsState.length;i++){
            lampsState[i]=true;
        }

        ll = (LinearLayout) findViewById(R.id.gameLayout);


        swLamps  = new Switch[nbLamps];


        for(int i = 0; i<swLamps.length;i++){


            swLamps[i] = (Switch)  findViewById(getResources().getIdentifier("SwLamp"+(i+1),"id",this.getPackageName()));
            swLamps[i].setOnCheckedChangeListener(lampsControlListener);

        }




        random = new Random();
        btLight = (Button) findViewById(R.id.btLight);
        btLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(NetworkMulti.getInstance().isCoTested()){

                    JSONObject a = new JSONObject();

                    //boolean[] li = new boolean[nbLamps];


                    JSONArray b = null;

                    for(int i = 0 ; i<lampsState.length;i++){
                        lampsState[i] = random.nextBoolean();
                        swLamps[i].setChecked(lampsState[i]);
                    }

                    try {
                        b = new JSONArray(lampsState);
                    } catch (JSONException e) {
                        //e.printStackTrace();
                    }

                    try {
                        a.put("lamps",b);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    NetworkMulti.getInstance().sendEvent(a.toString());

                }

            }
        });





        dm = new DemoView(this, false);

        ll.addView(dm);


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



    private class DemoView extends View {

        private int i = 10;

        private  float decInit = 3.5f;


        public DemoView(Context context,Boolean testMode){
            super(context);

            listener = new NetworkMulti.networkMultiListener() {
                @Override
                public void setPosition(float[] p) {

                    if(p[0]<minX)
                        minX=p[0];

                    if(p[0]>maxX)
                        maxX=p[0];

                    if(p[1]<minY)
                        minY=p[1];

                    if(p[1]>maxY)
                        maxY=p[1];

                    lastPosition[0] = p[0]+decInit;
                    lastPosition[1] = 50-p[1]+decInit; //t

                    postInvalidate();

                }

                @Override
                public void setBPM(int bpm) {
                    //Log.e("-","aps");
                    lastBPM=bpm;
                    appendNewHR((double) bpm);

                    postInvalidate();
                }
            };

            if(testMode){
                NetworkMulti.getInstance().startTestThread(listener);

            } else {
                NetworkMulti.getInstance().startRcvThread(listener);
            }
        }

        private int lastBPM = 1;
        private float[] lastPosition = new float[]{0,0};
        private int offsetPositionX = 50;
        private int offsetPositionY = 50; // avant 50

        private float minX = 10;
        private float minY = 10;
        private float maxX = 10;
        private float maxY = 10;

        private float scaleX = 10;
        private float scaleY = 10;

        private int sizeExt = 630;

        private int corridorLen = 70;

        float [][] p1 = {{offsetPositionX,offsetPositionY,offsetPositionX,offsetPositionY+sizeExt},
                {offsetPositionX,offsetPositionY,offsetPositionX+sizeExt,offsetPositionY},
                {offsetPositionX+sizeExt,offsetPositionY,offsetPositionX+sizeExt,offsetPositionY+sizeExt},
                {offsetPositionX,offsetPositionY+sizeExt,offsetPositionX+sizeExt,offsetPositionY+sizeExt}};

        float [][] p2 = {{offsetPositionX+corridorLen,offsetPositionY+corridorLen,offsetPositionX+corridorLen,offsetPositionY+sizeExt-corridorLen},//1
                {offsetPositionX+corridorLen,offsetPositionY+corridorLen,offsetPositionX+sizeExt-corridorLen,offsetPositionY+corridorLen},//2
                {offsetPositionX+sizeExt-corridorLen,offsetPositionY+corridorLen,offsetPositionX+sizeExt-corridorLen,offsetPositionY+sizeExt-corridorLen},//3
                {offsetPositionX+corridorLen,offsetPositionY+sizeExt-corridorLen,offsetPositionX+sizeExt-corridorLen,offsetPositionY+sizeExt-corridorLen}};

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){

            setMeasuredDimension(widthMeasureSpec,heightMeasureSpec);

        }





        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);


            //this.getWidth();
            //this.getHeight();

            i+=5;
            // custom drawing code here
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);





            // make the entire canvas white
            paint.setColor(Color.WHITE);
            canvas.drawPaint(paint);


            //stouf

            paint.setColor(Color.BLACK);
            paint.setTextSize(48);
            canvas.drawText("BPM :"+lastBPM,100,750,paint);



            //{offsetPositionX,offsetPositionY,offsetPositionX,offsetPositionY}


            for (float[] p :p1){
                canvas.drawLines(p,paint);
            }

            paint.setColor(Color.GREEN);

            for (float[] p :p2){
                canvas.drawLines(p,paint);
            }



            paint.setAntiAlias(true);
            paint.setColor(Color.RED);

            canvas.drawCircle(offsetPositionX+lastPosition[0]*scaleX,offsetPositionY+lastPosition[1]*scaleY,8,paint);



            paint.setAntiAlias(false);


            paint.setColor(Color.BLUE);
            paint.setTextSize(18);

            canvas.drawText("(X:Y) = ("+lastPosition[0]+" : "+lastPosition[1]+")",100,800,paint);

            paint.setColor(Color.BLACK);
            /*canvas.drawText("minX :"+minX,100,900,paint);
            canvas.drawText("maxX :"+maxX,100,1000,paint);
            canvas.drawText("minY :"+minY,100,1100,paint);
            canvas.drawText("maxY :"+maxY,100,1200,paint);*/



            /*
            // draw blue circle with anti aliasing turned off
            paint.setAntiAlias(false);
            paint.setColor(Color.BLUE);
            canvas.drawCircle(20, 20, 15, paint);

            // draw green circle with anti aliasing turned on
            paint.setAntiAlias(true);
            paint.setColor(Color.GREEN);
            canvas.drawCircle(60, 20, 15, paint);

            // draw red rectangle with anti aliasing turned off
            paint.setAntiAlias(false);
            paint.setColor(Color.RED);
            canvas.drawRect(i, 5, 200, 30, paint);

            // draw the rotated text
            canvas.rotate(-45);

            paint.setStyle(Paint.Style.FILL);
            canvas.drawText("Graphics Rotation", 40, 180, paint);
*/
            //undo the rotate
            //canvas.restore();
        }
    }
}
