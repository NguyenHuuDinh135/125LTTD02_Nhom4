package com.example.nhom4.ui.page.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.nhom4.R;
import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.Post;
import com.example.nhom4.ui.adapter.PeekPageTransformer;
import com.example.nhom4.ui.adapter.StoryImageAdapter;
import com.example.nhom4.ui.adapter.StoryThumbnailAdapter;
import com.example.nhom4.ui.viewmodel.MainViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Activity hiển thị tất cả story đã đăng của user hiện tại.
 * Hỗ trợ vuốt ngang để xem các story và đồng bộ với thumbnails ở bottom.
 */
public class StoryAllActivity extends AppCompatActivity {

    private ViewPager2 viewPagerImages;
    private RecyclerView recyclerViewThumbnails;
    private TextView tvMoodTitle;
    private TextView tvTimestamp;
    private TextView tvYear;
    private TextView tvDate;
    private MaterialButton btnBack;
    private MaterialButton btnShare;

    private StoryImageAdapter imageAdapter;
    private StoryThumbnailAdapter thumbnailAdapter;
    private MainViewModel viewModel;
    private List<Post> storyList = new ArrayList<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_all);

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

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
    }

    private void setupAdapters() {
        imageAdapter = new StoryImageAdapter();
        thumbnailAdapter = new StoryThumbnailAdapter();
    }

    private void setupViewPager() {
        viewPagerImages.setAdapter(imageAdapter);
        viewPagerImages.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPagerImages.setOffscreenPageLimit(2);

        // Thiết lập PageTransformer để tạo peek effect
        // Padding 48dp đã được set trong XML, PageTransformer sẽ điều chỉnh scale và translation
        int paddingPx = (int) (48 * getResources().getDisplayMetrics().density);
        viewPagerImages.setPageTransformer(new PeekPageTransformer(paddingPx));

        // Đồng bộ TabLayout với ViewPager2
        viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateUIForPosition(position);
                thumbnailAdapter.setSelectedPosition(position);
                // Scroll thumbnail đến vị trí được chọn
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

        // Xử lý click thumbnail để chuyển story
        thumbnailAdapter.setOnThumbnailClickListener(position -> {
            viewPagerImages.setCurrentItem(position, true);
        });
    }

    private void setupEventHandlers() {
        btnBack.setOnClickListener(v -> finish());

        btnShare.setOnClickListener(v -> {
            int currentPosition = viewPagerImages.getCurrentItem();
            if (currentPosition >= 0 && currentPosition < storyList.size()) {
                Post currentPost = storyList.get(currentPosition);
                shareStory(currentPost);
            }
        });
    }

    private void loadUserStories() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        // Load tất cả posts của user hiện tại, sắp xếp theo thời gian mới nhất
        db.collection("posts")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    storyList.clear();
                    for (var doc : queryDocumentSnapshots) {
                        Post post = doc.toObject(Post.class);
                        // Chỉ lấy posts có ảnh (story)
                        if (post != null && post.getPhotoUrl() != null && !post.getPhotoUrl().isEmpty()) {
                            post.setPostId(doc.getId());
                            storyList.add(post);
                        }
                    }

                    if (storyList.isEmpty()) {
                        Toast.makeText(this, "Chưa có story nào", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    // Cập nhật adapters
                    imageAdapter.setStoryList(storyList);
                    thumbnailAdapter.setStoryList(storyList);

                    // Cập nhật UI cho story đầu tiên
                    if (!storyList.isEmpty()) {
                        updateUIForPosition(0);
                        thumbnailAdapter.setSelectedPosition(0);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải story: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void updateUIForPosition(int position) {
        if (position < 0 || position >= storyList.size()) return;

        Post post = storyList.get(position);

        // Cập nhật mood title
        if (post.getMoodName() != null && !post.getMoodName().isEmpty()) {
            String moodText = post.getMoodName();
            if (post.getCaption() != null && !post.getCaption().isEmpty()) {
                moodText += " - " + post.getCaption();
            }
            tvMoodTitle.setText(moodText);
        } else if (post.getCaption() != null && !post.getCaption().isEmpty()) {
            tvMoodTitle.setText(post.getCaption());
        } else {
            tvMoodTitle.setText("Story");
        }

        // Cập nhật timestamp
        if (post.getCreatedAt() != null) {
            Date date = post.getCreatedAt().toDate();
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            tvTimestamp.setText(timeFormat.format(date));

            // Cập nhật date và year ở toolbar
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            tvYear.setText(String.valueOf(cal.get(Calendar.YEAR)));

            SimpleDateFormat dateFormat = new SimpleDateFormat("'Ngày' d 'tháng' M", Locale.getDefault());
            tvDate.setText(dateFormat.format(date));
        }
    }

    private void shareStory(Post post) {
        if (post == null || post.getPhotoUrl() == null) {
            Toast.makeText(this, "Không thể chia sẻ story này", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String shareText = "Xem story của tôi: " + post.getPhotoUrl();
        if (post.getCaption() != null && !post.getCaption().isEmpty()) {
            shareText = post.getCaption() + "\n" + shareText;
        }
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Chia sẻ story"));
    }
}

