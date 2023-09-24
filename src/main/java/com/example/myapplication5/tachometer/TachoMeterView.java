package com.example.myapplication5.tachometer;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.myapplication5.R;
import com.example.myapplication5.common.ColoredRange;
import com.example.myapplication5.common.LabelConverter;

import java.util.ArrayList;
import java.util.List;

public class TachoMeterView extends View {

    private static final String TAG = TachoMeterView.class.getSimpleName();

    public static final double DEFAULT_MAX_SPEED = 6000.0;
    public static final double DEFAULT_MAJOR_TICK_STEP = 1000.0;
    public static final int DEFAULT_MINOR_TICKS = 500;

    private double maxSpeed = DEFAULT_MAX_SPEED;
    private double speed = 0;
    private int defaultColor = Color.rgb(180, 180, 180);
    private double majorTickStep = DEFAULT_MAJOR_TICK_STEP;
    private int minorTicks = DEFAULT_MINOR_TICKS;

    // Variables used to draw ticks.
    private float availableAngle;
    private float majorStep;
    private float minorStep;
    private float majorTicksLength = 30;
    private float minorTicksLength = majorTicksLength/2;
    private float currentAngle;
    private double curProgress;

    private LabelConverter labelConverter;

    private List<ColoredRange> ranges;

    private Paint backgroundPaint;
    private Paint backgroundInnerPaint;
    private Paint maskPaint;
    private Paint needlePaint;
    private Paint ticksPaint;
    private Paint txtPaint;

    private Paint txtMeterInfo;

    private Rect rectTextBounds;

    private String txt = "Engine RPM";
    private Paint colorLinePaint;

    private Bitmap mMask;

    private Handler handler;

    private Runnable runnable;

    private ValueAnimator valueAnimator;
    private RectF oval;




    public TachoMeterView(Context context) {
        super(context);
        init();
    }

