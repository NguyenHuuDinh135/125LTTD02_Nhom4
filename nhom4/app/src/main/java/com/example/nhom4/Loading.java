package com.example.nhom4;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Loading extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        ImageView imgLoader = findViewById(R.id.imgLoader);
        Animation rotate = AnimationUtils.loadAnimation(this, R.anim.rotate);
        imgLoader.startAnimation(rotate);

        // Chờ 3s rồi chuyển qua MainActivity
        new Handler().postDelayed(() -> {
            startActivity(new Intent(Loading.this, MainActivity.class));
            finish();
        }, 3000);
    }
}