package com.example.myapplication5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private SpeedoMeterView Speed;

    private int frameRate =60;
    private Handler handler;

    private Runnable frameUpdateRunnable;

    private TachoMeterView Rpm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Handler(Looper.getMainLooper());
        Speed = (SpeedoMeterView)findViewById(R.id.speedometer);
        Speed.setLabelConverter(new SpeedoMeterView.LabelConverter() {
            @Override
            public String getLabelFor(double progress, double maxProgress) {
                return String.valueOf((int) Math.round(progress));
            }
        });
// configure value range and ticks
        Speed.setMaxSpeed(200);
        Speed.setMajorTickStep(25);
        Speed.setMinorTicks(5);

// Configure value range colors
        Speed.addColoredRange(0, 50*5, Color.GREEN);
        Speed.addColoredRange(50, 75, Color.YELLOW);
        Speed.addColoredRange(75, 100, Color.RED);
        Speed.startAnimationThread();


// TachoMeter
        Rpm = (TachoMeterView)findViewById(R.id.tachometer);
        Rpm.setLabelConverter(new TachoMeterView.LabelConverter() {
            @Override
            public String getLabelFor(double progress, double maxProgress) {
                return String.valueOf((int) Math.round(progress));
            }
        });

// configure value range and ticks
        Rpm.setMaxRpm(100);
        Rpm.setMajorTickStep(25);
        Rpm.setMinorTicks(0);

// Configure value range colors
        Rpm.addColoredRange(0, 50, Color.GREEN);
        Rpm.addColoredRange(50, 75, Color.YELLOW);
        Rpm.addColoredRange(75, 100, Color.RED);
        Rpm.setRpm(25, 2000, 500);

        frameUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                Speed.invalidate();
                handler.postDelayed(this,1000/frameRate);
            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(frameUpdateRunnable,1000/frameRate);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(frameUpdateRunnable);
    }
}