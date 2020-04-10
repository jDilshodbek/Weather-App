package com.test.weatherapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.test.weatherapp.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Declare views and animate for introduction
        ImageView imageIntro = findViewById(R.id.introImage);
        TextView textIntro = findViewById(R.id.introText);
        // Animation
        Animation animationFromRight = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.enter);
        Animation animationFromLeft = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.pop_enter);
        // start animations
        imageIntro.startAnimation(animationFromRight);
        textIntro.startAnimation(animationFromLeft);
        // Go to main page after 2 seconds
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter, R.anim.exit);
                finish();
            }
        }, 2000);

    }
}
