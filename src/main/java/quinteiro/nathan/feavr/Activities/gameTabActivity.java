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
import android.widget.RelativeLayout;
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
import quinteiro.nathan.feavr.utils.Preferences;

public class gameTabActivity extends AppCompatActivity {

    DemoView dm ;

    NetworkMulti.networkMultiListener listener;

    RelativeLayout ll;

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

        ll = (RelativeLayout) findViewById(R.id.gameLayout);


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
        hrGraph.getViewport().setMaxY(160);

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



        private  float initOffsetX = 3.5f;
        private float initOffsetY = -51.5f;


        public DemoView(final Context context, Boolean testMode){
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


                    // unity and android have not the same reference for the point 0,0
                    // in android part the 0,0 is the left upper part
                    lastPosition[0] = p[0]+initOffsetX;
                    lastPosition[1] = -(p[1]+initOffsetY);
                    validPosition = true;

                    // tell that the view need to be redraw !
                    postInvalidate();

                }

                @Override
                public void setBPM(int bpm) {
                    //Log.e("-","aps");
                    lastBPM=bpm;
                    appendNewHR((double) bpm);

                    postInvalidate();
                }

                @Override
                public void setEndGame(String gameReference) {


                    if(gameReference!=null){
                        Preferences.saveLastGameReference(gameReference,context);
                        Log.d("gameTab","end game msg rcv, refgame:"+gameReference);
                    } else {
                        Log.d("gameTab","end game msg rcv, no gameref");
                    }

                    finish();
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
        private boolean validPosition = false;


        private float minX = 10;
        private float minY = 10;
        private float maxX = 10;
        private float maxY = 10;

        private float scale;

        private float scaleX = 10;
        private float scaleY = 10;


        private float radiusGamer = 1;
        private int radiusGamerScaled ;

        private float radiusLamps = 0.5f;
        private int radiusLampsScaled;



        private int xOffset = 0;
        private int yOffset = 0;

        private int widthMap = 63;
        private int heightMap = 55;
        private float corridorSize = 7f;//7.5

        float [][] extLimits ={{xOffset,yOffset,xOffset+widthMap,yOffset},
                {xOffset+widthMap,yOffset,xOffset+widthMap,yOffset+heightMap},
                {xOffset+widthMap,yOffset+heightMap,xOffset,yOffset+heightMap},
                {xOffset,yOffset+heightMap,xOffset,yOffset}};


        float [][] intLimits ={{xOffset+corridorSize,yOffset+corridorSize,xOffset+widthMap-corridorSize,yOffset+corridorSize},
                {xOffset+widthMap-corridorSize,yOffset+corridorSize,xOffset+widthMap-corridorSize,yOffset+heightMap-corridorSize},
                {xOffset+widthMap-corridorSize,yOffset+heightMap-corridorSize,xOffset+corridorSize,yOffset+heightMap-corridorSize},
                {xOffset+corridorSize,yOffset+heightMap-corridorSize,xOffset+corridorSize,yOffset+corridorSize}};


        int posXBetweenLamps = 14;
        int posYBetweenLamps = 12;


        float [][] lampsPosition = {

                {corridorSize/2,heightMap-corridorSize/2-posYBetweenLamps},
                {corridorSize/2,heightMap-corridorSize/2-posYBetweenLamps*2},
                {corridorSize/2,heightMap-corridorSize/2-posYBetweenLamps*3},
                {corridorSize/2,heightMap-corridorSize/2-posYBetweenLamps*4},

                {corridorSize/2+posXBetweenLamps,corridorSize/2},
                {corridorSize/2+posXBetweenLamps*2,corridorSize/2},
                {corridorSize/2+posXBetweenLamps*3,corridorSize/2},
                {corridorSize/2+posXBetweenLamps*4,corridorSize/2},

                {widthMap-corridorSize/2,corridorSize/2+posYBetweenLamps},
                {widthMap-corridorSize/2,corridorSize/2+posYBetweenLamps*2},
                {widthMap-corridorSize/2,corridorSize/2+posYBetweenLamps*3},
                {widthMap-corridorSize/2,corridorSize/2+posYBetweenLamps*4},

                {widthMap-corridorSize/2-posYBetweenLamps,heightMap-corridorSize/2},
                {widthMap-corridorSize/2-posYBetweenLamps*2,heightMap-corridorSize/2},
                {widthMap-corridorSize/2-posYBetweenLamps*3,heightMap-corridorSize/2}};


        float [][] extLimitsScaled = null;
        float [][] intLimitsScaled = null;
        float [][] lampsPosScaled = null;


        int offsetTextLamps = 2;
        float offsetTextLampsScaled;


        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){

            // map almost square so force the used space to be a square
            int size = heightMeasureSpec;
            if(widthMeasureSpec<heightMeasureSpec) {
                size = widthMeasureSpec;
            }
            setMeasuredDimension(size,size);
            //setMeasuredDimension(widthMeasureSpec,heightMeasureSpec);

        }





        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);


            if(extLimitsScaled == null) {

                scale = this.getWidth() / widthMap;

                // the map is almost a square so use the same offset and scale for x and y
                xOffset = (this.getWidth() % widthMap)/2;
                yOffset = xOffset;

                Log.d("Scale :", " " + scale);
                scaleX = scale;
                scaleY = scale;

                offsetTextLampsScaled = offsetTextLamps*scale;

                radiusGamerScaled = (int)  (radiusGamer*scale);
                radiusLampsScaled = (int) (radiusLamps*scale);

            }




            // compute real position of map only once (the first time)
            if(extLimitsScaled == null){

                extLimitsScaled = new float[4][4];

                for(int i = 0 ; i< extLimits.length;i++){
                    for(int j = 0 ; j < extLimits[i].length;j++){
                        extLimitsScaled[i][j]= extLimits[i][j]*scale+xOffset;
                    }
                }

            }

            // compute real position of lamps only once (the first time)
            if(lampsPosScaled == null){
                lampsPosScaled = new float[nbLamps][];

                for(int i = 0; i< nbLamps; i++){

                    lampsPosScaled[i] = new float[2];

                    for(int j = 0; j< lampsPosition[i].length;j++){


                        lampsPosScaled[i][j] = lampsPosition[i][j]*scale+xOffset;
                    }
                }
            }

            // compute real position of map only once (the first time)
            if(intLimitsScaled == null){

                intLimitsScaled = new float[4][4];

                for(int i = 0 ; i< intLimits.length;i++){
                    for(int j = 0 ; j < intLimits[i].length;j++){
                        intLimitsScaled[i][j]= intLimits[i][j]*scale+xOffset;
                    }
                }

            }




            // custom drawing code here
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);


            // make the entire canvas white
            paint.setColor(Color.WHITE);
            canvas.drawPaint(paint);



            if(validPosition) {

                // draw external game map
                paint.setColor(Color.BLACK);
                for (float[] p :extLimitsScaled){
                    canvas.drawLines(p,paint);
                }



                // draw internal game map
                for (float[] p :intLimitsScaled){
                    canvas.drawLines(p,paint);
                }


                // draw lamps
                for(int i = 0; i< nbLamps;i++){

                    if(lampsState[i]){

                        paint.setColor(Color.YELLOW);
                        canvas.drawCircle(lampsPosScaled[i][0],lampsPosScaled[i][1],radiusLampsScaled,  paint);
                        //canvas.drawCircle(p[0],p[1],5,paint);

                    } else {
                        paint.setColor(Color.BLACK);
                        canvas.drawCircle(lampsPosScaled[i][0],lampsPosScaled[i][1],radiusLampsScaled,  paint);
                    }

                    paint.setColor(Color.BLACK);
                    paint.setTextSize(2*scale);
                    canvas.drawText("L"+(i+1),lampsPosScaled[i][0]-offsetTextLampsScaled,lampsPosScaled[i][1]+offsetTextLampsScaled*1.5f,paint);

                }


                // draw gamer
                paint.setAntiAlias(true);
                paint.setColor(Color.RED);
                canvas.drawCircle(xOffset + lastPosition[0] * scaleX, yOffset + lastPosition[1] * scaleY, radiusGamerScaled, paint);


                paint.setAntiAlias(false);
            } else {

                // if unity part not started on smartphone draw loading message
                paint.setAntiAlias(true);
                paint.setTextSize(4*scale);
                paint.setColor(Color.RED);
                paint.setStyle(Paint.Style.FILL);

                canvas.drawText("Game is loading ...",this.getWidth()/4,this.getHeight()/2,paint);
                paint.setAntiAlias(false);

            }

        }
    }
}
