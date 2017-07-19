package com.wherzit.sammy.wherzit;

import android.content.Intent;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class SplashActvity extends AppCompatActivity {

    private static int SPLASH_TIMER = 2800;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Animation anim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.drop_down);
        findViewById(R.id.splashText).startAnimation(anim);

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
