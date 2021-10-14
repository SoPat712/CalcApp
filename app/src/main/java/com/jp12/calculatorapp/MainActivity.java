package com.jp12.calculatorapp;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = findViewById(R.id.button);
        final LinearLayout linearLayout = findViewById(R.id.linlayout1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(linearLayout.getLayoutParams().height == 1275){
                    System.out.println(linearLayout.getLayoutParams().height);
                    ResizeAnimation resizeAnimation = new ResizeAnimation(
                            linearLayout,
                            -150,
                            1275
                    );
                    resizeAnimation.setDuration(150);
                    view.startAnimation(resizeAnimation);
                } else{
                    System.out.println(linearLayout.getLayoutParams().height);
                    ResizeAnimation resizeAnimation = new ResizeAnimation(
                            linearLayout,
                            150,
                            1125
                    );
                    resizeAnimation.setDuration(250);
                    view.startAnimation(resizeAnimation);
                }

            }
        });
    }
}
