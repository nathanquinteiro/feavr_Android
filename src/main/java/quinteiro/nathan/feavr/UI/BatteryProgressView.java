package quinteiro.nathan.feavr.UI;

/**
 * BatteryProgressView
 *
 * A view that displays the battery level of a sensor in percent with a circle arc showing the
 * proportion of battery. An image is drawn and either indicate that the battery is charged enough
 * or low.
 */

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import quinteiro.nathan.feavr.R;

public class BatteryProgressView extends View {
    private static final float BATTERY_LOW_THRESHOLD = 20;
    private int width,height;
    private int offset = 0;
    private int lastOffset = 0;
    private Paint circlePaint,progressPaint,textPaint,percentPaint,batteryPaint;
    private int circleColor=0xFF00BDEB;
    private int progressColor=0xFF000000;
    private int batteryColor=0xFF000000;
    private int batteryAlertColor=0xFFFF0000;
    private int textColor=0xFFFFFFFF;
    private boolean mShowText = false;
    private int circleStrokeWidth = 4;
    private int innerStrokeWidth = 8;
    private static final int START_ANGLE=270;
    private RectF progressBounds;
    private int innerRadius,outerRadius;
    private float progress=0,maxProgress=100,lastProgress=0,progressUpdate;
    private ValueAnimator animator;
    private String progressText = "";
    private Bitmap batteryFullBitmapOriginal;
    private Bitmap batteryEmptyBitmapOriginal;
    private Bitmap batteryFullBitmap;
    private Bitmap batteryEmptyBitmap;
    ColorMatrixColorFilter colorFilter;
    ColorMatrixColorFilter alertColorFilter;



    public BatteryProgressView(Context context) {
        super(context);
        init(context,null);
    }

    public BatteryProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.BatteryProgressView,
                0, 0);

        try {
            mShowText = a.getBoolean(R.styleable.BatteryProgressView_showText, true);
            textColor = a.getColor(R.styleable.BatteryProgressView_textColor, textColor);
            progressColor = a.getColor(R.styleable.BatteryProgressView_progressColor, progressColor);
            innerStrokeWidth = (int) a.getDimension(R.styleable.BatteryProgressView_innerStrokeWidth, innerStrokeWidth);
            circleStrokeWidth = (int) a.getDimension(R.styleable.BatteryProgressView_circleStrokeWidth, circleStrokeWidth);
            batteryColor = a.getColor(R.styleable.BatteryProgressView_batteryColor, batteryColor);
            batteryAlertColor = a.getColor(R.styleable.BatteryProgressView_batteryAlertColor, batteryAlertColor);
            circleColor = a.getColor(R.styleable.BatteryProgressView_circleColor, circleColor);
        } finally {
            a.recycle();
        }

        colorFilter = getColorFilter(batteryColor);
        alertColorFilter = getColorFilter(batteryAlertColor);
        batteryPaint = new Paint();


        circlePaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setColor(circleColor);

        progressPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setColor(progressColor);

        progressBounds=new RectF();

        textPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextAlign(Paint.Align.CENTER);

        percentPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        percentPaint.setColor(textColor);
        percentPaint.setTextAlign(Paint.Align.CENTER);

        batteryFullBitmapOriginal = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_battery_charging_full_black_48dp);
        batteryEmptyBitmapOriginal = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_battery_alert_black_48dp);
    }

    public ColorMatrixColorFilter getColorFilter(int color){
        float[] colorTransform = {
                0, 0, 0, Color.red(color)/255f, 0,
                0, 0, 0, Color.green(color)/255f, 0,
                0, 0, 0, Color.blue(color)/255f, 0,
                0, 0, 0, 1f, 0};

        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0f);
        colorMatrix.set(colorTransform);
        return new ColorMatrixColorFilter(colorMatrix);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        circlePaint.setStrokeWidth(circleStrokeWidth);
        progressPaint.setStrokeWidth(innerStrokeWidth);
        canvas.drawCircle((width-offset)/2+offset,height/2,outerRadius,circlePaint);
        canvas.drawCircle((width-offset)/2+offset,height/2,innerRadius,circlePaint);
        canvas.drawArc(progressBounds,START_ANGLE,progressUpdate,false,progressPaint);
        if(progress > BATTERY_LOW_THRESHOLD) {
            batteryPaint.setColorFilter(colorFilter);
            if(offset != lastOffset || batteryFullBitmap == null) {
                batteryFullBitmap = Bitmap.createScaledBitmap(batteryFullBitmapOriginal, offset, offset, false);
                lastOffset = offset;
            }
            canvas.drawBitmap(batteryFullBitmap, 0, 0, batteryPaint);
        } else {
            if(offset != lastOffset || batteryEmptyBitmap == null) {
                batteryEmptyBitmap = Bitmap.createScaledBitmap(batteryEmptyBitmapOriginal, offset, offset, false);
                lastOffset = offset;
            }
            batteryPaint.setColorFilter(alertColorFilter);
            canvas.drawBitmap(batteryEmptyBitmap, 0, 0, batteryPaint);
        }

        if(mShowText) {
            float textSize = height/4;
            textPaint.setTextSize(textSize);
            percentPaint.setTextSize(textSize/1.5f);
            float textWidth=textPaint.measureText(progressText);
            float percentWidth=percentPaint.measureText("%");
            canvas.drawText(progressText, ((width-offset)/2) + offset - percentWidth/2, height/2-((textPaint.descent()+textPaint.ascent())/2),textPaint);
            canvas.drawText("%", ((width-offset)/2) + offset + textWidth/2, height/2-((percentPaint.descent()+percentPaint.ascent())/2),percentPaint);
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = 300;
        int desiredHeight = 300;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
        } else {
            //Be whatever you want
            width = desiredWidth;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
        } else {
            //Be whatever you want
            height = desiredHeight;
        }


        offset = width-height;
        outerRadius=((height/2)- circleStrokeWidth/2);
        innerRadius=((height/2)- innerStrokeWidth - circleStrokeWidth - circleStrokeWidth/2);

        //MUST CALL THIS
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        width=w;
        height=h;
        offset=width-height;

        float margin = circleStrokeWidth + innerStrokeWidth/2;
        progressBounds.set(offset + margin, margin, width - margin, height-margin);
     }

    public void setProgress(int progress) {
        lastProgress=this.progress;
        this.progress = progress;
        post(new Runnable() {
            @Override
            public void run() {
                float increment=360/maxProgress;
                if(lastProgress<BatteryProgressView.this.progress) {
                    animator = ValueAnimator.ofFloat(increment*lastProgress, increment * (BatteryProgressView.this.progress));
                    animator.setDuration(800);
                    animator.addUpdateListener(animatorUpdateListener);
                    animator.setInterpolator(new DecelerateInterpolator());
                    animator.start();
                }else {
                    animator = ValueAnimator.ofFloat((increment*lastProgress), increment * (BatteryProgressView.this.progress));
                    animator.setDuration(800);
                    animator.addUpdateListener(animatorUpdateListener);
                    animator.setInterpolator(new DecelerateInterpolator());
                    animator.start();
                }
            }
        });

    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
    }
    ValueAnimator.AnimatorUpdateListener animatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float update = (float) (animation.getAnimatedValue());
            float increment=360/maxProgress;
            float value=(update/increment);
            progressUpdate=update;
            progressText=((int)value)+"";
            invalidate();
        }
    };
}
