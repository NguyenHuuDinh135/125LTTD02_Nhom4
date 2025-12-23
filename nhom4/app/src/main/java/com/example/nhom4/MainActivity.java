package com.example.nhom4;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.nhom4.ui.adapter.MainPagerAdapter;
import com.example.nhom4.ui.page.calendar.ProfileActivity;
import com.example.nhom4.ui.viewmodel.MainViewModel;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPagerMain;

    // Top Bar
    private View topBar;
    private ShapeableImageView imgAvatar;
    private MaterialCardView cardFriendsPill;
    private ImageView btnChat;
    private ImageView btnNavIcon;
    private TextView tvScreenTitle;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔥 EDGE TO EDGE – QUAN TRỌNG
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        initViews();
        setupStatusBarInset();   // 👈 FIX STATUS BAR
        setupViewPager();
        loadCurrentUserAvatar();
        handleOpenPostFromWidget(getIntent());

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // ⚠️ RẤT QUAN TRỌNG
        handleOpenPostFromWidget(intent);
    }
    /**
     * XỬ LÝ INSET STATUS BAR CHO TOP BAR
     */
    private void setupStatusBarInset() {
        ViewCompat.setOnApplyWindowInsetsListener(topBar, (view, insets) -> {
            int statusBarHeight =
                    insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;

            view.setPadding(
                    view.getPaddingLeft(),
                    statusBarHeight,
                    view.getPaddingRight(),
                    view.getPaddingBottom()
            );
            return insets;
        });
    }

    private void initViews() {
        viewPagerMain = findViewById(R.id.viewPagerMain);

        // Top bar container (include)
        topBar = findViewById(R.id.top_bar);

        imgAvatar = findViewById(R.id.img_avatar);
        cardFriendsPill = findViewById(R.id.card_friends_pill);
        btnChat = findViewById(R.id.btn_chat);
        btnNavIcon = findViewById(R.id.btn_nav_icon);
        tvScreenTitle = findViewById(R.id.tv_screen_title);

        btnNavIcon.setOnClickListener(v -> {
            int currentItem = viewPagerMain.getCurrentItem();
            if (currentItem == 0) {
                startActivity(new Intent(this, ProfileActivity.class));
            } else if (currentItem == 2) {
                navigateToFeed();
            }
        });

        imgAvatar.setOnClickListener(v -> navigateToCalendar());

        btnChat.setOnClickListener(v -> {
            int currentItem = viewPagerMain.getCurrentItem();
            if (currentItem == 0) {
                navigateToFeed();
            } else if (currentItem == 1) {
                navigateToDiscovery();
            }
        });
    }

    private void setupViewPager() {
        MainPagerAdapter adapter = new MainPagerAdapter(this);
        viewPagerMain.setAdapter(adapter);
        viewPagerMain.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPagerMain.setOverScrollMode(ViewPager2.OVER_SCROLL_NEVER);

        viewPagerMain.setCurrentItem(1, false);
        updateTopBarUI(1);

        viewPagerMain.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        updateTopBarUI(position);
                    }
                });
    }

    private void handleOpenPostFromWidget(Intent intent) {
        if (intent == null) return;

        boolean openPost = intent.getBooleanExtra("OPEN_POST", false);
        if (!openPost) return;

        String postId = intent.getStringExtra("POST_ID");
        if (postId == null || postId.isEmpty()) return;

        // Chuyển về tab Feed
        viewPagerMain.setCurrentItem(1, false);

        // 🔥 LƯU POST ID VÀO VIEWMODEL
        MainViewModel viewModel =
                new ViewModelProvider(this).get(MainViewModel.class);

        viewModel.setOpenPostId(postId);
    }

    private void updateTopBarUI(int position) {
        switch (position) {
            case 0: // Calendar
                imgAvatar.setVisibility(View.GONE);
                cardFriendsPill.setVisibility(View.GONE);

                btnNavIcon.setVisibility(View.VISIBLE);
                btnNavIcon.setImageResource(R.drawable.outline_account_circle_24);

                btnChat.setVisibility(View.VISIBLE);
                btnChat.setImageResource(R.drawable.outline_arrow_forward_ios_24);

                tvScreenTitle.setVisibility(View.VISIBLE);
                tvScreenTitle.setText("Lịch sử & Streak");
                break;

            case 1: // Main
                imgAvatar.setVisibility(View.VISIBLE);
                cardFriendsPill.setVisibility(View.VISIBLE);

                btnChat.setVisibility(View.VISIBLE);
                btnChat.setImageResource(R.drawable.outline_chat_24);

                btnNavIcon.setVisibility(View.GONE);
                tvScreenTitle.setVisibility(View.GONE);
                break;

            case 2: // Discovery
                imgAvatar.setVisibility(View.GONE);
                cardFriendsPill.setVisibility(View.GONE);
                btnChat.setVisibility(View.GONE);

                btnNavIcon.setVisibility(View.VISIBLE);
                btnNavIcon.setImageResource(R.drawable.outline_arrow_back_ios_24);

                tvScreenTitle.setVisibility(View.VISIBLE);
                tvScreenTitle.setText("Khám phá");
                break;
        }
    }

    private void loadCurrentUserAvatar() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(imgAvatar);
        }
    }

    public void navigateToCalendar() {
        viewPagerMain.setCurrentItem(0, true);
    }

    public void navigateToFeed() {
        viewPagerMain.setCurrentItem(1, true);
    }

    public void navigateToDiscovery() {
        viewPagerMain.setCurrentItem(2, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCurrentUserAvatar();
    }
}
