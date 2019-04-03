package com.example.ball;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    enum State { IDLE, THROWING, THROWN }
    private final String TAG = "MainActivity";

    private final double GRAVITY = 9.81f;

    private State mState = State.IDLE;

    private double mMinAcc = 4.0d;           // m/s^2
    private double mMaxAcceleration = 0.0d;  // the highest acceleration during a throw (m/s^2)
    private double mVelocity = 0.0d;
    private double mInitialVelocity = 0.0d;
    private double mHeight = 0.0d;
    private double mElapsedTime = 0.0d;
    private double mTimeOfThrow = 0.0d;

    private double mHighestThrow = 0.0d;

    int mSlidingWindowSize = 20;
    int mSlidingWindowIndex = 0;
    double[] mSlidingWindow;

    private SensorManager mSensorManager;
    private Sensor mSensor;

    private Handler mHandler;
    private int mHandlerDelay = 10;
    Runnable mRunnable;

    //private TextView mTxtX;
    //private TextView mTxtY;
    //private TextView mTxtZ;
    private TextView mTxtScore;
    private TextView mTxtHighScore;
    private Button mBtnPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager =
                (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);

        mHandler = new Handler();

        //mTxtX = findViewById(R.id.txt_x);
        //mTxtY = findViewById(R.id.txt_y);
        //mTxtZ = findViewById(R.id.txt_z);
        mTxtScore = findViewById(R.id.txt_score);
        mTxtHighScore = findViewById(R.id.txt_high_score);
        mBtnPrefs = findViewById(R.id.btn_prefs);

        mSlidingWindow = new double[mSlidingWindowSize];

        mBtnPrefs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PrefsActivity.class);
                startActivityForResult(intent, 0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == 0) {
            mMinAcc = data.getDoubleExtra("minAcc", 4.0d);
        }
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        //mTxtX.setText("X: " + x);
        //mTxtY.setText("Y: " + y);
        //mTxtZ.setText("Z: " + z);

        double acceleration = Math.sqrt(x * x + y * y + z * z) - GRAVITY;
        //mTxtScore.setText("Acceleration: " + acceleration);

        if (acceleration > mMaxAcceleration) {
            mMaxAcceleration = acceleration;
        }

        if (mState == State.IDLE) {
            // check if a throw is started
            if (acceleration > mMinAcc) {
                // throw the ball
                mState = State.THROWING;
            }
        }

        if (mState == State.THROWING) {
            if (mSlidingWindowIndex >= mSlidingWindowSize) {
                int maxIndex = 0;
                for (int i = 0; i < mSlidingWindowSize; i++) {
                    if (mSlidingWindow[i] > mSlidingWindow[maxIndex]) {
                        maxIndex = i;
                    }
                }
                if (maxIndex == 0) {
                    mState = State.THROWN;
                    mHeight = 0.0d;
                    mVelocity = mMaxAcceleration;
                    mInitialVelocity = mVelocity;
                    double maxHeight = Math.pow(mMaxAcceleration, 2) / (2 * GRAVITY);
                    if (maxHeight > mHighestThrow) {
                        mHighestThrow = maxHeight;
                        mTxtHighScore.setText("High Score: " + mHighestThrow);
                    }
                    mMaxAcceleration = 0.0f;
                    mTimeOfThrow = Math.sqrt(2 * maxHeight / GRAVITY) * 2;
                    mElapsedTime = 0.0d;
                    mSlidingWindowIndex = 0;

                    mRunnable = new Runnable() {
                        @Override
                        public void run() {
                            update();
                            mHandler.postDelayed(this, mHandlerDelay);
                        }
                    };
                    mHandler.postDelayed(mRunnable, mHandlerDelay);
                }
                for (int i = 0; i < mSlidingWindowSize - 1; i++) {
                    mSlidingWindow[i] = mSlidingWindow[i + 1];
                }
            } else {
                Log.d(TAG, "onSensorChanged: " + mSlidingWindowIndex);
                mSlidingWindow[mSlidingWindowIndex++] = acceleration;
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    protected void update() {
        if (mState == State.THROWN) {
            // physics simulation

            double newVelocity = mVelocity - GRAVITY * mHandlerDelay / 1000.0d;
            mElapsedTime += (double)mHandlerDelay / 1000.0d;
            mHeight = mInitialVelocity * mElapsedTime - 0.5 * GRAVITY * Math.pow(mElapsedTime, 2);

            // reached top
            if (mVelocity > 0.0d && newVelocity <= 0.0d) {
                playSound();
            } else {
                mTxtScore.setText("Height (metres): " + mHeight + "\nElapsed time: " + mElapsedTime);
            }
            mVelocity = newVelocity;

            if (mElapsedTime >= mTimeOfThrow) {
                mState = State.IDLE;
                mHandler.removeCallbacks(mRunnable);
            }
        }
    }

    protected void playSound() {
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.sound);
        mp.start();
    }
}