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

    private TextView tvTitle, tvTime, tvMembers, tvProgress;
    private ImageView imgHeader;
    private GridLayout gridDays;
    private Toolbar toolbar;
    private MaterialButton btnViewAllPosts;

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

        initViews();

        // === XỬ LÝ NHẬN DỮ LIỆU TỪ INTENT ===
        Activity passedActivity = getIntent().getParcelableExtra("ACTIVITY");
        String activityId = getIntent().getStringExtra("ACTIVITY_ID");

        if (passedActivity != null) {
            // Trường hợp mở từ trong app (có truyền toàn bộ object Activity)
            currentActivity = passedActivity;
            // Đảm bảo ID được set (phòng trường hợp thiếu)
            if (activityId != null) {
                currentActivity.setId(activityId);
            }

            setupAfterLoad();
        } else if (activityId != null) {
            // Trường hợp mở từ Widget (chỉ có ID)
            loadActivityFromFirestore(activityId);
        } else {
            Toast.makeText(this, "Không tải được hoạt động", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadActivityFromFirestore(String activityId) {
        FirebaseFirestore.getInstance()
                .collection("activities")
                .document(activityId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    currentActivity = documentSnapshot.toObject(Activity.class);
                    if (currentActivity != null) {
                        currentActivity.setId(activityId);
                        setupAfterLoad();
                    } else {
                        Toast.makeText(this, "Không tìm thấy hoạt động", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void setupAfterLoad() {
        // Chỉ gọi các hàm này sau khi đã có currentActivity chắc chắn
        viewModel = new ViewModelProvider(this).get(DetailViewModel.class);
        viewModel.setCurrentActivity(currentActivity);

        bindData();
        setupGridDays();
        observeViewModel();
        setupViewAllButton();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        tvTime = findViewById(R.id.tv_time);
        tvMembers = findViewById(R.id.tv_members);
        tvProgress = findViewById(R.id.tv_progress);
        imgHeader = findViewById(R.id.img_header);
        gridDays = findViewById(R.id.grid_days);
        btnViewAllPosts = findViewById(R.id.btn_view_all_posts);
    }

    private void bindData() {
        tvTitle.setText(currentActivity.getTitle());

        if (currentActivity.getScheduledTime() != null) {
            Date start = currentActivity.getScheduledTime().toDate();
            long endMillis = start.getTime() + (currentActivity.getDurationSeconds() * 1000);
            Date end = new Date(endMillis);

            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            String timeRange = timeFormat.format(start) + " - " + timeFormat.format(end);

            String frequency = currentActivity.isDaily() ? "Hằng ngày" : "Tùy chỉnh";
            tvTime.setText(frequency + " • " + timeRange);
        } else {
            tvTime.setText("Chưa đặt thời gian");
        }

        List<String> participants = currentActivity.getParticipants();
        int count = participants != null ? participants.size() : 1;
        tvMembers.setText(count + " thành viên" + (count > 1 ? "" : ""));

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
                    imgSun.setImageResource(R.drawable.outline_light_mode_24);
                    // Khi click vào ngày có post → mở feed posts
                    dayView.setOnClickListener(v -> openPostsFeed());
                } else {
                    imgSun.setImageResource(R.drawable.outline_light_mode_24);
                    imgSun.setAlpha(0.3f); // Làm mờ icon ngày chưa có post
                    dayView.setOnClickListener(null);
                }
            });

            gridDays.addView(dayView);
        }
    }

    /**
     * Thiết lập nút "Xem tất cả bài đăng"
     */
    private void setupViewAllButton() {
        if (btnViewAllPosts != null) {
            btnViewAllPosts.setOnClickListener(v -> openPostsFeed());
        }
    }

    /**
     * Mở màn hình feed posts (giống TikTok - vuốt dọc)
     */
    private void openPostsFeed() {
        Intent intent = new Intent(this, ActivityPostsFeedActivity.class);
        intent.putExtra("ACTIVITY_ID", currentActivity.getId());
        intent.putExtra("ACTIVITY_TITLE", currentActivity.getTitle());
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