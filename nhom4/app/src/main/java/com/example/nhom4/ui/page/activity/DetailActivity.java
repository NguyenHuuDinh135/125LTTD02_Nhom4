package com.example.nhom4.ui.page.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.nhom4.R;
import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.Activity;
import com.example.nhom4.ui.page.calendar.StoryAllActivity;
import com.example.nhom4.ui.viewmodel.DetailViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    private DetailViewModel viewModel;
    private Activity currentActivity;

    private TextView tvTitle, tvTime, tvMembers, tvProgress;
    private ImageView imgHeader;
    private GridLayout gridDays;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Setup Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("");
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        // Nhận dữ liệu từ Intent
        currentActivity = getIntent().getParcelableExtra("ACTIVITY");
        if (currentActivity == null) {
            Toast.makeText(this, "Không tải được hoạt động", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(DetailViewModel.class);
        viewModel.setCurrentActivity(currentActivity);

        initViews();
        bindData();
        setupGridDays();
        observeViewModel();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        tvTime = findViewById(R.id.tv_time);
        tvMembers = findViewById(R.id.tv_members);
        tvProgress = findViewById(R.id.tv_progress);
        imgHeader = findViewById(R.id.img_header);
        gridDays = findViewById(R.id.grid_days);
    }

    private void bindData() {
        // --- TITLE ---
        tvTitle.setText(currentActivity.getTitle());

        // --- TIME ---
        if (currentActivity.getScheduledTime() != null) {
            Date start = currentActivity.getScheduledTime().toDate();
            long endMillis = start.getTime() + (currentActivity.getDurationSeconds() * 1000);
            Date end = new Date(endMillis);

            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            String timeRange = timeFormat.format(start) + " - " + timeFormat.format(end);

            String frequency = currentActivity.isDaily() ? "Everyday" : "Custom";
            tvTime.setText(frequency + " • " + timeRange);
        } else {
            tvTime.setText("Chưa đặt thời gian");
        }

        // --- MEMBERS ---
        List<String> participants = currentActivity.getParticipants();
        int count = participants != null ? participants.size() : 1;
        tvMembers.setText(count + " member" + (count > 1 ? "s" : ""));

        // --- HEADER IMAGE (Dùng Glide) ---
        // Đảm bảo bạn đã thêm dependency Glide trong build.gradle
        if (currentActivity.getImageUrl() != null && !currentActivity.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentActivity.getImageUrl())
                    .placeholder(R.drawable.default_image_background) // Ảnh chờ (cần có trong drawable)
                    .error(R.drawable.default_image_background)       // Ảnh lỗi
                    .centerCrop()
                    .into(imgHeader);
        } else {
            imgHeader.setImageResource(R.drawable.default_image_background);
        }
    }

    private void setupGridDays() {
        gridDays.removeAllViews();

        Calendar calendar = Calendar.getInstance();
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int day = 1; day <= daysInMonth; day++) {
            View dayView = getLayoutInflater().inflate(R.layout.item_day_grid, gridDays, false);

            ImageView imgSun = dayView.findViewById(R.id.img_sun);
            TextView tvDay = dayView.findViewById(R.id.tv_day);
            tvDay.setText(String.valueOf(day));

            final int finalDay = day;
            viewModel.isDayCheckedIn(currentActivity.getId(), finalDay).observe(this, checked -> {
                if (checked != null && checked) {
                    // Nếu ngày đó có check-in -> Hiện icon sáng
                    imgSun.setImageResource(R.drawable.outline_light_mode_24); // Đảm bảo có resource này

                    dayView.setOnClickListener(v -> openStoryAll(finalDay));
                } else {
                    // Nếu không -> Hiện icon mờ/tối
                    imgSun.setImageResource(R.drawable.outline_light_mode_24);

                    dayView.setOnClickListener(null);
                }
            });

            gridDays.addView(dayView);
        }
    }

    private void openStoryAll(int day) {
        Intent intent = new Intent(this, StoryAllActivity.class);
        intent.putExtra("ACTIVITY_ID", currentActivity.getId());
        intent.putExtra("DAY_OF_MONTH", day);
        intent.putExtra("MONTH", Calendar.getInstance().get(Calendar.MONTH) + 1);
        intent.putExtra("YEAR", Calendar.getInstance().get(Calendar.YEAR));
        startActivity(intent);
    }

    private void observeViewModel() {
        viewModel.getProgress().observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                int progressVal = resource.data;
                tvProgress.setText(progressVal + " / " + currentActivity.getTarget() + " lần");
            }
        });
    }
}