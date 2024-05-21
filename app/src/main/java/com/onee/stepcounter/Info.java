package com.onee.stepcounter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

public class Info extends AppCompatActivity
{
    private TextView ty;
    LottieAnimationView lottieAnimationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        ty=findViewById(R.id.tybutton);
        lottieAnimationView=findViewById(R.id.lottieeee);

        lottieAnimationView.setRepeatCount(Animation.INFINITE);
        lottieAnimationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(Info.this,MainActivity.class);
                startActivity(i);
                finish();
                System.gc();
            }
        });

        ty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(Info.this,MainActivity.class);
                startActivity(i);
                finish();
                System.gc();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent i=new Intent(Info.this,MainActivity.class);
        startActivity(i);
        finish();
        System.gc();
    }
}