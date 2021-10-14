package com.jp12.calculatorapp;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void onClick(View view){
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(500);
        }
        System.out.println("vibrated");
    }
    public void resizeClick(View view){
        final LinearLayout linearLayout = findViewById(R.id.linlayout1);
        getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
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
}
