package com.wherzit.sammy.wherzit;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActvity extends AppCompatActivity {

    private static int SPLASH_TIMER = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed( new Runnable() {

            @Override
            public void run(){
                Intent i = new Intent(SplashActvity.this, MainActivity.class);
                startActivity(i);

                finish();

            }
        }, SPLASH_TIMER);

    }
}
