package com.jp12.calculatorapp;

import androidx.appcompat.app.AppCompatActivity;

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
        final ViewGroup.LayoutParams params = linearLayout.getLayoutParams();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String TAG = "Tag 1";
                for(int i = 400; i >= 300; i--){
                    int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, i, getResources().getDisplayMetrics());
                    params.height = height;
                    Log.d(TAG, String.valueOf(height));
                    linearLayout.setLayoutParams(params);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, "onClick: asdf");
            }
        });
    }
}
