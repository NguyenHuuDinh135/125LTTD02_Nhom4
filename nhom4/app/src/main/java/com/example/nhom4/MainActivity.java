package com.example.nhom4;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.nhom4.ui.page.friend.FriendsBottomSheet;
import com.example.nhom4.ui.page.post.PostAdapter; // Import adapter mới
import com.example.nhom4.ui.adapter.MainPagerAdapter;
import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPagerMain;

    // Khai báo View trong Top Bar Mới
    private View topBarContainer;

    // Nhóm Center (Main)
    private ImageView imgAvatar, btnChat;
    private MaterialCardView cardFriendsPill;

    // Nhóm Side (Trái/Phải)
    private ImageView btnNavIcon; // Nút back hoặc menu
    private TextView tvScreenTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Ánh xạ ViewPager
        viewPagerMain = findViewById(R.id.viewPagerMain);

        // 2. Ánh xạ các View trong Top Bar (Dùng ID mới trong component_top_bar.xml)
        topBarContainer = findViewById(R.id.top_bar); // ID của thẻ include trong activity_main

        // Lưu ý: findViewById sẽ tìm trong toàn bộ layout active nên nó sẽ thấy các view trong include
        imgAvatar = findViewById(R.id.img_avatar);
        cardFriendsPill = findViewById(R.id.card_friends_pill);
        btnChat = findViewById(R.id.btn_chat);

        btnNavIcon = findViewById(R.id.btn_nav_icon);
        tvScreenTitle = findViewById(R.id.tv_screen_title);

        // 3. Setup Adapter
        MainPagerAdapter mainAdapter = new MainPagerAdapter(this);
        viewPagerMain.setAdapter(mainAdapter);
        viewPagerMain.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPagerMain.setOffscreenPageLimit(2);

        // 4. Xử lý sự kiện khi lướt trang (Logic đổi Top Bar)
        viewPagerMain.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateTopBarUI(position);
            }
        });

        // 5. Mặc định vào trang giữa
        viewPagerMain.setCurrentItem(1, false);
        // Cập nhật UI lần đầu
        updateTopBarUI(1);
    }

    // --- HÀM QUAN TRỌNG: Đổi giao diện Top Bar theo trang ---
    private void updateTopBarUI(int position) {
        switch (position) {
            case 0: // Màn hình TRÁI (Ví dụ: Lịch sử / Calendar)
                // Ẩn nhóm Main
                imgAvatar.setVisibility(View.GONE);
                cardFriendsPill.setVisibility(View.GONE);
                btnNavIcon.setVisibility(View.GONE);

                // Hiện nhóm Side
                btnChat.setVisibility(View.VISIBLE);
                btnChat.setImageResource(R.drawable.outline_arrow_forward_ios_24); // Hoặc icon Menu
                tvScreenTitle.setVisibility(View.VISIBLE);
                tvScreenTitle.setText("Lịch sử");
                break;

            case 1: // Màn hình GIỮA (Main Feed - Giống ảnh bạn gửi)
                // Hiện nhóm Main
                imgAvatar.setVisibility(View.VISIBLE);
                cardFriendsPill.setVisibility(View.VISIBLE);
                btnChat.setVisibility(View.VISIBLE);

                // Ẩn nhóm Side
                btnNavIcon.setVisibility(View.GONE);
                tvScreenTitle.setVisibility(View.GONE);
                break;

            case 2: // Màn hình PHẢI (Ví dụ: Discovery)
                // Ẩn nhóm Main
                imgAvatar.setVisibility(View.GONE);
                cardFriendsPill.setVisibility(View.GONE);
                btnChat.setVisibility(View.GONE); // Hoặc giữ lại nếu muốn

                // Hiện nhóm Side
                btnNavIcon.setVisibility(View.VISIBLE);
                btnNavIcon.setImageResource(R.drawable.outline_arrow_back_ios_24);
                tvScreenTitle.setVisibility(View.VISIBLE);
                tvScreenTitle.setText("Khám phá");
                break;
        }
    }

    public void navigateToCenter() {
        viewPagerMain.setCurrentItem(1, true);
    }
}