    public TachoMeterView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray attributes = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.SpeedometerView,
                0, 0);

        try {
            // read attributes
            setMaxSpeed(attributes.getFloat(R.styleable.SpeedometerView_maxSpeed, (float) DEFAULT_MAX_SPEED));
            setSpeed(attributes.getFloat(R.styleable.SpeedometerView_speed, 0));
        } finally {
            attributes.recycle();
        }
        init();
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(double maxSpeed) {
        if (maxSpeed <= 0)
            throw new IllegalArgumentException("Non-positive value specified as max speed.");
        this.maxSpeed = maxSpeed;
        invalidate();
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        if (speed < 0)
            throw new IllegalArgumentException("Non-positive value specified as a speed.");
        if (speed > maxSpeed)
            speed = maxSpeed;
        this.speed = speed;
        invalidate();
    }


    public int getDefaultColor() {
        return defaultColor;
    }

    public void setDefaultColor(int defaultColor) {
        this.defaultColor = defaultColor;
        invalidate();
    }

    public double getMajorTickStep() {
        return majorTickStep;
    }

    public void setMajorTickStep(double majorTickStep) {
        if (majorTickStep <= 0)
            throw new IllegalArgumentException("Non-positive value specified as a major tick step.");
        this.majorTickStep = majorTickStep;
        invalidate();
    }

    public int getMinorTicks() {
        return minorTicks;
    }

    public void setMinorTicks(int minorTicks) {
        this.minorTicks = minorTicks;
        invalidate();
    }

    public void setLabelConverter(LabelConverter labelConverter) {
        this.labelConverter = labelConverter;
        invalidate();
    }

    public void addColoredRange(double begin, double end, int color) {
        if (begin >= end)
            throw new IllegalArgumentException("Incorrect number range specified!");
        if (begin < - 5.0/160* maxSpeed)
            begin = - 5.0/160* maxSpeed;
        if (end > maxSpeed * (5.0/160 + 1))
            end = maxSpeed * (5.0/160 + 1);
        ranges.add(new ColoredRange(color, begin, end));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Clear canvas
        canvas.drawColor(Color.TRANSPARENT);

        // Draw Metallic Arc and background
        drawBackground(canvas);

        // Draw Text
        drawText(canvas);

        // Draw Ticks and colored arc
        drawTicks(canvas);

        // Draw Needle
        drawNeedle(canvas);

    }

    private int widthMode;
    private int widthSize;
    private int heightMode;
    private int heightSize;
    private int width;
    private int height;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
         widthMode = MeasureSpec.getMode(widthMeasureSpec);
         widthSize = MeasureSpec.getSize(widthMeasureSpec);
         heightMode = MeasureSpec.getMode(heightMeasureSpec);
         heightSize = MeasureSpec.getSize(heightMeasureSpec);

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.AT_MOST) {
            //Must be this size
            width = widthSize;
        } else {
            width = -1;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY || heightMode == MeasureSpec.AT_MOST) {
            //Must be this size
            height = heightSize;
        } else {
            height = -1;
        }

        if (height >= 0 && width >= 0) {
            width = Math.min(height, width);
            height = width/2;
        } else if (width >= 0) {
            height = width/2;
        } else if (height >= 0) {
            width = height*2;
        } else {
            width = 0;
            height = 0;
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height);
    }

    private void drawNeedle(Canvas canvas) {
        updateOval(canvas, 1);
        float radius = oval.width()*0.35f;

        float angle = 10 + (float) (getSpeed()/ getMaxSpeed()*160);
        canvas.drawLine(
                (float) (oval.centerX() + 0),
                (float) (oval.centerY() - 0),
                (float) (oval.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * (radius)),
                (float) (oval.centerY() - Math.sin(angle / 180 * Math.PI) * (radius)),
                needlePaint
        );

        updateOval(canvas, 0.2f);
        canvas.drawArc(oval, 180, 180, true, backgroundPaint);
    }


    private void drawTicks(Canvas canvas) {
       majorStep = (float) (majorTickStep/ maxSpeed *availableAngle);
       minorStep = majorStep / (1 + minorTicks);

        updateOval(canvas, 1);
        float radius = oval.width()*0.35f;
        currentAngle = 10;
        curProgress = 0;
        while (currentAngle <= 170) {
            canvas.drawLine(
                    (float) (oval.centerX() + Math.cos((180-currentAngle)/180*Math.PI)*(radius-majorTicksLength/2)),
                    (float) (oval.centerY() - Math.sin(currentAngle/180*Math.PI)*(radius-majorTicksLength/2)),
                    (float) (oval.centerX() + Math.cos((180-currentAngle)/180*Math.PI)*(radius+majorTicksLength/2)),
                    (float) (oval.centerY() - Math.sin(currentAngle/180*Math.PI)*(radius+majorTicksLength/2)),
                    ticksPaint
            );

            for (int i=1; i<=minorTicks; i++) {
                float angle = currentAngle + i*minorStep;
                if (angle >= 170 + minorStep/2) {
                    break;
                }
                canvas.drawLine(
                        (float) (oval.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * radius),
                        (float) (oval.centerY() - Math.sin(angle / 180 * Math.PI) * radius),
                        (float) (oval.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * (radius + minorTicksLength)),
                        (float) (oval.centerY() - Math.sin(angle / 180 * Math.PI) * (radius + minorTicksLength)),
                        ticksPaint
                );
            }

            if (labelConverter != null) {
                canvas.save();
                canvas.rotate(180 + currentAngle, oval.centerX(), oval.centerY());
                canvas.rotate(+90, (oval.centerX() + radius + majorTicksLength/2 + 8), oval.centerY());
                canvas.drawText(labelConverter.getLabelFor(curProgress, maxSpeed), (oval.centerX() + radius + majorTicksLength/2 + 8), oval.centerY(), txtPaint);
                canvas.restore();
            }
            currentAngle += majorStep;
            curProgress += majorTickStep;
        }
        updateOval(canvas, 0.7f);
        colorLinePaint.setColor(defaultColor);
        canvas.drawArc(oval, 185, 170, false, colorLinePaint);

        for (ColoredRange range: ranges) {
            colorLinePaint.setColor(range.getColor());
            canvas.drawArc(oval, (float) (190 + range.getBegin()/ maxSpeed *160), (float) ((range.getEnd() - range.getBegin())/ maxSpeed *160), false, colorLinePaint);
        }
    }

    private void updateOval(Canvas canvas, float factor) {
        final int canvasWidth = canvas.getWidth() - getPaddingLeft() - getPaddingRight();
        final int canvasHeight = canvas.getHeight() - getPaddingTop() - getPaddingBottom();

        if (canvasHeight*2 >= canvasWidth) {
            oval.set(0, 0, canvasWidth*factor, canvasWidth*factor);
        } else {
            oval.set(0, 0, canvasWidth*factor, canvasWidth*factor);
        }
        oval.offset((canvasWidth-oval.width())/2 + getPaddingLeft(), (canvasHeight*2-oval.height())/2 + getPaddingTop());
    }


    private void drawText(Canvas canvas){
        // Draw the text in the center
        canvas.drawText(txt, (canvas.getWidth() - rectTextBounds.width())/2, (canvas.getHeight() + rectTextBounds.height()) / 2, txtMeterInfo);
    }
    private void drawBackground(Canvas canvas) {
        updateOval(canvas, 1);
        canvas.drawArc(oval, 180, 180, true, backgroundPaint);

        updateOval(canvas, 0.9f);
        canvas.drawArc(oval, 180, 180, true, backgroundInnerPaint);

    }


    private void init() {
        availableAngle =160;
        majorStep = (float) (majorTickStep/ maxSpeed *availableAngle);
        minorStep = majorStep / (1 + minorTicks);


        oval=new RectF();

        this.setLabelConverter(new LabelConverter() {
            @Override
            public String getLabelFor(double progress, double maxProgress) {
                return String.valueOf((int) Math.round(progress));
            }
        });

        // configure value range and ticks
        this.setMaxSpeed(DEFAULT_MAX_SPEED);
        this.setMajorTickStep(DEFAULT_MAJOR_TICK_STEP);
        this.setMinorTicks(DEFAULT_MINOR_TICKS);

// Configure value range colors
        ranges = new ArrayList<ColoredRange>();
        this.addColoredRange(0, 3000, Color.GREEN);
        this.addColoredRange(3000,5000 , Color.YELLOW);
        this.addColoredRange(5000, 6000, Color.RED);

//  Creating Runnable to start the animation on click of this view.
        // Set of speed values to be animated.
        valueAnimator = ValueAnimator.ofInt(0,4000,2000,6000,0,5000,3000,7500,0);
        valueAnimator.setDuration(5000);
        runnable = new Runnable() {
            @Override
            public void run() {
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
                    @Override
                    public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                        double value = Double.valueOf((int) animation.getAnimatedValue());
                        setSpeed(value);
                    }
                });
                valueAnimator.start();
            }
        };
        handler = new Handler(Looper.getMainLooper());
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.post(runnable);
            }
        });

        if (Build.VERSION.SDK_INT >= 11 && !isInEditMode()) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null);

        }

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(Color.rgb(127, 127, 127));

        backgroundInnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundInnerPaint.setStyle(Paint.Style.FILL);
        //backgroundInnerPaint.setColor(Color.rgb(150, 150, 150));
        backgroundInnerPaint.setColor(Color.BLACK);

        txtPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        txtPaint.setColor(Color.WHITE);
        txtPaint.setTextSize(35);
        txtPaint.setTextAlign(Paint.Align.CENTER);

        txtMeterInfo = new Paint(Paint.ANTI_ALIAS_FLAG);
        txtMeterInfo.setColor(Color.WHITE);
        txtMeterInfo.setTextSize(35);
        txtMeterInfo.setTextAlign(Paint.Align.LEFT);
        rectTextBounds = new Rect();
        txtMeterInfo.getTextBounds(txt,0,txt.length(),rectTextBounds);

        mMask = BitmapFactory.decodeResource(getResources(), R.drawable.spot_mask);
        mMask = Bitmap.createBitmap(mMask, 0, 0, mMask.getWidth(), mMask.getHeight()/2);

        maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maskPaint.setDither(true);

        ticksPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ticksPaint.setStrokeWidth(3.0f);
        ticksPaint.setStyle(Paint.Style.STROKE);
        ticksPaint.setColor(defaultColor);

        colorLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        colorLinePaint.setStyle(Paint.Style.STROKE);
        colorLinePaint.setStrokeWidth(5);
        colorLinePaint.setColor(defaultColor);

        needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        needlePaint.setStrokeWidth(5);
        needlePaint.setStyle(Paint.Style.STROKE);
        needlePaint.setColor(Color.argb(200, 255, 0, 0));
    }
}