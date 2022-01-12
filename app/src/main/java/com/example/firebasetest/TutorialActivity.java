package com.example.firebasetest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class TutorialActivity extends AppCompatActivity {

    ImageView imgLeft, imgRight;
    RelativeLayout layoutTutorial;

    int pos = 0;
    int IdsSize = 2;
    int[] layoutDrawableIds = {
            R.drawable.tutorial_ice, R.drawable.tutorial_davinci
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        layoutTutorial = findViewById(R.id.layout_tutorial);
        imgLeft = findViewById(R.id.img_tutorial_left);
        imgRight = findViewById(R.id.img_tutorial_right);

        imgLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pos = (pos + IdsSize - 1) % IdsSize;
                layoutTutorial.setBackgroundResource(layoutDrawableIds[pos]);
            }
        });

        imgRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pos = (pos + 1) % IdsSize;
                layoutTutorial.setBackgroundResource(layoutDrawableIds[pos]);
            }
        });
    }
}
