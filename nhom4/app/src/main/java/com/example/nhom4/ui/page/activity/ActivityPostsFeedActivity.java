package com.example.nhom4.ui.page.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;
import com.example.nhom4.R;
import com.example.nhom4.ui.adapter.PostAdapter;
import com.example.nhom4.ui.viewmodel.ActivityPostsViewModel;
import com.google.android.material.appbar.MaterialToolbar;

public class ActivityPostsFeedActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private PostAdapter postAdapter;
    private MaterialToolbar toolbar;
    private ActivityPostsViewModel viewModel;
    private ProgressBar progressBar;
    private TextView tvEmptyState;

    private String activityId;
    private String activityTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts_feed);

        activityId = getIntent().getStringExtra("ACTIVITY_ID");
        activityTitle = getIntent().getStringExtra("ACTIVITY_TITLE");

        if (activityId == null) {
            Toast.makeText(this, "Lỗi: Không có ID hoạt động", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupViewPager();
        setupViewModel();
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPagerPosts);
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        toolbar.setTitle(activityTitle != null ? activityTitle : "Bài đăng");
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupViewPager() {
        postAdapter = new PostAdapter(this);
        viewPager.setAdapter(postAdapter);
        viewPager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);

        viewPager.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ActivityPostsViewModel.class);

        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getPosts().observe(this, posts -> {
            if (posts != null && !posts.isEmpty()) {
                postAdapter.setPostList(posts);
                viewPager.setVisibility(View.VISIBLE);
                tvEmptyState.setVisibility(View.GONE);
                viewPager.setCurrentItem(0, false);
            } else {
                viewPager.setVisibility(View.GONE);
                tvEmptyState.setVisibility(View.VISIBLE);
                tvEmptyState.setText("Chưa có bài đăng nào\nHãy là người đầu tiên check-in!");
            }
        });

        viewModel.loadPostsForActivity(activityId);
    }
}