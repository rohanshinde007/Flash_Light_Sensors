package com.mul.flash007;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnPaidEventListener;
import com.google.android.gms.ads.ResponseInfo;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.util.Objects;

import static android.content.ContentValues.TAG;

//there
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {
    ToggleButton toggle_button;
    CameraManager camaraManager;
    String getCameraID;
    ImageView image, image2;


    private InterstitialAd mInterstitialAd;
    private SensorManager mSensorManager;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;

    // THIS IS FOR VERSION

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("Shaking Torch");

        Intent serviceIntent = new Intent(this, MyBagroundService.class);
        startService(serviceIntent);





        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        toggle_button = findViewById(R.id.toggle_button);
        camaraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        // FOR DECREASING ERROR USE TRY CATCH
        try {
            //
            // by Deafult
            // when your device are not able access camara flash light
            // then print exception
            getCameraID = camaraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


        // this is for shake event

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Objects.requireNonNull(mSensorManager).registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 10f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator m = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
// Vibrate for 500 milliseconds

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        m.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE));
                    }
                 else {
                    //deprecated in API 26
                    m.vibrate(70);
                }

                if (mInterstitialAd!=null) {
                    mInterstitialAd.show(MainActivity.this);
                }else{
                    openSecond();
                }
            }
        });
        AdRequest adRequest = new AdRequest.Builder().build();
//   ca-app-pub-3940256099942544/1033173712 test ad
        //  ca-app-pub-9399051416600684/3197089612  real add

            InterstitialAd.load(MainActivity.this, "ca-app-pub-9399051416600684/3197089612", adRequest,
                    new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            // The mInterstitialAd reference will be null until
                            // an ad is loaded.
                            mInterstitialAd = interstitialAd;
                            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    openSecond();
                                }
                            });
                        }
                    });
    }

    private final SensorEventListener mSensorListener = new SensorEventListener() {

        @SuppressLint("SetTextI18n")
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onSensorChanged(SensorEvent event) {

            ImageView image = (ImageView) findViewById(R.id.image);
            ImageView image2 = (ImageView) findViewById(R.id.image2);


            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;
            if (mAccel > 16) {

                try {
                    //this is for enable the torch
                    camaraManager.setTorchMode(getCameraID, true);
                    image.setVisibility(View.VISIBLE);
                    image2.setVisibility(View.INVISIBLE);
                    toggle_button.setText("on");

                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            } else if (mAccel > 9) {

                try {
                    // of the torch
                    camaraManager.setTorchMode(getCameraID, false);
                    image.setVisibility(View.INVISIBLE);
                    image2.setVisibility(View.VISIBLE);
                    toggle_button.setText("off");

                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Override
    protected void onResume() {
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        super.onResume();
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        super.onPause();
    }

    // for latest version control
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void toggleFlashLeight(View view) {
        ImageView image = (ImageView) findViewById(R.id.image);
        ImageView image2 = (ImageView) findViewById(R.id.image2);
        if (toggle_button.isChecked()) {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //deprecated in API 26
                v.vibrate(70);
            }
            try {
                //this is for enable the torch
                camaraManager.setTorchMode(getCameraID, true);
                image.setVisibility(View.VISIBLE);
                image2.setVisibility(View.INVISIBLE);


            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
// Vibrate for 500 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //deprecated in API 26
                v.vibrate(70);
            }
            try {
                // of the torch
                camaraManager.setTorchMode(getCameraID, false);
                image.setVisibility(View.INVISIBLE);
                image2.setVisibility(View.VISIBLE);

            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }


    private void openSecond() {
        Intent i = new Intent(MainActivity.this,ShakingSensors.class);
        startActivity(i);
    }
}

//---------->THANKS----------->
//==============SUBSCRIBE ME PLEASE FOR MORE CONTENT