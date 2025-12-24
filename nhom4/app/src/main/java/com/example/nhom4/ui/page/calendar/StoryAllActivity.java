package com.example.nhom4.ui.page.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.nhom4.R;
import com.example.nhom4.data.bean.Post;
import com.example.nhom4.ui.adapter.PeekPageTransformer;
import com.example.nhom4.ui.adapter.StoryImageAdapter;
import com.example.nhom4.ui.adapter.StoryThumbnailAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StoryAllActivity extends AppCompatActivity {

    private ViewPager2 viewPagerImages;
    private RecyclerView recyclerViewThumbnails;
    private TextView tvMoodTitle, tvTimestamp, tvYear, tvDate;
    private MaterialButton btnBack, btnShare;

    private StoryImageAdapter imageAdapter;
    private StoryThumbnailAdapter thumbnailAdapter;
    private List<Post> storyList = new ArrayList<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    // Biến lưu ID bài viết cần cuộn tới (nếu có)
    private String targetPostId = null;

    // Các biến nhận từ Intent (từ DetailActivity)
    private String activityId = null;
    private int day = -1;
    private int month = -1;
    private int year = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_all);

        // Nhận dữ liệu từ Intent
        if (getIntent() != null) {
            targetPostId = getIntent().getStringExtra("TARGET_POST_ID");
            activityId = getIntent().getStringExtra("ACTIVITY_ID");
            day = getIntent().getIntExtra("DAY_OF_MONTH", -1);
            month = getIntent().getIntExtra("MONTH", -1);
            year = getIntent().getIntExtra("YEAR", -1);
        }

        initViews();
        setupAdapters();
        setupViewPager();
        setupRecyclerView();
        setupEventHandlers();
        loadUserStories();
    }

    private void initViews() {
        viewPagerImages = findViewById(R.id.viewPagerImages);
        recyclerViewThumbnails = findViewById(R.id.recyclerViewThumbnails);
        tvMoodTitle = findViewById(R.id.tvMoodTitle);
        tvTimestamp = findViewById(R.id.tvTimestamp);
        tvYear = findViewById(R.id.tvYear);
        tvDate = findViewById(R.id.tvDate);
        btnBack = findViewById(R.id.btnBack);
        btnShare = findViewById(R.id.btnShare);
    }

    private void setupAdapters() {
        imageAdapter = new StoryImageAdapter();
        thumbnailAdapter = new StoryThumbnailAdapter();
    }

    private void setupViewPager() {
        viewPagerImages.setAdapter(imageAdapter);
        viewPagerImages.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPagerImages.setOffscreenPageLimit(3);

        int paddingPx = (int) (48 * getResources().getDisplayMetrics().density);
        viewPagerImages.setPageTransformer(new PeekPageTransformer(paddingPx));

        viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateUIForPosition(position);

                thumbnailAdapter.setSelectedPosition(position);
                if (recyclerViewThumbnails.getLayoutManager() != null) {
                    ((LinearLayoutManager) recyclerViewThumbnails.getLayoutManager())
                            .scrollToPositionWithOffset(position, 0);
                }
            }
        });
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewThumbnails.setLayoutManager(layoutManager);
        recyclerViewThumbnails.setAdapter(thumbnailAdapter);
        thumbnailAdapter.setOnThumbnailClickListener(position -> viewPagerImages.setCurrentItem(position, true));
    }

    private void setupEventHandlers() {
        btnBack.setOnClickListener(v -> finish());
        btnShare.setOnClickListener(v -> {
            int pos = viewPagerImages.getCurrentItem();
            if (pos >= 0 && pos < storyList.size()) {
                shareStory(storyList.get(pos));
            }
        });
    }

    private void loadUserStories() {
        if (auth.getCurrentUser() == null) {
            finish();
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        Query query = db.collection("posts")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING);

        // Filter theo activityId nếu có (từ DetailActivity)
        if (activityId != null && !activityId.isEmpty()) {
            query = query.whereEqualTo("activityId", activityId);
        }

        // Filter theo ngày cụ thể nếu có
        if (day != -1 && month != -1 && year != -1) {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month - 1, day, 0, 0, 0); // month -1 vì Calendar tháng bắt đầu từ 0
            cal.set(Calendar.MILLISECOND, 0);
            Date startOfDay = cal.getTime();

            cal.add(Calendar.DAY_OF_MONTH, 1);
            Date endOfDay = cal.getTime();

            query = query.whereGreaterThanOrEqualTo("createdAt", new Timestamp(startOfDay))
                    .whereLessThan("createdAt", new Timestamp(endOfDay));
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    storyList.clear();

                    for (var doc : queryDocumentSnapshots) {
                        Post post = doc.toObject(Post.class);
                        if (post != null) {
                            post.setPostId(doc.getId());

                            boolean hasPhoto = post.getPhotoUrl() != null && !post.getPhotoUrl().isEmpty();
                            boolean hasMood = post.getMoodIconUrl() != null && !post.getMoodIconUrl().isEmpty();

                            if (hasPhoto || hasMood) {
                                storyList.add(post);
                            }
                        }
                    }

                    if (storyList.isEmpty()) {
                        Toast.makeText(this, "Chưa có story nào cho hoạt động này", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    imageAdapter.setStoryList(storyList);
                    thumbnailAdapter.setStoryList(storyList);

                    // Cuộn đến ảnh mới nhất (hoặc target nếu có)
                    int initialPosition = 0;
                    if (targetPostId != null) {
                        for (int i = 0; i < storyList.size(); i++) {
                            if (storyList.get(i).getPostId().equals(targetPostId)) {
                                initialPosition = i;
                                break;
                            }
                        }
                    }

                    viewPagerImages.setCurrentItem(initialPosition, false);
                    updateUIForPosition(initialPosition);
                    thumbnailAdapter.setSelectedPosition(initialPosition);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateUIForPosition(int position) {
        if (position < 0 || position >= storyList.size()) return;
        Post post = storyList.get(position);

        String title = "";
        if ("activity".equals(post.getType()) && post.getActivityTitle() != null) {
            title = post.getActivityTitle();
        } else if ("mood".equals(post.getType()) && post.getMoodName() != null) {
            title = post.getMoodName();
        } else {
            title = "Story";
        }

        if (post.getCaption() != null && !post.getCaption().isEmpty()) {
            title += " - " + post.getCaption();
        }

        tvMoodTitle.setText(title);

        if (post.getCreatedAt() != null) {
            Date date = post.getCreatedAt().toDate();
            tvTimestamp.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date));

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            tvYear.setText(String.valueOf(cal.get(Calendar.YEAR)));
            tvDate.setText(new SimpleDateFormat("'tháng' M 'ngày' d", Locale.getDefault()).format(date));
        }
    }

    private void shareStory(Post post) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String content = "Xem story của tôi";
        if (post.getPhotoUrl() != null) content += ": " + post.getPhotoUrl();
        else if (post.getMoodIconUrl() != null) content += " (Mood): " + post.getMoodIconUrl();

        shareIntent.putExtra(Intent.EXTRA_TEXT, content);
        startActivity(Intent.createChooser(shareIntent, "Chia sẻ"));
    }
}