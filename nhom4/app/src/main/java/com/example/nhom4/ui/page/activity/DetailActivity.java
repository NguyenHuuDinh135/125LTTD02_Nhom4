package com.example.nhom4.ui.page.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.example.nhom4.ui.page.main.FocusActivity;
import com.example.nhom4.ui.viewmodel.DetailViewModel;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    private DetailViewModel viewModel;
    private Activity currentActivity;

    private TextView tvTitle, tvTime, tvMembers, tvProgress, tvFinished;
    private ImageView imgHeader;
    private GridLayout gridDays;
    private Toolbar toolbar;
    private MaterialButton btnStartFocus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Toolbar setup
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(DetailViewModel.class);

        // Debug intent chi tiết
        Log.d("DetailActivity", "Intent action: " + getIntent().getAction());
        Log.d("DetailActivity", "Intent component: " + getIntent().getComponent());
        Log.d("DetailActivity", "Intent extras keys: " + (getIntent().getExtras() != null ? getIntent().getExtras().keySet().toString() : "null"));
        Log.d("DetailActivity", "Intent extras full: " + (getIntent().getExtras() != null ? getIntent().getExtras().toString() : "null"));

        String activityId = getIntent().getStringExtra("ACTIVITY_ID");
        Activity parcelActivity = getIntent().getParcelableExtra("ACTIVITY");

        if (parcelActivity != null) {
            Log.d("DetailActivity", "From normal intent, full Activity");
            currentActivity = parcelActivity;
            viewModel.setCurrentActivity(currentActivity);
            initViews();
            bindData();
            setupGridDays();
            observeViewModel();
            checkTodayCheckedIn();
        } else if (activityId != null && !activityId.isEmpty()) {
            Log.d("DetailActivity", "From widget, ID: " + activityId);
            fetchActivityById(activityId);
        } else {
            Log.e("DetailActivity", "No activity ID or object");
            Toast.makeText(this, "Không tải được hoạt động", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchActivityById(String activityId) {
        FirebaseFirestore.getInstance().collection("activities")
                .document(activityId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentActivity = documentSnapshot.toObject(Activity.class);
                        if (currentActivity != null) {
                            currentActivity.setId(activityId); // Đảm bảo ID đúng
                            viewModel.setCurrentActivity(currentActivity);
                            initViews();
                            bindData();
                            setupGridDays();
                            observeViewModel();
                            checkTodayCheckedIn();
                        } else {
                            Toast.makeText(this, "Không tìm thấy hoạt động", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Hoạt động không tồn tại", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        tvTime = findViewById(R.id.tv_time);
        tvMembers = findViewById(R.id.tv_members);
        tvProgress = findViewById(R.id.tv_progress);
        tvFinished = findViewById(R.id.tv_finished);
        btnStartFocus = findViewById(R.id.btn_start_focus);
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

        // --- HEADER IMAGE ---
        if (currentActivity.getImageUrl() != null && !currentActivity.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentActivity.getImageUrl())
                    .placeholder(R.drawable.default_image_background)
                    .error(R.drawable.default_image_background)
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
                    // Ngày có post → load first photoUrl
                    viewModel.getFirstPhotoUrlOfDay(currentActivity.getId(), finalDay).observe(this, photoUrl -> {
                        if (photoUrl != null) {
                            Glide.with(DetailActivity.this)
                                    .load(photoUrl)
                                    .placeholder(R.drawable.outline_light_mode_24)
                                    .error(R.drawable.outline_light_mode_24)
                                    .centerCrop()
                                    .into(imgSun);
                        } else {
                            imgSun.setImageResource(R.drawable.outline_light_mode_24); // Icon sáng
                        }
                    });

                    dayView.setOnClickListener(v -> openStoryAll(finalDay));
                } else {
                    imgSun.setImageResource(R.drawable.outline_light_mode_24); // Icon tối
                    dayView.setOnClickListener(null);
                }
            });

            gridDays.addView(dayView);
        }
    }

    private void checkTodayCheckedIn() {
        int today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        viewModel.isDayCheckedIn(currentActivity.getId(), today).observe(this, checked -> {
            if (checked != null && checked) {
                // Đã check-in hôm nay → hiện "Finished"
                tvFinished.setVisibility(View.VISIBLE);
                btnStartFocus.setVisibility(View.GONE);
            } else {
                // Chưa check-in → hiện nút "Thực hiện"
                tvFinished.setVisibility(View.GONE);
                btnStartFocus.setVisibility(View.VISIBLE);
                btnStartFocus.setOnClickListener(v -> startFocusSession());
            }
        });
    }

    private void startFocusSession() {
        Intent intent = new Intent(this, FocusActivity.class);
        intent.putExtra("title", currentActivity.getTitle());
        intent.putExtra("duration", currentActivity.getDurationSeconds());
        intent.putExtra("imgUrl", currentActivity.getImageUrl());
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Cập nhật progress, grid days và trạng thái nút sau khi hoàn thành focus
            viewModel.loadProgress();
            setupGridDays();
            checkTodayCheckedIn();
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