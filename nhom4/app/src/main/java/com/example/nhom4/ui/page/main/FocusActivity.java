package com.example.nhom4.ui.page.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.nhom4.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.Locale;

public class FocusActivity extends AppCompatActivity {

    private TextView tvTimer, tvTitle;
    private CircularProgressIndicator progressIndicator;
    private FloatingActionButton btnFinish;
    private View btnClose;
    private ShapeableImageView imgCenterIcon;

    private CountDownTimer countDownTimer;
    private long totalTimeInMillis;
    private long timeLeftInMillis;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus);

        initViews();
        setupData();
        startTimer();
    }

    private void initViews() {
        tvTimer = findViewById(R.id.tv_timer);
        tvTitle = findViewById(R.id.tv_activity_title);
        progressIndicator = findViewById(R.id.progress_timer);
        btnFinish = findViewById(R.id.btn_finish);
        btnClose = findViewById(R.id.btn_close);
        imgCenterIcon = findViewById(R.id.img_center_icon);

        btnFinish.setOnClickListener(v -> finishFocusSession());
        btnClose.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private void setupData() {
        String title = getIntent().getStringExtra("title");
        long durationSec = getIntent().getLongExtra("duration", 900);
        String imgUrl = getIntent().getStringExtra("imgUrl");

        tvTitle.setText(title != null ? title : "Hoạt động");

        if (imgUrl != null && !imgUrl.isEmpty()) {
            Glide.with(this).load(imgUrl).into(imgCenterIcon);
        }

        totalTimeInMillis = durationSec * 1000;
        timeLeftInMillis = totalTimeInMillis;

        progressIndicator.setMax(100);
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
                updateProgressBar();
            }

            @Override
            public void onFinish() {
                timeLeftInMillis = 0;
                updateCountDownText();
                progressIndicator.setProgress(0);
                Toast.makeText(FocusActivity.this, "Hoàn thành! Hãy check-in.", Toast.LENGTH_SHORT).show();
                finishFocusSession();
            }
        }.start();
    }

    private void updateCountDownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
    }

    private void updateProgressBar() {
        int progress = (int) ((timeLeftInMillis * 100) / totalTimeInMillis);
        progressIndicator.setProgress(progress, true);
    }

    private void finishFocusSession() {
        if (countDownTimer != null) countDownTimer.cancel();
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}