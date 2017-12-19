package quinteiro.nathan.feavr.Activities;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import quinteiro.nathan.feavr.utils.NetworkMulti;

import quinteiro.nathan.feavr.R;

public class gameTabActivity extends AppCompatActivity {

    DemoView dm ;

    NetworkMulti.networkMultiListener listener;

    LinearLayout ll;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_game_tab);


        Boolean testMode = getIntent().getExtras().getBoolean(MainActivity.EXTRA_TEST_MODE);

        Log.e("EXTRA","value : "+testMode);


        setContentView(R.layout.activity_game_tab);
        ll = (LinearLayout) findViewById(R.id.gameLayout);




        dm = new DemoView(this,testMode);

        ll.addView(dm);



        //setContentView();
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
                    lastPosition[1] = p[1]+decInit;

                    postInvalidate();

                }

                @Override
                public void setBPM(int bpm) {
                    //Log.e("-","aps");
                    lastBPM=bpm;


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
        private int offsetPositionY = 50;

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

            setMeasuredDimension(widthMeasureSpec,heightMeasureSpec/2);

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