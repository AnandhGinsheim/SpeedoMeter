package com.example.myapplication5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private SpeedoMeterView speed;

    private int frameRate =60; // Frame rate implementation has bugs.
    private Handler handler;
    private Runnable frameUpdateRunnable;
    private Runnable speedAnimationRunnable;
    private ValueAnimator valueAnimator;
    private TachoMeterView rpm;
    double value;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Handler(Looper.getMainLooper());
        speed = (SpeedoMeterView)findViewById(R.id.speedometer);

        frameUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                speed.invalidate();
                handler.postDelayed(this,1000/frameRate);
            }
        };

// TachoMeter
        rpm = (TachoMeterView)findViewById(R.id.tachometer);
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