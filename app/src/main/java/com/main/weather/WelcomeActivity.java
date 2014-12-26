package com.main.weather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.util.Calendar;
import java.util.GregorianCalendar;


public class WelcomeActivity extends Activity implements Animation.AnimationListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ImageView iv1 = (ImageView) findViewById(R.id.welcome_bg);
        Animation animZoomIn;

        Calendar calendar = new GregorianCalendar();

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour >= 20 || hour <= 6) {
            iv1.setImageDrawable(getResources().getDrawable(R.drawable.evening));
        } else if (hour >=18) {
            iv1.setImageDrawable(getResources().getDrawable(R.drawable.dusk));
        } else if (hour >= 10) {
            iv1.setImageDrawable(getResources().getDrawable(R.drawable.afternoon));
        } else {
            iv1.setImageDrawable(getResources().getDrawable(R.drawable.morning));
        }



        animZoomIn = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.zoom_in);

        animZoomIn.setAnimationListener(this);

        iv1.startAnimation(animZoomIn);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                startActivity(intent);
                WelcomeActivity.this.finish();
            }
        }, 2000);
    }


    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {

    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
