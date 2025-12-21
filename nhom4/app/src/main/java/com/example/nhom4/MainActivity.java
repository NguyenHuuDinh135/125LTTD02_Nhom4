package com.example.nhom4;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.nhom4.ui.adapter.MainPagerAdapter;
import com.example.nhom4.ui.page.calendar.ProfileActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPagerMain;

    // Các thành phần của Top Bar
    private ShapeableImageView imgAvatar;
    private MaterialCardView cardFriendsPill;
    private ImageView btnChat;
    private ImageView btnNavIcon; // Nút icon bên trái (dùng để back về Main hoặc mở Profile)
    private TextView tvScreenTitle;

    private FirebaseAuth auth; // Auth để lấy avatar user hiện tại

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Khởi tạo Firebase Auth
        auth = FirebaseAuth.getInstance();

        // 2. Khởi tạo View và Cấu hình
        initViews();
        setupViewPager();

        // 3. Load ảnh đại diện
        loadCurrentUserAvatar();
    }

    private void loadCurrentUserAvatar() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.getPhotoUrl() != null) {
            // Load ảnh avatar vào nút imgAvatar (ở trang Main) dùng thư viện Glide
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.ic_launcher_background) // Ảnh hiển thị khi đang tải
                    .error(R.drawable.ic_launcher_background)       // Ảnh hiển thị khi lỗi
                    .into(imgAvatar);
        }
    }

    private void initViews() {
        viewPagerMain = findViewById(R.id.viewPagerMain);

        // Ánh xạ view trong Top Bar
        imgAvatar = findViewById(R.id.img_avatar);
        cardFriendsPill = findViewById(R.id.card_friends_pill);
        btnChat = findViewById(R.id.btn_chat);
        btnNavIcon = findViewById(R.id.btn_nav_icon);
        tvScreenTitle = findViewById(R.id.tv_screen_title);

        // --- XỬ LÝ SỰ KIỆN CLICK NÚT NAV (BÊN TRÁI) ---
        btnNavIcon.setOnClickListener(v -> {
            int currentItem = viewPagerMain.getCurrentItem();
            if (currentItem == 0) {
                // Đang ở Calendar -> Icon là hình người -> Bấm mở Profile
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);

            } else if (currentItem == 2) {
                // Đang ở Discovery -> Icon là mũi tên back -> Bấm quay về Main
                navigateToFeed();
            }
        });

        // Click vào Avatar ở trang Main cũng mở Profile (tuỳ chọn)
        imgAvatar.setOnClickListener(v -> navigateToCalendar());

        // Click nút bên phải (btnChat) - hành vi thay đổi theo trang
        btnChat.setOnClickListener(v -> {
            int currentItem = viewPagerMain.getCurrentItem();

            if (currentItem == 0) {
                // Đang ở Calendar -> bấm nút forward bên phải → về trang chính (Main)
                navigateToFeed(); // position 1
            } else if (currentItem == 1) {
                // Click nút Chat -> Sang màn hình Discovery
                navigateToDiscovery();
            }
        });
    }

    private void setupViewPager() {
        MainPagerAdapter mainAdapter = new MainPagerAdapter(this);
        viewPagerMain.setAdapter(mainAdapter);
        viewPagerMain.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

        // Tắt hiệu ứng overscroll (tuỳ chọn, giúp cảm giác lướt "cứng" hơn giống Locket)
        viewPagerMain.setOverScrollMode(ViewPager2.OVER_SCROLL_NEVER);

        // --- QUAN TRỌNG: Đặt trang mặc định là trang giữa (MainFragment) ---
        viewPagerMain.setCurrentItem(1, false);
        updateTopBarUI(1); // Cập nhật UI ban đầu

        // Lắng nghe sự kiện thay đổi trang để cập nhật Top Bar
        viewPagerMain.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateTopBarUI(position);
            }
        });
    }

    // Hàm cập nhật giao diện Top Bar theo vị trí trang
    private void updateTopBarUI(int position) {
        switch (position) {
            case 0: // Calendar Fragment (Bên Trái)
                // Ẩn các nút của trang Main
                imgAvatar.setVisibility(View.GONE);
                cardFriendsPill.setVisibility(View.GONE);

                // Hiện nút điều hướng (Icon Profile) và Tiêu đề
                btnNavIcon.setVisibility(View.VISIBLE);
                // Đảm bảo bạn có icon này trong drawable, nếu chưa có hãy thêm vào hoặc dùng icon tạm
                btnNavIcon.setImageResource(R.drawable.outline_account_circle_24);
                btnChat.setImageResource(R.drawable.outline_arrow_forward_ios_24);

                tvScreenTitle.setVisibility(View.VISIBLE);
                tvScreenTitle.setText("Lịch sử & Streak");
                break;

            case 1: // Main Fragment (Ở Giữa)
                // Hiện các nút của trang Main
                imgAvatar.setVisibility(View.VISIBLE);
                cardFriendsPill.setVisibility(View.VISIBLE);
                btnChat.setVisibility(View.VISIBLE);

                // Ẩn nút điều hướng và Tiêu đề
                btnNavIcon.setVisibility(View.GONE);
                tvScreenTitle.setVisibility(View.GONE);
                break;

            case 2: // Discovery Fragment (Bên Phải)
                // Ẩn các nút của trang Main
                imgAvatar.setVisibility(View.GONE);
                cardFriendsPill.setVisibility(View.GONE);
                btnChat.setVisibility(View.GONE);

                // Hiện nút điều hướng (Icon Back) và Tiêu đề
                btnNavIcon.setVisibility(View.VISIBLE);
                // Đảm bảo bạn có icon này trong drawable
                btnNavIcon.setImageResource(R.drawable.outline_arrow_back_ios_24);

                tvScreenTitle.setVisibility(View.VISIBLE);
                tvScreenTitle.setText("Khám phá");
                break;
        }
    }

    // Các hàm điều hướng công khai (có thể gọi từ Fragment nếu cần)
    public void navigateToCalendar() {
        viewPagerMain.setCurrentItem(0, true);
    }

    public void navigateToFeed() {
        viewPagerMain.setCurrentItem(1, true);
    }

    public void navigateToDiscovery() {
        viewPagerMain.setCurrentItem(2, true);
    }

    // Hàm hỗ trợ refresh lại avatar nếu người dùng vừa đổi ảnh ở ProfileActivity quay về
    @Override
    protected void onResume() {
        super.onResume();
        loadCurrentUserAvatar();
    }
}
