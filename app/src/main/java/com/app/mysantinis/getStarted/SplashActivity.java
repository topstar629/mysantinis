package com.app.mysantinis.getStarted;

import android.os.Bundle;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.app.mysantinis.R;

public class SplashActivity extends AppCompatActivity {

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        progressBar = findViewById(R.id.progress);

        progressBar.setMax(100);
        progressAnimation();

    }

    public void progressAnimation(){
        FirstScreenAnimation anim = new FirstScreenAnimation(this, progressBar, 0f,100f);
        anim.setDuration(2000);
        progressBar.setAnimation(anim);
    }
}
